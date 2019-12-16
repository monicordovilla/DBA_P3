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
 * @author Kieran, Celia
 */
public class AgenteBurocrata extends AgenteSimple {
    int max_x;
    int max_y;
    int[][] mapa;
    JsonArray mapa_recibido; //El objeto recibido por el JSON inicial que describe la imagen png del mapa
    String clave;
    String session;
    
    ArrayList<DronData> drones;
    double fuelRestante;
    boolean mapaAlto=true;

    public AgenteBurocrata(AgentID aid)throws Exception{
        super(aid);
        this.drones = new ArrayList<>();

        System.out.println("BUR: Inicializando");
        this.drones.add(new DronData("GI_Fly01", Rol.Fly));
        this.drones.add(new DronData("GI_Rescue01", Rol.Rescue));
        
        new AgenteFly(new AgentID(drones.get(0).nombre)).start();
        new AgenteRescate(new AgentID(drones.get(1).nombre)).start();

        System.out.println("BUR: Inicializado drones");
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
    *   PREGUNTAR
    * @author Monica
    * Decodifica el estado del dron
    *
    * INFORM{"result":{"gps":"{x,y,z}", "infrared":"{0,0,...}",
    * "gonio":"{"distance": -1, "angle": -1}", "fuel":100, "goal": false,
    * "status": operative, "awacs":[{"name":<agent1>, "x":10, "y":99, "z":100,
    * "direction": accion}, ...] }}:CONVERSATION-ID@
    */
    /*private String JSONEncode_variables(JsonObject mensaje){
        JsonObject a = new JsonObject();

        JsonObject coordenadas = new JsonObject();
        coordenadas.add("x", gps.x);
        coordenadas.add("y", gps.y);
        coordenadas.add("z", gps.z);
        a.add("gps", coordenadas);

        //infrared
        JsonArray inf = new JsonArray();
        for(int i=0; i<max_y; i++){
            for(int j=0; j<max_x; j++){
                inf.add( mapa[i][j] );
            }
        }
        a.add("infrared", inf);

        //gonio
        JsonObject g = new JsonObject();
        g.add("distance", gonio.distancia);
        g.add("distance", gonio.angulo);
        a.add("gonio", g);

        //fuel
        a.add("fuel", fuel);

        //status
        a.add("status", status);

        //awacs
        a.add("awacs", awacs);
        return a.asString();
    }*/

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
    * Calcula la distancia entre 2 puntos
    * @author Mónica
    */
    protected double distancia(int x1, int y1, int z1, int x2, int y2, int z2){
        double d =  Math.sqrt( (Math.pow(x2-x1,2)) + (Math.pow(y2-y1,2)) + (Math.pow(z2-z1,2)) );
        return d;
    }

    /**
    *
    * @author Mónica
    */
    protected void avisarObjetivoEncontrado(){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivo-encontrado", true);

        //avisa al dron mas cercano en estado busqueda
        //calcular diferencia entre angulos para ver el mas cercano

    }
    
    /**
    *   
    * @author Mónica
    * 
    * MODIFICAR: DEVOLVER X E Y
    */
    protected boolean recibirObjetivoEncontrado(){
        JsonObject mensaje = escuchar();
        return mensaje.get("objetivo-encontrado").asBoolean();
    }
    
    /**
    *   MODIFICADO CAMBIAR EN DIAGRAMA ANA
    * @author Mónica
    */
    protected void avisarObjetivoIdentificado(int x, int y){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivo-identificado", true);
        mensaje.add("x", x);
        mensaje.add("y", y);

        System.out.println("Avisando a rescate");
        //avisa al dron de rescate
        comunicar(quienRescata(x, y), mensaje.toString(), ACLMessage.INFORM, null); //TODO escoger que dron de rescate es: borrar esto luego
        System.out.println("Rescate Avisado");
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
    
    private void actualizarDatos(ACLMessage inbox){
        String id= inbox.getSender().toString();
        DronData  dron = getDronData(id);
        comunicar(id, "datos", ACLMessage.QUERY_REF, "datos");
        
        JsonObject mensaje = Json.parse(inbox.getContent()).asObject();
        
        dron.gps.x = mensaje.get("gps").asObject().get("x").asInt();
        dron.gps.y = mensaje.get("gps").asObject().get("y").asInt();
        dron.gps.z = mensaje.get("gps").asObject().get("z").asInt();
        
        //Extraer el valor del combustible
        dron.fuel = mensaje.get("fuel").asFloat();
        
        dron.consumo_fuel = mensaje.get("consumo_fuel").asFloat();
    }
    
    
    DronData getDronData(String id){
        for(DronData d : drones){
            if(d.nombre.equals(id))
                return d;
        }
        
        return null;
    }
    
    //METODOS DE CONTROL


    /**
    * @author Celia
    */

    boolean puedeRepostar(String nombre){ //PRACT3 -- cambiar a string
        ArrayList<DronData> volver =new ArrayList<>();
        DronData dron = getDronData(nombre);
       
        
        if(dron==null)
            return false;

        //actualizarDatos(nombre);
        volver.add(dron);
                
        if(dron.rol==Rol.Rescue){
            for(DronData d : drones)
                if(d.rol==Rol.Rescue && d.recogidos>dron.recogidos){
                    //actualizarDatos(d.nombre);
                    volver.add(d);
                }
        }
        else if(dron.rol==Rol.Fly){
            for(DronData d : drones)
                 if(d.rol==Rol.Rescue){
                    //actualizarDatos(d.nombre);
                    volver.add(d);
                 }
        }
        else if(dron.rol==Rol.Sparrow){
             for(DronData d : drones)
                 if(d.rol==Rol.Rescue || d.rol==Rol.Fly){
                    //actualizarDatos(d.nombre);
                    volver.add(d);    
                 }
        }
        else
            for(DronData d : drones)
                 if(d.rol!=Rol.Hawk){
                    //actualizarDatos(d.nombre);
                    volver.add(d);
                 }

        return puedenVolver(volver);
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
    
    boolean puedenVolver(ArrayList<DronData> drones){
        int pasos=0;
        double fuelNecesario=0;
        double fuelTotal=this.fuelRestante;
        int vecesRecarga;

        for(DronData dron : drones){
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

    String quienRescata(int x, int y){
        int z = mapa[x][y];
        DronData dron = null;
        int pasosMin = Integer.MAX_VALUE;
        int pasos;
        
        for(DronData d : drones){
            if(d.rol==Rol.Rescue){
                //actualizarDatos(d.nombre);
                pasos = numPasos(d.gps.x, d.gps.y, d.gps.z, x, y, z);
                if(pasos<pasosMin){
                    dron=d;
                    pasosMin=pasos;
                }
            }
        }
        
        return dron.nombre;
    }
    
    /**
    * @author Celia
    *
    */
    
    ArrayList<Integer> asignarInicio(String id){
        ArrayList<Integer> inicio = new ArrayList<>();
     
        int x=0;
        int y=0;
        

        if(id.equals(drones.get(0).nombre)){ //FLY1
            x=Math.max(max_x/2-20, 0);

        }else if(id.equals(drones.get(2).nombre)){ //FLY2
            x=Math.min(max_x/2+20, max_x);
        }
        
        else if(id.equals(drones.get(1).nombre)){ //RESCUE1
            y = max_y/2;    
        }
        else if(id.equals(drones.get(3).nombre)){ //RESCUE2
            x = max_x;
            y = max_y/2;    
        }
        
        getDronData(id).ini_x=x;
        getDronData(id).ini_y=y;
        
        inicio.add(x);
        inicio.add(y);  
        
        return inicio;
    }


//METODOS DE SUPERAGENT: Métodos sobreescritos y heredados de la clase SuperAgent

    /**
    *
    * @author Kieran, Monica, Celia
    */
    @Override
    public void execute(){
        ACLMessage inbox=null;
        
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
        
        ArrayList<Integer> inicio;
        String m;
        for(DronData dron : drones){
            inicio = asignarInicio(dron.nombre);
            m = JSONEncode_InicialDron(inicio.get(0), inicio.get(1));
            System.out.println("BUR: Codificando JSON");
            comunicar(dron.nombre, m, ACLMessage.INFORM, null);
        }
       
//PRAC3 -- DESCOMENTAR LUEGO        
/*        while(validarRespuesta(mensaje)){
            //Espera mensaje
            escuchar();
        }
        if(!validarRespuesta(mensaje)) { //si se sale por un resultado invalido devuelve las percepciones antes de la traza
            escuchar();
        }
*/      
        //comunicarDron(dronAux, m, ACLMessage.INFORM, null);
        //comunicarDron(dronRescue2, m, ACLMessage.INFORM, null);
        
        //PRUBEA DE RESCATE
        for(int i = 0; i < 5; i++){
            JsonObject coords_objetivo = escuchar();
        System.out.println("AAAA3");
            avisarObjetivoIdentificado(coords_objetivo.get("x").asInt(), coords_objetivo.get("y").asInt());
        System.out.println("AAAA4");
        }
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
    public void finalize() { //Opcional
        System.out.println("\nFinalizando burocrata");
//        comunicar("Izar", "", ACLMessage.CANCEL, clave);
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
