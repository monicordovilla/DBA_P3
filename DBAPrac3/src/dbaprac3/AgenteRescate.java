/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.Stack;

/**
 *
 * @author Kieran
 */
public class AgenteRescate extends AgenteDron {
    static int tamanio_radar = 1;
    static int centro_radar = 0;
    
    public AgenteRescate(AgentID aid) throws Exception {
        super(aid);
        consumo_fuel = 0.5;
    }
}
