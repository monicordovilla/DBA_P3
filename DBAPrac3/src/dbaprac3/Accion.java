/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbaprac3;

/**
 *
 * @author Celia, Kieran
 */
public enum Accion {
    moveNW(7),
    moveN(0),
    moveNE(1),
    moveW(6),
    moveE(2),
    moveSW(5),
    moveS(4),
    moveSE(3),
    moveUP(10),
    moveDW(11),
    refuel(20),
    stop(25),
    recue,
    logout(30);
    
    public final int value;
    private Accion(int value){
        this.value = value;
    }
    
    public static Accion valueOfAccion(int i){
        for(Accion a: values()){
            if(a.value == i) {
                return a;
            }
        }
        return null;
    }
    
    public static float AccionToAngulo(Accion a){
            if(a == moveUP || a == moveDW || a == refuel || a == logout) return -1;
            return a.value*45.0f;
    }
}
