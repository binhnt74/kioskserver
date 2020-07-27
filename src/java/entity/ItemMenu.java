/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.List;

/**
 *
 * @author Son
 */
public class ItemMenu {
    private String text;
    private byte[] iv;
    private String command;
    private String mclass;
    private Object[] para;
    private List<CatMenu> subcats;
    private String commandType;

    public ItemMenu() {
    }

    public Object[] getPara() {
        return para;
    }

    public void setPara(Object[] para) {
        this.para = para;
    }

    public String getMclass() {
        return mclass;
    }

    public void setMclass(String mclass) {
        this.mclass = mclass;
    }

    public List<CatMenu> getSubcats() {
        return subcats;
    }

    public void setSubcats(List<CatMenu> subcats) {
        this.subcats = subcats;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
    
    
}
