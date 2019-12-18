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
import javafx.util.Pair;

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

    int tamCola = 100;
    MessageQueue datos = new MessageQueue(tamCola);
    MessageQueue repostar = new MessageQueue(tamCola);
    MessageQueue objetivos = new MessageQueue(tamCola);

    public AgenteBurocrata(AgentID aid)throws Exception{
        super(aid);
        this.drones = new ArrayList<>();

        System.out.println("BUR: Inicializando");
        this.drones.add(new DronData("GI_FlyK3-001", Rol.Fly));
        this.drones.add(new DronData("GI_SparrowK3-002", Rol.Sparrow));
        this.drones.add(new DronData("GI_SparrowK3-003", Rol.Sparrow));
        this.drones.add(new DronData("GI_RescueK3-004", Rol.Rescue));

        new AgenteFly(new AgentID(drones.get(0).nombre)).start();
        new AgenteSparrow(new AgentID(drones.get(1).nombre)).start();
        new AgenteSparrow(new AgentID(drones.get(2).nombre)).start();
        new AgenteRescate(new AgentID(drones.get(3).nombre)).start();

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
    private String JSONEncode_InicialDron(int x, int y, int fin_x){
        JsonObject a = new JsonObject();

        a.add("result", "OK");
        a.add("session", session);
        a.add("dimx", max_x);
        a.add("dimy", max_y);

        //Codificando el mapa
        JsonArray map = new JsonArray();
        map = JSON_Mapa();
        a.add("map",map);

        if(fin_x < 0 || fin_x >= max_x) { fin_x = max_x-1; }
        
        a.add("x", x);
        a.add("y", y);
        a.add("fin_x", fin_x);
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
    * al final no se usa
    */
    protected void avisarObjetivoEncontrado(){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivo-encontrado", true);

    }

    /**
    *
    * @author Mónica
    *
    * MODIFICAR: DEVOLVER X E Y
    */
    protected Pair<Integer,Integer> recibirObjetivoEncontrado(ACLMessage inbox){
        String coordenadasJSON = inbox.getContent();
        JsonObject c = Json.parse(coordenadasJSON).asObject();
        //MODIFICAR
        int x = c.get("coordenadas").asObject().get("x").asInt();
        int y = c.get("coordenadas").asObject().get("y").asInt();

        Pair<Integer,Integer> coords_obj = new Pair<>(x, y);
        return coords_obj;
    }

    /**
    *   MODIFICADO CAMBIAR EN DIAGRAMA ANA
    * @author Mónica
    */
    protected void avisarObjetivoIdentificado(int x, int y){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivo-identificado", true);

        JsonObject coordenadas =  new JsonObject();
        coordenadas.add("x", x);
        coordenadas.add("y", y);
        mensaje.add("coordenadas", coordenadas);

        System.out.println("Avisando a rescate");
        //avisa al dron de rescate
        comunicar(quienRescata(x, y), mensaje.toString(), ACLMessage.INFORM, null); //TODO escoger que dron de rescate es: borrar esto luego
        System.out.println("Rescate Avisado");
    }

    /**
    *
    * @author Mónica
    * al final no se usa
    */
    protected void avisarObjetivosCompletados(){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivos-encontrados", true);
    }
    
    /**
    *
    * @author Mónica
    */
    protected void pararDron(String nombreDron){
        JsonObject mensaje = new JsonObject();
        mensaje.add("para", true);
        String m = mensaje.toString();
        comunicar(nombreDron, m, ACLMessage.REQUEST, clave);
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
    * @author Celia
    */
    private void actualizarDatos(ACLMessage inbox){
        String id= inbox.getSender().toString();
        DronData  dron = getDronData(id);

        JsonObject mensaje = Json.parse(inbox.getContent()).asObject();

        dron.gps.x = mensaje.get("gps").asObject().get("x").asInt();
        dron.gps.y = mensaje.get("gps").asObject().get("y").asInt();
        dron.gps.z = mensaje.get("gps").asObject().get("z").asInt();

        //Extraer el valor del combustible
        dron.fuel = mensaje.get("fuel").asFloat();

        dron.consumo_fuel = mensaje.get("consumo_fuel").asFloat();
    }

    /**
    *
    * @author Celia
    */
    DronData getDronData(String id){
        for(DronData d : drones){
            if(d.nombre.equals(id))
                return d;
        }

        return null;
    }

        /**
    *
    * @author Celia
    */
    private void solicitarDatos(String id){
        comunicar(id, "datos", ACLMessage.QUERY_REF, "datos");

        ACLMessage inbox = null;
        try{
            inbox = this.receiveACLMessage();
        }
        catch(Exception e){
            System.out.println("Error de comunicación: Excepción al escuchar");
        }
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

        solicitarDatos(nombre);
        volver.add(dron);

        if(dron.rol==Rol.Rescue){
            for(DronData d : drones)
                if(d.rol==Rol.Rescue && d.recogidos>dron.recogidos){
                    solicitarDatos(d.nombre);
                    volver.add(d);
                }
        }
        else if(dron.rol==Rol.Fly){
            for(DronData d : drones)
                 if(d.rol==Rol.Rescue){
                    solicitarDatos(d.nombre);
                    volver.add(d);
                 }
        }
        else if(dron.rol==Rol.Sparrow){
             for(DronData d : drones)
                 if(d.rol==Rol.Rescue || d.rol==Rol.Fly){
                    solicitarDatos(d.nombre);
                    volver.add(d);
                 }
        }
        else
            for(DronData d : drones)
                 if(d.rol!=Rol.Hawk){
                    solicitarDatos(d.nombre);
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
                //solicitarDatos(d.nombre);
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

    void asignarInicio(int id){
        int x=0;
        int y=0;
        int fx = max_x-1;

        DronData dron = drones.get(id);
        
        if(dron.rol == Rol.Fly){
            x = max_x-1;
            y = max_y/2;
            fx = (id+1)*max_x/3;
        }        
        else if(dron.rol == Rol.Sparrow){
            x = 0; fx = max_x/2;
            if(id == 1) { x = max_x/2; fx = max_x-1; }
        }
        else{
            x = max_x/2;
            y = max_y/2;
        }

        dron.ini_x = x; dron.ini_y = y; dron.fin_x = fx;
    }
    
//METODOS PARA LA GESTION DE MENSAJES

    /**
    * @author Celia, Monica
    * Separa los mensajes en distintas colas segun su tipo
    */
    private void separarMensajes() throws InterruptedException{
        ACLMessage inbox= queue.Pop();

        String mensaje = inbox.getContent();
        JsonObject m = Json.parse(mensaje).asObject();

        if( inbox.getPerformative().equals(ACLMessage.QUERY_IF) ){
            repostar.Push(inbox);
        }
        else if( m.get("objetivo-identificado") != null ){
            objetivos.Push(inbox);
        }
        else if ( m.get("result") != null  ){
            datos.Push(inbox);
        }
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

        Pair<Integer,Integer> inicio;
        String m;

        int num_dron = 0;
        for(DronData dron : drones){
            asignarInicio(num_dron);
            m = JSONEncode_InicialDron(dron.ini_x, dron.ini_y, dron.fin_x);
            System.out.println("BUR: Codificando JSON");
            comunicar(dron.nombre, m, ACLMessage.INFORM, null);
            num_dron++;
        }

        while(num_dron != 0){

            while( queue.isEmpty() ) { // Iddle mientras no ha recibido nada. No bloqueante
                sleep(1000); // Espera 1 segundo hasta siguiente chequeo
            }
            // En cuanto la cola tiene al menos un mensaje, se separa entre las distintas colas que tenemos
            while(!queue.isEmpty()){
                try {
                    separarMensajes();
                } catch (InterruptedException ex) {
                    Logger.getLogger(AgenteBurocrata.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            while( !repostar.isEmpty() || !objetivos.isEmpty() || !datos.isEmpty() ){
                inbox = null;
                if( !datos.isEmpty() ){
                    try {
                        inbox = datos.Pop();
                        //Llamar al guardar drones actualizarDatos
                        actualizarDatos(inbox);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AgenteBurocrata.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if( !repostar.isEmpty() ){
                    try {
                        inbox = repostar.Pop();
                        responderPeticionRepostaje(inbox.getSender().toString());
                        System.out.println("Envia peticion el bicho " + inbox.getSender().toString());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AgenteBurocrata.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if( !objetivos.isEmpty() ){
                    try {
                        inbox = objetivos.Pop();
                        Pair<Integer,Integer> coordenadas_objetivo = recibirObjetivoEncontrado(inbox);
                        avisarObjetivoIdentificado(coordenadas_objetivo.getKey(), coordenadas_objetivo.getValue());

                    } catch (InterruptedException ex) {
                        Logger.getLogger(AgenteBurocrata.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }
        /*if(!finalizadoExitoso()) { //si se sale por un resultado invalido devuelve las percepciones antes de la traza
            //crear traza
        }*/

        //comunicarDron(dronAux, m, ACLMessage.INFORM, null);
        //comunicarDron(dronRescue2, m, ACLMessage.INFORM, null);

        //PRUBEA DE RESCATE
        /*for(int i = 0; i < 5; i++){
            JsonObject coords_objetivo = escuchar();
        System.out.println("AAAA3");
            avisarObjetivoIdentificado(coords_objetivo.get("x").asInt(), coords_objetivo.get("y").asInt());
        System.out.println("AAAA4");
        }*/
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
        comunicar("Izar", "", ACLMessage.CANCEL, clave);
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
