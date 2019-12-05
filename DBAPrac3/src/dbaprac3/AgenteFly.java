/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import static dbaprac3.Accion.*;
import static dbaprac3.AgenteDron.centro_radar;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;
import javafx.util.Pair;

/**
 *
 * @author Kieran
 */
public class AgenteFly extends AgenteDron {
    
    ArrayList<Pair<Integer, Integer>> infrarojo;
    boolean dir_norte;
    boolean dir_oeste;
    int pasos_desplazamiento; //Cuantos pasos nos quedan para poder realizar un nuevo barrido
    
    public AgenteFly(AgentID aid) throws Exception {
        //No cambiar
        super(aid);
        consumo_fuel = 0.1;
        tamanio_radar = 5;
        centro_radar = 2;
        rol = "fly";
        radar = new int[tamanio_radar][tamanio_radar];
        infrared = new int[tamanio_radar][tamanio_radar];
        
        //Cambiar si se quiere
        infrarojo = new ArrayList();
        pasos_desplazamiento = tamanio_radar;
    }
    
    /**
    *
    * @author Ana, Kieran
    * //Método sobreescrito de la clase padre para adaptarlo al Fly
    */
    @Override
    protected Accion comprobarAccion(){
      Accion accion = null;
      
      if(repostando) return checkRepostaje(accion);
      
      accion = checkMeta();
      if(accion != null) return accion;

      accion = (dir_norte) ? moveN : moveS;
      
      busquedaMapasAltos(accion);
      accion = checkRepostaje(accion);
    
      return accion;
        
    }
      
    /**
    *
    * @author Ana, Kieran
    * Estrategia de búsqueda para mapas altos en los que el equipo esta formado por 2 Fly y 2 Rescue
    */
    public Accion busquedaMapasAltos(Accion accion){ //Cuando creemos las instancias debemos establecerle un identificador para que aqui segn eso recorra una parte del mapa
        
        int x=0,y=0;
        boolean dron_demasiado_bajo = false;
        Pair<Integer,Integer> coords = movimientoEnRadar(accion, centro_radar);
        x = coords.getKey();
        y = coords.getValue();
        
        for(int i=0; i<radar.length && !dron_demasiado_bajo; i++) 
        {
            for(int j=0; j<radar.length; j++)
            {
                if(infrared[i][j] == -1) {
                    dron_demasiado_bajo = true;//Subir si i j vale -1
                }
                if(infrared[i][j] == 1) {
                    Pair<Integer,Integer> coords_obj = new Pair<>(gps.x+(i-centro_radar),gps.y+(j-centro_radar));
                    if(!infrarojo.contains(coords_obj)) {//mandar al burocrata que hay uno y la posicion
                        infrarojo.add(coords_obj);
                        //TODO -- MANDAR AL BUROCRATA
                    }
                }
            }
        }

        if(gps.x == min_x || gps.x == max_x) { //Vemos si hemos llegado a la parte superior o inferior del mapa
            if(pasos_desplazamiento != 0) {
                Accion dir = (dir_oeste) ? moveW : moveE; //Movemos al este u oeste segun corresponda
                if((dir_oeste && gps.y == min_y) || (!dir_oeste && gps.y == max_y)) { //Vemos si estamos en el borde lateral, si estamos paramos
                    accion = stop; 
                }
                else { accion = dir; }
                pasos_desplazamiento--;
            }
            else {
                pasos_desplazamiento = tamanio_radar;
                dir_norte = !dir_norte;
            }
        }
        
        coords = movimientoEnRadar(accion, centro_radar);
        x = coords.getKey();
        y = coords.getValue();
        
        if(radar[x][y]==0) //Si la hemos liado, salir
            return logout;
        else if(dron_demasiado_bajo || (radar[x][y] > gps.z && (gps.z+5 <= max_z) && puedeSubir(accion)))
            return moveUP;
        else if(radar[x][y] <= gps.z) //Estamos a la altura de la celda a la que queremos ir o superor
            return accion;
            
        return accion;
                
        }
    
    
    
    /**
    *
    * @author Ana
    * Estrategia de búsqueda para mapas bajos en los que el equipo esta formado por 1 Fly, 1 Hawk y 2 Rescue
    */
    /*public void estrategiaMapasBajos(){//Variable que tenemos que ver donde recoje su valor)
        //if encuentra busqueda directa
    }
    
    /**
    *
    * @author Ana
    * Se conoce donde esta el objetivo y se dirige directamente hacia el
    */
    /*public void eobjetivoDirecto(){//Variable que tenemos que ver donde recoje su valor)
        //if encuentra busqueda directa
    }*/
}
