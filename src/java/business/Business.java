/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package business;

import entity.ClientKiosk;
import entity.DataConnect;
import entity.Frame;

/**
 *
 * @author kiosk01
 */
public class Business {

    public ClientKiosk checkKiosk(String ipAddress) {
        DataConnect dataConnect = new DataConnect();
        return dataConnect.getKiosk(ipAddress);
    }

    public Frame getFrameLogin() {
        DataConnect dataConnect = new DataConnect();
        Frame frame = dataConnect.getFrame("kioskgui/login.fxml");
        frame.lstComponent = dataConnect.getAllComponents(frame);
        return frame;
    }
    public Frame getFrameByName(String fname) {
        DataConnect dataConnect = new DataConnect();
        Frame frame = dataConnect.getFrame(fname);
        frame.lstComponent = dataConnect.getAllComponents(frame);
        return frame;
    }
    
}
