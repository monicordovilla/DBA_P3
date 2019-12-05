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
    int[][] infrared;
    Gonio gonio;
    float fuel;
    int torescue;
    boolean goal;
    JsonValue awacs;

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
    String burocrata;
    int[][] mapa;

    //dimensiones del mundo en el que se ha logueado, se asigna valor en el JSONDecode_Inicial
    int max_x;
    int max_y;
    //por si queremos evitar bordes
    int min_x = 0;
    int min_y = 0;
    //altura mínima y máxima a las que el drone puede volar, se asigna valor en el JSONDecode_Inicial
    int min_z = 0;
    int max_z;
    //PRAC3 -- posiciones iniciales para mandar en JSONEncode_Inicial
    int ini_x;
    int ini_y;

    int pasos = 0;
    int pasos_repetidos = 0;
    int max_pasos = 10000;
    int max_pasos_repetidos = 10;

    int unidades_updown = 5; //Unidades que consume las bajadas y subidas
    boolean repostando; //Actualmente esta bajando para repostar
    Stack<Accion> mano_dcha; //Pila con las direcciones a las que desea moverse

    String session;
    Estado estado;
    String id;

    public AgenteDron(AgentID aid) throws Exception {
        super(aid);
        gps = new GPS();
        gonio = new Gonio();
        mano_dcha = new Stack<>();
        repostando = false;
        estado = Estado.REPOSO;


    }

