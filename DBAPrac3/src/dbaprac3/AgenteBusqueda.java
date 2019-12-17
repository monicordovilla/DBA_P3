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
import javafx.util.Pair;

/**
 *
 * @author anabosch
 */
public class AgenteBusqueda extends AgenteDron {
    
    ArrayList<Pair<Integer, Integer>> infrarojo;
    int pasos_desplazamiento; //Cuantos pasos nos quedan para poder realizar un nuevo barrido
    int alemanes_debug = 0; //BORRAR LUEGO, el numero de alemanes que ha visto
    int max_z_sparrow = 240;
    Estrategia plan;
    boolean[][] mapaMemoria;
    boolean objetivo;
    Pair<Integer,Integer> coords_dest;
    boolean busquedaCompletada;
    boolean mapaMemoria_init;;
    
    public AgenteBusqueda(AgentID aid) throws Exception{
        super(aid);
        infrarojo = new ArrayList<>();
    }

    /**
    *
    * @author Ana, Kieran
    * Estrategia de búsqueda para mapas altos en los que el equipo esta formado por 2 Fly y 2 Rescue
    */
    public Accion scanInfrarojos(Accion accion){ //Cuando creemos las instancias debemos establecerle un identificador para que aqui segn eso recorra una parte del mapa
        
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
    
        /**
    *
    * @author Ana
    * Mapa de memoria inicial
    */
    protected void inicializarMapa(){
        int i, j;
        
        mapaMemoria = new boolean[max_x][max_y];
        for( i=0; i<max_x; i++) {
            for( j=0; j<max_y; j++) {
                mapaMemoria[i][j] = mapa[i][j] > max_z;
            }
        }
        mapaMemoria_init = true;
    }
    
    /**
    *
    * @author Ana, Kieran
    * Se actualiza la información del mapa de memoria con la información conocida por el drone
    */
    protected void actualizarMapa(){
        int i, j;
        for(i=0; i < tamanio_radar; i++) {
            for(j=0; j < tamanio_radar; j++) {
                int x = gps.x-(j-centro_radar);
                int y = gps.y-(i-centro_radar);
//                System.out.println(x + " " + y + " enl " + enLimites(x,y));
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
    protected void JSONDecode_Inicial(JsonObject mensaje) {
        super.JSONDecode_Inicial(mensaje);
        inicializarMapa();
    }
    
    /**
    *
    * @author Ana, Kieran
    * Busqueda en anchura para encontrar un lugar no explorado
    */
    protected Pair<Integer,Integer> busquedaAnchura(Pair<Integer,Integer> coords_act){
      int tam_barrido = centro_radar+1;
      int x = coords_act.getKey();
      int y = coords_act.getValue();
      
      int tasa_aceptabilidad = tamanio_radar*(centro_radar-2);
      int tam_barrido_maximo = Integer.MAX_VALUE;
      
      Pair<Integer,Integer> mejor_pair = null;
      Pair<Integer,Integer> aux_pair;
      int mejor_int = Integer.MAX_VALUE;
      int aux_int;
      
      while(x+tam_barrido < max_x || y + tam_barrido < max_y || x-tam_barrido >= 0 || y-tam_barrido >= 0){ //Mientras la cola no este vacia y no tengmaos objetivo
          for(int i = -tam_barrido; i <= tam_barrido; i++){
              for(int j = -tam_barrido; j <= tam_barrido; j++) {
                  if (Math.abs(i) == tam_barrido || Math.abs(j) == tam_barrido){
                    
                    //Si no esta en el mapa de memoria, buscamos el mejor que tenga suficientes a false para estar en la tasa de aceptabildad, si no buscamos otro
                    if( enLimites(x+i,y+j) && !mapaMemoria[x+i][y+j] ) {
                        aux_pair = new Pair<Integer, Integer>(x+i,y+j);
                        aux_int = busquedaAnchura_truesEnVision(aux_pair);
                        if (mejor_pair == null || mejor_int > aux_int ) { mejor_pair = aux_pair; mejor_int = aux_int; }
                        
                        if(aux_int > tasa_aceptabilidad && tam_barrido_maximo == Integer.MAX_VALUE){ //Para que no se quede en bucle donde varios son el mejor
                            tam_barrido_maximo  = tam_barrido + centro_radar;
                        }
                        else{ return mejor_pair; }
                    
                    
                    }
                  }
              }
          }
          
          tam_barrido++;
          if(tam_barrido > tam_barrido_maximo) { return mejor_pair; }
      }
      
      return null;
      
    }
    
    /**
    *
    * @author Kieran
    */
    protected int busquedaAnchura_truesEnVision(Pair<Integer,Integer> coords_act) {
        int i, j;
        int x = coords_act.getKey();
        int y = coords_act.getValue();
        int trues = 0;
                 
        for(i=0; i < tamanio_radar; i++) {
            for(j=0; j < tamanio_radar; j++) {
                int x2 = x-(j-centro_radar);
                int y2 = y-(i-centro_radar);
//                System.out.println(x + " " + y + " enl " + enLimites(x,y));
                if(enLimites(x2,y2) && mapaMemoria[x2][y2])
                    trues++;
            }
        }
        return trues;
    }

}
