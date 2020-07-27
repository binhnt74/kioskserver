/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.ArrayList;


/**
 *
 * @author ngocduc
 */
public class WSFObject implements Serializable{
    public ArrayList<Object> value;
    public String type;
    public String name;
}
