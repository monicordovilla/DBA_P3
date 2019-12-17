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
public class AgenteFly extends AgenteDron {
    
    ArrayList<Pair<Integer, Integer>> infrarojo;
    boolean dir_norte = false; //Cambiar luego
    boolean dir_oeste = false; //Cambiar luego
    int pasos_desplazamiento; //Cuantos pasos nos quedan para poder realizar un nuevo barrido
    int alemanes_debug = 0; //BORRAR LUEGO, el numero de alemanes que ha visto
    
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
      
      accion = busquedaMapasAltos(accion);
      accion = tmp();
      accion = checkNavegacionNormal(accion);
      accion = checkRepostaje(accion);
          
      return accion;
        
    }
    
    protected Accion tmp() { //BORRAR LUEGO: metodo para detectar los objetivos del mapa 1 mas rapido que con el barrido
        if (gps.x < 30) return moveSE;
        else if (gps.x == 30 && gps.y < 90) return moveS;
        else if (gps.x < 60) return moveE;
        else if(gps.y > 0) return moveN;
        else return moveDW; //Kamikaze para que se de prisa
    }
    
    /**
    *
    * @author Ana, Kieran
    * Estrategia de búsqueda para mapas altos en los que el equipo esta formado por 2 Fly y 2 Rescue
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
        
        if(dron_demasiado_bajo)
            accion = moveUP;
                   
        return accion;
                
        }
    
    @Override
    public void finalize(){
        System.out.println("\nAlemanes encontrados: " + alemanes_debug);
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
    
}
