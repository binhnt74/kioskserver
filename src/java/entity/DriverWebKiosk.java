/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author ngocduc
 */
public class DriverWebKiosk {

    public WebDriver driver;
    String gecko = "/home/kiosk/kioskserver/kioskserver/Son/9-4-2020-Version2/Version2/KioskServer_v2/lib/geckodriver";

    public void get(String url) {
        //try {
            driver.get(url);
        //   return ("Da goi xong!");
        //} catch (Exception e) {
        //    System.err.println(e);
        //    return "";
        //}
    }
    
    public void initDriver(){
        System.setProperty("webdriver.gecko.driver", gecko);
        driver = new FirefoxDriver();
    }

    public DriverWebKiosk() {
//        System.setProperty("webdriver.gecko.driver", "/home/kiosk/NetBeansProjects/lib/geckodriver");
        //ProfilesIni profileIni = new ProfilesIni();
        //FirefoxProfile profile = profileIni.getProfile("default");
        //FirefoxOptions options = new FirefoxOptions();
        //options.setProfile(profile);
        //System.setProperty("webdriver.firefox.bin", "/usr/lib/firefox/firefox");
        System.setProperty("webdriver.gecko.driver", gecko);
        driver = new FirefoxDriver();
        //driver = new FirefoxDriver(options);
    }

    public byte[] getImageURL(WebDriver driver, String xpath) {
        byte[] ret = null;
        try {
            WebElement we = driver.findElement(By.xpath(xpath));
            String url = we.getAttribute("src");
            URL imageURL = new URL(url);
            BufferedImage originalImage = ImageIO.read(imageURL);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, "jpg", baos);
            ret = baos.toByteArray();

        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return ret;
    }

