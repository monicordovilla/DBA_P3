/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author Kieran
 */
public class AgenteHawk extends AgenteBusqueda {
    
    public AgenteHawk(AgentID aid) throws Exception {
        super(aid);
        consumo_fuel = 2;
        tamanio_radar = 41;
        centro_radar = 20;
        rol = "hawk";
        max_z = 230;
        infrared = new int[tamanio_radar][tamanio_radar];
        
        //Cambiar si se quiere
        plan = Estrategia.ANCHURA_BAJO;
        infrarojo = new ArrayList();

    }
}
