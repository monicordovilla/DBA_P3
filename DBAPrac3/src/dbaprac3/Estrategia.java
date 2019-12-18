/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

/**
 *
 * @author anabosch
 * aquellos que empiezan BURO_ es para que el burocrata inicialize los drones
 * y el resto son estrategias de exploracion de los distintos drones de busqueda
 */
public enum Estrategia {
    BARRIDO_SIMPLE,
    ANCHURA_BAJO,
    ANCHURA_ALTO,
    BURO_1_CADA,
    BURO_2_SPARROW,
    BURO_3_FLY;
}
