/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import com.eclipsesource.json.JsonObject;
import static dbaprac3.Accion.*;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import javafx.util.Pair;

/**
 *
 * @author Ana
 */
public class AgenteSparrow extends AgenteDron {
    
    ArrayList<Pair<Integer, Integer>> infrarojo;
    boolean dir_norte = false; //Cambiar luego
    boolean dir_oeste = false; //Cambiar luego
    int pasos_desplazamiento; //Cuantos pasos nos quedan para poder realizar un nuevo barrido
    int alemanes_debug = 0; //BORRAR LUEGO, el numero de alemanes que ha visto
    boolean[][] mapaMemoria;
    boolean objetivo;
    Pair<Integer,Integer> coords_obj;
    boolean busquedaCompletada;

            
    public AgenteSparrow(AgentID aid) throws Exception {
        //No cambiar
        super(aid);
        consumo_fuel = 0.5;
        tamanio_radar = 11;
        centro_radar = 5;
        rol = "sparrow";
        max_z = 240;
        infrared = new int[tamanio_radar][tamanio_radar];
        
        //Cambiar si se quiere
        infrarojo = new ArrayList();
        pasos_desplazamiento = tamanio_radar;
        objetivo = false;
        busquedaCompletada = false;
        coords_obj = null;
    }
    
    /**
    *
    * @author Ana
    * Mapa de memoria inicial
    */
    private void inicializarMapa(){
        int i, j;
        
        mapaMemoria = new boolean[max_x][max_y];
        System.out.println("REEEEE" + max_x);
        for( i=0; i<max_x; i++)
            for( j=0; j<max_y; i++)
                mapaMemoria[i][j] = mapa[i][j] > max_z;
    }
    
    /**
    *
    * @author Ana, Kieran
    * Se actualiza la información del mapa de memoria con la información conocida por el drone
    */
    private void actualizarMapa(){
        int i, j;
        for(i=0; i < tamanio_radar; i++) {
            for(j=0; j < tamanio_radar; j++) {
                int x = gps.x-(j-centro_radar);
                int y = gps.y-(i-centro_radar);
                System.out.println(x + " " + y + " enl " + enLimites(x,y));
                System.out.println(mapaMemoria[x][y]);
                if(enLimites(x,y) && !mapaMemoria[x][y])
                    mapaMemoria[x][y] = true;
            }
        }
                
    }
    
    /**
    *
    * @author Kieran
    * Se actualiza la información del mapa de memoria con la información conocida por el drone
    */
    @Override
    protected void JSONDecode_ActualizarMapa(JsonObject mensaje) {
        super.JSONDecode_ActualizarMapa(mensaje);
        inicializarMapa();
    }
    
    /**
    *
    * @author Kieran
    */    
    protected Accion checkDirObjetivo(Pair<Integer,Integer> obj){
        double angulo_falsogonio = Math.atan2(-(gps.x-obj.getKey()), gps.y-obj.getValue()) * 180/Math.PI;
        if (angulo_falsogonio < 0) { angulo_falsogonio = 360 + angulo_falsogonio; }
        gonio.angulo = (float) angulo_falsogonio;
        gonio.distancia = 5;
        return siguienteDireccion(false);
    }
    
    /**
    *
    * @author Ana, Kieran
    * Método sobreescrito de la clase padre para adaptarlo al Sparrow
    */
    @Override
    protected Accion comprobarAccion(){
      Accion accion = null;
      
      Pair<Integer,Integer> coords_act = new Pair<>(gps.x,gps.y);
      
      actualizarMapa();
      
      if(!busquedaCompletada) {
        if(!objetivo) {
        
        System.out.println("x");    coords_obj = busquedaAnchura(coords_act);
            if(coords_obj == null) { busquedaCompletada = true; }
            else { objetivo = true; }
        }
        
        System.out.println("x");
        accion = checkDirObjetivo(coords_obj);
        
        System.out.println("x");accion = busquedaMapasAltos(accion);
        accion = checkNavegacionNormal(accion);
        
        System.out.println("x");accion = checkManoDerecha(accion);
        
        System.out.println("x");
      }
      accion = checkRepostaje(accion);
          
      return accion;
        
    }
    
    /**
    *
    * @author Ana, Kieran
    * Busqueda en anchura para encontrar un lugar no explorado
    */
    private Pair<Integer,Integer> busquedaAnchura(Pair<Integer,Integer> coords_act){
      int tam_barrido = centro_radar+1;
      int x = coords_act.getKey();
      int y = coords_act.getValue();
      
      while(x+tam_barrido < max_x || y + tam_barrido < max_y || x-tam_barrido >= 0 || y-tam_barrido >= 0){ //Mientras la cola no este vacia y no tengmaos objetivo
          for(int i = -tam_barrido; i <= tam_barrido; i++){
              for(int j = -tam_barrido; j <= tam_barrido; j++) {
                  if (Math.abs(i) == tam_barrido || Math.abs(j) == tam_barrido){
                    if( enLimites(x+i,y+j) && mapaMemoria[x+i][y+j]) { return new Pair<Integer, Integer>(x+i,y+j); }
                  }
              }
          }
          
          tam_barrido++;
          
      }
      
      return null;
      
    }
    
    
    /**
    *
    * @author Ana, Kieran
    * Estrategia de búsqueda 
    */
    public Accion busquedaMapasAltos(Accion accion){ //Cuando creemos las instancias debemos establecerle un identificador para que aqui segn eso recorra una parte del mapa
        
        boolean dron_demasiado_bajo = false;
        
        
        System.out.println("DRON: INFRAROJOS: " + infrarojo);
        //Comprobar el infrarojos por si hay que subir o enviar algo
        for(int i=0; i<infrared.length; i++) 
        {
            for(int j=0; j<infrared.length; j++)
            {
                if(infrared[i][j] == -1) {
                    dron_demasiado_bajo = true;//Subir si i j vale -1
                }
                if(infrared[i][j] == 1) {
                    Pair<Integer,Integer> coords_obj = new Pair<>(gps.x+(j-centro_radar),gps.y+(i-centro_radar));
                    if(!infrarojo.contains(coords_obj)) {//mandar al burocrata que hay uno y la posicion
                        infrarojo.add(coords_obj);
                        System.out.println("DRON: COORDS OBJ: " + coords_obj);
                        avisarObjetivoIdentificado(coords_obj.getKey(), coords_obj.getValue());
                        alemanes_debug++;
                    }
                }
            }
        }
        
        if(dron_demasiado_bajo && gps.z+5 <= max_z)
            accion = moveUP;
                   
        return accion;
                
        }
    
    @Override
    public void finalize(){
        System.out.println("\nAlemanes encontrados: " + alemanes_debug);
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
    
}
