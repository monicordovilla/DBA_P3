/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import com.eclipsesource.json.JsonObject;
import static dbaprac3.Accion.*;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import javafx.util.Pair;

/**
 *
 * @author Kieran
 */
public class AgenteRescate extends AgenteDron {
    
    Queue<Pair<Integer,Integer>> objetivos;
    int obj_x;
    int obj_y;
    boolean ignorar_msg_objetivos; //Sigue escuchando si le llega un mensaje tipo objetivo
    
    public AgenteRescate(AgentID aid) throws Exception {
        super(aid);
        consumo_fuel = 0.5;
        tamanio_radar = 3;
        centro_radar = 1;
        rol = "rescue";
        max_z = 255;
        infrared = new int[tamanio_radar][tamanio_radar];
        
        
        objetivos = new LinkedList<Pair<Integer,Integer>>();
        obj_x = -1;
        obj_y = -1;
        ignorar_msg_objetivos = true;
    } 

    /**
    *
    * @author Kieran
    */
    protected void recibirObjetivoEncontrado(JsonObject mensaje){
        
        System.out.println("Rescate: Recibido objetivo");
        int x = mensaje.get("coordenadas").asObject().get("x").asInt();
        int y = mensaje.get("coordenadas").asObject().get("y").asInt();

        System.out.println("Rescate:procesado objetivo");
        objetivos.add(new Pair(x,y));
    }
    
    /**
    *
    * @author Kieran
    */
    @Override
    protected Accion comprobarAccion(){
      Accion accion = null;

      accion = checkMeta();
      if(accion != null) return accion;

      System.out.println("DRON-R: Cola: " + objetivos +". Obj actual: " + obj_x + " " + obj_y);  
      
      accion = checkDirObjetivo();
      accion = checkNavegacionNormal(accion);
      accion = checkRepostaje(accion);
      return accion;

    }

    /**
    *
    * @author Kieran
    */    
    protected Accion checkDirObjetivo(){
        double angulo_falsogonio = Math.atan2(-(gps.x-obj_x), gps.y-obj_y) * 180/Math.PI;
        if (angulo_falsogonio < 0) { angulo_falsogonio = 360 + angulo_falsogonio; }
        gonio.angulo = (float) angulo_falsogonio;
        gonio.distancia = 5;
        return siguienteDireccion(false);
    }
    
    /**
    *
    * @author Kieran
    */
    protected Accion checkMeta(){
        if( (gps.x == obj_x && gps.y == obj_y) || (gps.x == ini_x && gps.y == ini_y && torescue == 0) ) {
            if(gps.z != mapa[gps.x][gps.y]) {
                return moveDW;
            }
            else {
                if(torescue == 0) { done = true; return stop; }
                objetivos.poll();
                System.out.println("DRON-R: Rescatando. Cola: " + objetivos +". Obj actual: " + obj_x + " " + obj_y);  
                obj_x = -1; obj_y = -1;
                return rescue;
            }
        }
        return null;
    }
    
    protected JsonObject escuchar_until_servidor(){ //Descutrear luego. Escucha hasta que le llegue un mensaje del servidor, luego para.
        ACLMessage ultimo_mensaje_real = ultimo_mensaje_recibido;
        JsonObject escuchado = super.escuchar();
        
        while(escuchado.get("objetivo-identificado") != null){
            recibirObjetivoEncontrado(escuchado);
            ultimo_mensaje_recibido = ultimo_mensaje_real; //Ignora mensajes del burocrata para la conversación con el servidor. Descutrear luego. 
            System.out.println("DRON-R: Si existe el campo objetivo-encontrado. Guardando objetivo.");  
            escuchado = super.escuchar();
        }
        System.out.println("DRON-R: No existe el campo objetivo-encontrado. Siguiendo.");
        return escuchado;
    }
    
    /**
    *
    * @author Kieran
    */
    @Override
    protected JsonObject escuchar(){
        JsonObject escuchado;
        
        escuchado = (ignorar_msg_objetivos) ? escuchar_until_servidor() : super.escuchar();
        
        return escuchado;
    }
    
    /**
    *
    * @author Kieran
    */
    @Override
    protected void bucleExecute(){
        perception();
        JsonObject msg = escuchar();
        reply_key = ultimo_mensaje_recibido.getReplyWith();

        JSONDecode_variables(msg);
        
        if(torescue == 0) { //Si hemos acabado, volver
            System.out.println("RRRRRRRRRR");
            obj_x = ini_x;
            obj_y = ini_y;
        }
        else if(objetivos.isEmpty() || obj_x == -1 || obj_y == -1){
            ignorar_msg_objetivos = false;
            while(objetivos.isEmpty()){
                recibirObjetivoEncontrado(escuchar());
            }
            obj_x = objetivos.peek().getKey();
            obj_y = objetivos.peek().getValue();
        }
        
        ignorar_msg_objetivos = true; //Hace falta filtrar los mensajes de que ha llegado un nuevo objetivo durante esta parte
        
        Accion accion = comprobarAccion();
        move(accion);
        escuchar();
        reply_key = ultimo_mensaje_recibido.getReplyWith();
        
    }
}
