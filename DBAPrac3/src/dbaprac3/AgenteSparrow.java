/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import com.eclipsesource.json.JsonObject;
import static dbaprac3.Accion.*;
import es.upv.dsic.gti_ia.core.AgentID;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import javafx.util.Pair;
import javax.imageio.ImageIO;

/**
 *
 * @author Ana
 */
public class AgenteSparrow extends AgenteBusqueda {
            
    public AgenteSparrow(AgentID aid) throws Exception {
        //No cambiar
        super(aid);
        consumo_fuel = 0.5;
        tamanio_radar = 11;
        centro_radar = 5;
        rol = "sparrow";
        max_z = 240;
        infrared = new int[tamanio_radar][tamanio_radar];
        plan = Estrategia.ANCHURA_BAJO;
        
        //Cambiar si se quiere
        infrarojo = new ArrayList();
        pasos_desplazamiento = tamanio_radar;
        objetivo = false;
        busquedaCompletada = false;
        mapaMemoria_init = false;
        coords_dest = null;
    }          
}