/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WsOther;

import entity.ButtonClient;
import entity.ComponentClient;
import entity.DataConnect;
import entity.Frame;
import entity.WSFObject;
import entity.WSObject;
import entity.comboboxdata;
import entity.function;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import service.DocumentInform;
import service.HoSo;
import service.HoSoService;
import service.HoSoService_Service;
import service.ServiceConfig;
import service.ServiceData;

/**
 *
 * @author kiosk
 */
public class BacNinhWebservice {
    private static BacNinhWebservice bnws;
    private final HoSoService connector;
    private final List<ServiceConfig> listService;
    private final DataConnect dbconnect = new DataConnect();
    
    private BacNinhWebservice(){
        HoSoService_Service service = new HoSoService_Service();
        connector = service.getHoSoServicePort();
        listService = connector.getServiceConfig();// list cac dich vu cung cap boi webservice.
    }
    
    public static BacNinhWebservice getInstance(){
        if (bnws == null) 
            bnws = new BacNinhWebservice();
        return bnws;
    }
    
    private HoSo convertWSObject2HoSo(WSObject obj, String status){
        int idS = 0;//id cua dich vu ben webservice.
        //List<ServiceConfigDetails> listDetail = null;
        if ((idS = hasService(listService, obj.serviceToExecute)) == 0) return null;
        if(idS != 0){ 
            //listDetail = connector.getServiceConfigDetails(idS);
            HoSo hs = new HoSo();
            hs.setSid(idS);
            if(obj.Did != null) hs.setDid(obj.Did);
            Map<String, WSFObject> data = obj.frame.getData();
            DocumentInform doc = new DocumentInform(); 
            doc.setSubDate(new SimpleDateFormat("yyy/MM/dd HH:mm:ss").format(new Date()));
            doc.setUserID(Integer.parseInt(obj.idUser));
            doc.setStatus(status);
            hs.setDocumentInform(doc);
            data.forEach((key, value) -> {
                //System.out.println(value.type+"-"+value.value);
                String colid = value.name.substring(3);
                ServiceData sd = new ServiceData();
                if (value.type.equalsIgnoreCase("String")) {
                    sd.setColid(Integer.parseInt(colid));
                    sd.setColtype("STRING");
                    sd.setValue(value.value.get(0).toString());
                    hs.getListServiceData().add(sd);
                }
                else if(value.type.equalsIgnoreCase("IMG")){
                    sd.setColid(Integer.parseInt(key.substring(3,5)));
                    sd.setColtype("IMAGE");
                    sd.setImgvalue((byte[]) value.value.get(0));
                    hs.getListServiceData().add(sd);
                }
                else if(value.type.equalsIgnoreCase("Combobox")){
                    doc.setAgency(value.value.get(0).toString());
                }
            }); 
            return hs;
        }
        return null;
    }
 
