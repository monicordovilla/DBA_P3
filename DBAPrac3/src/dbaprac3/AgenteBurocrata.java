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
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import com.eclipsesource.json.Json;

/**
 *
 * @author Kieran
 */
public class AgenteBurocrata extends AgenteSimple {
    int max_x;
    int max_y;
    int[][] mapa;
    JsonArray mapa_recibido; //El objeto recibido por el JSON inicial que describe la imagen png del mapa
    String clave;
    String session;
    AgenteDron dronFly;
    AgenteDron dronAux;
    AgenteDron dronRescue;
    AgenteDron dronRescue2;
    String nombreFly;
    String nombreAux;
    String nombreRescue;
    String nombreRescue2;
    ArrayList<Integer> objetivosRecogidos = new ArrayList<>(); //Objetivos recogidos por los drones Rescue
    double fuelRestante;
    boolean mapaAlto=true;

    public AgenteBurocrata(AgentID aid)throws Exception{
        super(aid);

        System.out.println("BUR: Inicializando");
        nombreFly = "GI_Fly12";
        dronFly = new AgenteFly(new AgentID(nombreFly));

        System.out.println("BUR: Inicializado dron");
        dronFly.start();
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
    private void guardarMapa(JsonArray ja) throws Exception{
        FileOutputStream fos = null;
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

        for(int i = 0; i < max_y; i++){
            for(int j = 0; j < max_x; j++) mapa[i][j] = image.getRGB( i, j ) & 0xff;
        }

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
        mapa_recibido = mensaje.get("map").asArray();

        clave = ultimo_mensaje_recibido.getConversationId();
    }

    /**
    *
    * @author Monica
    * Convierte el mapa en un array Json
    */
    private JsonArray JSON_Mapa(){
        //Codificando el mapa
        JsonArray map = new JsonArray();
        for(int i=0; i<max_y; i++){
            for(int j=0; j<max_x; j++){
                map.add( mapa[i][j] );
            }
        }

        return map;
    }

    /**
    *
    * @author Monica
    * Codifica el primer mesaje que se le va a enviar al dron
    * INFORM{"result":"<OK>", "session":"<master>", "dimx":<w>, "dimy":<h>, "map":[...], "x":<int>, "y":<int>, "estado":"<s>"}
    */
    private String JSONEncode_InicialDron(int x, int y){
        JsonObject a = new JsonObject();

        a.add("result", "OK");
        a.add("session", session);
        a.add("dimx", max_x);
        a.add("dimy", max_y);

        //Codificando el mapa
        JsonArray map = new JsonArray();
        map = JSON_Mapa();
        a.add("map",map);

        a.add("x", x); //PRAC3 -- CAMBIAR DESPUES
        a.add("y", y); //PRAC3 -- CAMBIAR DESPUES
        a.add("estado", "EXPLORACION" );

        String mensaje = a.toString();
        return mensaje;
    }

    /**
    * NUEVO AÑADIR ANA A DIAGRAMA
    * @author Monica
    * Codifica la actualización del mapa que se le va a enviar al dron
    * REQUEST{"mapa":"{}", "dimx":"<int>", "dimy":<int>}
    */
    private String JSONEncode_MapaActualizar(){
        JsonObject a = new JsonObject();

        //Codificando el mapa
        JsonArray map = new JsonArray();
        map = JSON_Mapa();
        a.add("map",map);

        //Pasar las variable de dimension del mapa
        a.add("dimx",max_x);
        a.add("dimy",max_y);

        String mensaje = a.toString();
        return mensaje;
    }

    /**
    *
    * @author Mónica
    */
    protected void avisarObjetivoEnontrado(){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivo-encontrado", true);

        //avisa al dron mas cercano en estado busqueda
        //calcular diferencia entre angulos para ver el mas cercano

    }

    /**
    *   MODIFICADO CAMBIAR EN DIAGRAMA ANA
    * @author Mónica
    */
    protected void avisarObjetivoIdentificado(int x, int y){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivo-identificado", true);
        mensaje.add("x", x);
        mensaje.add("y", x);

        //avisa al dron de rescate
       // comunicarDron(dronRescue, mensaje.asString(), ACLMessage.INFORM, clave);
    }

    /**
    *   MODIFICADO CAMBIAR EN DIAGRAMA ANA
    * @author Mónica
    */
    protected void avisarObjetivosCompletados(int x, int y){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivos-encontrados", true);

        //avisa al dron de rescate
        //comunicarDron(dronRescue, mensaje.asString(), ACLMessage.INFORM, clave);
    }

    
    /**
    *
    * @author Mónica
    */
    protected void responderPeticionRepostaje(String dron){ //PRACT3 -- cambiar a string 
        if (puedeRepostar(dron)){
            comunicar(dron, "ACEPTADO", ACLMessage.CONFIRM, clave);
        }
        else{
            comunicar(dron, "DENEGADO", ACLMessage.DISCONFIRM, clave);
        }
    }
    
    /**
    *
    * NUEVO AÑADIR ANA A DIAGRAMA
    * @author Celia
    */  
    
    private DronData obtenerDatos(String id){
        DronData  dron = new DronData();
        comunicar(id, "datos", ACLMessage.QUERY_REF, "datos");
        
        ACLMessage inbox;
        try{
            inbox = this.receiveACLMessage();
        }
        catch(Exception e){
            System.out.println("Error de comunicación: Excepción al escuchar");
            return null;
        }
        
        while(!(inbox.getPerformative().equals("INFORM") && inbox.getConversationId().equals("datos") && inbox.getSender().equals(id))){
            this.send(inbox);
            
            try{
                sleep(20);
                inbox = this.receiveACLMessage();
            }catch(Exception e){
                System.out.println("Error de comunicación: Excepción al escuchar");
                return null;
            }
        }
        
        JsonObject mensaje = Json.parse(inbox.getContent()).asObject();
        
        dron.gps.x = mensaje.get("gps").asObject().get("x").asInt();
        dron.gps.y = mensaje.get("gps").asObject().get("y").asInt();
        dron.gps.z = mensaje.get("gps").asObject().get("z").asInt();
        
        //Extraer el valor del combustible
        dron.fuel = mensaje.get("fuel").asFloat();
        
        dron.consumo_fuel = mensaje.get("consumo_fuel").asFloat();
        dron.ini_x = mensaje.get("ini_x").asInt();
        dron.ini_y = mensaje.get("ini_y").asInt();        
        dron.id = id;
        
        
        return dron;
    }
    
    //METODOS DE CONTROL


    /**
    * @author Celia
    */

    boolean puedeRepostar(String dron){ //PRACT3 -- cambiar a string
            if(dron.equals(nombreFly))
                return puedenVolver(true,true,true,false);
            if(dron.equals(nombreAux))
                return puedenVolver(true,true,false,true);
            if(puedenVolver(true,true,false,false))        //Soy rescate y pueden volver los dos drones de rescate
		return true;
            if(dron.equals(nombreRescue))
                return puedenVolver(true, false, false, false) && (objetivosRecogidos.get(0)>=objetivosRecogidos.get(1) || !puedenVolver(false, true, false, false));

            return puedenVolver(false, true, false, false) && (objetivosRecogidos.get(0)<objetivosRecogidos.get(1) || !puedenVolver(true, false, false, false));
    }
    /**
    *
    * @author Celia
    *
    *    Comprueba si pueden volver a casa los drones especificados
    *    (distancia hasta casa más bajada y margen "por posibles obstáculos")
    *    con el fuel general (que tiene y que puede repostar)
    *    d1 -> dronRescue  d2 -> dronRescue2   d3 -> dronFly   d4 -> dronAux
    */
    boolean puedenVolver(boolean d1, boolean d2, boolean d3, boolean d4){
        int pasos=0;
        double fuelNecesario=0;
        double fuelTotal=this.fuelRestante;
        int vecesRecarga;
        DronData dron = null;

        for(int i=0; i<4; i++){
            switch (i){
                case 0: if(d1) dron = obtenerDatos(nombreRescue); break;
                case 1: if(d2) dron = obtenerDatos(nombreRescue2); break;
                case 2: if(d3) dron = obtenerDatos(nombreFly); break;
                case 3: if(d4) dron = obtenerDatos(nombreAux); break;
            }

            if(dron!=null){
                 pasos = numPasos(dron.gps.x, dron.gps.y, dron.gps.z, dron.ini_x, dron.ini_y, mapa[dron.ini_x][dron.ini_y]) + 10; //Margen de 10 pasos
                 fuelNecesario = pasos*dron.consumo_fuel - dron.fuel; //fuel que necesita sin contar el que ya tiene

                 if(fuelNecesario>0){ //Si necesita
                      if(fuelTotal > fuelNecesario){
                        vecesRecarga = (int) Math.ceil(fuelNecesario/100); //Numero de veces que necesita recargar
                        fuelTotal -= vecesRecarga*100;
                      }
                      else
                          return false;
                 }
            }

            dron=null;
        }

        return true;
    }

    /**
    *
    * @author Celia
    *
    * Numero de pasos necesarios para ir de una posicion a otra.
    */

    int numPasos(int x_ini, int y_ini, int z_ini, int x_fin, int y_fin, int z_fin){
        return Math.max(Math.abs(x_fin-x_ini) , Math.abs(y_fin-y_ini)) +
                         (int) Math.ceil(Math.abs(z_fin-z_ini)/5);
    }


    /**
    * @author Celia
    *
    * El dron que rescata el objetivo encontrado es el que más cerca está del objetivo
    */

    String quienRescata(int x, int y, int z){
        DronData dronR1 = obtenerDatos(nombreRescue);
        DronData dronR2 =  obtenerDatos(nombreRescue2);
        int pasosR1 = numPasos(dronR1.gps.x, dronR1.gps.y, dronR1.gps.z, x, y, z);
        int pasosR2 = numPasos(dronR2.gps.x, dronR2.gps.y, dronR2.gps.z, x, y, z);
        if(pasosR1 <= pasosR2)
            return nombreRescue;
        return nombreRescue2;
    }
    
    /**
    * @author Celia
    *
    */
    
    ArrayList<Integer> asignarInicio(String id){
        ArrayList<Integer> inicio = new ArrayList<>();
     
        int x=0;
        int y=0;
        if(mapaAlto){
            if(id.equals(nombreFly)){
                x=Math.max(max_x/2 - 20,0);
                y=Math.min(20, max_y);
                
            }else if(id.equals(nombreAux)){
                x=Math.min(max_x/2 + 20,max_x);
                y=Math.min(20, max_y);
            }
        }else{             
            if(id.equals(nombreFly)){
                x = Math.max(max_x/2 - 120, 0);
                y = max_y/2;
            }else if(id.equals(nombreAux)){
                 x = max_x/2;
                 y = max_y/2;
            }
        }
        
        if(id.equals(nombreRescue)){
            x = max_x/4;
            y = max_y/4;    
        }
        else if(id.equals(nombreRescue2)){
            x = 3*max_x/4;
            y = 3*max_y/4;    
        }    
        
        
        inicio.add(x);
        inicio.add(y);  
        
        return inicio;
    }


//METODOS DE SUPERAGENT: Métodos sobreescritos y heredados de la clase SuperAgent

    /**
    *
    * @author Kieran
    */
    @Override
    public void execute(){
        JsonObject mensaje;
        String map = seleccionarMapa();
        String a = JSONEncode_Inicial(map);
        comunicar("Izar", a, ACLMessage.SUBSCRIBE, null);
        System.out.println("BUR: Enviando petición del mapa");
        mensaje = escuchar(true);
        JSONDecode_Inicial(mensaje);
        System.out.println("BUR: Guardando mapa");
        try {
            guardarMapa(mapa_recibido);
        } catch (Exception ex) {
            System.out.println("Excepcion: Error al obtener el mapa");
        }
        System.out.println("BUR: Inicializando drones");
         //Llamada a los drones
        
        ArrayList<Integer> inicio = asignarInicio(nombreFly);
        
        String m = JSONEncode_InicialDron(inicio.get(0), inicio.get(1));
        System.out.println("BUR: Codificando JSON");
        comunicar(nombreFly, m, ACLMessage.INFORM, null);
        //comunicarDron(dronAux, m, ACLMessage.INFORM, null);
        //comunicarDron(dronRescue, m, ACLMessage.INFORM, null);
        //comunicarDron(dronRescue2, m, ACLMessage.INFORM, null);


        //BORRAR LUEGO - PRUEBA
        /*
        for(int i = 0; i < max_y; i++){
            for(int j = 0; j < max_x; j++) System.out.print(String.format("%03d",mapa[i][j]) + ' ');
            System.out.print('\n');
        }

        comunicar("Izar", "", ACLMessage.CANCEL, clave);
        escuchar(true);
        escuchar(true);
        */
        //FIN DE PRUEBA

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
//        comunicar("Izar", "", ACLMessage.CANCEL, clave);
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
