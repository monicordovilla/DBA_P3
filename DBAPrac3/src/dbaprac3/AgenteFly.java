/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

import es.upv.dsic.gti_ia.core.AgentID;
import java.util.Stack;

/**
 *
 * @author Kieran
 */
public class AgenteFly extends AgenteDron {
    
    public AgenteFly(AgentID aid) throws Exception {
        super(aid);
        consumo_fuel = 0.1;
        tamanio_radar = 5;
        centro_radar = 2;
    }
    
    /**
    *
    * @author Ana
    * Selección de la estrategia de búsqueda que va a seguir en función del mapa y grupo de rescate que se elija
    */
    public void selectorEstrategia(){//Variable que tenemos que ver donde recoje su valor)
        if(variable)
            //Realizas esta eleccion
        else
            //esta   
    }
    
    /**
    *
    * @author Ana
    * Estrategia de búsqueda para mapas altos en los que el equipo esta formado por 2 Fly y 2 Rescue
    */
    public void estrategiaMapasAltos(){//Variable que tenemos que ver donde recoje su valor)
        //Cuando creemos las instancias debemos establecerle un identificador para que aqui segn eso recorra una parte del mapa
        if(encuentra)
            //busqueda directa
    }
    
    /**
    *
    * @author Ana
    * Estrategia de búsqueda para mapas bajos en los que el equipo esta formado por 1 Fly, 1 Hawk y 2 Rescue
    */
    public void estrategiaMapasBajos(){//Variable que tenemos que ver donde recoje su valor)
        //if encuentra busqueda directa
    }
    
    /**
    *
    * @author Ana
    * Se conoce donde esta el objetivo y se dirige directamente hacia el
    */
    public void estrategiaMapasBajos(){//Variable que tenemos que ver donde recoje su valor)
        //if encuentra busqueda directa
    }
}
