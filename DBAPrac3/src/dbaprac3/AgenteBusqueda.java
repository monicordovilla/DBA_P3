/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import static dbaprac3.Accion.*;
import es.upv.dsic.gti_ia.core.AgentID;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.util.ArrayList;
import javafx.util.Pair;
import javax.imageio.ImageIO;

/**
 *
 * @author anabosch
 */
public class AgenteBusqueda extends AgenteDron {
    
    ArrayList<Pair<Integer, Integer>> infrarojo;
    int pasos_desplazamiento; //Cuantos pasos nos quedan para poder realizar un nuevo barrido
    int alemanes_debug = 0; //BORRAR LUEGO, el numero de alemanes que ha visto
    int max_z_sparrow = 240;
    Estrategia plan;
    
    int area_ini_x = -1;
    int area_fin_x = -1;
    boolean area_limitado = false;
    
    //Estrategia BARRIDO_SIMPLE
    boolean dir_norte; //Cambiar luego
    boolean dir_oeste; //Cambiar luego
   
    //Estrategia ANCHURA_ALTO y ANCHURA_BAJO
    boolean[][] mapaMemoria;
    boolean objetivo;
    Pair<Integer,Integer> coords_dest;
    boolean busquedaCompletada;
    boolean mapaMemoria_init;
    
    public AgenteBusqueda(AgentID aid) throws Exception{
        super(aid);
        infrarojo = new ArrayList<>();
        objetivo = false;
        busquedaCompletada = false;
        mapaMemoria_init = false;
        coords_dest = null;
    }
    
    /**
    *
    * @author Ana, Kieran
    * Método sobreescrito de la clase padre para adaptarlo al Sparrow
    */
    @Override
    protected Accion comprobarAccion(){
        
        Accion accion = null;
        
        if(torescue == 0 || busquedaCompletada) {
            return goHome();
        }
        
        if (plan == Estrategia.BARRIDO_SIMPLE) { accion = (dir_norte) ? moveN : moveS; }

        else if (plan == Estrategia.ANCHURA_ALTO || plan == Estrategia.ANCHURA_BAJO) {
            accion = preparacionBusqueda();
            if(accion == null) {
                accion = goHome();
                System.out.println("DRON-"+rol+": goHome con " + accion);
            }
        }
        
        accion = scanInfrarojos(accion);
            if (plan == Estrategia.BARRIDO_SIMPLE && accion != moveUP) {  barridoEnBorde(accion); }
        accion = checkNavegacionNormal(accion);
            if(max_z < 255) { accion = checkManoDerecha(accion); }
        accion = checkRepostaje(accion);
          
      return accion;
        
    }

    /**
    *
    * @author Kieran
    * Método sobreescrito de la clase padre para adaptarlo al Sparrow
    */
    protected Accion goHome(){
        if(!busquedaCompletada) { coords_dest = new Pair<Integer,Integer>(ini_x,ini_y); }
        busquedaCompletada = true;
        Accion accion = checkDirObjetivo(coords_dest);
        accion = checkNavegacionNormal(accion);
            if(max_z < 255) { accion = checkManoDerecha(accion); }
        accion = checkRepostaje(accion);
        accion = checkMeta(accion);
        
        return accion;
    }
    
    /**
    *
    * @author Ana, Kieran
    * Método sobreescrito de la clase padre para adaptarlo al Sparrow
    */
    protected Accion preparacionBusqueda() {
        actualizarMapaMemoria();
            
        if(coords_dest != null && coords_dest.getKey() == gps.x && coords_dest.getValue() == gps.y) { objetivo = false; }

        if(!busquedaCompletada) {
            if(!objetivo) {
                System.out.println("DRON-" + rol + ": Buscando Objetivo");
                coords_dest = busquedaAnchura(new Pair<Integer,Integer>(gps.x,gps.y));
                if(coords_dest == null) {
                    System.out.println("DRON-" + rol + ": Finalizado");
                    return null;
                }
                else { objetivo = true; }
                System.out.println("DRON-" + rol + ": Coords: " + coords_dest);
            }

            return checkDirObjetivo(coords_dest);
            }
        return null;
    }
    
       /**
    *
    * @author Ana, Kieran
    * Estrategia de búsqueda para mapas altos en los que el equipo esta formado por 2 Fly y 2 Rescue
    */
    protected Accion barridoEnBorde(Accion accion){ //Cuando creemos las instancias debemos establecerle un identificador para que aqui segn eso recorra una parte del mapa
        
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
                   
        return accion;
                
    }
    
    /**
    *
    * @author Ana, Kieran
    * Estrategia de búsqueda para mapas altos en los que el equipo esta formado por 2 Fly y 2 Rescue
    */
    public Accion scanInfrarojos(Accion accion){ //Cuando creemos las instancias debemos establecerle un identificador para que aqui segn eso recorra una parte del mapa
        
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
        
        if(dron_demasiado_bajo && gps.z+5 <= max_z)
            accion = moveUP;
                   
        return accion;
                
        }
    
        /**
    *
    * @author Ana, Kieran
    * Mapa de memoria inicial
    */
    protected void inicializarMapaMemoria(){
        int i, j;
        
        int aux_i = (plan == Estrategia.ANCHURA_ALTO || !area_limitado) ? 0 : area_ini_x;
        int aux_f = (plan == Estrategia.ANCHURA_ALTO || !area_limitado) ? max_x-1 : area_fin_x;
        
        mapaMemoria = new boolean[max_x][max_y];
        for( i=0; i<max_x; i++) {
            for( j=0; j<max_y; j++) {
                if(i < aux_i || i > aux_f) { mapaMemoria[i][j] = true; continue; }
                if(plan == Estrategia.ANCHURA_BAJO) {
                    mapaMemoria[i][j] = mapa[i][j] > max_z;
                }
                else {
                    mapaMemoria[i][j] = mapa[i][j] <= max_z_sparrow;
                }
            }
        }
        
        mapaMemoria_init = true;
    }
    