    public Image getImage(WebDriver driver, String xpath) {
        Image ret = null;
        try {
            WebElement we = driver.findElement(By.xpath(xpath));
            String url = we.getAttribute("src");
            URL imageURL = new URL(url);
            BufferedImage originalImage = ImageIO.read(imageURL);
            ret = SwingFXUtils.toFXImage(originalImage, null);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return ret;
    }

//    public void sendKey(String linkWeb, Object obj) {
//        try {
//            driver.findElement(By.xpath(linkWeb)).sendKeys(obj.toString());
//        } catch (Exception e) {
//        }
//    }

    public void clearText(String linkWeb, String name) {
        findElementComponent(linkWeb, name).clear();
    }

    public String getLinkCurrent() {
        return driver.getCurrentUrl();
    }

    public WSFObject getValueComponent(ComponentClient component) {
        WSFObject wsfobject = new WSFObject();
        wsfobject.type = component.getType();
        wsfobject.value = new ArrayList<>();
        if (wsfobject.type.equals(Constant.lstType[0])) {
            wsfobject.value = getValueSelect(component);
        } else if (wsfobject.type.equals(Constant.lstType[1])) {
            wsfobject.value = getValueInput(component);
        } else if (wsfobject.type.equals(Constant.lstType[2])) {
            wsfobject.value = getValueCheckBox(component);
        } else {
            wsfobject.value = getValueInput(component);
        }
        //get value from label
        return wsfobject;
    }

    private ArrayList<Object> getValueSelect(ComponentClient component) {

        long millis = System.currentTimeMillis();
        long millis2;

        ArrayList<Object> returnVaulue = new ArrayList<>();
        WebElement elementSelect = driver.findElement(By.xpath(component.getLinkWeb()));
        Select select = new Select(elementSelect);

        WebElement element = select.getFirstSelectedOption();
        returnVaulue.addAll(getListOption(elementSelect, select.getFirstSelectedOption().getText()));

//        List<WebElement> liOp = select.getOptions();
//        
//        liOp.stream().filter((value) -> (!value.getText().equals(element.getText()) )).forEachOrdered((value) -> {
//            returnVaulue.add(value.getText());
//        });
        return returnVaulue;
    }

    private ArrayList<Object> getValueInput(ComponentClient component) {
        ArrayList<Object> value = new ArrayList<>();
        try {
            value.add(driver.findElement(By.xpath(component.getLinkWeb())).getText());

        } catch (Exception e) {
            value.add("fail");
        }

        return value;
    }

    private ArrayList<Object> getValueCheckBox(ComponentClient component) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String Click(WebElement we) throws NoAlertPresentException {
        try {
            ((JavascriptExecutor)driver).executeScript("arguments[0].click();", we);
        } catch (StaleElementReferenceException e) {
            System.out.println("ko thuc hien click bang js duoc.");
        }
        Alert alert = driver.switchTo().alert();
        String text = alert.getText();
        alert.accept();
        return text;
    }

    public void sendOption(String linkWeb, String name, Object obj) {
        Select option = new Select(findElementComponent(linkWeb, name));
        option.selectByVisibleText(obj.toString());
    }

    private static ArrayList<Object> getListOption(WebElement element, String selected) {

        ArrayList<Object> lstOption = new ArrayList<>();
        lstOption.add(selected);
        String lstValue = element.getAttribute("innerHTML");
        char[] lstChar = lstValue.toCharArray();
        String value = "";
        boolean start = false;
        boolean check = true;

        for (char c : lstChar) {
            if (start) {
                if (c == '<') {
                    start = false;
                    if (!value.equals(selected)) {
                        lstOption.add(value);
                    }

                } else {
                    value += c;
                }
            } else if (c == '>') {
                if (check) {
                    value = "";
                    start = true;
                    check = false;
                } else {
                    check = true;
                }
            }
        }
        return lstOption;
    }

    public ArrayList<ComponentClient> getDataDocumentManagement() {
        ArrayList<ComponentClient> lstComponent = new ArrayList<>();
        List<WebElement> listTbTag = driver.findElements(By.tagName("table"));
        for (WebElement webElement : listTbTag) {
            if (webElement.getAttribute("class").equals("oep-table")) {
                WebElement body = webElement.findElement(By.tagName("tbody"));
                int step = 0;
                for (int i = 1; i < body.findElements(By.xpath("*")).size(); i++) {
                    WebElement we = body.findElements(By.xpath("*")).get(i);
                    for (int j = 0; j < we.findElements(By.xpath("*")).size(); j++) {
                        WebElement tdTag = we.findElements(By.xpath("*")).get(j);
                        ComponentClient comp = new ComponentClient();
                        comp.setText(tdTag.getText());
                        //comp.setPercentWidth(getPercentW(tdTag));
                        comp.setStep(step);
                        if (j == 4) {
                            WebElement div = tdTag.findElement(By.tagName("div"));
                            WebElement b = div.findElement(By.tagName("button"));
                            comp.setType("Combobox");
                            comp.setText(b.getText());
                            comp.setLinkWeb(getAbsoluteXPath(b));                            
                            WebElement ul = div.findElement(By.tagName("ul"));
                            List<WebElement> listLI = ul.findElements(By.xpath("*"));
                            List<comboboxdata> listData = new ArrayList<>();
                            for (int k = 0; k < listLI.size(); k++) {
                                WebElement li = listLI.get(k);
                                WebElement a = li.findElement(By.tagName("a"));
                                comboboxdata data = new comboboxdata();
                                data.setData(getAbsoluteXPath(a));
                                data.setIdcomboboxdata(a.getAttribute("innerText").trim());
                                listData.add(data);
                            }
                            comp.setLdataCombobox(listData);
                        } else {
                            comp.setType("Label");
                        }
                        step++;
                        lstComponent.add(comp);
                    }
                }
            }
        }
        return lstComponent;
    }
    public void getDataDocument(List<Frame> lFrames){
        for (ComponentClient componentClient : lFrames.get(0).lstComponent) {
            if (componentClient.getLinkWeb() != null) {
                WebElement we = driver.findElement(By.xpath(componentClient.getLinkWeb()));
                
            }
        }
    }

    private double getPercentW(WebElement we) {
        WebElement parent = we.findElement(By.xpath("./.."));
        double total = 0;
        for (WebElement element : parent.findElements(By.xpath("*"))) {
            total += element.getSize().width;
        }
        return we.getSize().width / total;
    }
    
    public WebElement findElementComponent(String xpath, String name){
        WebElement we = null;
        String xpathRelative = null;
        
        //init
        if (xpath.contains("input")) {
            xpathRelative = "//input[@name='"+name+"']";
        }else if(xpath.contains("select")){
            xpathRelative = "//select[@name='"+name+"']";
        }else if(xpath.contains("textarea")){
            xpathRelative = "//textarea[@name='"+name+"']";
        }
        
        //find
        if (driver.findElements(By.xpath(xpath)).size() > 0 ) {//xpath true.
            we = driver.findElement(By.xpath(xpath));
        } else if (driver.findElements(By.xpath(xpathRelative)).size() > 0) {
            we = driver.findElement(By.xpath(xpathRelative));
        }else {System.out.println("\n\nko tim thay xpath: "+xpath+"\nxpathRelative: "+xpathRelative);}
        return we;
    }
    
    public WebElement findElementButton(String xpath, String name){
        WebElement we = null;
        String xpathRelative = null;
        //find <form>
        if(driver.findElements(By.xpath("//form[contains(@name,'fmt')]")).size() > 0){
//            WebElement form = driver.findElement(By.xpath("//form[contains(@name,'fmt')]"));
            String xpathButton = xpath.split("form")[1];
            if (xpathButton.startsWith("[")) xpathButton = xpathButton.substring(3);
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(DriverWebKiosk.class.getName()).log(Level.SEVERE, null, ex);
            }
            return driver.findElement(By.xpath("//form[contains(@name,'fmt')]"+xpathButton));
        }
        
        //find
        if (driver.findElements(By.xpath(xpath)).size() > 0 ) {//xpath true.
            we = driver.findElement(By.xpath(xpath));
        } else if (driver.findElements(By.xpath(xpathRelative)).size() > 0) {
            we = driver.findElement(By.xpath(xpathRelative));
        }
        return we;
    }
    
