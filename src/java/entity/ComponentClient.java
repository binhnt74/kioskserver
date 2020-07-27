/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.util.List;

/**
 *
 * @author ngocduc
 */
public class ComponentClient {
    private int idComponent;
    private String TenComponent;
    private String text;
    private String LinkWeb;
    private int idFrame;
    private String type;
    private int step;
    private double percentWidth;
    private List<comboboxdata> ldataCombobox;
    private Object me;// Lưu Node nhưng JAXB ko đọc đc file .xml chứa thành phần interface :((
    private String cssPath;
    
    public ComponentClient() {
    }

    public ComponentClient(int idComponent, String TenComponent, String text, String LinkWeb, int idFrame,
            String type, int step, double percentWidth, List<comboboxdata> ldataCombobox, String cssPath) {
        this.idComponent = idComponent;
        this.TenComponent = TenComponent;
        this.text = text;
        this.LinkWeb = LinkWeb;
        this.idFrame = idFrame;
        this.type = type;
        this.step = step;
        this.percentWidth = percentWidth;
        this.ldataCombobox = ldataCombobox;
        this.cssPath = cssPath;
    }

    public int getIdComponent() {
        return idComponent;
    }

    public void setIdComponent(int idComponent) {
        this.idComponent = idComponent;
    }

    public String getTenComponent() {
        return TenComponent;
    }

    public void setTenComponent(String TenComponent) {
        this.TenComponent = TenComponent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public String getLinkWeb() {
        return LinkWeb;
    }

    public void setLinkWeb(String LinkWeb) {
        this.LinkWeb = LinkWeb;
    }

    public int getIdFrame() {
        return idFrame;
    }

    public void setIdFrame(int idFrame) {
        this.idFrame = idFrame;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public double getPercentWidth() {
        return percentWidth;
    }

    public void setPercentWidth(double percentWidth) {
        this.percentWidth = percentWidth;
    }

    public Object getMe() {
        return  me;
    }

    public void setMe(Object me) {
        this.me = me;
    }
    
    public List<comboboxdata> getLdataCombobox() {
        return ldataCombobox;
    }

    public void setLdataCombobox(List<comboboxdata> ldataCombobox) {
        this.ldataCombobox = ldataCombobox;
    }

    public String getCssPath() {
        return cssPath;
    }

    public void setCssPath(String cssPath) {
        this.cssPath = cssPath;
    }
    
}
