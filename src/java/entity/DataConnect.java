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
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import service.FingerLib;

/**
 *
 * @author ngocduc
 */
public class DataConnect {
    Connection conn;
    private PreparedStatement pstm;
    String database = "kioskserver_v2";
//    String database = "testFX";
    String user = "kiosk";
    String pass = "Kiosk@db#2020";
    String port = "3306";
    String IP = "localhost";

    public DataConnect() {
        getConnection();
    }
    
    public Connection getConnection() {        
        try {
            System.setProperty("javax.net.ssl.trustStore","/home/kiosk/client-ssl/truststore"); 
            System.setProperty("javax.net.ssl.trustStorePassword","Kiosk@db#2020");
            System.setProperty("javax.net.ssl.keyStore","/home/kiosk/client-ssl/keystore"); 
            System.setProperty("javax.net.ssl.keyStorePassword","Kiosk@db#2020");
            Class.forName("com.mysql.jdbc.Driver");
            if (conn == null) {
                conn = DriverManager.getConnection("jdbc:mysql://"+IP+":"+port+"/"+database+
                    "?verifyServerCertificate=true&useSSL=true&requireSSL=true&autoReconnect=true"
                            + "&useUnicode=true&characterEncoding=utf-8",user,pass);
            }                    
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(DataConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }
    
    //get location
    public ArrayList<Location> getLocation() {
        ArrayList<Location> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM "+database+".location";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                Location data = new Location(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                list.add(data);
            }
        } catch (SQLException ex) {
            throw new UnsupportedOperationException("khong tim thay location");
        }
        return list;
    }

    public Frame getFrame(String nameFrame) {
        Statement st;
        ResultSet rs;
        try {
            String query = "SELECT * FROM frame where TenFrame LIKE \""
                    + nameFrame + "\";";
            st = conn.createStatement();
            rs = st.executeQuery(query);
            rs.next();
            Frame frame = new Frame();
            frame.idService = rs.getInt("service_idService");
            frame.idFrame = rs.getInt("idFrame");
            frame.nameFrame = rs.getString("TenFrame");
            frame.step = rs.getInt("step");
            return frame;
        } catch (Exception e) {
            throw new UnsupportedOperationException();
        }
        
    }
    public String getNextService(String tenService)
    {
        String ret="";
        Statement st;
        ResultSet rs;
        try {
            String query = "select * from service where TenService='"+tenService+"'"; 
            st = conn.createStatement();
            rs = st.executeQuery(query);
            boolean res = rs.first();
            if(res)
            ret=rs.getString("NextService");
        } catch (Exception e) {
            throw new UnsupportedOperationException();
        }
        return ret;
    }

    public ArrayList<ComponentClient> getAllComponents(Frame frame) {
        Statement st;
        //ResultSet rs;
        ArrayList<ComponentClient> lstComponents = new ArrayList<>();
        try {
            // thêm các component cho frame
                pstm = conn.prepareStatement("select * from component where idFrame = " + frame.idFrame);
                ResultSet rs = pstm.executeQuery();
                while (rs.next()) {
                    lstComponents.add(new ComponentClient(rs.getInt(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), frame.idFrame, rs.getString(6), rs.getInt(7),
                            rs.getDouble(8), null, rs.getString(9)));
                }
                frame.lstComponent = lstComponents;
                // thêm data cho combobox nếu có.
                for (ComponentClient comp : lstComponents) {
                    if (comp.getType().equals("ComboBox")) {
                        List<comboboxdata> lComboboxdata = new ArrayList<>();
                        pstm = conn.prepareStatement("select * from comboboxdata where idcombobox = " + comp.getIdComponent());
                        rs = pstm.executeQuery();
                        while (rs.next()) {
                            lComboboxdata.add(new comboboxdata(rs.getString(1), comp.getIdComponent(), rs.getString(3)));
                        }
                        comp.setLdataCombobox(lComboboxdata);
                    }
                }
        } catch (Exception e) {
            System.out.println(e+"\n");
            throw new UnsupportedOperationException();
        }
        return lstComponents;
    }

