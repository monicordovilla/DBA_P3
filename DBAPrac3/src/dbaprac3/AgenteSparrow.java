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
        
        //Cambiar si se quiere
        infrarojo = new ArrayList();
        pasos_desplazamiento = tamanio_radar;
        objetivo = false;
        busquedaCompletada = false;
        mapaMemoria_init = false;
        coords_dest = null;
    }
        
    /**
    *
    * @author Kieran
    */    
    protected Accion checkDirObjetivo(Pair<Integer,Integer> obj){
        double angulo_falsogonio = Math.atan2(-(gps.x-obj.getKey()), gps.y-obj.getValue()) * 180/Math.PI;
        if (angulo_falsogonio < 0) { angulo_falsogonio = 360 + angulo_falsogonio; }
        gonio.angulo = (float) angulo_falsogonio;
        gonio.distancia = 5;
        return siguienteDireccion(true);
    }
    
    /**
    *
    * @author Ana, Kieran
    * Método sobreescrito de la clase padre para adaptarlo al Sparrow
    */
    @Override
    protected Accion comprobarAccion(){
      Accion accion = null;
      
      Pair<Integer,Integer> coords_act = new Pair<>(gps.x,gps.y);
      
      actualizarMapa();
      
      if(coords_dest != null && coords_dest.getKey() == gps.x && coords_dest.getValue() == gps.y) { objetivo = false; }
      
      if(!busquedaCompletada) {
        if(!objetivo) {
            System.out.println("DRON-S: Buscando Objetivo");
            coords_dest = busquedaAnchura(coords_act);
            if(coords_dest == null) { busquedaCompletada = true; }
            else { objetivo = true; }
            System.out.println("DRON-S: Coords: " + coords_dest);
        }
        
        accion = checkDirObjetivo(coords_dest);
        accion = scanInfrarojos(accion);
        accion = checkNavegacionNormal(accion);
        accion = checkManoDerecha(accion);
      }
      accion = checkRepostaje(accion);
          
      return accion;
        
    }
    
    @Override
    public void finalize(){
        System.out.println("\nAlemanes encontrados: " + alemanes_debug);
        super.finalize(); //Pero si se incluye, esto es obligatorio
        if(mapaMemoria_init) { imprimirMapaMemoria(); System.out.println("DRON-S: Guardado mapa a disco"); }
    }
    
    public void imprimirMapaMemoria(){
        /*BufferedImage imagen = new BufferedImage(max_x, max_y, BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i < max_x; i++){
            for(int j = 0; j < max_y; j++){
                imagen.setRGB(i, j, (mapaMemoria[i][j]) ? ((mapa[i][j] > max_z) ? 0x000080 : 0xffffff) : 0x00000 );
            }
        }
        
        FileOutputStream fos;
        try{
            fos = new FileOutputStream(this.getAid().toString() + "_memoria.png");
            ImageIO.write(imagen, "png", fos);
            fos.close();
            System.out.println("Traza guardada");
        }
        catch(Exception e) { e.printStackTrace(); }*/
    }
    
}
