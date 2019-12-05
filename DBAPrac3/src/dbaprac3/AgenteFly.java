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
      
      accion = (dir_norte) ? moveN : moveS;
      
      busquedaMapasAltos(accion);
      accion = checkNavegacionNormal(accion);
      accion = checkRepostaje(accion);
          
      return accion;
        
    }
    
    /**
    *
    * @author Ana, Kieran
    * Estrategia de búsqueda para mapas altos en los que el equipo esta formado por 2 Fly y 2 Rescue
    */
    public Accion busquedaMapasAltos(Accion accion){ //Cuando creemos las instancias debemos establecerle un identificador para que aqui segn eso recorra una parte del mapa
        
        boolean dron_demasiado_bajo = false;
        
        
        //Comprobar el infrarojos por si hay que subir o enviar algo
        for(int i=0; i<infrared.length && !dron_demasiado_bajo; i++) 
        {
            for(int j=0; j<infrared.length; j++)
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

        //Mover a la izda/dcha si se ha llegado al limite del mapa
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
        
        if(dron_demasiado_bajo)
            accion = moveUP;
                   
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
