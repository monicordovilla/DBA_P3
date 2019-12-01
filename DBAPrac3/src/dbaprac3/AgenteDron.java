/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import DBA.SuperAgent;
import java.util.Scanner;
import static dbaprac3.Accion.*;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonValue;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author Kieran, Monica, Ana
 */
public abstract class AgenteDron extends AgenteSimple{

    protected class GPS {
        public int x;
        public int y;
        public int z;

        GPS() {
            x = -1; y = -1; z = -1;
        }
    }
    protected class Gonio {
        public float angulo;
        public float distancia;
    }
    
    GPS gps;
    int[][] radar;
    int[][] magnetic;
    Gonio gonio;
    float fuel;
    int torescue;
    
    //PRAC3 -- OBLIGATORIO SOBREESCRIBIR ESTOS METODOS EN SUBCLASES
    String rol;
    static int tamanio_radar;
    static int centro_radar;
    double consumo_fuel; //Consumo de fuel por movimiento
    
    String status;
    Accion command; //Siguiente accion que tiene que hacer el agente
    Accion accion_anterior; //Acción anterior
    boolean[][] memoria;
    String clave;   //Clave que hay que enviar con cada comando que se envía
    int[][] mapa;    
    
    //dimensiones del mundo en el que se ha logueado, se asigna valor en el JSONDecode_Inicial
    int max_x;
    int max_y;
    //ahora mismo no se usa, por si queremos evitar bordes
    int min_x;
    int min_y;
    //altura mínima y máxima a las que el drone puede volar, se asigna valor en el JSONDecode_Inicial
    int min_z;
    int max_z;
    //PRAC3 -- posiciones iniciales para mandar en JSONEncode_Inicial
    int ini_x;
    int ini_y;
    
    int pasos = 0;
    int pasos_repetidos = 0;
    int max_pasos = 10000;
    int max_pasos_repetidos = 10;

    int unidades_updown; //Unidades que consume las bajadas y subidas
    boolean repostando; //Actualmente esta bajando para repostar
    Stack<Accion> mano_dcha; //Pila con las direcciones a las que desea moverse
    

    public AgenteDron(AgentID aid) throws Exception {
        super(aid);
        mano_dcha = new Stack<>();
        repostando = false;
    }

//METODOS DE EVALUACIÓN: La funcionalidad inteligente del agente, para decidir que hacer. PRAC3 -- **NO** LOS TOCAN LAS SUBCLASES, DRONRESCATE **NO** LAS PUEDE USAR
    
    /**
    * 
    * @author Kieran
    * Devuelve las coordenadas de radar de la casilla de al lado
    */
    protected Pair<Integer,Integer> movimientoEnRadar(Accion sigAccion, int offset){
        int x = 0, y = 0;
        switch(sigAccion) {
                case moveNW: x--;   y--; break; //Comprobación del movimiento NW
                case moveN:  x--;        break;//Comprobación del movimiento N
                case moveNE: x--;   y++; break; //Comprobación del movimiento NE
                case moveW:  y--;        break; //Comprobación del movimiento W
                case moveE:  y++;        break; //Comprobación del movimiento E
                case moveSW: x++;   y--; break; //Comprobación del movimiento SW
                case moveS:  x++;        break;//Comprobación del movimiento S
                case moveSE: x++;   y++; break;//Comprobación del movimiento SE
              }
        return new Pair<>(x,y);
    }
    
    /**
    *
    * @author Kieran
    * Comprueba si se puede mover a la casilla a la que nos llevaria sigAccion
    */
    private boolean puedeMover(Accion sigAccion) {
        int x,y,z=0;
        
        Pair<Integer,Integer> coords = movimientoEnRadar(sigAccion, centro_radar);
        x = coords.getKey();
        y = coords.getValue();
        
        switch(sigAccion){
                case moveDW: z = -5;     break;
                case moveUP: z = 5;      break;
        }
        
        return(gps.z+z >= radar[x][y] && radar[x][y] >= min_z && gps.z+z <= max_z);
    }
    
    
    /**
    *
    * @author Monica, Kieran
    * Comprueba si se puede subir por encima de la casilla a la que nos llevaría sigAccion
    */
    private boolean puedeSubir(Accion sigAccion){
        boolean sube = true;
        int x,y,z=0;
        
        Pair<Integer,Integer> coords = movimientoEnRadar(sigAccion, centro_radar);
        x = coords.getKey();
        y = coords.getValue();
            
        if(radar[x][y] > max_z || radar[x][y] < min_z){
            sube = false;
        }
        
        return sube;
    }
    
