/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

/**
 *
 * @author Celia
 */

class DronData {
    
    protected class GPS {
        public int x;
        public int y;
        public int z;

        GPS() {
            x = -1; y = -1; z = -1;
        }
    }

    GPS gps = new GPS();
    float fuel;
    double consumo_fuel; //Consumo de fuel por movimiento
    int ini_x;
    int ini_y;
    String id;
    
}