    public WSObject submit(WSObject obj){
        WSObject ret = new WSObject();
        if(obj.serviceToExecute.equals("Đăng nhập cổng thông tin điện tử")){
            int idUser = connector.checkLogin(obj.frame.getData().get("USERNAME").value.get(0).toString(), 
                    obj.frame.getData().get("PASSWORD").value.get(0).toString());
            if(idUser != 0)
                ret.idUser = ""+idUser;
            else ret.notification = "Đăng nhập không thành công. Vui lòng nhập lại tài khoản mật khẩu.";
        }else if(obj.serviceToExecute.equals("Tra cứu hồ sơ")){
            String Did = obj.frame.getData().get("TXTDID").value.get(0).toString();
            if (Did == null) {
                ret.notification = "Mã hồ sơ không tồn tại.";
            }else{
                HoSo resHS = connector.viewHoSo(Did);
                WSObject o = null;
                if(resHS.getSname() != null){
                    o = convertHoSo2WSObject(obj, resHS);
                    for (Iterator<ButtonClient> iterator = o.frame.lstButton.iterator(); iterator.hasNext();) {
                        ButtonClient btn = iterator.next();
                        if (btn.getText().equals("Quay lại")) {
                            if(!"0".equals(obj.idUser)) 
                                btn.setFunction(new function(10, "quitToCategory2"));
                            else btn.setFunction(new function(9, "quitToCategory1"));
                        }else if(btn.getText().equals("Tải lên"));
                        else iterator.remove();
                    }
                    o.Did = Did;
                }else {
                    o = new WSObject();
                }
                return o;
            }
        }else{
            if(obj.Did == null){//submit or save
                for (ButtonClient ButtonClient : obj.frame.lstButton) {
                    WSFObject wsf = obj.frame.getData().get(ButtonClient.getName().toUpperCase());
                    if (wsf != null) {
                        if (ButtonClient.getFunction().getNameFunction().equals("saveDocument")) {
                            HoSo hsSave = convertWSObject2HoSo(obj, "saved");
                            String did = connector.insertHoSo(hsSave);//tra ve id Document sau khi insert thanh cong.
                            if (did != null)
                                ret.notificationKQ = "Lưu hồ sơ thành công!\nMã hồ sơ là: "+did;
                            else ret.alertWeb = "Lưu hồ sơ thất bại!\nVui lòng kiểm tra lại thông tin trước khi nộp.";
                            break;
                        }   
                        if (ButtonClient.getFunction().getNameFunction().equals("submit")) {
                            HoSo hsSubmit = convertWSObject2HoSo(obj, "submited");
                            String did = connector.insertHoSo(hsSubmit);//tra ve id Document sau khi insert thanh cong.
                            if (did != null)
                                ret.notificationKQ = "Nộp hồ sơ thành công!\nMã hồ sơ là: "+did;
                            else ret.alertWeb = "Nộp hồ sơ thất bại!\nVui lòng kiểm tra lại thông tin trước khi nộp.";
                            break;
                        } 
                    }
                }
            }else{//edit
                for (ButtonClient ButtonClient : obj.frame.lstButton) {
                    WSFObject wsf = obj.frame.getData().get(ButtonClient.getName().toUpperCase());
                    if (wsf != null) {
                        if (ButtonClient.getFunction().getNameFunction().equals("saveDocument")) {
                            HoSo hsSave = convertWSObject2HoSo(obj, "saved");
                            String kq = connector.updateHoSo(hsSave);
                            if (kq.equals("updated"))
                                ret.notificationKQ = "Cập nhập hồ sơ thành công!\nMã hồ sơ là: "+obj.Did;
                            else ret.alertWeb = "Lưu hồ sơ thất bại!\nVui lòng kiểm tra lại thông tin trước kia nộp.";
                            break;
                        }   
                        if (ButtonClient.getFunction().getNameFunction().equals("submit")) {
                            HoSo hsSubmit = convertWSObject2HoSo(obj, "submited");
                            String kq = connector.updateHoSo(hsSubmit);
                            if (kq.equals("updated"))
                                ret.notificationKQ = "Cập nhập hồ sơ thành công!\nMã hồ sơ là: "+obj.Did;
                            else ret.alertWeb = "Nộp hồ sơ thất bại!\nVui lòng kiểm tra lại thông tin trước kia nộp.";
                            break;
                        } 
                    }
                }            
            }
        }
        return ret;
    }
    
    private int hasService(List<ServiceConfig> listService, String name){
        for (ServiceConfig service : listService) {
            if (service.getSname().trim().equalsIgnoreCase(name.trim())) {
                return service.getSid();
            }
        }
        return 0;
    }
    
    public ArrayList<ComponentClient> documentsManageData(WSObject obj){
        List<HoSo> listHS = connector.getHoSoByUserID(Integer.parseInt(obj.idUser));
        ArrayList<ComponentClient> listComp = new ArrayList<>();
        int step = 0;
        for (HoSo hs : listHS) {
            ComponentClient stt = new ComponentClient();
            stt.setType("Label");
            stt.setStep(step);
            stt.setText(listHS.indexOf(hs) + 1 + "");
            step++;
            listComp.add(stt);
            
            ComponentClient did = new ComponentClient();
            did.setType("Label");
            did.setStep(step);
            did.setText(hs.getDid());
            step++;
            listComp.add(did);
            
            ComponentClient info = new ComponentClient();
            info.setType("Label");
            info.setStep(step);
            info.setText(hs.getSname()+"\n\n(" + hs.getDocumentInform().getSubDate()+")\n");
            step++;
            listComp.add(info);
            
            ComponentClient status = new ComponentClient();
            status.setType("Label");
            status.setStep(step);
            status.setText(hs.getDocumentInform().getStatus());
            step++;
            listComp.add(status);
            
            ComponentClient action = new ComponentClient();
            action.setType("Combobox");
            action.setStep(step);
            action.setText("Thao tác");
            action.setLinkWeb("action"+stt.getText());
            step++;
            List<comboboxdata> listData = new ArrayList<>();
            if (hs.getDocumentInform().getStatus().equals("saved")) {
                comboboxdata view = new comboboxdata();
                view.setIdcomboboxdata("Xem chi tiết hồ sơ");
                view.setData(hs.getDid()+"");
                listData.add(view);
                
                comboboxdata edit = new comboboxdata();
                edit.setIdcomboboxdata("Chỉnh sửa lại hồ sơ");
                edit.setData(hs.getDid()+"");
                listData.add(edit);
                
                comboboxdata delete = new comboboxdata();
                delete.setIdcomboboxdata("Xóa hồ sơ");
                delete.setData(hs.getDid()+"");
                listData.add(delete);
            }
            if (hs.getDocumentInform().getStatus().equals("submited")) {
                comboboxdata view = new comboboxdata();
                view.setIdcomboboxdata("Xem chi tiết hồ sơ");
                view.setData(hs.getDid()+"");
                listData.add(view);
                
                comboboxdata print = new comboboxdata();
                print.setIdcomboboxdata("In hóa đơn");
                print.setData(hs.getDid()+"");
                listData.add(print);
            }
            action.setLdataCombobox(listData);
            listComp.add(action);
        }
        return listComp;
    }
   
