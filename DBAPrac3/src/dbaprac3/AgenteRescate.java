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
import java.util.Stack;
import javafx.util.Pair;

/**
 *
 * @author Kieran
 */
public class AgenteRescate extends AgenteDron {
    
    Stack<Pair<Integer,Integer>> objetivos;
    int obj_x;
    int obj_y;
    
    public AgenteRescate(AgentID aid) throws Exception {
        super(aid);
        consumo_fuel = 0.5;
        tamanio_radar = 3;
        centro_radar = 1;
        rol = "rescue";
        max_z = 255;
        infrared = new int[tamanio_radar][tamanio_radar];
        
        objetivos = new Stack<Pair<Integer,Integer>>();
        obj_x = -1;
        obj_y = -1;
    } 

    /**
    *
    * @author Kieran
    */
    protected void recibirObjetivoEncontrado(JsonObject mensaje){
        
        System.out.println("Rescate: Recibido objetivo");
        int x = mensaje.get("x").asInt();
        int y = mensaje.get("y").asInt();

        System.out.println("Rescate:procesado objetivo");
        objetivos.push(new Pair(x,y));
    }
    
    @Override
    protected Accion comprobarAccion(){
      Accion accion = null;

      System.out.println("cccc");
      accion = checkMeta();
      if(accion != null) return accion;

        System.out.println("ccccc");
      accion = checkDirObjetivo();
      accion = checkNavegacionNormal(accion);
      accion = checkRepostaje(accion);
      return accion;

    }
    
    protected Accion checkDirObjetivo(){
        System.out.println("ccc");
        double angulo_falsogonio = Math.atan2(-(gps.y-obj_y), gps.x-obj_x) * 180/Math.PI;
        gonio.angulo = (float) angulo_falsogonio;
        gonio.distancia = 5;
        return siguienteDireccion(false);
    }
    
    @Override
    protected Accion checkMeta(){
        if( gps.x == obj_x && gps.y == obj_y || (gps.x == ini_x && gps.y == ini_y && torescue == 0) ) {
            if(gps.z != mapa[gps.x][gps.y]) {
                return moveDW;
            }
            else {
                objetivos.pop();
                return rescue;
            }
        }
        return null;
    }
    
    @Override
    protected JsonObject escuchar(){
        ACLMessage ultimo_mensaje_real = ultimo_mensaje_recibido;
        JsonObject escuchado = super.escuchar();
        
            System.out.println("Esto peta?");
        if(!escuchado.get("objetivo-encontrado").isNull()){
            recibirObjetivoEncontrado(escuchado);
            ultimo_mensaje_recibido = ultimo_mensaje_real; //Ignora mensajes del burocrata para la conversación con el servidor. Descutrear luego. 
            System.out.println("DRON-R: Si existe el campo objetivo-encontrado");           
        }
        else {
            System.out.println("DRON-R: No existe el campo objetivo-encontrado");
        }
        return escuchado;
    }
    
    @Override
    protected void bucleExecute(){
        if(torescue == 0) {
            obj_x = ini_x;
            obj_y = ini_y;
        }
        if(objetivos.empty()){
            while(objetivos.empty()){
                recibirObjetivoEncontrado(escuchar());
            }
            obj_x = objetivos.peek().getKey();
            obj_y = objetivos.peek().getValue();
            System.out.println("cc");
        }
        super.bucleExecute();
    }
}
