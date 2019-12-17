/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 *
 * @author Kieran
 */
public class DBAPrac3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        AgentsConnection.connect(
                "isg2.ugr.es",  //Host: localhost si se quiere probar en nuestra propia maquina
                6000,           //Puerto: por defecto 5672
                "Practica3",        //VHOST
                "Ibbotson",        //Usuario
                "oLARuosE",       //Contraseña
                false           //SSL
        );

        AgenteBurocrata Huph;

        try {
            Huph = new AgenteBurocrata(new AgentID("GI_tIgnoreK3-000"));
        } catch (Exception ex) {
            System.err.println("already on the platform, goofy");
            return;
        }

        Huph.start();
    }

}
