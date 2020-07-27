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
 * frame xac dinh WSObject tac dong len frame nao cua client
 * data la du lieu cua frame o client, duoc xac dinh dua vao 
 * String: Name cua Component
 * WSFObject: data cua Component
 * Co 3 dang data chu yeu: 
 * 1. String danh cho textField, label,..
 * 2. IMG danh cho label
 * 3. ArrayList<String> danh cho combobox
 */
public class WSObject implements Serializable
{
    public Frame frame;
    public String notification;
    public String ipAddress;
    public String kioskid;
    public String idUser;//web
    public int retCode;
    public String frameToOpen;
    public String serviceToExecute;
    public byte[] fmd;
    public String loginType;
    public ArrayList<String> lstService;
    public ArrayList<CatMenu> listCatMenu;
    public String alertWeb;//lưu text của cửa sổ dialog web bật lên.
    public String notificationKQ;
    public String Did;//id document 
}
