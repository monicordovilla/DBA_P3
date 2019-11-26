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
public class AgenteFly extends AgenteDron {
    static int tamanio_radar = 5;
    static int centro_radar = 2;
    
    public AgenteFly(AgentID aid) throws Exception {
        super(aid);
        consumo_fuel = 0.1;
    }
}
