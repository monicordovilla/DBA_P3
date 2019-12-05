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
    
    //id de los drones
    String dronFly;
    String dronAux;
    String dronRescue;
    String dronRescue2;
    
    ArrayList<Integer> objetivosRecogidos = new ArrayList<>(); //Objetivos recogidos por los drones Rescue
    double fuelRestante;
    
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
            for(int j = 0; j < max_x; j++) mapa[i][j] = image.getRGB( j, i ) & 0xff;
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
    private String JSONEncode_InicialDron(){
        JsonObject a = new JsonObject();
        
        a.add("result", "OK");
        a.add("session", session);
        a.add("dimx", max_x);
        a.add("dimy", max_y);
        
        //Codificando el mapa
        JsonArray map = new JsonArray();
        map = JSON_Mapa();
        a.add("map",map);
        
        a.add("x", 0); //PRAC3 -- CAMBIAR DESPUES
        a.add("y", 0); //PRAC3 -- CAMBIAR DESPUES
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
      
       comunicarDron(dronRescue, mensaje.asString(), ACLMessage.INFORM, clave);
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
        comunicarDron(dronRescue, mensaje.asString(), ACLMessage.INFORM, clave);
    }
    
    /**
    *   MODIFICADO CAMBIAR EN DIAGRAMA ANA
    * @author Mónica
    */
    protected void avisarObjetivosCompletados(int x, int y){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivos-encontrados", true);
        
        //avisa al dron de rescate
        comunicarDron(dronRescue, mensaje.asString(), ACLMessage.INFORM, clave);
    }
    
        //METODOS DE CONTROL
      
    /**
    * 
    * @author Mónica
    */
    protected void responderPeticionRepostaje(String dron){
        if (puedeRepostar(dron)){
            comunicarDron(dron, "ACEPTADO", ACLMessage.CONFIRM, clave);
        }
        else{
            comunicarDron(dron, "DENEGADO", ACLMessage.DISCONFIRM, clave);
        }
    }
    
    /**
    * @author Celia
    */ 
    
    boolean puedeRepostar(AgenteDron dron){
      
            if(dron==dronFly)
                return puedenVolver(true,true,true,false);
            if(dron == dronAux)
                return puedenVolver(true,true,false,true);
            if(puedenVolver(true,true,false,false))        //Soy rescate y pueden volver los dos drones de rescate
		return true;
            if(dron == dronRescue)
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
        AgenteDron dron =null;
        
        for(int i=0; i<4; i++){
            switch (i){
                case 0: if(d1) dron = dronRescue; break;
                case 1: if(d2) dron = dronRescue2; break;
                case 2: if(d3) dron = dronFly; break;
                case 3: if(d4) dron = dronAux; break;
            }
            
            if(dron!=null){
                 pasos = Math.max(Math.abs(dronRescue.gps.x-dronRescue.ini_x) , Math.abs(dronRescue.gps.y-dronRescue.ini_y)) +
                         dronRescue.gps.z/5 + 10; //Margen de 10 pasos
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
        mensaje = escuchar(true);
        JSONDecode_Inicial(mensaje);
        try {
            guardarMapa(mapa_recibido);
        } catch (Exception ex) {
            System.out.println("Excepcion: Error al obtener el mapa");
        }
        
         //Llamada a los drones
        String m = JSONEncode_InicialDron();
        comunicarDron(dronFly, m, ACLMessage.INFORM, null);
        comunicarDron(dronAux, m, ACLMessage.INFORM, null);
        comunicarDron(dronRescue, m, ACLMessage.INFORM, null);
        comunicarDron(dronRescue2, m, ACLMessage.INFORM, null);
        
        
        //BORRAR LUEGO - PRUEBA
        for(int i = 0; i < max_y; i++){
            for(int j = 0; j < max_x; j++) System.out.print(String.format("%03d",mapa[i][j]) + ' ');
            System.out.print('\n');
        }
        
        comunicar("Izar", "", ACLMessage.CANCEL, clave);
        escuchar(true);
        escuchar(true);
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
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
