/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

/**
 *
 * @author ngocDuc function de xac dinh hanh dong tiep theo cua client bao gom:
 * next, previous, submit
 */
public class ButtonClient {

    private int idButton;
    private String text;
    private String name;
    private int idFrame;
    private String linkWeb;
    private int idFunction;
    private String parameter;
    private int step;
    private double percentWidth;
    private function function;

    private Object me;//comment giong ben class ComponentClient :))

    public ButtonClient() {
    }

    public ButtonClient(int idButton, String text, String name, int idFrame, String linkWeb, int idFunction, String parameter, int step, double percentWidth) {
        this.idButton = idButton;
        this.text = text;
        this.name = name;
        this.idFrame = idFrame;
        this.linkWeb = linkWeb;
        this.idFunction = idFunction;
        this.parameter = parameter;
        this.step = step;
        this.percentWidth = percentWidth;
    }

    public int getIdButton() {
        return idButton;
    }

    public void setIdButton(int idButton) {
        this.idButton = idButton;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIdFrame() {
        return idFrame;
    }

    public void setIdFrame(int idFrame) {
        this.idFrame = idFrame;
    }

    public String getLinkWeb() {
        return linkWeb;
    }

    public void setLinkWeb(String linkWeb) {
        this.linkWeb = linkWeb;
    }

    public int getIdFunction() {
        return idFunction;
    }

    public void setIdFunction(int idFunction) {
        this.idFunction = idFunction;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Object getMe() {
        return me;
    }

    public void setMe(Object me) {
        this.me = me;
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

    public function getFunction() {
        return function;
    }

    public void setFunction(function function) {
        this.function = function;
    }

}
