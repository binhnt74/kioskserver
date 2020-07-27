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
public class Location {
    String id;
    String pid;
    String locname;
    String loclevel;

    public Location(String mid, String mname){
        id = mid;
        locname = mname;
    }
    
    public Location(String mid, String mpid, String mname, String mloclevel){
        id = mid;
        locname = mname;
        pid = mpid;
        loclevel = mloclevel;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getLocname() {
        return locname;
    }

    public void setLocname(String locname) {
        this.locname = locname;
    }

    public String getLoclevel() {
        return loclevel;
    }

    public void setLoclevel(String loclevel) {
        this.loclevel = loclevel;
    }
    
    @Override
    public String toString(){
        return this.locname;
    }
}
