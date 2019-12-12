/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import DBA.SuperAgent;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.logging.Logger;

/**
 *
 * @author Kieran, Monica, Ana
 */
public abstract class AgenteSimple extends SuperAgent{

    ACLMessage ultimo_mensaje_recibido;    
    String reply_key;
    protected MessageQueue queue;
    protected int size = 1000;

    public AgenteSimple(AgentID aid) throws Exception {
        super(aid);
        queue = new MessageQueue(size);
    }

//METODOS DE JSON: Codifican y descodifican los mensajes en formato JSON para facilitar el manejo de los datos recibidos


    /**
    *
    * @author Kieran
    */
    protected boolean validarRespuesta(JsonObject respuesta){
        boolean valido = respuesta.get("result").asString().equals("ok");
        if(!valido){
            System.out.println("Error in response to '" + respuesta.get("in-reply-to").asString() + "': " + respuesta.get("result").asString());
        }
        return valido;
    }

//METODOS DE COMUNICACIÓN: Mandan mensajes al agente en el lado del servidor

    /**
    *
    * @author Kieran
    */
    protected void comunicar(String nombre, String mensaje, int performativa, String conv_id, String reply_to) {
        ACLMessage outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setPerformative(performativa);
        outbox.setReceiver(new AgentID(nombre));
        outbox.setContent(mensaje);
        if(null != (conv_id) && !conv_id.isEmpty()) outbox.setConversationId(conv_id);
        if(null != (reply_to) && !reply_to.isEmpty()) outbox.setInReplyTo(reply_to);
        System.out.println(outbox.toString());
        this.send(outbox);
    }
    protected void comunicar(String nombre, String mensaje, int performativa, String conv_id) {
        comunicar(nombre, mensaje, performativa, conv_id, null);
    }


    /**
    *
    * @author Kieran
    */
    protected JsonObject escuchar(){
        return escuchar(true);
    }

    /**
    *
    * @author Kieran
    */
    protected JsonObject escuchar(boolean echo) {
        ACLMessage inbox = escucharPerformativa();

        ultimo_mensaje_recibido = inbox;
        String mensaje = inbox.getContent();
        if(echo) System.out.println("Mensaje recibido:\n" + mensaje + "\nRaw:\n" + inbox.toString());
        JsonObject m = Json.parse(mensaje).asObject();
        
        boolean valido = m.get("result").asString().equals("ok");
        if(!valido){
            System.out.println("Error in response to '" + inbox.getSender() + "': " + m.get("details").asString());
            return null;
        }
        
        return m;
    }
    
    /**
    *
    * @author Kieran, Monica
    */
    protected ACLMessage escucharPerformativa() {
        ACLMessage inbox;
        try{
            inbox = this.receiveACLMessage();
        }
        catch(Exception e){
            System.out.println("Error de comunicación: Excepción al escuchar");
            return null;
        }
        
        return inbox;
        
    }
    
    // Hebra de recepción
    /**
    * Cada vez que llega un mensaje se llama
    * Codigo de la clase Consumer proporcionado por el profesor para el semnario 5
    */
    public void onMessage(ACLMessage msg)  {
        try {
            queue.Push(msg); // Cada mensaje nuevo que llega se encola en el orden de llegada
            System.out.println("\n["+this.getName()+"] Encolando: "+msg.getContent());
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
}