    public void waitForLoad() {
        ExpectedCondition<Boolean> pageLoadCondition = (WebDriver driver1) -> ((JavascriptExecutor) driver1).executeScript("return document.readyState").equals("complete");
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }

    public String getAbsoluteXPath(WebElement element) {
        return (String) ((JavascriptExecutor) driver).executeScript(
        "function absoluteXPath(element) {"
        + "var comp, comps = [];"
        + "var parent = null;"
        + "var xpath = '';"
        + "var getPos = function(element) {"
        + "var position = 1, curNode;"
        + "if (element.nodeType == Node.ATTRIBUTE_NODE) {"
        + "return null;"
        + "}"
        + "for (curNode = element.previousSibling; curNode; curNode = curNode.previousSibling) {"
        + "if (curNode.nodeName == element.nodeName) {"
        + "++position;"
        + "}"
        + "}"
        + "return position;"
        + "};"
        + "if (element instanceof Document) {"
        + "return '/';"
        + "}"
        + "for (; element && !(element instanceof Document); element = element.nodeType == Node.ATTRIBUTE_NODE ? element.ownerElement : element.parentNode) {"
        + "comp = comps[comps.length] = {};"
        + "switch (element.nodeType) {"
        + "case Node.TEXT_NODE:"
        + "comp.name = 'text()';"
        + "break;"
        + "case Node.ATTRIBUTE_NODE:"
        + "comp.name = '@' + element.nodeName;"
        + "break;"
        + "case Node.PROCESSING_INSTRUCTION_NODE:"
        + "comp.name = 'processing-instruction()';"
        + "break;"
        + "case Node.COMMENT_NODE:"
        + "comp.name = 'comment()';"
        + "break;"
        + "case Node.ELEMENT_NODE:"
        + "comp.name = element.nodeName;"
        + "break;"
        + "}"
        + "comp.position = getPos(element);"
        + "}"
        + "for (var i = comps.length - 1; i >= 0; i--) {"
        + "comp = comps[i];"
        + "xpath += '/' + comp.name.toLowerCase();"
        + "if (comp.position !== null) {"
        + "xpath += '[' + comp.position + ']';"
        + "}"
        + "}"
        + "return xpath;"
        + "} return absoluteXPath(arguments[0]);", element);
    }
}