    /**
    *
    * @author Ana, Kieran
    * Se actualiza la información del mapa de memoria con la información conocida por el drone
    */
    protected void actualizarMapaMemoria(){
        int i, j;
        for(i=0; i < tamanio_radar; i++) {
            for(j=0; j < tamanio_radar; j++) {
                int x = gps.x-(j-centro_radar);
                int y = gps.y-(i-centro_radar);
//                System.out.println(x + " " + y + " enl " + enLimites(x,y));
                if(enLimites(x,y) && !mapaMemoria[x][y])
                    mapaMemoria[x][y] = true;
            }
        }
                
    }
    
    /**
    *
    * @author Kieran
    * Se actualiza la información del mapa de memoria con la información conocida por el drone
    */
    @Override
    protected void JSONDecode_Inicial(JsonObject mensaje) {
        super.JSONDecode_Inicial(mensaje);
        
        JsonValue estrategia = mensaje.get("estrategia");
        switch(estrategia.toString()){
            case "BARRIDO_SIMPLE":
                plan = Estrategia.BARRIDO_SIMPLE;
                break;
            case "ANCHURA_BAJO":
                plan = Estrategia.ANCHURA_BAJO;
                break;
            case "ANCHURA_ALTO":
                plan = Estrategia.ANCHURA_ALTO;
                break;
        }
        
        JsonValue a = mensaje.get("area_ini_x");
        try{
            if(a != null && a.asInt() != -1) { area_ini_x = a.asInt(); }
            a = mensaje.get("area_fin_x");
            if(a != null&& a.asInt() != -1) { area_fin_x = a.asInt(); }
            if(area_fin_x != -1 && area_ini_x != -1) { area_limitado = true; }
        }
        catch(Exception e) { System.out.println("Error cogiendo area_ini_x y area_fin_x"); }
        inicializarMapaMemoria();
    }
    
    /**
    *
    * @author Ana, Kieran
    * Busqueda en anchura para encontrar un lugar no explorado
    */
    protected Pair<Integer,Integer> busquedaAnchura(Pair<Integer,Integer> coords_act){
      int tam_barrido = centro_radar+1;
      int x = coords_act.getKey();
      int y = coords_act.getValue();
      
      int tasa_aceptabilidad = tamanio_radar*(centro_radar-2);
      int tam_barrido_maximo = Integer.MAX_VALUE;
      
      Pair<Integer,Integer> mejor_pair = null;
      Pair<Integer,Integer> aux_pair;
      int mejor_int = Integer.MAX_VALUE;
      int aux_int;
      
      while(x+tam_barrido < max_x || y + tam_barrido < max_y || x-tam_barrido >= 0 || y-tam_barrido >= 0){ //Mientras la cola no este vacia y no tengmaos objetivo
          for(int i = -tam_barrido; i <= tam_barrido; i++){
              for(int j = -tam_barrido; j <= tam_barrido; j++) {
                  if (Math.abs(i) == tam_barrido || Math.abs(j) == tam_barrido){
                    
                    //Si no esta en el mapa de memoria, buscamos el mejor que tenga suficientes a false para estar en la tasa de aceptabildad, si no buscamos otro
                    if( enLimites(x+i,y+j) && !mapaMemoria[x+i][y+j] ) {
                        aux_pair = new Pair<Integer, Integer>(x+i,y+j);
                        aux_int = busquedaAnchura_truesEnVision(aux_pair);
                        if (mejor_pair == null || mejor_int > aux_int ) { mejor_pair = aux_pair; mejor_int = aux_int; }
                        
                        if(aux_int > tasa_aceptabilidad && tam_barrido_maximo == Integer.MAX_VALUE){ //Para que no se quede en bucle donde varios son el mejor
                            tam_barrido_maximo  = tam_barrido + centro_radar;
                        }
                        else{ return mejor_pair; }
                    
                    
                    }
                  }
              }
          }
          
          tam_barrido++;
          if(tam_barrido > tam_barrido_maximo) { return mejor_pair; }
      }
      
      return null;
      
    }
    
    /**
    *
    * @author Kieran
    */
    protected int busquedaAnchura_truesEnVision(Pair<Integer,Integer> coords_act) {
        int i, j;
        int x = coords_act.getKey();
        int y = coords_act.getValue();
        int trues = 0;
                 
        for(i=0; i < tamanio_radar; i++) {
            for(j=0; j < tamanio_radar; j++) {
                int x2 = x-(j-centro_radar);
                int y2 = y-(i-centro_radar);
//                System.out.println(x + " " + y + " enl " + enLimites(x,y));
                if(enLimites(x2,y2) && mapaMemoria[x2][y2])
                    trues++;
            }
        }
        return trues;
    }

    @Override
    public void finalize(){
        System.out.println("\nAlemanes encontrados: " + alemanes_debug);
        super.finalize(); //Pero si se incluye, esto es obligatorio
        if(mapaMemoria_init) { imprimirMapaMemoria(); System.out.println("DRON-"+rol+": Guardado mapa a disco"); }
    }
    
    public void imprimirMapaMemoria(){
        BufferedImage imagen = new BufferedImage(max_x, max_y, BufferedImage.TYPE_INT_RGB);
        for(int i = 0; i < max_x; i++){
            for(int j = 0; j < max_y; j++){
                imagen.setRGB(i, j, (area_limitado && (i < area_ini_x || i > area_fin_x)) ? 0x800000 : (mapaMemoria[i][j]) ?  ((mapa[i][j] > max_z) ? 0x000080 : 0xffffff) : 0x00000 );
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