/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import es.upv.dsic.gti_ia.core.AgentID;
import java.util.Stack;

/**
 *
 * @author Kieran
 */
public class AgenteHawk extends AgenteDron {
    static int tamanio_radar = 41;
    static int centro_radar = 20;
    
    public AgenteHawk(AgentID aid) throws Exception {
        super(aid);
        consumo_fuel = 2;
    }
}
