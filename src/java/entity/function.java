/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 *
 * @author son
 */
public class function {
    private int idFunction;
    private String nameFunction;

    public function() {
    }
    
    public function(int idFunction, String nameFunction) {
        this.idFunction = idFunction;
        this.nameFunction = nameFunction;
    }

    public int getIdFunction() {
        return idFunction;
    }

    public String getNameFunction() {
        return nameFunction;
    }

    public void setIdFunction(int idFunction) {
        this.idFunction = idFunction;
    }

    public void setNameFunction(String nameFunction) {
        this.nameFunction = nameFunction;
    }
 
}