    /**
    *
    * @author Celia, Monica, Kieran
    * siguienteAccion() renombrado
    * Copiado-pegado de rodearObstaculoAccion, ya que este simplemente selecciona la mejor opcion sin contar los invalidos.
    * Se ha de tener en cuenta de que rodearObstaculoAccion solo se lanza cuando supere la altura maxima asi que se han tenido que ajustar un par de cosas
    * Si se pasa false como parametro, solo miria la direccion y no comprueba la validez
    */
    private Accion siguienteDireccion(){ return siguienteDireccion(true); }
    
    private Accion siguienteDireccion(boolean comprobar_validez){
        final int dirs = 8;
        final int MAX = 999;
        final float grados_entre_dir = 45;
        
        boolean validos[] = {true,true,true,true,true,true,true,true};
        //System.out.println(accion_anterior.value);
        if(comprobar_validez) {
            for(int i = 0; i < dirs; i++) { //Eliminamos direcciones imposibles de la lista. Estos incluyen aquellos que ya hemos visitado, y los que no podemos ir a, ni subir para llegar a
                if((!puedeMover(Accion.valueOfAccion(i)) && !puedeSubir(Accion.valueOfAccion(i))) /*|| estaEnMemoria(Accion.valueOfAccion(i))*/) validos[i] = false;
                //if(accion_anterior.value < 8 && (accion_anterior.value+4)%8 == i) validos[i] = false;
            }
            //System.out.println(Arrays.toString(validos));
        }
        float diff_menor = MAX;
        int indice_menor = MAX;
        for(int i = 0; i < 8; i++) {
            if(!validos[i]) continue;
            float dist_real = Math.abs(gonio.angulo-(i*grados_entre_dir))%360;
            dist_real = dist_real > 180 ? 360-dist_real : dist_real;
            if(dist_real < diff_menor){
                indice_menor = i;
                diff_menor = dist_real;
                
                //System.out.println("angulo: " + gonio.angulo + "accion escogido: " + Accion.valueOfAccion(i));
                
            }
        }
        if(indice_menor == MAX) return logout;
        
        return Accion.valueOfAccion(indice_menor);
    }

    /**
    *
    * @author Ana, Celia
    * Se comprueba si ya hemos pasado por la posición a la que nos lleva la siguiente acción, devuelve TRUE si no se ha visitado ya
    */
    private boolean estaEnMemoria(Accion accion)
    {
        int x,y,z=0;
        
        Pair<Integer,Integer> coords = movimientoEnRadar(accion, 0);
        y = coords.getKey(); //Al reves intencionalmente, coordenadas en mapa != en matriz
        x = coords.getValue();

      
      if(x < 0 || y < 0 || x > max_x || y > max_y) return true; //Para no salirse de la matriz

      return (memoria[x][y] == true);
    }
         
    /**
    *
    * @author Ana, Kieran
    * Calcula cuantos movimientos de bajada vamos a necesitar para llegar al suelo
    */
    private int unidadesBajada(int x, int y){
      int movs;
      movs = gps.z - radar[x][y]; //Cada bajada conlleva 5 unidades. Calculamos en funcion de la altura cuantos movimientos necesitamos para llegar al suelo
      //if(repostando) System.out.println(movs/5);
      return movs;
    }
    
    /**
    *
    * @author Kieran, Ana
    */
    private boolean necesitaRepostar(Accion accion){
        int x=0,y=0;
        
        Pair<Integer,Integer> coords = movimientoEnRadar(accion, centro_radar);
        x = coords.getKey();
        y = coords.getValue();
        double fuel_necesario = unidadesBajada(x, y)/(1.0*unidades_updown) * consumo_fuel + 2*consumo_fuel;

        return fuel <= fuel_necesario;
    }

