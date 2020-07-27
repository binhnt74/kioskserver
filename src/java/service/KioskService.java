/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import WsOther.BacNinhWebservice;
import business.Business;
import entity.ButtonClient;
import entity.ClientKiosk;
import entity.ComponentClient;
import entity.Constant;
import entity.DataConnect;
import entity.DriverWebKiosk;
import entity.Frame;
import entity.Location;
import entity.Service;
import entity.WSFObject;
import entity.WSObject;
import entity.Wrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 * @author ngocduc
 */
@WebService(serviceName = "kioskservice")
public class KioskService {

    //map<macadress,client>
    Map<String, ClientKiosk> lstClient = new HashMap<>();
    private Business business = new Business();

    /**
     * Web service operation
     */
    @WebMethod(operationName = "startKioskClient")
    public WSObject startKioskClient(@WebParam(name = "ipAddress") WSObject wsobject) {
        //TODO write your implementation code here:
        ClientKiosk client = business.checkKiosk(wsobject.ipAddress);
        if (client != null) {
            lstClient.put(wsobject.ipAddress, client);

            WSObject returnWsobject = new WSObject();
            //returnWsobject.frame = business.getFrameLogin();
            returnWsobject.frame = business.getFrameByName(wsobject.frameToOpen);

            return returnWsobject;

        } else {
            wsobject.notification = " Client nay khong co quyen tham gia he thong!";
            return wsobject;
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "openSingleFrame")
    public WSObject openSingleFrame(@WebParam(name = "ipAddress") WSObject wsobject) {
        //TODO write your implementation code here:
        ClientKiosk client = business.checkKiosk(wsobject.ipAddress);
        if (client != null) {
            lstClient.put(wsobject.ipAddress, client);
            WSObject returnWsobject = new WSObject();
            //returnWsobject.frame = business.getFrameLogin();
            returnWsobject.frame = business.getFrameByName(wsobject.frameToOpen);
            return returnWsobject;
        } else {
            wsobject.notification = " Client nay khong co quyen tham gia he thong!";
            return wsobject;
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "LoginDisplayService")
    public WSObject LoginDisplayService(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        ArrayList<String> lstService = null;
        //ArrayList<Service> lService = null;
        WSObject returnWsobject = new WSObject();
        DataConnect connect = new DataConnect();
        connect.getConnection();
        int idUser = -1;
        if (wsobject.loginType.equals("CONGDAN")) {//login mac dinh bang user cong dan.
            idUser = 3;
            System.out.println("Login with default account.");
        } else if (wsobject.loginType.equals("PASSWORD")) {
            String user = (String) wsobject.frame.getData().get("TXTUSER").value.get(0);
            String pass = (String) wsobject.frame.getData().get("TXTPASS").value.get(0);
            idUser = connect.SelectIdUser(user, pass);
            //lstService = getListService(user, pass, wsobject.kioskid); 
            //lService = connect.getListService(user, pass, wsobject.kioskid); 
            if (idUser == -1) {
                returnWsobject = wsobject;
                returnWsobject.notification = "Sai tên mật khẩu hoặc password";
            }
        } else if (wsobject.loginType.equals("FINGERPRINT")) {
            idUser = connect.SelectIdUser(wsobject.fmd);
            //lstService = getListService(wsobject.fmd, wsobject.kioskid);
            //lService = connect.getListService(wsobject.fmd, wsobject.kioskid); 
            if (idUser == -1) {
                returnWsobject = wsobject;
                returnWsobject.notification = "Vân tay không hợp lệ.";
            }
        } else if (wsobject.loginType.equals("FACE")) {
            /*
            gui anh dang byte[] sang server python de predict -> iduer -> tra ve kiosk
            */
            FaceLib fl = new FaceLib();
            String res = fl.predict(wsobject.fmd);
            returnWsobject = wsobject;
            try {
                idUser = Integer.parseInt(res);
                String username = connect.verifyID(idUser);
                if (username == null) {
                    returnWsobject.notification = "Đã có lỗi xảy ra, bạn có thể thử lại sau.";
                }else returnWsobject.notificationKQ = username;
            } catch (NumberFormatException e) {
                idUser = -1;
            }
        }
//        if (idUser == -1) {
//        //if (lstService == null) {
//            returnWsobject = wsobject;
//            returnWsobject.notification = "Sai tên mật khẩu hoặc password";
//            //returnWsobject.retCode = -1;
//            //return returnWsobject;
//        } //else {
            //returnWsobject.lstService = lstService;
        returnWsobject.retCode = idUser;
            //returnWsobject.listCatMenu = connect.genMenuService(lService);
            return returnWsobject;
        //}
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "executeService")
    public WSObject executeService(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        WSObject returnWsobject = new WSObject();
        returnWsobject.retCode = 1;
        ClientKiosk client = lstClient.get(wsobject.ipAddress);
        DataConnect connect = new DataConnect();
        connect.getConnection();
        client.setServiceToOpen(wsobject.serviceToExecute);
        connect.setClient(wsobject.serviceToExecute, client);
        ArrayList<Frame> lstFrame = connect.getListFrame(wsobject.serviceToExecute, 
                wsobject.retCode + "", wsobject.kioskid);
        if(lstFrame.size() > 0){
            client.setFrame(lstFrame);
            returnWsobject.frame = client.getFrame();
            returnWsobject.serviceToExecute = wsobject.serviceToExecute;
            // lấy tất cả commponent
            client.SetAllComponent(connect);       
            if (connect.getProtocol(client.getFrame().idService).equals("Webserver")) {
                DriverWebKiosk driver = client.getDriver();
                //chạy trang web
                if(client.urlService != null)
                    try {
                        driver.get(client.urlService);
                        if (driver.driver.getTitle().equalsIgnoreCase("Server Not Found")) {
                            returnWsobject.notification = "Đã có lỗi xảy ra. Vui lòng thử lại sau...";
                        }
                    } catch (Exception e) {
                        returnWsobject.notification = "Đã có lỗi xảy ra. Vui lòng thử lại sau...";
                    }

                if (wsobject.serviceToExecute.equals("Tra cứu hồ sơ")) {// getcatpcha do co 1 service nay co catcha :v
                    returnWsobject.fmd = client.DownloadImage(lstFrame.get(0).lstComponent);// luu anh vao mang byte[].   
                }
                if(wsobject.serviceToExecute.equals("Đăng ký tài khoản dịch vụ công quốc gia")){
                    returnWsobject.fmd = client.convertBase64Tobyte(lstFrame.get(0).lstComponent);// luu anh vao mang byte[].
                }
                if (wsobject.serviceToExecute.equals("Quản lí hồ sơ")) {
                    ArrayList<ComponentClient> res = driver.getDataDocumentManagement();
                    returnWsobject.frame.lstComponent = res;  
                }
                if (wsobject.serviceToExecute.equals("Đăng xuất cổng thông tin điện tử")) {
                    wsobject.serviceToExecute = "Category1st";
                    returnWsobject = executeService(wsobject);
                    client.getDriver().driver.quit();
                    client.getDriver().driver = null;
                }
                if (wsobject.serviceToExecute.equals("Đăng nhập dịch vụ công quốc gia")) {
                    List<WebElement> l0 = client.getDriver().driver.findElements(By.xpath("/html/body/div[1]/div/div/div[2]/div/div[2]/div/div/form/fieldset/div/button[2]"));
                    if (l0.size() > 0){//web Bac Ninh
                        l0.get(0).click();
                        List<WebElement> l = client.getDriver().driver.findElements(By.xpath("/html/body/div[1]/div[2]/div/div/div[2]/div[2]/div[2]/div/div/div[1]"));
                        if (l.size()>0) {
                            WebElement we = l.get(0);
                            we.click();
                            returnWsobject.fmd = client.convertBase64Tobyte(lstFrame.get(0).lstComponent);// luu anh vao mang byte[].
                        }
                    }
                }
//                if (wsobject.serviceToExecute.equals("Đăng ký tài khoản dịch vụ công quốc gia-otp")){
//                    for (ComponentClient comp : client.getFrame().lstComponent) {
//                        if (comp.getType().equals("Label") && comp.getLinkWeb() != null){
//                            comp.setText(client.getDriver().driver.findElement(By.xpath(comp.getLinkWeb())).getText());
//                            System.out.println(comp.getText());
//                        }
//                    }
//                }
            }else{//Protocol is Webservice
                System.out.println(wsobject.serviceToExecute);
                if (wsobject.serviceToExecute.equals("Quản lí hồ sơ")) {
                    ArrayList<ComponentClient> listComp = BacNinhWebservice.getInstance().documentsManageData(wsobject);
                    returnWsobject.frame.lstComponent = listComp;
                }
                if (wsobject.serviceToExecute.equals("Đăng xuất cổng thông tin điện tử")) {
                    wsobject.serviceToExecute = "Category1st";
                    returnWsobject = executeService(wsobject);
                }
                if (wsobject.serviceToExecute.equals("Tra cứu hồ sơ")) { // getcatpcha do co 1 service nay co catcha :v
                    List<Object> data = BacNinhWebservice.getInstance().getCaptcha();
                    returnWsobject.fmd = (byte[]) data.get(0);// luu anh vao mang byte[].
                    returnWsobject.frameToOpen = (String) data.get(1);
                }
            }
        }else returnWsobject.notification = "Dịch vụ chưa sẵn sàng!";// dịch vụ chưa có trong kiosk hoặc quyền của user.
        //het test
        return returnWsobject;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "SubmitLogin")
    public WSObject SubmitLogin(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:

        WSObject returnWsobject = new WSObject();

        String user = (String) wsobject.frame.getData().get("TXTUSER").value.get(0);
        String pass = (String) wsobject.frame.getData().get("TXTPASS").value.get(0);

        ArrayList<String> lstService = getListService(user, pass, wsobject.kioskid);
        if (lstService == null) {
            returnWsobject = wsobject;
            returnWsobject.notification = "Sai tên mật khẩu hoặc password";
            returnWsobject.retCode = -1;
            return returnWsobject;
        } else {
            returnWsobject.retCode = 1;
            WSFObject wsfobject = new WSFObject();
            wsfobject.type = Constant.TypeComboBox;
            wsfobject.value = new ArrayList<>();

            lstService.forEach((service) -> {
                wsfobject.value.add(service);
            });
//            returnWsobject.data.put("comboListService", wsfobject);

            //test Luu y xoa
            ClientKiosk client = lstClient.get(wsobject.ipAddress);
            DataConnect connect = new DataConnect();
            connect.getConnection();
            connect.setClient("ChungTu", client);
            ArrayList<Frame> lstFrame = connect.getListFrame();
            client.setFrame(lstFrame);
            returnWsobject.frame = client.getFrame();
//            returnWsobject.frame = business.getFrameLogin();
            // lấy tất cả commponent
            client.SetAllComponent(connect);
            DriverWebKiosk driver = client.getDriver();

            //chạy trang web
            driver.get(client.urlService);

            //het test
            return returnWsobject;
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "startService")
    public WSObject startService(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        try {

            WSObject returnobject = new WSObject();
            DataConnect connect = new DataConnect();
            connect.getConnection();

            // client : client dang goi toi duoc luu trong map
            ClientKiosk client = lstClient.get(wsobject.ipAddress);

            // xac dinh service can phai chay
//            WSFObject wsfobject = wsobject.data.get("comboListService");
            WSFObject wsfobject = new WSFObject();
            String tenService = (String) wsfobject.value.get(0);
            // lấy idService, Url Service cần phải chạy
            client = connect.setClient(tenService, client);

            // Lấy tất cả frame liên quan tới service
            ArrayList<Frame> lstFrame = connect.getListFrame();
            client.setFrame(lstFrame);

            // lấy tất cả commponent
            client.SetAllComponent(connect);
            DriverWebKiosk driver = client.getDriver();

            //chạy trang web
            driver.get(client.urlService);

            // lấy dữ liệu và trả về client
            returnobject = getData(driver, connect, client.getFrame());
            return returnobject;
        } catch (Exception e) {
            wsobject.notification = "Lỗi Service";
            return wsobject;
        }
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "continueServiceV2")
    public WSObject continueServiceV2(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        wsobject.frame.complete();
        DataConnect connect = new DataConnect();
        connect.getConnection();

        ClientKiosk client = lstClient.get(wsobject.ipAddress);

        Frame frame = client.getFrame();
        frame.setData(wsobject.frame.getData());

        /*Check su kien cua button hay combobox
        neu la button thi se gui toan bo du lieu o frame len tren web
        neu la combobox se gui theo configue o database */
        WSFObject submit = wsobject.frame.getData().get("Clicked");
        submit.name = submit.name.toUpperCase();

        if (submit.type.equals("Button")) {
            try {
                //Khoi tao wsboject tra ve cho client
                WSObject returnobject = new WSObject();

                // Kiem tra xem frame tiep theo se la frame nao, dua vao configue cua button
                ButtonClient button = getButton(frame.lstButton, submit);
                if (button.getFunction().equals("next")) {
                    frame = client.next();
                } else if (button.getFunction().equals("previous")) {
                    frame = client.pre();
                } else if (button.getFunction().equals("submit")) {
                    client.submit();
                    frame = client.next();
                } else if (button.getFunction().equals("finish")) {
                    client.submit();
                    // return ket qua tra ve
                }

                returnobject.frame.setData(frame.getData());
                returnobject.notification = null;
                return returnobject;

            } catch (Exception e) {
                /*Neu xay ra loi, gui thong bao loi, va tra lai wsboject ben client da gui
                Nhu vay, moi du lieu ben client se giu nguyen, va co them thong bao loi */
                wsobject.notification = "Loi";
                return wsobject;
            }
        } // neu la combobox, se chi update lai du lieu frame, khong next hay previous
        else {
//            try {
//                // Gui du lieu len web
//                ArrayList<ComponentClient> lstComponents = frame.lstComponent;
//                
//                ComponentClient componentClient = getComponentClient(lstComponents,submit);
//                SendData(driver, wsobject.data, connect, componentClient);
//                
//                ComponentClient listComponentSendToWeb = getListComponentSendToWeb(connect,componentClient);
//                
//                SendData(driver, wsobject.data, connect, componentClient);
//                //Khoi tao wsboject tra ve cho client
//                WSObject returnobject = new WSObject();
//                
//                returnobject.frame = frame.nameFrame;
//
//                returnobject = getData(driver, connect, frame);
//
//                returnobject.notification = null;
//                return returnobject;
//
//            } catch (Exception e) {
//                /*Neu xay ra loi, gui thong bao loi, va tra lai wsboject ben client da gui
//                Nhu vay, moi du lieu ben client se giu nguyen, va co them thong bao loi */
//                wsobject.notification = "Loi";
//                return wsobject;
//            }
            return wsobject;
        }
    }

    private ArrayList<String> getListService(String user, String pass, String kid) {
        DataConnect connect = new DataConnect();
        connect.getConnection();
        ArrayList<Service> listService = connect.getListService(user, pass, kid);
        ArrayList<String> listNameService = new ArrayList<>();
        for (Service s : listService) {
            listNameService.add(s.getTenService());
        }
        return listNameService;
    }

    private ArrayList<String> getListService(byte[] mfmd, String kid) {
        DataConnect connect = new DataConnect();
        connect.getConnection();
        ArrayList<Service> listService = connect.getListService(mfmd, kid);
        ArrayList<String> listNameService = new ArrayList<>();
        for (Service s : listService) {
            listNameService.add(s.getTenService());
        }
        return listNameService;
    }

    private WSObject getData(DriverWebKiosk driver, DataConnect connect, Frame frame) {

        WSObject wsobject = new WSObject();

        ArrayList<ComponentClient> lstComponents = frame.lstComponent;
//        wsobject.data = getData(driver,lstComponents,wsobject.data);

        return wsobject;
    }

    private Map<String, WSFObject> getData(DriverWebKiosk driver, ArrayList<ComponentClient> lstComponents, Map<String, WSFObject> data) {

        lstComponents.forEach((component) -> {
            try {
                data.put(component.getTenComponent(), driver.getValueComponent(component));
            } catch (Exception e) {
            }
        });
        return data;
    }

    private ButtonClient getButton(ArrayList<ButtonClient> lstButton, WSFObject submit) {
        for (ButtonClient buttonClient : lstButton) {
            if (submit.name.equals(buttonClient.getName())) {
                return buttonClient;
            }
        }

        // Neu khong thay button se bao loi voi phia client
        throw new UnsupportedOperationException("Khong thay button submit tai frame hien tai");
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "notification")
    public String notification(@WebParam(name = "ipAddress") String ipAddress) {
        //TODO write your implementation code here:
        try {
            ClientKiosk client = lstClient.get(ipAddress);
            client.updateStatus();
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "submit")
    public WSObject submit(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        //Khoi tao wsboject tra ve cho client
        WSObject returnobject = new WSObject();
        DataConnect connect = new DataConnect();
        connect.getConnection();

        ClientKiosk client = lstClient.get(wsobject.ipAddress);

        wsobject.frame.complete();
        client.setFrame(wsobject.frame);
        Frame frame = client.getFrame();
        if(connect.getProtocol(wsobject.frame.idService).equals("Webserver")){
            /*Check su kien cua button hay combobox
            neu la button thi se gui toan bo du lieu o frame len tren web
            neu la combobox se gui theo configue o database */
            try {
                boolean isrefresh = true;
                for (ComponentClient comp  : wsobject.frame.lstComponent) {
                    if (comp.getTenComponent().equals("catcha") || comp.getTenComponent().equals("captcha")
                            || wsobject.frame.nameFrame.equals("formDangKyTaiKhoanDVCQG-password")){
                        isrefresh = false;
                        break;
                    }
                }
                if (isrefresh) client.getDriver().driver.navigate().refresh();
                String text = client.submit();
                String notification = checkSubmit(wsobject, client);
                if((wsobject.frame.nameFrame.equals("loginWeb") || wsobject.frame.nameFrame.contains("formDangKyTaiKhoanDVCQG")
                       || wsobject.frame.nameFrame.equals("formDangNhapDVCQG"))&& notification != null) {
                    wsobject.notification = notification;
                    return wsobject;
                }
                // Sau submit lam gi?
                wsobject.serviceToExecute = connect.getNextService(client.getServiceToOpen());
                if(wsobject.serviceToExecute != null){System.out.println(wsobject.serviceToExecute);
                    returnobject = this.executeService(wsobject);
                    frame = client.getFrame();
                    returnobject.frame.setData(frame.getData());
                    for (ComponentClient comp : client.getFrame().lstComponent) {
                        if(comp.getType().equals("Label") && comp.getLinkWeb()  != null){
                            if(client.getDriver().driver.findElements(By.xpath(comp.getLinkWeb())).size() > 0)
                                comp.setText("\n\n"+client.getDriver().driver.findElement(By.xpath(comp.getLinkWeb())).getText()+"\n\n");
                            break;
                        }
                    }
                }
                returnobject.alertWeb = text;
                returnobject.retCode = wsobject.retCode;
                returnobject.notification = notification;
            } catch (Exception e) {
                /*Neu xay ra loi, gui thong bao loi, va tra lai wsboject ben client da gui
                Nhu vay, moi du lieu ben client se giu nguyen, va co them thong bao loi */
                wsobject.notification = e.toString();
                return wsobject;
            }
        }else{
            WSObject o = BacNinhWebservice.getInstance().submit(wsobject);
            if(o.notification == null){//submit ok
                if(wsobject.serviceToExecute.equals("Tra cứu hồ sơ")){//open 1 form khac ko phai la next service.
                    return o;
                }else{
                    wsobject.serviceToExecute = connect.getNextService(client.getServiceToOpen());
                    if(wsobject.serviceToExecute != null){
                        returnobject = this.executeService(wsobject);
                        returnobject.idUser = o.idUser;
                        if(returnobject.notification == null){
                            frame = client.getFrame();
                            returnobject.frame.setData(frame.getData());
                            returnobject.notification = o.alertWeb;
                            returnobject.notificationKQ = o.notificationKQ;
                        }else {
                            wsobject.notification = returnobject.notification;
                            return wsobject;
                        }
                    }
                }
            }else {
                wsobject.notification = o.notification;
                return wsobject;
            }
        }
        return returnobject;
    }

    private String checkSubmit(WSObject wsobject, ClientKiosk client){
        client.getDriver().waitForLoad();
        String ret = null;
        System.err.println("linkweb hien tai: "+client.getDriver().driver.getCurrentUrl());
        if (wsobject.frame.nameFrame.equals("loginWeb")) {
            //check xem co login thanh cong hay ko?
            String expectedUrl = "https://dvc.bacninh.gov.vn/web/guest";
            int i = 3;
            while (true) {                
                if (client.getDriver().driver.getCurrentUrl().equals(expectedUrl)) {
                    break;
                }else if (i==0) {
                    client.getDriver().driver.navigate().back();
                    ret =  "Đăng nhập không thành công. Vui lòng đăng nhập lại.";
                    i=0;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(KioskService.class.getName()).log(Level.SEVERE, null, ex);
                }
                i--;
            }
            
        }
        if (wsobject.frame.nameFrame.equals("formTraCuuHoSo")) {
            int Num = client.getDriver().driver.findElements(By.xpath("/html/body/div[1]/div/div[1]/div/div/div/div/div/div/div")).size();
            if (Num > 0) {
                wsobject.retCode = client.getDriver().driver.findElement(By.xpath("/html/body/div[1]/div/div[1]/div/div/div/div/div/div/div")).getSize().height;
                ret = client.getDriver().driver.findElement(By.xpath("/html/body/div[1]/div/div[1]/div/div/div/div/div/div/div")).getAttribute("innerHTML");
                client.getDriver().driver.get("https://dvc.bacninh.gov.vn/tra-cuu-thong-tin?p_p_id=tracuuhoso_WAR_oepdvcportlet&p_p_lifecycle=0&_tracuuhoso_WAR_oepdvcportlet_checkcaptCha=false&_tracuuhoso_WAR_oepdvcportlet_search=false&_tracuuhoso_WAR_oepdvcportlet_maSo=");
            }else{
                wsobject.retCode = client.getDriver().driver.findElement(By.xpath("/html/body/div[1]/div/div[1]/div/div/div/div/div/div/h3")).getSize().height;
                String text = client.getDriver().driver.findElement(By.xpath("/html/body/div[1]/div/div[1]/div/div/div/div/div/div/h3")).getText();
                ret = "<h3> "+text+" </h3>";
            }
        }
        if(wsobject.frame.nameFrame.equals("formDangKyTaiKhoanDVCQG")){
            String expectedUrl = "https://dangky.dichvucong.gov.vn/sendOtp";
            if (client.getDriver().driver.getCurrentUrl().equals(expectedUrl)) {
            }else{
                ret =  "Thông tin vừa nhập không chính xác, yêu cầu nhập lại.";
            }
        }
        if(wsobject.frame.nameFrame.equals("formDangKyTaiKhoanDVCQG-otp")){
            String expectedUrl = "https://dangky.dichvucong.gov.vn/password";
            ret =  "Xác nhận mã OTP thất bại do các nguyên nhân sau: \n" +
                        "\n" +
                        "- Mã OTP vừa nhập không chính xác. \n" +
                        "\n" +
                        "- Mã OTP quá thời gian chờ (2 phút kể từ khi nhận được tin nhắn). \n" +
                        "\n" +
                        "- Thông tin bạn nhập ở cửa sổ trước không khớp với thông tin đăng kí với nhà mạng. ";
//            int i = 2;
//            while (i>0) {   
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(KioskService.class.getName()).log(Level.SEVERE, null, ex);
            }
            List<WebElement> l = client.getDriver().driver.findElements(By.xpath("/html/body/div/div/div[2]/div[2]/div[1]/div/div/div[4]/div/div"));
            int hasNote = l.size();
            if(hasNote > 0){
                ret = l.get(0).getText();
            }
            if (client.getDriver().driver.getCurrentUrl().equals(expectedUrl)) {
                ret = null;
            } //else i--;
            System.out.println(client.getDriver().driver.getCurrentUrl());
            return ret;
        }
//        }
        if(wsobject.frame.nameFrame.equals("formDangKyTaiKhoanDVCQG-password")){
            String expectedUrl = "https://xacthuc.dichvucong.gov.vn/authenticationendpoint/login.do";//https://xacthuc.dichvucong.gov.vn/authenticationendpoint/login.do?......
            String unexpectedUrl = "https://dangky.dichvucong.gov.vn/password";
            if (client.getDriver().driver.getCurrentUrl().contains(expectedUrl)) {
            }else if (client.getDriver().driver.getCurrentUrl().equals(unexpectedUrl)) {
                ret = "Sử dụng 8 ký tự trở lên bao gồm chữ thường, chữ in hoa, số và ký tự đặc biệt.";
                List<WebElement> l = client.getDriver().driver.findElements(By.xpath("/html/body/div/div/div[2]/div[2]/div[1]/div/div/div[2]/div/div"));
                int hasNote = l.size();
                if(hasNote > 0){
                    ret = l.get(0).getText();
                }
            }
        }
        if (wsobject.frame.nameFrame.equals("formDangNhapDVCQG")){
            String unexpectedUrl = "https://xacthuc.dichvucong.gov.vn/authenticationendpoint/retry.do";
            //String expectedUrl = "https://xacthuc.dichvucong.gov.vn/vnconnect-auth/vnconnect-authenticator.jsp";//https://xacthuc.dichvucong.gov.vn/vnconnect-auth/vnconnect-authenticator.jsp?.....
            //if (client.getDriver().driver.getCurrentUrl().contains(expectedUrl)) {
            //}else{
                ret =  "Đã xảy ra lỗi trong quá trình xác thực. Vui lòng thử đăng nhập lại.";
                WebElement a = client.getDriver().driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[1]/div[1]/form/div[2]/div[2]/section/div/div/div/div/div[1]/div[1]"));
                if(a.isDisplayed()){
                    ret = a.getText();
                }else ret = null;
            //}
        }
        if (wsobject.frame.nameFrame.equals("formDangNhapDVCQG-otp")){
            String expectedUrl = "https://dvc.bacninh.gov.vn/web/guest";
            if (client.getDriver().driver.getCurrentUrl().equals(expectedUrl)) {
            }else{
                List<WebElement> l = client.getDriver().driver.findElements(By.xpath("/html/body/div/div[2]/div[1]/div[1]/form/div[3]/div/div[1]/div/div[1]/div"));
                int hasNote = l.size();
                if(hasNote > 0){
                    ret = l.get(0).getText();
                }
            }
        }
        return ret;
    }
    
    /**
     * Web service operation
     */
    @WebMethod(operationName = "retrieveData")
    public WSObject retrieveData(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        WSObject wsobj = new WSObject();
        DataConnect connect = new DataConnect();
        connect.getConnection();
        ClientKiosk client = lstClient.get(wsobject.ipAddress);
        try {
            client.setFrame(wsobject.frame);
            client.getFrame().setData(wsobject.frame.getData());
            client.retrieveDataThai();
            wsobj.frame = client.getFrame();
        } catch (Exception e) {
            wsobject.notification = "Loi";
        }
        return wsobj;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getLocation")
    public Wrapper getLocation() {
        //TODO write your implementation code here:
        DataConnect connect = new DataConnect();
        Wrapper location = new Wrapper();
        location.setData(connect.getLocation());
        return location;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "operation")
    public Location operation() {
        //TODO write your implementation code here:
        return null;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "viewService")
    public WSObject viewService(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        WSObject returnWsobject = new WSObject();
        returnWsobject.retCode = 1;
        ClientKiosk client = lstClient.get(wsobject.ipAddress);
        DataConnect connect = new DataConnect();
        connect.getConnection();
        connect.setClient(wsobject.serviceToExecute, client);
        ArrayList<Frame> lstFrame = connect.getListFrame();
        client.setFrame(lstFrame);
        returnWsobject.frame = client.getFrame();
        // lấy tất cả commponent
        client.SetAllComponent(connect);
        DriverWebKiosk driver = client.getDriver();
        //chạy trang web
        driver.get(client.urlService);
        //het test
        return returnWsobject;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "closeService")
    public WSObject closeService(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        WSObject returnWsobject = new WSObject();
        returnWsobject.retCode = 1;
        ClientKiosk client = lstClient.get(wsobject.ipAddress);
        DataConnect connect = new DataConnect();
        connect.getConnection();
        DriverWebKiosk driver = client.getDriver();
        //dong cac trang web
        driver.driver.close();

        //het test
        return returnWsobject;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "refreshCaptcha")
    public WSObject refreshCaptcha(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        WSObject returnWsobject = new WSObject();
        returnWsobject.retCode = 1;
        ClientKiosk client = lstClient.get(wsobject.ipAddress);
        DataConnect connect = new DataConnect();
        connect.getConnection();
        client.setServiceToOpen(wsobject.serviceToExecute);
        connect.setClient(wsobject.serviceToExecute, client);
        ArrayList<Frame> lstFrame = connect.getListFrame(wsobject.serviceToExecute, 
                wsobject.retCode + "", wsobject.kioskid);
        if(lstFrame.size() > 0){
            client.setFrame(lstFrame);
            returnWsobject.frame = client.getFrame();
            returnWsobject.serviceToExecute = wsobject.serviceToExecute;
            // lấy tất cả commponent
            client.SetAllComponent(connect);
            if (connect.getProtocol(client.getFrame().idService).equals("Webserver")) {
                DriverWebKiosk driver = client.getDriver();
                //tim xpath cua button refresh de click
                for (ButtonClient btn : wsobject.frame.lstButton) {
                    if (btn.getFunction().getNameFunction().equals("refreshCaptcha")) {
                        driver.driver.findElement(By.xpath(btn.getLinkWeb())).click();               
                    }
                } 
                try {
                    wsobject.fmd = client.DownloadImage(wsobject.frame.lstComponent);
                } catch (Exception e) {
                    wsobject.fmd = client.convertBase64Tobyte(wsobject.frame.lstComponent);
                }
                return wsobject;
            }else{
                List<Object> data = BacNinhWebservice.getInstance().getCaptcha();
                returnWsobject.fmd = (byte[]) data.get(0);// luu anh vao mang byte[].
                returnWsobject.frameToOpen = (String) data.get(1);
            }
        }
        return returnWsobject;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getMenuService")
    public WSObject getMenuService(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        DataConnect connect = new DataConnect();
        // iduser lấy từ wsobject.retCode (lưu tạm :3)
        ArrayList<Service> lService = connect.getListService(wsobject.retCode, wsobject.kioskid);
        WSObject returnWsobject = new WSObject();
        if (lService.size() > 0) {
            returnWsobject.listCatMenu = connect.genMenuService(lService);
        }else{
            returnWsobject.notification = "Không có dịch vụ nào được hỗ trợ.\n"
                    + "Bạn có thể gọi cho nhân viên hỗ trợ nếu cần.";
        }
        return returnWsobject;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "documentManagement")
    public WSObject documentManagement(@WebParam(name = "wsobject") WSObject wsobject) {
        //TODO write your implementation code here:
        WSObject returnObj = new WSObject();
        ClientKiosk client = lstClient.get(wsobject.ipAddress);
        DataConnect connect = new DataConnect();
        Frame f = wsobject.frame;
        String fullnameService = f.lstComponent.get(0).getText().split("\\(")[0].trim();
        Service service = connect.getService(wsobject.retCode, wsobject.kioskid, fullnameService);
        if (service == null) {
            returnObj.notification = "Dịch vụ này hiện không được hỗ trợ trên Kiosk!";
        }else{
            wsobject.serviceToExecute = service.getTenService();
            if(service.getProtocol().equals("Webserver")){
                ComponentClient comp = f.lstComponent.get(1);
                String xpathThaoTac = comp.getLinkWeb();
                String xpathOptionSelected = comp.getLdataCombobox().get(0).getData();
                DriverWebKiosk driver = client.getDriver(); 
                driver.driver.get("https://dvc.bacninh.gov.vn/ho-so-oi-xu-ly");
                try {
                    driver.driver.findElement(By.xpath(xpathThaoTac)).click();
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(KioskService.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    driver.driver.findElement(By.xpath(xpathOptionSelected)).click();
                } catch (Exception e) {
                }
                //
                if (comp.getLdataCombobox().get(0).getIdcomboboxdata().equalsIgnoreCase("Xóa hồ sơ")
                        || comp.getLdataCombobox().get(0).getIdcomboboxdata().equalsIgnoreCase("Copy hồ sơ")) {
                    driver.driver.switchTo().alert().accept();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(KioskService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    driver.driver.navigate().refresh();
                    returnObj.serviceToExecute = "Quản lí hồ sơ";
                    returnObj.ipAddress = wsobject.ipAddress;
                    WSObject object = executeService(returnObj);
                    return object;
                }else{  
                    ArrayList<Frame> listFrame = connect.getListFrame(service.getId());
                    for (Frame frame : listFrame) {
                        frame.lstComponent = connect.getAllComponents(frame);
                        frame.lstButton = connect.getAllButtons(frame);
                    }
                    client.setFrame(listFrame);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(KioskService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    client.retrieveDataWeb();
                    returnObj.frame = client.getFrame();
                    driver.driver.navigate().back();
                }
            }else{ //protocol is Webservice.
                WSObject o = BacNinhWebservice.getInstance().documentManage(wsobject);
                if (o.notificationKQ != null) {
                    wsobject.serviceToExecute = "Quản lí hồ sơ";
                    WSObject oo = executeService(wsobject);
                    oo.notificationKQ = o.notificationKQ;
                    return oo;
                }
                return o;
            }
        }
        return returnObj;
    }


}
