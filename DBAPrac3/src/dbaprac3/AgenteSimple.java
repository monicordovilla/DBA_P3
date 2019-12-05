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

/**
 *
 * @author Kieran, Monica, Ana
 */
public abstract class AgenteSimple extends SuperAgent{

    ACLMessage ultimo_mensaje_recibido;
    String reply_key;
    
    public AgenteSimple(AgentID aid) throws Exception {
        super(aid);
    }

//METODOS DE JSON: Codifican y descodifican los mensajes en formato JSON para facilitar el manejo de los datos recibidos


    /**
    *
    * @author Kieran
    */
    protected abstract boolean validarRespuesta(ACLMessage a);

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
        ACLMessage inbox;
        try{
            inbox = this.receiveACLMessage();
        }
        catch(Exception e){
            System.out.println("Error de comunicación: Excepción al escuchar");
            return null;
        }

        ultimo_mensaje_recibido = inbox;
        String mensaje = inbox.getContent();
        if(echo) System.out.println("Mensaje recibido:\n" + mensaje + "\nRaw:\n" + inbox.toString());
        return Json.parse(mensaje).asObject();
    }
}