//METODOS DE EVALUACIÓN: La funcionalidad inteligente del agente, para decidir que hacer. PRAC3 -- **NO** LOS TOCAN LAS SUBCLASES, DRONRESCATE **NO** LAS PUEDE USAR

    protected boolean enLimites(int x, int y){
        return (x >= min_x && x < max_x && y >= min_y && y < max_y);
    }

    /**
    *
    * @author Kieran
    * Devuelve las coordenadas de radar de la casilla de al lado
    */
    protected Pair<Integer,Integer> movimientoEnRadar(Accion sigAccion, int x, int y){
        switch(sigAccion) {
                case moveNW: x--;   y--; break; //Comprobación del movimiento NW
                case moveN:  x--;        break;//Comprobación del movimiento N
                case moveNE: x--;   y++; break; //Comprobación del movimiento NE
                case moveW:         y--; break; //Comprobación del movimiento W
                case moveE:         y++; break; //Comprobación del movimiento E
                case moveSW: x++;   y--; break; //Comprobación del movimiento SW
                case moveS:  x++;        break;//Comprobación del movimiento S
                case moveSE: x++;   y++; break;//Comprobación del movimiento SE
              }
        return new Pair<>(x,y);
    }

    protected Pair<Integer,Integer> movimientoEnMapa(Accion sigAccion, int x, int y){
                switch(sigAccion) {
                case moveNW: y--;   x--; break; //Comprobación del movimiento NW
                case moveN:  y--;        break;//Comprobación del movimiento N
                case moveNE: y--;   x++; break; //Comprobación del movimiento NE
                case moveW:         x--; break; //Comprobación del movimiento W
                case moveE:         x++; break; //Comprobación del movimiento E
                case moveSW: y++;   x--; break; //Comprobación del movimiento SW
                case moveS:  y++;        break;//Comprobación del movimiento S
                case moveSE: y++;   x++; break;//Comprobación del movimiento SE
              }
        return new Pair<>(x,y);
    }

    /**
    *
    * @author Kieran
    * Devuelve las coordenadas de radar de la casilla de al lado
    */

    /**
    *
    * @author Kieran
    * Comprueba si se puede mover a la casilla a la que nos llevaria sigAccion
    */
    private boolean puedeMover(Accion sigAccion) {
        int x,y,z=0;

        Pair<Integer,Integer> coords = movimientoEnMapa(sigAccion, gps.x, gps.y);
        x = coords.getKey();
        y = coords.getValue();
        z = gps.z;

        switch(sigAccion){
                case moveDW: z = -5;     break;
                case moveUP: z = 5;      break;
        }

        return(enLimites(x,y) && gps.z+z >= mapa[x][y] && mapa[x][y] >= min_z && gps.z+z <= max_z);
    }


    /**
    *
    * @author Monica, Kieran
    * Comprueba si se puede subir por encima de la casilla a la que nos llevaría sigAccion
    */
    protected boolean puedeSubir(Accion sigAccion){
        boolean sube = true;
        int x,y;

        Pair<Integer,Integer> coords = movimientoEnMapa(sigAccion, gps.x, gps.y);
        x = coords.getKey();
        y = coords.getValue();

        if(!enLimites(x,y) || mapa[x][y] > max_z || mapa[x][y] < min_z){
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

        Pair<Integer,Integer> coords = movimientoEnMapa(accion, gps.x, gps.y);
        x = coords.getKey();
        y = coords.getValue();


      if(!enLimites(x,y)) return true; //Para no salirse de la matriz

      return (memoria[x][y] == true);
    }

    /**
    *
    * @author Ana, Kieran
    * Calcula cuantos movimientos de bajada vamos a necesitar para llegar al suelo
    */
    private int unidadesBajada(int x, int y){
      int movs;
      movs = gps.z - mapa[x][y]; //Cada bajada conlleva 5 unidades. Calculamos en funcion de la altura cuantos movimientos necesitamos para llegar al suelo
      //if(repostando) System.out.println(movs/5);
      return movs;
    }

    /**
    *
    * @author Kieran, Ana
    */
    private boolean necesitaRepostar(Accion accion){
        int x,y;

        Pair<Integer,Integer> coords = movimientoEnMapa(accion, gps.x, gps.y);
        x = coords.getKey();
        y = coords.getValue();

        if(!enLimites(x,y)) return false; //ERROR

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
    protected Accion comprobarAccion(){
      Accion accion = null;

      Accion tmp = checkRepostaje(accion);
      if(tmp != null) return tmp;

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
        if( gps.x == ini_x && gps.y == ini_y && torescue == 0 ) {
            //return algo, los drones de rescate bajarán en la meta mientras que los otros solo lo harán para volver a casa;
        }
        return null;
    }
    protected Accion checkRepostaje(Accion accion){
        if(repostando || (necesitaRepostar(accion) /*&& puedeRepostar()*/)) { //PRAC3 -- DESCOMENTAR
            repostando = true;
            if(gps.z == mapa[gps.x][gps.y]){
                repostando = false;
                return refuel;
            }
            return moveDW;
        }
        return accion;
    }
    protected Accion checkNavegacionNormal(Accion accion){
        int x,y=0;

        Pair<Integer,Integer> coords = movimientoEnMapa(accion, gps.x, gps.y);
        x = coords.getKey();
        y = coords.getValue();

        if(enLimites(x,y)) System.out.println("mapa[x][y] = " + mapa[x][y] + " x="+x + " y="+y);
        else System.out.println("mapa[x][y] = OUT OF BOUNDS");

        if(!enLimites(x,y))
            return accion; //ERROR

        if(mapa[x][y] <= gps.z) //Estamos a la altura de la celda a la que queremos ir o superor
            return accion;
        else if(mapa[x][y] > gps.z && puedeSubir(accion)) //La celda a la que queremos ir esta a una altura superior y podemos llegar a ella
            return moveUP;
        return accion; //ERROR
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
    * @author Monica
    * Decodifica el primer mensaje del burocrata
    * INFORM{"result":"OK", "session":"<master>", "dimx":"<w>", "dimy":"<h>", "map":[]}:CONVERSATION-ID@
    */
    private void JSONDecode_Inicial(JsonObject mensaje){
        System.out.println("a");
        session = mensaje.get("session").asString();
        max_x = mensaje.get("dimx").asInt();
        max_y = mensaje.get("dimy").asInt();
        System.out.println("aa");

        JsonArray mapa_recibido = mensaje.get("map").asArray();
        mapa = new int[max_x][max_y];
        for(int i=0; i<max_x; i++){
            for(int j=0; j<max_y; j++){
                mapa[i][j] = mapa_recibido.get(j+i*max_y).asInt();
            }
        }
        System.out.println("aaa");
        ini_x = mensaje.get("x").asInt();
        ini_y = mensaje.get("y").asInt();

        clave = ultimo_mensaje_recibido.getConversationId();
    }


    /**
    *
    * @author Monica
    * Decodifica el primer mensaje del controller
    * INFORM{"result":{"gps":"{x,y,z}", "infrared":"{0,0,...}",
    * "gonio":"{"distance": -1, "angle": -1}", "fuel":100, "goal": false,
    * "status": operative, "awacs":[{"name":<agent1>, "x":10, "y":99, "z":100,
    * "direction": accion}, ...] }}:CONVERSATION-ID@
    */
    protected void JSONDecode_variables(JsonObject mensaje){
        //Extraer los valores asociados al GPS
        mensaje = mensaje.get("result").asObject();
        System.out.println(mensaje);

        gps.x = mensaje.get("gps").asObject().get("x").asInt();
        gps.y = mensaje.get("gps").asObject().get("y").asInt();
        gps.z = mensaje.get("gps").asObject().get("z").asInt();

        //Exraer los valores asociados al infrared
        JsonArray vector_inf = mensaje.get("infrared").asArray();
        for(int i=0; i<tamanio_radar; i++){
            for(int j=0; j<tamanio_radar; j++){
                infrared[i][j] = vector_inf.get(j+i*tamanio_radar).asInt();
            }
        }
        //Extraer los valores asociados al gonio
        //gonio.angulo = mensaje.get("gonio").asObject().get("angle").asFloat();
        //gonio.distancia = mensaje.get("gonio").asObject().get("distance").asFloat();

        //Extraer el valor del combustible
        fuel = mensaje.get("fuel").asFloat();

        //Extraer informacion sobre si nos hallamos en una meta
        //goal = mensaje.get("goal").asBoolean();

        //Extraer información sobre el estado del dron
        status = mensaje.get("status").asString();

        //Extraer valores asociado a awacs
        //awacs = mensaje.get("awacs");
    }


    /**
    *
    * @author Monica
    * Decodifica el primer mensaje del controller
    *
    * INFORM{"result":{"gps":"{x,y,z}", "infrared":"{0,0,...}",
    * "gonio":"{"distance": -1, "angle": -1}", "fuel":100, "goal": false,
    * "status": operative, "awacs":[{"name":<agent1>, "x":10, "y":99, "z":100,
    * "direction": accion}, ...] }}:CONVERSATION-ID@
    */
    private String JSONEncode_variables(JsonObject mensaje){
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
    }

    /**
    *
    * @author Monica
    * Decodifica el mapa actualizado por parte del burocrata
    */
    protected void JSONDecode_ActualizarMapa(JsonObject mensaje){
        JsonArray mapa_recibido = mensaje.get("map").asArray();
        for(int i=0; i<max_x; i++){
            for(int j=0; j<max_y; j++){
                mapa[i][j] = mapa_recibido.get(j+i*max_y).asInt();
            }
        }
    }

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
    * @author Kieran, Mónica
    */
    protected void checkin(){
        String mensaje;

        JsonObject a = new JsonObject();
        a.add("command", "checkin");
        a.add("session", session);
        a.add("rol", rol);
        a.add("x", ini_x);
        a.add("y", ini_y);
        mensaje = a.toString();

        comunicar("Izar", mensaje, ACLMessage.REQUEST, clave);
    }
    protected void move(Accion accion){
        String mensaje = JSONCommand(accion.toString());
        comunicar("Izar", mensaje, ACLMessage.REQUEST, clave, reply_key);
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
        comunicar("Izar", "", ACLMessage.QUERY_REF, clave, reply_key);
    }

    /**
    * NUEVO CAMBIAR EN DIAGRAMA ANA
    * @author Mónica
    */
    protected void avisarObjetivoEnontrado(){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivo-encontrado", true);
        comunicar("Izar", mensaje.asString(), ACLMessage.INFORM, clave);
    }

    /**
    *   NUEVO CAMBIAR EN DIAGRAMA ANA
    * @author Mónica
    */
    protected void avisarObjetivoIdentificado(int x, int y){
        JsonObject mensaje = new JsonObject();
        mensaje.add("objetivo-identificado", true);
        mensaje.add("x", x);
        mensaje.add("y", x);

        //avisa al dron de rescate
        comunicar( burocrata, mensaje.asString(), ACLMessage.INFORM, clave);
    }
    protected void puedeRepostar(){
        comunicar(id, "repostar", ACLMessage.QUERY_IF, clave);
    }

    //No se cual de las 2 implementar y como hacerlo
    /**
    *
    * @author Kieran
    */
    protected boolean validarRespuesta(JsonObject a){
        return true; //PRAC3 -- CAMBIAR
    }

    @Override
    protected boolean validarRespuesta(ACLMessage a){//PRAC3 -- VER COMO SE HACE/BORRAR LUEGO
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

        System.out.println("DRON: Inicializando");

        JsonObject respuesta = escuchar();

        System.out.println("DRON: Msg Escuchado");

        JSONDecode_Inicial(respuesta);

        System.out.println("DRON: Haciendo checkin");

        checkin();
        escuchar();
        clave = ultimo_mensaje_recibido.getConversationId();
        reply_key = ultimo_mensaje_recibido.getReplyWith();

        System.out.println("DRON: Bucle principal");

        while(validarRespuesta(respuesta) && status != "crashed")
        {

            System.out.println("b");
            perception();
            JsonObject msg = escuchar();
            reply_key = ultimo_mensaje_recibido.getReplyWith();

            System.out.println("bb");
            JSONDecode_variables(msg);
            Accion accion = comprobarAccion();
            System.out.println("bbb");
            move(accion);
            escuchar();
            reply_key = ultimo_mensaje_recibido.getReplyWith();
        }
        if(!validarRespuesta(respuesta)) { //si se sale por un resultado invalido devuelve las percepciones antes de la traza
            escuchar();
        }
    }

    @Override
    public void finalize() { //Opcional
        comunicar("Izar", "", ACLMessage.CANCEL, clave);
        System.out.println("\nFinalizando");
        super.finalize(); //Pero si se incluye, esto es obligatorio
    }
}
