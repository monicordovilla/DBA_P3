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
        plan = Estrategia.ANCHURA_ALTO;      
        
        dir_norte = false; //Cambiar luego
        dir_oeste = false; //Cambiar luego
        infrarojo = new ArrayList();
        pasos_desplazamiento = tamanio_radar;
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

            
    @Override
    public void finalize(){
        System.out.println("\nAlemanes encontrados: " + alemanes_debug);
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
    
}
