/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import es.upv.dsic.gti_ia.core.AgentID;
import DBA.SuperAgent;
import com.eclipsesource.json.JsonObject;
import java.util.Scanner;



/**
 *
 * @author Kieran
 */
public class AgenteBurocrata extends SuperAgent {
    public AgenteBurocrata(AgentID aid)throws Exception{
        super(aid);
    }
    int dimx;
    int dimy;
    int map[][];
    
    //METODOS DE COMUNICACION CON EL CONTROLLER
    
    /**
    *
    * @author Kieran
    * Método para seleccionar el mapa que queremos probar desde la terminal
    */
    private String seleccionarMapa(){
        System.out.println("Inserte el nombre del mapa a probar:");
        Scanner s = new Scanner(System.in);
        String mapa_seleccionado = s.nextLine();
        return mapa_seleccionado;
    }
    
    /**
    *
    * @author Monica
    * SUSCRIBE{"map":"<m>", "user":"<u>", "password":"<p>"}
    */
    private String JSONEncode_Inicial(String mapa){
        JsonObject a = new JsonObject();
        //Iniciamos y mandamos el mapa que queremos
        //a.add("command", "login");
        a.add("map", mapa);

        /*//Solicitamos los sensores de los que queremos informacion
        a.add("radar", true);
        a.add("elevation", false);
        a.add("magnetic", true);
        a.add("gps", true);
        a.add("fuel", true);
        a.add("gonio", true);*/

        //Mandamos nuestro usuario y contraseña
        a.add("user", "Ibbotson");
        a.add("password", "oLARuosE");

        String mensaje = a.toString();
        return mensaje;
    }
    
    /**
    *
    * @author Kieran, Monica
    * Decodifica el primer mensaje con atributos del mapa
    * INFORM{"result":"OK", "session":"<master>", "dimx":"<w>", "dimy":"<h>", "map":[]}:CONVERSATION-ID@
    */
    private void JSONDecode_Inicial(JsonObject mensaje){
        max_x = mensaje.get("dimx").asInt();
        max_y = mensaje.get("dimy").asInt();
        min_z = mensaje.get("min").asInt();
        max_z = mensaje.get("max").asInt();
        clave = mensaje.get("key").asString();
        
    }
    
    
}
