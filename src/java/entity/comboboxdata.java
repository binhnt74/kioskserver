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
public class comboboxdata {
    private String idcomboboxdata;
    private int idcombobox;
    private String data;

    public comboboxdata() {
    }

    public comboboxdata(String idcomboboxdata, int idcombobox, String data) {
        this.idcomboboxdata = idcomboboxdata;
        this.idcombobox = idcombobox;
        this.data = data;
    }

    public String getIdcomboboxdata() {
        return idcomboboxdata;
    }

    public void setIdcomboboxdata(String idcomboboxdata) {
        this.idcomboboxdata = idcomboboxdata;
    }

    public int getIdcombobox() {
        return idcombobox;
    }

    public void setIdcombobox(int idcombobox) {
        this.idcombobox = idcombobox;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    
    
}