    /**
    *
    * @author Celia, Kieran
    */
    
    private Accion reglaManoDerecha(){
        
        int enCola = mano_dcha.peek().value; //Obtener valor de la primera accion en cola
        Accion siguiente;
        boolean pasado=false;
        for(int i=0; i<8; i++){
            siguiente = valueOfAccion((8+enCola-i)%8); //+8 para evitar modulos negativos
            if(siguiente.value==accion_anterior.value) 
                pasado=true;
                
            if(puedeMover(siguiente)){
                if(siguiente.value == enCola)
                    mano_dcha.pop();
                else if(siguiente.value!=accion_anterior.value && pasado)
                    mano_dcha.push(accion_anterior);
                return siguiente;
            }
            else if(puedeSubir(siguiente))
                return moveUP;
        }
                
        return moveDW; //placeholder - borrar ahora
    }


//METODOS DE NAVEGACIÓN: Las funciones que determinan el comportamiento a un nivel mas abstracto del dron. PRAC3 -- LAS QUE SE HAN DE SOBREESCRIBIR EN SUBCLASES

    /**
    *
    * @author Ana, Kieran, Monica
    * Se comprueba si se puede realizar la acción más prometedora
    */
    private Accion comprobarAccion(){
      Accion accion = null;
      
      if(repostando) return checkRepostaje(accion);
      
      accion = checkMeta();
      if(accion != null) return accion;

      accion = checkNavegacionNormal(accion);
      accion = checkRepostaje(accion);
      accion = checkManoDerecha(accion);
      return accion;
      
    }
    
    /**
    * 
    * @author Kieran
    * Limpieza de las diversas funcionalidades de comprobarAccion
    */
    protected Accion checkMeta(){
        if(magnetic[centro_radar][centro_radar] == 1 || (gps.x == ini_x && gps.y == ini_y && torescue == 0)) {
            //return algo, los drones de rescate bajarán en la meta mientras que los otros solo lo harán para volver a casa;
        }
        return null;
    }
    protected Accion checkRepostaje(Accion accion){
        if(necesitaRepostar(accion) /*&& puedeRepostar()*/) { //PRAC3 -- DESCOMENTAR
            repostando = true;
          if(gps.z == radar[centro_radar][centro_radar]){
            repostando = false;
            return refuel;
          }
          return moveDW;
        }
        return accion;
    }
    protected Accion checkNavegacionNormal(Accion accion){
        int x,y=0;

        Pair<Integer,Integer> coords = movimientoEnRadar(accion, centro_radar);
        x = coords.getKey();
        y = coords.getValue();

        if(radar[x][y]==0) //Si la hemos liado, salir
            return logout;
        else if(radar[x][y] <= gps.z) //Estamos a la altura de la celda a la que queremos ir o superor
            return accion;
        else if(radar[x][y] > gps.z && (gps.z+5 <= max_z) && puedeSubir(accion)) //La celda a la que queremos ir esta a una altura superior y podemos llegar a ella
            return moveUP;
        return stop;
    }
    protected Accion checkManoDerecha(Accion accion){
      if(!mano_dcha.empty()) { 
          accion = reglaManoDerecha(); 
        /*System.out.println(mano_dcha.toString());*/
      } //REGLA DE MANO DERECHA
      if(accion_anterior != null && accion_anterior.value < 8 && (accion_anterior.value+4)%8 == accion.value) { //Si estamos atrapado en un bucle, ACTIVAMOS MANO DERECHA
          //System.out.println("mano dcha");
          mano_dcha.push(siguienteDireccion(false));
          accion = reglaManoDerecha();
      }
      return accion;
    }

//METODOS DE COMUNICACIÓN: Mandan mensajes al agente en el lado del servidor.
//  +
//METODOS DE JSON: Codifican y descodifican los mensajes en formato JSON para facilitar el manejo de los datos recibidos
//PRAC3 -- NO LAS TOCAN LAS SUBLCASES, FALTAN COSAS AQUI

