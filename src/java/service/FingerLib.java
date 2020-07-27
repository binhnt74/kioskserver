/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.digitalpersona.uareu.*;
import com.mysql.jdbc.PreparedStatement;
import entity.DataConnect;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import javax.swing.event.HyperlinkEvent;

/**
 *
 * @author kiosk01
 */
public class FingerLib {

    public com.digitalpersona.uareu.Reader m_reader;
    public Fid.Format m_format;
    public com.digitalpersona.uareu.Reader.ImageProcessing m_proc;
    public ReaderCollection rdc;
    public Connection con;
    public PreparedStatement pst;
    public ResultSet rs;
    public Fmd.Format fmd_format;
    public Fid.Format fid_format;
    public Fid retFid;
    public Fmd retFmd;
    public byte[] retImg;
    public ImageView imv;
    public boolean initFingerLib() {
        boolean ret=false;
        try {
            m_format = Fid.Format.ANSI_381_2004;
            fmd_format = Fmd.Format.ANSI_378_2004;
            fid_format = Fid.Format.ANSI_381_2004;
            m_proc = com.digitalpersona.uareu.Reader.ImageProcessing.IMG_PROC_DEFAULT;
            DataConnect connect = new DataConnect();
            con=connect.getConnection();
            ret=true;
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return ret;
    }
    

    public BufferedImage getImage(Fid mfid) {
        BufferedImage img = null, img1;
        Fid.Fiv view = mfid.getViews()[0];
        img = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        img.getRaster().setDataElements(0, 0, view.getWidth(), view.getHeight(), view.getImageData());
        img1 = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
        img1.getGraphics().drawImage(img, 0, 0, null);
        return img1;
    }

    public BufferedImage getImage(Fid mfid, ImageView imv) {
        BufferedImage img = null, img1;
        Fid.Fiv view = mfid.getViews()[0];
        img = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        img.getRaster().setDataElements(0, 0, view.getWidth(), view.getHeight(), view.getImageData());
        img1 = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
        img1.getGraphics().drawImage(img, 0, 0, null);
        imv.setImage(SwingFXUtils.toFXImage(img1, null));
        return img1;
    }

    public BufferedImage getImageMinutiae(Fid mfid, ImageView imv) {
        BufferedImage img = null, img1;
        Fid.Fiv view = mfid.getViews()[0];
        img = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        img.getRaster().setDataElements(0, 0, view.getWidth(), view.getHeight(), view.getImageData());
        img1 = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
        img1.getGraphics().drawImage(img, 0, 0, null);
        Fmd fmd = getFmd(mfid, fmd_format);
        fmdDecode(fmd, img1);
        imv.setImage(SwingFXUtils.toFXImage(img1, null));
        return img1;
    }

    public void showImage(BufferedImage img, ImageView imv) {
        imv.setImage(SwingFXUtils.toFXImage(img, null));
    }

    public void showFinger(byte[] buf, ImageView imv) {
        BufferedImage img = createImageFromBytes(buf);
        imv.setImage(SwingFXUtils.toFXImage(img, null));
    }

    public Fmd getFmd(Fid mfid, Fmd.Format mformat) {
        Fmd mfmd = null;
        try {
            Engine engine = UareUGlobal.GetEngine();
            mfmd = engine.CreateFmd(mfid, mformat);
        } catch (UareUException ex) {
            Logger.getLogger(FingerLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mfmd;
    }

   

    
    public Fid captureFid() {
        Fid mfid = null;
        try {
            com.digitalpersona.uareu.Reader.CaptureResult cr = m_reader.Capture(m_format, m_proc, m_reader.GetCapabilities().resolutions[0], -1);
            if (cr.quality == com.digitalpersona.uareu.Reader.CaptureQuality.GOOD) {
                mfid = cr.image;
            }
        } catch (UareUException ex) {
            Logger.getLogger(FingerLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mfid;
    }

    public void fmdDecode(Fmd mfmd, BufferedImage img) {
        try {
            byte[] a = mfmd.getData();
            int mnCount = mfmd.getViews()[0].getMinutiaCnt();
            int x, y, type, angle, quality;
            int i;
            int w = mfmd.getWidth(), h = mfmd.getHeight();
            Graphics g = null;
            DataInputStream bi = new DataInputStream(new ByteArrayInputStream(a));
            if (img != null) {
                g = img.getGraphics();
                g.setColor(Color.red);
            }
            for (i = 0; i < 30; i++) {
                bi.readByte();
            }
            for (i = 0; i < mnCount; i++) {
                short s1 = bi.readShort();
                short s2 = bi.readShort();
                x = s1 & 0x3FFF;
                y = s2 & 0x3FFF;
                type = s1 >> 14;
                angle = bi.readByte();
                quality = bi.readByte();
                if (g != null) {
                    if (x < w && y < h) {
                        if (type == 0) {
                            g.drawString("0", x, y);
                        } else if (type == 0) {
                            g.drawString("1", x, y);
                        } else {
                            g.drawString("2", x, y);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FingerLib.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public boolean compare(Fmd fmd1, Fmd fmd2) {
        int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000; //target rate is 0.00001
        Engine engine = UareUGlobal.GetEngine();
        int mfmr = 0;
        try {
            mfmr = engine.Compare(fmd1, 0, fmd2, 0);
        } catch (UareUException ex) {
            Logger.getLogger(FingerLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (mfmr <= target_falsematch_rate);
    }

    public boolean saveFid(Fid mfid, String uid, String uname) {
        boolean ret = false;
        java.sql.PreparedStatement pst;
        byte[] bufImg;
        byte[] bufFmd;
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM user WHERE idUser=" + uid);
            if (!rs.first()) {
                pst = con.prepareStatement("INSERT INTO user(idUser,UserName,fprint1,fprint1fmd) values(?,?,?,?)");
                pst.setString(1, uid);
                pst.setString(2, uname);
                bufImg = createBytesFromImage(getImage(mfid));
                pst.setBytes(3, bufImg);
                Fmd mfmd = getFmd(mfid, fmd_format);
                bufFmd = mfmd.getData();
                pst.setBytes(4, bufFmd);
            } else {
                pst = con.prepareStatement("UPDATE user SET fprint1=?,fprint1fmd=? WHERE idUser=?");
                bufImg = createBytesFromImage(getImage(mfid));
                pst.setBytes(1, bufImg);
                System.out.println("FID LENG=" + bufImg.length);
                Fmd mfmd = getFmd(mfid, fmd_format);
                bufFmd = mfmd.getData();
                System.out.println("FMD LENG=" + bufFmd.length);
                pst.setBytes(2, bufFmd);
                pst.setString(3, uid);
            }
            pst.execute();
        } catch (SQLException ex) {
            Logger.getLogger(FingerLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    public Fmd readFmd(String uname) {
        Fmd ret = null;
        java.sql.PreparedStatement pst;
        Importer imp = UareUGlobal.GetImporter();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM user WHERE UserName='" + uname + "'");
            if (rs.first()) {
                retImg = rs.getBytes("fprint1");
                byte[] b = rs.getBytes("fprint1fmd");
                //retFid=UareUGlobal.GetImporter().ImportFid(a, fid_format);
                ret = imp.ImportFmd(b, fmd_format, fmd_format);
            }

        } catch (Exception ex) {
            Logger.getLogger(FingerLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    public int identifyFmdRetID(byte[] mfmd) {
        int rets=0;
        Fmd ret = null,mret=null;
        java.sql.PreparedStatement pst;
        Importer imp = UareUGlobal.GetImporter();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM user");
            while (rs.next()) {
                retImg = rs.getBytes("fprint1");
                byte[] b = rs.getBytes("fprint1fmd");
                //retFid=UareUGlobal.GetImporter().ImportFid(a, fid_format);
                if (b != null){
                    ret = imp.ImportFmd(b, fmd_format, fmd_format);
                    mret = imp.ImportFmd(mfmd, fmd_format, fmd_format);
                    if(this.compare(mret, ret))
                    {
                        rets=rs.getInt("idUser");
                        break;
                    }
                }
            }
            rs.close();

        } catch (Exception ex) {
            Logger.getLogger(FingerLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rets;
    }
     public String identifyFmd(Fmd mfmd) {
        String rets="";
        Fmd ret = null;
        java.sql.PreparedStatement pst;
        Importer imp = UareUGlobal.GetImporter();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM user");
            while (rs.next()) {
                retImg = rs.getBytes("fprint1");
                byte[] b = rs.getBytes("fprint1fmd");
                //retFid=UareUGlobal.GetImporter().ImportFid(a, fid_format);
                ret = imp.ImportFmd(b, fmd_format, fmd_format);
                if(this.compare(mfmd, ret))
                {
                    rets=rs.getString("UserName");
                    break;
                }
            }
            rs.close();

        } catch (Exception ex) {
            Logger.getLogger(FingerLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rets;
    }
     
     public String identifyFmd() {
        String rets="";
        Fmd ret = null;
        java.sql.PreparedStatement pst;
        Importer imp = UareUGlobal.GetImporter();
        Fmd mfmd=null;
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM user");
            while (rs.next()) {
                retImg = rs.getBytes("fprint1");
                byte[] b = rs.getBytes("fprint1fmd");
                //retFid=UareUGlobal.GetImporter().ImportFid(a, fid_format);
                if(ret==null)
                    ret = imp.ImportFmd(b, fmd_format, fmd_format);
                if(mfmd==null && ret!=null)
                    mfmd = imp.ImportFmd(b, fmd_format, fmd_format);
                if(mfmd!=null)
                    if(this.compare(mfmd, ret))
                    {
                        rets=rs.getString("UserName");
                        break;
                    }
            }
            rs.close();

        } catch (Exception ex) {
            Logger.getLogger(FingerLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rets;
    }

    public BufferedImage createImageFromBytes(byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] createBytesFromImage(BufferedImage img) {
        byte[] res = null;
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", s);
            res = s.toByteArray();
            s.close(); //especially if you are using a different output stream.
        } catch (Exception e) {

        }
        return res;
    }
}
