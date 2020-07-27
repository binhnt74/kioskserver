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
public class Service {
    private int Id;
    private String tenService;
    private String linkWeb;
    private String protocol;
    private String webservice;
    private int idLinhVuc;
    private byte[] image;
    private Frame frame;
    private String TenProcedure;
    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public String getTenService() {
        return tenService;
    }

    public void setTenService(String tenService) {
        this.tenService = tenService;
    }

    public String getLinkWeb() {
        return linkWeb;
    }

    public void setLinkWeb(String linkWeb) {
        this.linkWeb = linkWeb;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getWebservice() {
        return webservice;
    }

    public void setWebservice(String webservice) {
        this.webservice = webservice;
    }

    public Frame getFrame() {
        return frame;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public int getIdLinhVuc() {
        return idLinhVuc;
    }

    public void setIdLinhVuc(int idLinhVuc) {
        this.idLinhVuc = idLinhVuc;
    }

    public String getTenProcedure() {
        return TenProcedure;
    }

    public void setTenProcedure(String TenProcedure) {
        this.TenProcedure = TenProcedure;
    }
    
    public Service() {
    }

    public Service(int Id, String tenService, String linkWeb, String protocol, 
            String webservice, int idLinhVuc, byte[] image, Frame frame, String TenProcedure) {
        this.Id = Id;
        this.tenService = tenService;
        this.linkWeb = linkWeb;
        this.protocol = protocol;
        this.webservice = webservice;
        this.idLinhVuc = idLinhVuc;
        this.image = image;
        this.frame = frame;
        this.TenProcedure = TenProcedure;
    }
    
    
}