    public Frame getFrame(int idFrameSuccess) {
        Statement st;
        ResultSet rs;
        try {
            String query = "SELECT * FROM frame where idFrame LIKE \""
                    + idFrameSuccess + "\";";
            st = conn.createStatement();
            rs = st.executeQuery(query);
            rs.next();
            Frame frame = new Frame();
//            frame.idQuyen = rs.getInt("Quyen_idQuyen");
            frame.idService = rs.getInt("service_idService");
            frame.idFrame = rs.getInt("idFrame");
            frame.nameFrame = rs.getString("TenFrame");
            frame.step = rs.getInt("step");
            return frame;
        } catch (Exception e) {
            throw new UnsupportedOperationException();
        }
    }
    
    public Service getService(int idUser, String kid, String TenProcedure) {
        Statement st;
        ResultSet rs;
        try {
            int idKiosk = Integer.parseInt(kid);
            if (idUser != -1)
            {
                String query = "select * from service where (role_idrole in (select role_idrole from role_has_user where user_idUser = "+idUser+")) "
                        + "and (idService in (select service_idService from service_has_kiosk where kiosk_idkiosk =  "+idKiosk+"))"
                        + "and (TenProcedure = '"+TenProcedure+"' or TenService = '"+TenProcedure+"')";
                st = conn.createStatement();
                rs = st.executeQuery(query);
                while (rs.next())
                {
                    return new Service(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(9), 
                            null, rs.getInt(6), rs.getBytes(7), null, rs.getString(8));
                }
            }
            else return null;
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    
    public ArrayList<Service> getListService(String user, String pass, String kid) {
        Statement st;
        ResultSet rs;
        try {
            int idUser = SelectIdUser(user,pass);
            int idKiosk = Integer.parseInt(kid);    // fixed cho 1 kiosk K1
            if (idUser != -1)
            {
//                String query = "SELECT service FROM service inner join user_has_quyen on user_has_quyen.idQuyen = quyen.idQuyen "
//                        + "where user_has_quyen.idUser = \""+idUser+"\""; 
                String query = "select * from service where not Protocol = '' and (role_idrole in (select role_idrole from role_has_user where user_idUser = "+idUser+")) "
                        + "and (idService in (select service_idService from service_has_kiosk where kiosk_idkiosk =  "+idKiosk+"))";
                st = conn.createStatement();
                rs = st.executeQuery(query);
                ArrayList<Service> returnList = new ArrayList<>();
                while (rs.next())
                {
                    returnList.add(new Service(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(9), 
                            null, rs.getInt(6), rs.getBytes(7), null, rs.getString(8)));
                }
                return returnList;
            }
            else return null;
        } catch (Exception e) {
            return null;
        }
    }
    public ArrayList<Service> getListService(byte[] mfmd, String kid) {
        Statement st;
        ResultSet rs;
        try {
            int idUser = SelectIdUser(mfmd);
            int idKiosk = Integer.parseInt(kid);    // fixed cho 1 kiosk K1
            if (idUser != -1)
            {
//                String query = "SELECT service FROM service inner join user_has_quyen on user_has_quyen.idQuyen = quyen.idQuyen "
//                        + "where user_has_quyen.idUser = \""+idUser+"\""; 
                String query = "select * from service where not Protocol = '' and (role_idrole in (select role_idrole from role_has_user where user_idUser = "+idUser+")) "
                        + "and (role_idrole in (select role_idrole from service_has_kiosk where kiosk_idkiosk =  "+idKiosk+"))";
                st = conn.createStatement();
                rs = st.executeQuery(query);
                ArrayList<Service> returnList = new ArrayList<>();
                while (rs.next())
                {
                    returnList.add(new Service(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(9), 
                            null, rs.getInt(6), rs.getBytes(7), null, rs.getString(8)));
                }
                return returnList;
            }
            else return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public ArrayList<Service> getListService(int idUser, String kid) {
        Statement st;
        ResultSet rs;
        try {
            int idKiosk = Integer.parseInt(kid);    // fixed cho 1 kiosk K1
            if (idUser != -1)
            {
//                String query = "SELECT service FROM service inner join user_has_quyen on user_has_quyen.idQuyen = quyen.idQuyen "
//                        + "where user_has_quyen.idUser = \""+idUser+"\""; 
                String query = "select * from service where (role_idrole in (select role_idrole from role_has_user where user_idUser = "+idUser+")) "
                        + "and (idService in (select service_idService from service_has_kiosk where kiosk_idkiosk =  "+idKiosk+"))";
                st = conn.createStatement();
                rs = st.executeQuery(query);
                ArrayList<Service> returnList = new ArrayList<>();
                while (rs.next())
                {
                    returnList.add(new Service(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(9), 
                            null, rs.getInt(6), rs.getBytes(7), null, rs.getString(8)));
                }
                return returnList;
            }
            else return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public ArrayList<CatMenu> genMenuService(ArrayList<Service> lServices){
        ArrayList<CatMenu> lCatMenus = new ArrayList<>();
        Map<String, ArrayList<Service>> lData = new HashMap<>();//String=TenLinhVuc
        ResultSet rs;
        for (Service service : lServices) {
            try {
                pstm = conn.prepareStatement("select TenLinhVuc from linhvuc where idLinhVuc = " + service.getIdLinhVuc());
                rs = pstm.executeQuery();
                while (rs.next()) {                    
                    String TenLinhVuc = rs.getString(1);
                    /*
                    nếu chưa có tên lv thì tạo mới và add Service
                    nếu có r thì tìm tên lv và thêm service vào list.
                    */
                    if (lData.keySet().contains(TenLinhVuc)) {
                        lData.get(TenLinhVuc).add(service);
                    }else{
                        ArrayList<Service> ls = new ArrayList<>();
                        ls.add(service);
                        lData.put(TenLinhVuc, ls);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(DataConnect.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lData.forEach((t, u) -> {
            CatMenu cat = new CatMenu();
            cat.setText(t);
            cat.setIv(convertImageToBytes());
            cat.setItems(new ArrayList<>());
            for (Service service : u) {
                ItemMenu im = new ItemMenu();
                im.setText(service.getTenService());
                if(service.getImage() != null)
                    im.setIv(service.getImage());
                else im.setIv(convertImageToBytes());
                im.setCommand("openService");
                im.setMclass("kioskgui.ServiceExecutor");
                im.setPara(new Object[]{service.getTenService()});
                im.setCommandType("openWindow");
                cat.getItems().add(im);
            }
            lCatMenus.add(cat);
        });
        return lCatMenus;
    }
    public byte[] convertImageToBytes() {
        byte[] res = null;
        try {
            BufferedImage bufferimage = null;
            try {
                bufferimage = ImageIO.read(new File(getClass().getResource("logo-dvc.png").toURI()));
            } catch (URISyntaxException ex) {
                System.err.println("ko tim thay path image");
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bufferimage, "png", output );
            res = output.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(DataConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }
    private int SelectIdKiosk(String nameKiosk) {
        try {
            String query = "SELECT idkiosk FROM kiosk Where name = \""
                    + nameKiosk + "\";";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.next();
            return rs.getInt("idkiosk");
        } catch (Exception e) {
            return -1;
        }
        
            
    }
    
      public int SelectIdUser(String user, String pass) {
        try {
            String query = "SELECT idUser FROM user Where UserName = \""
                    + user + "\" and Pass = \""+ pass +"\";";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.next();
            return rs.getInt("idUser");
        } catch (Exception e) {
            return -1;
        }
      }
        public int SelectIdUser(byte[] mfmd) {
        try {
            FingerLib fl=new FingerLib();
            fl.initFingerLib();
            return fl.identifyFmdRetID(mfmd);
        } catch (Exception e) {
            return -1;
        }
        
            
    }

//
//    public Frame getFrameTenQuyen(String tenQuyen) {
//        try {
//            String query = "select * from frame inner join quyen on frame.Quyen_idQuyen = quyen.idQuyen "
//                    + "where quyen.TenQuyen = \""+ tenQuyen +"\";";
//            
//            Statement st = conn.createStatement();
//            ResultSet rs = st.executeQuery(query);
//            rs.first();
//            Frame frame = new Frame();
//            frame.idFrame = rs.getInt("idFrame");
//            frame.nameFrame = rs.getString("TenFrame");
//            frame.protocol = rs.getString("Protocol");
//            frame.idQuyen = rs.getInt("Quyen_idQuyen");
//            frame.step = rs.getInt("step");
//            return frame;
//        } catch (Exception e) {
//            throw new UnsupportedOperationException("loi getFrameTenQuyen");
//        }
//    }

    public ArrayList<Frame> getListFrame(int idService) {
        Statement st;
        ResultSet rs;
        try {
            String query = "Select * from frame where service_idService = \""+idService+"\"order by step asc"; 
            st = conn.createStatement();
            rs = st.executeQuery(query);
            ArrayList<Frame> returnList = new ArrayList<>();
            while (rs.next())
            {
                Frame frame = new Frame();
                frame.idFrame = rs.getInt("idFrame");
                frame.nameFrame = rs.getString("TenFrame");
                frame.idService = rs.getInt("service_idService");
                frame.step = rs.getInt("step");
                returnList.add(frame);
            }
            
            return returnList;
            
           
        } catch (Exception e) {
            throw new UnsupportedOperationException();
        }
    }

    public ArrayList<Frame> getListFrame(String ServiceName, String idUser, String idKiosk) {
        Statement st;
        ResultSet rs;
        ArrayList<Frame> returnList = new ArrayList<>();
        try {
            String query = "select * from frame where service_idService in (select idService from service "
                    + "where TenService='"+ServiceName+"' and role_idrole in (select role_idrole from role_has_user where user_idUser = "+idUser+") "
                    + "and (idService in (select service_idService from service_has_kiosk where kiosk_idkiosk =  "+idKiosk+"))) "
                    + "order by step asc"; 
            String query2 = "select Protocol from service where TenService = '"+ServiceName+"'";
            String query3 = "select * from frame where service_idService in "
                        + "(select idService from service where TenService = '"+ServiceName+"')";
            if (idUser.equals("0"))
                query = "select * from frame where service_idService in (select idService from service "
                    + "where TenService='"+ServiceName+"' and role_idrole = 1 "
                    + "and (idService in (select service_idService from service_has_kiosk where kiosk_idkiosk =  "+idKiosk+"))) "
                    + "order by step asc";
            st = conn.createStatement();
            rs = st.executeQuery(query2);
            rs.next();
            boolean o = rs.getString(1).equals("Kiosk");
            rs.last();
            if (rs.getRow() == 0) return returnList;
            if (rs.getRow() == 1) {
                if (o) rs = st.executeQuery(query3);
                else rs = st.executeQuery(query);
            }
            if (rs.getRow() > 1) {
                rs = st.executeQuery(query);
            }
            while (rs.next())
            {
                Frame frame = new Frame();
                frame.idFrame = rs.getInt("idFrame");
                frame.nameFrame = rs.getString("TenFrame");
                frame.idService = rs.getInt("service_idService");
                frame.step = rs.getInt("step");
                returnList.add(frame);
            }
            return returnList;
        } catch (Exception e) {
            System.out.println(e);
            //throw new UnsupportedOperationException();
            return null;
        }
    }
    
    public String getProtocol(int idService){
        Statement st;
        ResultSet rs;
        String protocol = "";
        try {
            String query = "select Protocol from service where idService = " + idService;
            st = conn.createStatement();
            rs = st.executeQuery(query);  
            while (rs.next()) {                
                protocol = rs.getString(1);
            }
        } catch (Exception ex){
        
        }
        return protocol;
    }
    
    public String getLinkFrame(int idService) {
        try {
            String query = "select * from service where idService = \""+ idService +"\";";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.first();
            return rs.getString("linkWeb");
        } catch (Exception e) {
            throw new UnsupportedOperationException("loi sql");
        }
    }

    public Frame getNextFrame(Frame frame) {
        try {
            String query = "SELECT * FROM frame where "
                    + "Quyen_idQuyen = \""+ frame.idService+"\" and step = \""+ (frame.step + 1)+"\";";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.first();
            Frame nextFrame = new Frame();
            nextFrame.idFrame = rs.getInt("idFrame");
            nextFrame.nameFrame = rs.getString("TenFrame");
            nextFrame.idService = rs.getInt("service_idService");
            nextFrame.step = rs.getInt("step");
            return nextFrame;
        } catch (Exception e) {
            throw new UnsupportedOperationException("loi sql");
        }
    }

    public ClientKiosk setClient(String tenService,ClientKiosk client) {
        try {
            String query = "select * from service"
                    + " where TenService = \""+ tenService +"\";";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.first();
            client.urlService = rs.getString("linkWeb");
            return client;
        } catch (Exception e) {
            throw new UnsupportedOperationException("loi getFrameTenQuyen");
        }
    }

    public ArrayList<ButtonClient> getAllButtons(Frame currFrame) {
        Statement st;
        //ResultSet rs;
        ArrayList<ButtonClient> lstButtonClients = new ArrayList<>();
        try {
            // thêm các button cho frame
                pstm = conn.prepareStatement("select * from button where idFrame = " + currFrame.idFrame);
                 ResultSet rs = pstm.executeQuery();
                while (rs.next()) {
                    lstButtonClients.add(new ButtonClient(rs.getInt(1), rs.getString(2), rs.getString(3), currFrame.idFrame,
                            rs.getString(5), rs.getInt(6), rs.getString(7), rs.getInt(8), rs.getDouble(9)));
                }
                currFrame.lstButton = lstButtonClients;
                //
                for (ButtonClient btn : lstButtonClients) {
                    btn.setFunction(getfunction(btn.getIdFunction()));
                }
        } catch (Exception e) {
            throw new UnsupportedOperationException();
        }
        return lstButtonClients;
    }

    public function getfunction(int idFunction) {
        ResultSet rs;
        try {
            pstm = conn.prepareStatement("select * from function where idFunction = " + idFunction);
            rs = pstm.executeQuery();
            while (rs.next()) {
                return new function(rs.getInt(1), rs.getString(2));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DataConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public ClientKiosk getKiosk(String ipAddress) {
        
        try {
            String query = "select * from kiosk"
                    + " where ipAddress = '"+ ipAddress +"';";
            
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            rs.first();
            return new ClientKiosk(null, null, "", null, rs.getInt("idkiosk"), rs.getString("name"), rs.getString("latitude"), rs.getString("longitude"), ipAddress, rs.getString("status"));
            
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<Frame> getListFrame() {
        Statement st;
        ResultSet rs;
        try {
            String query = "Select * from frame where service_idService = \""+1+"\"order by step asc"; 
            st = conn.createStatement();
            rs = st.executeQuery(query);
            ArrayList<Frame> returnList = new ArrayList<>();
            while (rs.next())
            {
                Frame frame = new Frame();
                frame.idFrame = rs.getInt("idFrame");
                frame.nameFrame = rs.getString("TenFrame");
                frame.idService = rs.getInt("service_idService");
                frame.step = rs.getInt("step");
                returnList.add(frame);
            }
            
            return returnList;
            
           
        } catch (Exception e) {
            throw new UnsupportedOperationException();
        }
    }
    
    public String verifyID(int idUser){
        String username = "";
        try {
            String query = "SELECT * FROM user Where idUser = "+idUser;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            if(rs.next())
            return rs.getString(2);
        } catch (Exception e) {
            return null;
        }
        return null;
    }

}
