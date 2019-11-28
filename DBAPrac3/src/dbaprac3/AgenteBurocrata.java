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
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Kieran
 */
public class AgenteBurocrata extends AgenteSimple {
    int max_x;
    int max_y;
    int[][] mapa;
    String clave;
    String session;
    AgenteDron dronFly;
    AgenteDron dronAux;
    AgenteDron dronAux2;
    AgenteDron dronRescue;

    public AgenteBurocrata(AgentID aid)throws Exception{
        super(aid);
    }

    
    //METODOS PARA LA GESTION DEL MAPA
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
    * @author Kieran
    * Método para guardar el mapa
    */
    private void guardarMapa(JsonObject respuesta) throws Exception{
        FileOutputStream fos = null;
        JsonArray ja = respuesta.get("trace").asArray();
        byte data[] = new byte [ja.size()];
        for(int i=0; i<data.length;i++){
            data[i] = (byte) ja.get(i).asInt();
        }
        fos = new FileOutputStream("map.png");
        fos.write(data);
        fos.close();
        File mapFile = new File("map.png");
        BufferedImage image = ImageIO.read(mapFile);
        System.out.println("Mapa Descargada");

        mapa = new int[max_y][max_x];
    }
    
    
    //METODOS DE COMUNICACION CON EL CONTROLLER

    /**
    *
    * @author Monica
    * Codifica el primer mesaje que se le va a enviar al servidor
    * SUSCRIBE{"map":"<m>", "user":"<u>", "password":"<p>"}
    */
    private String JSONEncode_Inicial(String mapa){
        JsonObject a = new JsonObject();
        //Iniciamos y mandamos el mapa que queremos
        a.add("map", mapa);

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
        session = mensaje.get("session").asString();
        max_x = mensaje.get("dimx").asInt();
        max_y = mensaje.get("dimy").asInt();
        clave = mensaje.get("key").asString();
        //obtener mapa
    }

    
    //METODOS DE SUPERAGENT: Métodos sobreescritos y heredados de la clase SuperAgent
    
    /**
    *
    * @author Kieran
    */
    @Override
    public void execute(){
        String map = seleccionarMapa();
        String a = JSONEncode_Inicial(map);
        comunicar("Izar", a, ACLMessage.SUBSCRIBE);
        escuchar();

    }

    /**
    *
    * @author Kieran
    */
    @Override
    protected boolean validarRespuesta(ACLMessage a) {  //ACABAR LUEGO
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
    *
    * @author Kieran
    */
    @Override
    public void finalize() { //Opcional
        System.out.println("\nFinalizando");
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
