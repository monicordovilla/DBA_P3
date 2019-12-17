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
           
    @Override
    public void finalize(){
        System.out.println("\nAlemanes encontrados: " + alemanes_debug);
        super.finalize(); //Pero si se incluye, esto es obligatorio
        imprimirMapaMemoria();
        //if(mapaMemoria_init) { imprimirMapaMemoria(); System.out.println("DRON-S: Guardado mapa a disco"); }
    }
    
    public void imprimirMapaMemoria(){
        BufferedImage imagen = new BufferedImage(max_x, max_y, BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i < max_x; i++){
            for(int j = 0; j < max_y; j++){
                imagen.setRGB(i, j, (i < ini_x || i > fin_x) ? 0x800000 : (mapaMemoria[i][j]) ?  ((mapa[i][j] > max_z) ? 0x000080 : 0xffffff) : 0x00000 );
            }
        }
        
        FileOutputStream fos;
        try{
            fos = new FileOutputStream( (this.getName() + "_memoria.png") );
            ImageIO.write(imagen, "png", fos);
            fos.close();
            System.out.println("Traza guardada");
        }
        catch(Exception e) { e.printStackTrace(); }
    }
    
}
