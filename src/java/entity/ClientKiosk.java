/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import sun.misc.BASE64Decoder;

/**
 *
 * @author ngocduc
 */
public class ClientKiosk {
    private DriverWebKiosk driver;
    private Frame frame;
    public String urlService;
    public ArrayList<String> lstService;
    public String Protocol;
    private int id;
    private String name;
    private String latitude;
    private String longitude;
    private String ipAddress;
    private String status;
    private String serviceToOpen;

    public String getServiceToOpen() {
        return serviceToOpen;
    }

    public void setServiceToOpen(String serviceToOpen) {
        this.serviceToOpen = serviceToOpen;
    }
    public ClientKiosk() {
    }

    public ClientKiosk(DriverWebKiosk driver, Frame frame, String urlService, ArrayList<String> lstService, int id, String name, String latitude, String longitude, String ipAddress, String status) {
        this.driver = driver;
        this.frame = frame;
        this.urlService = urlService;
        this.lstService = lstService;
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ipAddress = ipAddress;
        this.status = status;
    }
    

    
    public String getUrlService() {
        return urlService;
    }

    public void setUrlService(String urlService) {
        this.urlService = urlService;
    }

    public ArrayList<String> getLstService() {
        return lstService;
    }

    public void setLstService(ArrayList<String> lstService) {
        this.lstService = lstService;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProtocol() {
        return Protocol;
    }

    public void setProtocol(String Protocol) {
        this.Protocol = Protocol;
    }
    
    public DriverWebKiosk getDriver() {
        if (driver == null)
        {
            driver = new DriverWebKiosk();
            return driver;
        }else if(driver.driver == null){
            driver.initDriver();
            return driver;
        }
        else return driver;
    }

    public void setDriver(DriverWebKiosk driver) {
        this.driver = driver;
    }

    public Frame getFrame() {
        if (frame == null)
        {
            frame = new Frame();
            return frame;
        } 
        else return frame;
    }

    public void setFrame(ArrayList<Frame> lstFrame) {
        ArrayList<Frame> FrameTMP = (ArrayList<Frame>) lstFrame.clone();
        if (FrameTMP != null && FrameTMP.size() > 0)
        {
            for (int i = 0; i < FrameTMP.size(); i++)
            {
                if (i < FrameTMP.size() - 1) FrameTMP.get(i).nextFrame = FrameTMP.get(i+1);
                if (i > 0) FrameTMP.get(i).preFrame = null;
            }
            this.frame = FrameTMP.get(0);
        } else this.frame = null;
        
    }
    public void setFrame(Frame frame){
        this.frame = frame;
    }
    // and button
    public void SetAllComponent(DataConnect connect) {
        Frame currFrame = frame;
        while (currFrame != null)
        {
            currFrame.lstComponent = connect.getAllComponents(currFrame);
            currFrame.lstButton = connect.getAllButtons(currFrame);
            currFrame = currFrame.nextFrame;
        }
    }
    
    public Frame next(){
        frame = frame.nextFrame;
        return frame;
    }

    public Frame pre() {
        frame = frame.preFrame;
        return frame;
    }

    public String submit() {
        while (frame.preFrame != null)
            pre();
        while(frame.nextFrame != null)
        {
            String text = frame.sendData(driver);
            if (text != null) {
                return text;
            }
            next();
        }
        frame.deleteFileUploaded(driver);
        String text = frame.sendData(driver);
        if (text != null) {
            return text;
        }                 
        return null;
    }
     public void retrieveData() {
        while (frame.preFrame != null)
            pre();
        while(frame.nextFrame != null)
        {
            frame.getData(driver);
            next();
        }
        frame.getData(driver);
            
    }

     public void retrieveDataThai() {
        while(frame.nextFrame != null)
        {
            Frame tmp = new Frame();
            frame.getData(driver);
            tmp= frame;
            next();
            frame.preFrame=tmp;
        }
        frame.getData(driver);

        while (frame.preFrame != null){
            pre();
            (frame.nextFrame).preFrame=null;
        }
     }
     
     public void retrieveDataWeb() {
        while(frame.nextFrame != null)
        {
            Frame tmp = new Frame();
            frame.retrieveDataWeb(driver);
            tmp= frame;
            next();
            frame.preFrame=tmp;
        }
        frame.retrieveDataWeb(driver);

        while (frame.preFrame != null){
            pre();
            (frame.nextFrame).preFrame=null;
        }
     }
     
    public void updateStatus() {
        DataConnect connect = new DataConnect();
            
    }

    public byte[] DownloadImage(List<ComponentClient> lComponent) {
        byte[] res = null;
        for (ComponentClient comp : lComponent) {
            if (comp.getType().equals("ImageView") && comp.getTenComponent().equals("catcha") || comp.getTenComponent().equals("captcha")) {
                try {       
                    WebElement Image = this.getDriver().driver.findElement(By.xpath(comp.getLinkWeb()));
                    int width = Image.getSize().getWidth();
                    int height = Image.getSize().getHeight();

                    File screen = ((TakesScreenshot) this.getDriver().driver).getScreenshotAs(OutputType.FILE);
                    BufferedImage img = ImageIO.read(screen);

                    BufferedImage dest = img.getSubimage(Image.getLocation().getX(), Image.getLocation().getY(), width, height);
                    //ImageIO.write(dest, "png", screen);
                    ByteArrayOutputStream s = new ByteArrayOutputStream();
                    ImageIO.write(dest, "png", s);
                    res = s.toByteArray();
                    s.close();
                } catch (IOException ex) {
                    Logger.getLogger(ClientKiosk.class.getName()).log(Level.SEVERE, null, ex);
                }
            }   
        }
        return res;
    }
    
    public byte [] convertBase64Tobyte(List<ComponentClient> lComponent){
        byte[] res = null;
        for (ComponentClient comp : lComponent) {
            if (comp.getType().equals("ImageView") && comp.getTenComponent().equals("catcha") || comp.getTenComponent().equals("captcha")) {
                int i = 3;
                //while (i > 0) {                    
                    try {
                        WebElement Image = null;
                        while (i > 0) {
                            List<WebElement> lImg = this.getDriver().driver.findElements(By.xpath(comp.getLinkWeb()));
                            if (lImg.size() > 0){
                                Image = lImg.get(0);
                                break;
                            }else{
                                i--;
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex1) {
                                    Logger.getLogger(ClientKiosk.class.getName()).log(Level.SEVERE, null, ex1);
                                }
                            }
                        }
                        if (Image == null) return null;
                        String base64Img = Image.getAttribute("src").split(",")[1];
                        BASE64Decoder decoder = new BASE64Decoder();
                        res = decoder.decodeBuffer(base64Img);
                        if (res.length > 0) {
                            return res;
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(ClientKiosk.class.getName()).log(Level.SEVERE, null, ex);
                    }
                //}
            }   
        }
        return res;
    }
}
