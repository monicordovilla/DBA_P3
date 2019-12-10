/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;
import java.util.Stack;
import javafx.util.Pair;

/**
 *
 * @author Kieran
 */
public class AgenteRescate extends AgenteDron {
    
    public AgenteRescate(AgentID aid) throws Exception {
        super(aid);
        consumo_fuel = 0.5;
        tamanio_radar = 1;
        centro_radar = 0;
    } 
    
    /**
    *
    * @author Pablo
    * Estrategia de movimiento de los drones de rescate hacia un objetivo
    */
    public int devolverAlturaDireccion(Accion mov){
        if(mov==Accion.moveN)
            return mapa[gps.x+1][gps.y];
        else if(mov==Accion.moveNE)
            return mapa[gps.x+1][gps.y+1];
        else if(mov==Accion.moveE)
            return mapa[gps.x][gps.y+1];
        else if(mov==Accion.moveSE)
            return mapa[gps.x-1][gps.y+1];
        else if(mov==Accion.moveS)
            return mapa[gps.x-1][gps.y];
        else if(mov==Accion.moveSW)
            return mapa[gps.x-1][gps.y-1];
        else if(mov==Accion.moveW)
            return mapa[gps.x][gps.y-1];
        else if(mov==Accion.moveNW)
            return mapa[gps.x+1][gps.y-1];
        else
            return 0;
    }
    
    /**
    *
    * @author Pablo
    * Movimiento vertical para rescatar a un objetivo
    */
    public Accion bajarYRescatar(){
        Accion accion=null;
        if(gps.z!=mapa[gps.x][gps.y])
            accion=Accion.moveDW;
        else
            accion=Accion.rescue;
        return accion;
    }
    
    /**
    *
    * @author Pablo
    * Estrategia de movimiento de los drones de rescate hacia un objetivo
    */
    public Accion acercarseAlObjetivo(int x_obj, int y_obj){
        Accion opcion1=null;
        Accion opcion2=null;
        Accion defin=null;
        int d_horizontal=Math.abs(y_obj-gps.y);
        int d_vertical=Math.abs(x_obj-gps.x);
        if(d_horizontal>d_vertical){
            if(gps.x<x_obj){
                if(gps.y<y_obj){
                    opcion1=Accion.moveSE;
                    opcion2=Accion.moveE;
                }
                else if(gps.y>y_obj){
                    opcion1=Accion.moveSW;
                    opcion2=Accion.moveW;
                }
            }
            else if(gps.x>x_obj){
                if(gps.y<y_obj){
                    opcion1=Accion.moveNE;
                    opcion2=Accion.moveE;
                }
                else if(gps.y>y_obj){
                    opcion1=Accion.moveNW;
                    opcion2=Accion.moveW;
                }
            }
            else{
                if(gps.y<y_obj){
                    opcion1=Accion.moveE;
                    opcion2=null;
                }
                else if(gps.y>y_obj){
                    opcion1=Accion.moveW;
                    opcion2=null;
                }
            }
        }
        if(d_horizontal<d_vertical){
            if(gps.x<x_obj){
                if(gps.y<y_obj){
                    opcion1=Accion.moveSE;
                    opcion2=Accion.moveS;
                }
                else if(gps.y>y_obj){
                    opcion1=Accion.moveSW;
                    opcion2=Accion.moveS;
                }
                else{
                    opcion1=Accion.moveS;
                    opcion2=null;
                }
            }
            else if(gps.x>x_obj){
                if(gps.y<y_obj){
                    opcion1=Accion.moveNE;
                    opcion2=Accion.moveN;
                }
                else if(gps.y>y_obj){
                    opcion1=Accion.moveNW;
                    opcion2=Accion.moveN;
                }
                else{
                    opcion1=Accion.moveN;
                    opcion2=null;
                }
            }
        }
        else{
            if(gps.x<x_obj){
                if(gps.y<y_obj){
                    opcion1=Accion.moveSE;
                    opcion2=null;
                }
                else if(gps.y>y_obj){
                    opcion1=Accion.moveSW;
                    opcion2=null;
                }
            }
            else if(gps.x>x_obj){
                if(gps.y<y_obj){
                    opcion1=Accion.moveNE;
                    opcion2=null;
                }
                else if(gps.y>y_obj){
                    opcion1=Accion.moveNW;
                    opcion2=null;
                }
            }
        }
        if(opcion2==null)
            defin=opcion1;
        else{
            if(Math.abs(devolverAlturaDireccion(opcion1)-gps.z)<Math.abs(devolverAlturaDireccion(opcion2)-gps.z))
                defin=opcion1;
            else
                defin=opcion2;
        }
        if(!super.puedeMover(defin))
            defin=Accion.moveUP;
        return defin;
    }
}
