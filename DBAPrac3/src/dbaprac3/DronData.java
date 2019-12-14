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

    DronData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    protected class GPS {
        public int x;
        public int y;
        public int z;

        GPS() {
            x = -1; y = -1; z = -1;
        }
    }

    String nombre;
    GPS gps;
    float fuel;
    double consumo_fuel; //Consumo de fuel por movimiento
    int ini_x;
    int ini_y;
    Rol rol;
    int recogidos;
    
    DronData(String nombre){
        this.nombre = nombre;
        gps=new GPS();
        recogidos=0;
    }
}