    /**
    *
    * @author Kieran
    */
    protected String JSONCommand(String content){
        JsonObject a = new JsonObject();
        a.add("command", content);
        return a.toString();
    }


    /**
    *
    * @author Monica
    * Decodifica el primer mensaje del burocrata
    * INFORM{"result":"OK", "session":"<master>", "dimx":"<w>", "dimy":"<h>", "map":[]}:CONVERSATION-ID@
    */
    private void JSONDecode_Inicial(JsonObject mensaje){
        clave = mensaje.get("session").asString();
        max_x = mensaje.get("dimx").asInt();
        max_y = mensaje.get("dimy").asInt();
        
        JsonArray mapa_recibido = mensaje.get("map").asArray();
        for(int i=0; i<radar.length; i++){
            for(int j=0; j<radar.length; j++){
                mapa[i][j] = mapa_recibido.get(j+i*radar.length).asInt();
            }
        }
        
        clave = ultimo_mensaje_recibido.getConversationId();
    }
    
    /**
    *
    * @author Monica, Kieran
    */
    protected String JSONEncode_Inicial(){
        JsonObject a = new JsonObject();
        a.add("command", "checkin");
        a.add("session", ""); //COMPROBAR DESPUES: NO TENGO NI IDEA DE QUE VA AQUI
        a.add("rol", rol);
        a.add("x", ini_x);
        a.add("y", ini_y);
        return a.toString();
    }
    
    /**
    *
    * @author Monica
    * Decodifica el mapa actualizado por parte del burocrata
    */
    private void JSONDecode_ActualizarMapa(JsonObject mensaje){        
        JsonArray mapa_recibido = mensaje.get("map").asArray();
        for(int i=0; i<radar.length; i++){
            for(int j=0; j<radar.length; j++){
                mapa[i][j] = mapa_recibido.get(j+i*radar.length).asInt();
            }
        }
    }

    /**
    *
    * @author Kieran
    */
    protected void checkin(){
        String mensaje = JSONCommand("checkin");
        comunicar("Izar", mensaje, ACLMessage.REQUEST, clave);
    }
    protected void move(Accion accion){
        String mensaje = JSONCommand(accion.toString());
        comunicar("Izar", mensaje, ACLMessage.REQUEST, clave);
    }
    protected void refuel(){
        String mensaje = JSONCommand("refuel");
        comunicar("Izar", mensaje, ACLMessage.REQUEST, clave);
    }
    protected void stop(){
        String mensaje = JSONCommand("stop");
        comunicar("Izar", mensaje, ACLMessage.REQUEST, clave);
    }
    protected void perception(){
        comunicar("Izar", "", ACLMessage.QUERY_REF, clave);
    }
    //protected boolean puedeRepostar(){} //PRAC3 -- IMPLEMENTAR COMUNICACION
    
    protected boolean validarRespuesta(JsonObject a){ //PRAC3 -- VER COMO SE HACE/BORRAR LUEGO
        return true; //PRAC3 -- CAMBIAR
    }
    
    @Override
    protected boolean validarRespuesta(ACLMessage a){
        return true; //PRAC3 -- CAMBIAR
    }


//METODOS DE SUPERAGENT: Métodos sobreescritos y heredados de la clase SuperAgent

    @Override
    public void init() { //Opcional
        System.out.println("\nInicializado");
    }

    /**
    *
    * @author Kieran, Ana, Celia, Monica
    */
    @Override
    public void execute() {
        //codificar el mensaje inicial JSON aqui
        checkin();

        JsonObject respuesta = escuchar();

        while(validarRespuesta(respuesta))
        {
            comprobarAccion();
            escuchar();
        }
        if(!validarRespuesta(respuesta)) { //si se sale por un resultado invalido devuelve las percepciones antes de la traza
            escuchar();
        }
        stop();
    }

    @Override
    public void finalize() { //Opcional
        System.out.println("\nFinalizando");
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
