/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import static dbaprac3.Accion.*;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;
import javafx.util.Pair;

/**
 *
 * @author Kieran
 */
public class AgenteFly extends AgenteBusqueda {
    
    boolean dir_norte = false; //Cambiar luego
    boolean dir_oeste = false; //Cambiar luego
    
    public AgenteFly(AgentID aid) throws Exception {
        //No cambiar
        super(aid);
        consumo_fuel = 0.1;
        tamanio_radar = 5;
        centro_radar = 2;
        rol = "fly";
        max_z = 255;
        infrared = new int[tamanio_radar][tamanio_radar];
        
        //Cambiar si se quiere
        infrarojo = new ArrayList();
        pasos_desplazamiento = tamanio_radar;
        plan = Estrategia.ANCHURA_ALTO;
    }
    
    /**
    *
    * @author Ana, Kieran
    * //Método sobreescrito de la clase padre para adaptarlo al Fly
    */
    @Override
    protected Accion comprobarAccion(){
      Accion accion = null;
      
      if (plan == Estrategia.BARRIDO_SIMPLE) { accion = (dir_norte) ? moveN : moveS; }
      
      accion = scanInfrarojos(accion);
      if (plan == Estrategia.BARRIDO_SIMPLE && accion == moveUP) { barridoEnBorde(accion); }
      accion = checkNavegacionNormal(accion);
      accion = checkRepostaje(accion);
          
      return accion;
        
    }
    
    /*
    protected Accion tmp() { //BORRAR LUEGO: metodo para detectar los objetivos del mapa 1 mas rapido que con el barrido
        if (gps.x < 30) return moveSE;
        else if (gps.x == 30 && gps.y < 90) return moveS;
        else if (gps.x < 60) return moveE;
        else if(gps.y > 0) return moveN;
        else return moveDW; //Kamikaze para que se de prisa
    }*/
    
    /**
    *
    * @author Ana, Kieran
    * Estrategia de búsqueda para mapas altos en los que el equipo esta formado por 2 Fly y 2 Rescue
    */
    public Accion barridoEnBorde(Accion accion){ //Cuando creemos las instancias debemos establecerle un identificador para que aqui segn eso recorra una parte del mapa
        
        //Mover a la izda/dcha si se ha llegado al limite del mapa
        if((gps.y == min_y && dir_norte) || (gps.y == max_y-1 && !dir_norte)) { //Vemos si hemos llegado a la parte superior o inferior del mapa
            if((dir_oeste && gps.x == min_x) || (!dir_oeste && gps.x == max_x-1)) { //Vemos si estamos en el borde lateral, si estamos paramos y bajamos
                pasos_desplazamiento = 0;
            }
            
            if(pasos_desplazamiento > 0) {
                Accion dir = (dir_oeste) ? moveW : moveE; //Movemos al este u oeste segun corresponda
                accion = dir;
                pasos_desplazamiento--;
            }
            else { //Cambio de sentido vertical, ya que hemos cubierto todod el radar
                pasos_desplazamiento = tamanio_radar;
                dir_norte = !dir_norte;
                accion = (dir_norte) ? moveN : moveS;
            }
        }
                   
        return accion;
                
    }
            
    @Override
    public void finalize(){
        System.out.println("\nAlemanes encontrados: " + alemanes_debug);
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
    
}