    public WSObject documentManage(WSObject obj){
        ComponentClient comp = obj.frame.lstComponent.get(1);
        HoSo resHS = null;
        String kq = null;
        switch (comp.getLdataCombobox().get(0).getIdcomboboxdata()){
            case "Chỉnh sửa lại hồ sơ":
                //view ~ edit
            case "Xem chi tiết hồ sơ":
                resHS = connector.viewHoSo(comp.getLdataCombobox().get(0).getData());
                WSObject o = convertHoSo2WSObject(obj, resHS);
                o.Did = comp.getLdataCombobox().get(0).getData();
                return o;
            case "Xóa hồ sơ":
                kq = connector.deleteHoSo(comp.getLdataCombobox().get(0).getData());
                String notification = null;
                if (kq.equals("deleted")) notification = "Xóa hồ sơ thành công!";
                else notification = "Xóa hồ sơ thất bại!";
                WSObject ret = new WSObject();
                ret.notificationKQ = notification;
                return ret;
            default: return null;
        }
    }
    
    private WSObject convertHoSo2WSObject(WSObject old, HoSo hs){
        WSObject ret = new WSObject();
        ret.serviceToExecute = hs.getSname();
        // lay component cua Frame
        Frame f = ret.frame = dbconnect.getListFrame(hs.getSname(), old.retCode+"", old.kioskid).get(0);
        ArrayList<ComponentClient> listComp = dbconnect.getAllComponents(f);
        ArrayList<ButtonClient> listButton = dbconnect.getAllButtons(f);
        // lay data cua Frame
        Map<String, WSFObject> data = f.getData();
        for (ComponentClient comp : listComp) {
            if(comp.getTenComponent().equals("agencies")){
                WSFObject obj = new WSFObject();
                obj.name = comp.getTenComponent();
                obj.type = "Combobox";
                obj.value = new ArrayList<>();
                obj.value.add(hs.getDocumentInform().getAgency());
                data.put(comp.getTenComponent(), obj);
            }
            for (ServiceData sData : hs.getListServiceData()) {
                if (comp.getTenComponent().contains("col") &&
                       comp.getTenComponent().substring(3).equals(sData.getColid()+"")){
                   WSFObject obj = new WSFObject();
                   obj.name = comp.getTenComponent();
                   obj.type = sData.getColtype();
                   obj.value = new ArrayList<>();
                   if (sData.getValue() != null) 
                       obj.value.add(sData.getValue());
                   data.put(comp.getTenComponent(), obj);
                   break;
                }
            }
        }
        for (ButtonClient btn : listButton) {
            for (ServiceData sData : hs.getListServiceData()) {
                if (btn.getName().contains("col") &&
                       btn.getName().substring(3).equals(sData.getColid()+"")){
                    WSFObject obj = new WSFObject();
                    obj.name = btn.getName()+"-1";
                    obj.type = "IMG";
                    obj.value = new ArrayList<>();
                    obj.value.add(sData.getImgvalue());
                    data.put(obj.name, obj);
                    break;
                }
            }
        }
        return ret;
    }

    public List<Object> getCaptcha() {
        return connector.getCaptcha();
    }
}
