/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 *
 * @author kiosk01
 */
public class User {
    private int id;
    private String username;
    private String password;
    private InfomationUser info;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public InfomationUser getInfo() {
        return info;
    }

    public void setInfo(InfomationUser info) {
        this.info = info;
    }

    public User() {
    }

    public User(int id, String username, String password, InfomationUser info) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.info = info;
    }
    
    
    
}
