/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import es.upv.dsic.gti_ia.core.AgentID;
import DBA.SuperAgent;
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
public class AgenteBurocrata extends SuperAgent {
    int max_x;
    int max_y;
    int[][] mapa;
    
    public AgenteBurocrata(AgentID aid)throws Exception{
        super(aid);
    }
   
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
    
    @Override
    public void execute(){
        String a = JSONEncode_Inicial;
        comunicar("Izar", a, ACLMessage.SUBSCRIBE)
    }
}
