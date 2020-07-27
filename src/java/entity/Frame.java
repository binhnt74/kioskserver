/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 * @author ngocduc
 */
public class Frame {
    public int idFrame;
    public String nameFrame;
    public int idService;
    public int step;
    public ArrayList<ComponentClient> lstComponent;
    public ArrayList<ButtonClient> lstButton; 
    public Frame nextFrame;
    public Frame preFrame;
    private Map<String, WSFObject> data;

    public Frame() {
        lstComponent = new ArrayList<>();
        lstButton = new ArrayList<>();
        data = new HashMap<>();
    }
    
    public Map<String, WSFObject> getData() {
        if (data == null)
            data = new HashMap<>();
        return data;
    }

    public void setData(Map<String, WSFObject> mdata) {
        if(mdata==null)
            this.data=new HashMap();
        else
            this.data = mdata;
    }
     public File saveImage(byte[] dataIMG, String fname) {
        File f=null;
        try {
            f= new File(fname);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(dataIMG));
            ImageIO.write(img, "JPG",f);
            
        } catch (Exception ex) {
        }
        return f;
    }
     public File saveImageUUID(byte[] dataIMG) {
        File f=null;
        try {
            f=File.createTempFile("image_", ".jpg");
            //f= new File(fname);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(dataIMG));
            ImageIO.write(img, "JPG",f);
            
        } catch (Exception ex) {
        }
        return f;
    }

    public String sendData(DriverWebKiosk driver) {
        lstComponent.forEach((component) -> {
            try {
                WSFObject obj = data.get(component.getTenComponent().toUpperCase());
                if (obj != null) {
                // send Image to Web
                if (obj.type.equals("IMG")) {
                    // luu file
                    byte[] dataIMG = (byte[]) obj.value.get(0);
                    File f=saveImage(dataIMG, obj.name+".jpg"); // fix tam
                    
                    //driver.sendKey(component.getLinkWeb(),f.getAbsolutePath());
                    driver.findElementComponent(component.getLinkWeb(), component.getTenComponent()).sendKeys(f.getAbsolutePath());
                } else if (obj.type.equals("Combobox")) {
                    if(obj.value.get(0)!=null)
                        driver.sendOption(component.getLinkWeb(), component.getTenComponent(), obj.value.get(0));

                } else if (obj.type.equals("Checkbox")) {
                    if((boolean)obj.value.get(0)
                            && !driver.findElementComponent(component.getLinkWeb(), component.getTenComponent()).isSelected())
                        try {
                            WebElement we = driver.findElementComponent(component.getLinkWeb(), component.getTenComponent());
                            ((JavascriptExecutor)driver.driver).executeScript("arguments[0].click();", we);
                        } catch (Exception e) {
                            System.out.println("khong click duoc checkbox.");
                        }
                } else if (obj.type.equals("RadioButton")) {
                    if((boolean)obj.value.get(0))
                        try {
                            WebElement we = driver.findElementComponent(component.getLinkWeb(), component.getTenComponent());
                            ((JavascriptExecutor)driver.driver).executeScript("arguments[0].click();", we);
                        } catch (Exception e) {
                            System.out.println("khong click duoc radiobutton.");
                        }
                }
                //else if (obj.type.equals("Button")){
                //    driver.Click(component.getLinkWeb());
                //    driver.wait(2000);
                //} 
                else {
                    if(obj.value.get(0)!=null)
                        driver.clearText(component.getLinkWeb(), component.getTenComponent());
                    driver.findElementComponent(component.getLinkWeb(), component.getTenComponent()).sendKeys((String)obj.value.get(0));
                } 
            }
            } catch (Exception e) {
                System.out.println("function send data: khong thay " + component.getTenComponent() + "(trong database) trong list data (nhan duoc tu client)");
            }

        });
        try {
            for (ButtonClient ButtonClient : lstButton) {
                WSFObject obj = data.get(ButtonClient.getName().toUpperCase());               
                if (ButtonClient.getFunction().getNameFunction().equals("scanImage")) {
                    //check xem co iframe hien len hay ko?
                    List<WebElement> btnCloseIFrame = driver.driver.findElements(By.className("btn close close-content yui3-widget btn-content"));
                    if (btnCloseIFrame.size() > 0) {//co iframe van dang hien thi
                        btnCloseIFrame.get(0).click();//click nut close.
                    }
                    //upload
                    for (String key : data.keySet()) {
                        if (key.contains(ButtonClient.getName())) {               
                            driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName()).click();
                            try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            WebDriver frame = driver.driver.switchTo().frame(driver.driver.findElement(By.xpath("//*[@id=\"tailen_iframe_\"]")));
                            byte[] dataIMG = (byte[]) data.get(key).value.get(0);
                            //File f=saveImage(dataIMG, data.get(key).name.toLowerCase()+".jpg");
                            File f=saveImageUUID(dataIMG);
                            //driver.sendKey("/html/body/div/div/div/div/div/div/form/table/tbody/tr/td[2]/span/span[1]/input",f.getAbsolutePath());
                            driver.driver.findElement(By.xpath("/html/body/div/div/div/div/div/div/form/table/tbody/tr/td[2]/span/span[1]/input")).sendKeys(f.getAbsolutePath());
                            frame.findElement(By.xpath("/html/body/div/div/div/div/div/div/form/input[3]")).click();
                            driver.driver.switchTo().defaultContent();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
                if (obj != null) {
                    if (ButtonClient.getFunction().getNameFunction().equals("nextFrame")) {
                        WebElement we = driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName());
                        String text = driver.Click(we);                     
                        if(text != null) return text;
                    }
                    if (ButtonClient.getFunction().getNameFunction().equals("saveDocument")) {
                        WebElement we = driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName());
                        String text = driver.Click(we);
                        return null;
                    }   
                    if (ButtonClient.getFunction().getNameFunction().equals("submit")) {
                        WebElement we = driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName());
                        String text = driver.Click(we);
                        return null;
                    }   
                }
            }
        } catch (NoAlertPresentException e) {
            return null;
        }
        return null;
    }
    
    public void deleteFileUploaded(DriverWebKiosk driver){
        //thêm case vì lúc chỉnh sửa form xóa hết các ảnh đã tải lên xong upload lại.
        // con luc submit thi ko anh huong.
        try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
            }
        List<WebElement> btnDeletes = driver.driver.findElements(By.className("oep-hoso-upload-delete"));
        while (btnDeletes.size() > 0) {  
            
            btnDeletes.get(0).click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
            }
            btnDeletes = driver.driver.findElements(By.className("oep-hoso-upload-delete"));
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void getData(DriverWebKiosk driver) {
        for(ComponentClient component:lstComponent)
          {
            try {
                WSFObject obj = data.get(component.getTenComponent().toUpperCase());
                    if (obj != null) {
                        // send Image to Web
                        if (obj.type.equals("IMG")) {
                            // luu file
                            byte[] buf=driver.getImageURL(driver.driver,component.getLinkWeb());
                            if(obj.value==null)
                                obj.value=new ArrayList<>();
                            obj.value.add(buf);
                        } else if (obj.type.equals("Combobox")) {
                            WebElement we=driver.findElementComponent(component.getLinkWeb(), component.getTenComponent());
                            obj.value.set(0,we.getText());
                        } else if (obj.type.equals("Checkbox")) {
                            WebElement we=driver.findElementComponent(component.getLinkWeb(), component.getTenComponent());
                            obj.value.set(0,we.isSelected()); 
                            if (!we.isSelected() && component.getTenComponent().equalsIgnoreCase("camKet")) {
                                we.click();
                            }
                        }else if (obj.type.equals("RadioButton")) {
                            WebElement we=driver.findElementComponent(component.getLinkWeb(), component.getTenComponent());
                            obj.value.set(0,we.isSelected()); 
                        } 
//                        else if (obj.type.equals("Button")){
//                            WebElement we=driver.driver.findElement(By.xpath(component.getLinkWeb()));
//                            obj.value.set(0,we.getText()); 
//                        } 
                        else {
                            WebElement we=driver.findElementComponent(component.getLinkWeb(), component.getTenComponent());
                            obj.value.set(0,we.getText()); 
                        } 
                    }
            } catch (Exception e) {
                System.out.println("function get data: khong thay " + component.getTenComponent() + "(trong database) trong list data (nhan duoc tu client)");
            }
        }
        for (ButtonClient ButtonClient : lstButton) {
                WSFObject obj = data.get(ButtonClient.getName().toUpperCase());               
                if (ButtonClient.getFunction().getNameFunction().equals("scanImage")) {
                    getImageUpload(ButtonClient, driver);
                }
                if (obj != null) {
                    if (ButtonClient.getFunction().getNameFunction().equals("nextFrame")) {
                        WebElement we = driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName());
                        String text = driver.Click(we);
//                        WebElement we=driver.driver.findElement(By.xpath(ButtonClient.getLinkWeb()));
                        obj.value.set(0,we.getText()); 
                    }
                    if (ButtonClient.getFunction().getNameFunction().equals("saveDocument")) {
//                        WebElement we=driver.driver.findElement(By.xpath(ButtonClient.getLinkWeb()));
                        WebElement we = driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName());
                        obj.value.set(0,we.getText()); 
                    }   
                    if (ButtonClient.getFunction().getNameFunction().equals("submit")) {
//                        WebElement we=driver.driver.findElement(By.xpath(ButtonClient.getLinkWeb()));
                        WebElement we = driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName());
                        obj.value.set(0,we.getText()); 
                    }   
                }
            }
    }
    
    public void retrieveDataWeb(DriverWebKiosk driver){
        data = this.getData();
        for (ComponentClient componentClient : lstComponent) {
            if (componentClient.getLinkWeb() != null 
                    && !componentClient.getLinkWeb().trim().isEmpty()) {
                WSFObject obj = new WSFObject();
                obj.value=new ArrayList<>();
                obj.name = componentClient.getTenComponent();
                if (componentClient.getType() != null) {
                    obj.type = componentClient.getType();
                    // send Image to Web
                    if (componentClient.getType().equals("IMG")) {
                        // luu file
                        byte[] buf=driver.getImageURL(driver.driver,componentClient.getLinkWeb());
//                        if(obj.value==null)
                            //obj.value=new ArrayList<>();
                        obj.value.add(buf);
                    } else if (componentClient.getType().equalsIgnoreCase("Combobox")) {
//                        WebElement we=driver.driver.findElement(By.xpath(componentClient.getLinkWeb()));
                        WebElement we=driver.findElementComponent(componentClient.getLinkWeb(), componentClient.getTenComponent());
//                        obj.value.add(0,we.getText());
                        String selected = (String) ((JavascriptExecutor) driver.driver).executeScript("return arguments[0].options[arguments[0].selectedIndex].text;", we);
                        obj.value.add(0,selected);
                    } else if (componentClient.getType().equalsIgnoreCase("Checkbox")) {
//                        WebElement we=driver.driver.findElement(By.xpath(componentClient.getLinkWeb()));
                        WebElement we=driver.findElementComponent(componentClient.getLinkWeb(), componentClient.getTenComponent());
                        obj.value.add(0,we.isSelected()); 
                        if (!we.isSelected() && componentClient.getTenComponent().equalsIgnoreCase("camKet")) {
                            we.click();
                        }
                    }else if (componentClient.getType().equalsIgnoreCase("RadioButton")) {
//                        WebElement we=driver.driver.findElement(By.xpath(componentClient.getLinkWeb()));
                        WebElement we=driver.findElementComponent(componentClient.getLinkWeb(), componentClient.getTenComponent());
                        obj.value.add(0,we.isSelected()); 
                    } 
                    else {
//                        WebElement we=driver.driver.findElement(By.xpath(componentClient.getLinkWeb()));
                        WebElement we=driver.findElementComponent(componentClient.getLinkWeb(), componentClient.getTenComponent());
                        obj.value.add(0,we.getAttribute("value")); 
                    } 
                }
                data.put(componentClient.getTenComponent(), obj);
            }
        }
        for (ButtonClient ButtonClient : lstButton) {
            if (ButtonClient.getLinkWeb() != null) {
                WSFObject obj = new WSFObject();  
                obj.name = ButtonClient.getName();
                obj.value=new ArrayList<>();
                if (ButtonClient.getFunction().getNameFunction().equals("scanImage")) {
                    getImageUpload(ButtonClient, driver);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (ButtonClient.getFunction().getNameFunction().equals("nextFrame")) {
//                    WebElement we=driver.driver.findElement(By.xpath(ButtonClient.getLinkWeb()));
                    WebElement we=driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName());
                    obj.value.add(0,we.getAttribute("value")); 
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Frame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try{
                        String text = driver.Click(we);
                    } catch (NoAlertPresentException e) {
                    }
                }
                if (ButtonClient.getFunction().getNameFunction().equals("saveDocument")) {
                    WebElement we=driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName());
                    obj.value.add(0,we.getAttribute("value")); 
                }   
                if (ButtonClient.getFunction().getNameFunction().equals("submit")) {
                    WebElement we=driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName());
                    obj.value.add(0,we.getAttribute("value")); 
                }   
            }
        }
    }
    
    private void getImageUpload(ButtonClient ButtonClient, DriverWebKiosk driver){
        WebElement parentBtnUpload = driver.findElementButton(ButtonClient.getLinkWeb(), ButtonClient.getName()).findElement(By.xpath("./.."));
        WebElement parent = parentBtnUpload.findElement(By.xpath("./.."));
        List<WebElement> findElements = parent.findElements(By.tagName("td")).get(0).findElement(By.tagName("div")).findElements(By.tagName("ul"));
        int size = parent.findElements(By.tagName("td")).get(0).findElement(By.tagName("div")).findElements(By.tagName("ul")).size();
        if (size > 0) {//neu co thi chi co 1
            WebElement ulTag = findElements.get(0);
            ///ArrayList<BufferedImage> lstImage = new ArrayList<>();
            for (WebElement liTag : ulTag.findElements(By.tagName("li"))) {
                String a = driver.getAbsoluteXPath(liTag)+"/a[1]";
                URL url = null;
                try {
                    url = new URL(driver.driver.findElement(By.xpath(a)).getAttribute("href"));
                    //Image img = Toolkit.getDefaultToolkit().createImage(url);
                    //BufferedImage bufImgOne = ImageIO.read(url);
                    //lstImage.add(bufImgOne);
                    //byte[] convertImageToBytes = convertImageToBytes(bufImgOne);
                    WSFObject wsf = new WSFObject();
                    wsf.name = driver.driver.findElement(By.xpath(a)).getAttribute("innerHTML").trim();
                    wsf.type = "IMG";
                    wsf.value = new ArrayList<>();
                    //wsf.value.add(convertImageToBytes);
                    wsf.value.add(url.toExternalForm());
                    data.put(ButtonClient.getName()+ulTag.findElements(By.tagName("li")).indexOf(liTag), wsf);
                } catch (MalformedURLException ex) {
                }
            }
        }
    }
    
    
    
    public byte[] convertImageToBytes(BufferedImage img) {
        byte[] res = null;
        if (img != null) {
            try {
                ByteArrayOutputStream s = new ByteArrayOutputStream();
                ImageIO.write(img, "png", s);
                res = s.toByteArray();
                s.close();
            } catch (IOException ex) {
            }
        }
        return res;
    }
   
    private ButtonClient getButton(ArrayList<ButtonClient> lstButton, WSFObject submit) {
        for (ButtonClient buttonClient : lstButton) {
            if (submit.name.equals(buttonClient.getName())){
                return buttonClient;
            }
        }
        
        // Neu khong thay button se bao loi voi phia client
        throw new UnsupportedOperationException("Khong thay button submit tai frame hien tai");
    }
    public void complete(){
        Frame frame = nextFrame;
        Frame preFrame = this;
        while (frame != null)
        {
            frame.preFrame = preFrame;
            preFrame = frame;
            frame = frame.nextFrame;
        }
    }
    
}
