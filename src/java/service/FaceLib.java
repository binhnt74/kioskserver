/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.sql.Array;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author kiosk
 */
public class FaceLib {

    public Socket socket = null;
    private DataOutputStream dos = null;
    private DataInputStream din = null;
    private final String HOST = "localhost";
    private final int PORT = 8886;
    private ArrayList<Integer> lengImgs = null;
    private ArrayList<BufferedImage> bufferedImages = null;
    public String result = "";
    boolean isDetect = true;
    
    public FaceLib() {
        try {
            clientConnect(HOST, PORT);
        } catch (IOException ex) {
            Logger.getLogger(FaceLib.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void clientConnect(String address, int port) throws IOException {
        socket = new Socket(address, port);
        din = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        System.out.println("client connect");
    }

    public void closeConect() {
        try {
            din.close();
            dos.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(FaceLib.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String receiveData() {
        String data = null;
        try {
            byte[] bytes = new byte[1024];
            din.read(bytes);
            data = new String(bytes);
        } catch (IOException ex) {
            Logger.getLogger(FaceLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public void sendCommand(String cmd, String name, ArrayList<Integer> lengByteOfImage) {
        try {
            String command = cmd + "|" + String.valueOf(lengByteOfImage.size()) + "|" + name;
            for (int leng : lengByteOfImage) {
                command += "|" + String.valueOf(leng);
            }
            command += "END";
            dos.write(command.getBytes());
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(FaceLib.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void sendData(ArrayList<BufferedImage> buffImages) {
        try {
            for (BufferedImage buffImage : buffImages) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(buffImage, "jpg", baos);
                dos.write(baos.toByteArray());
                dos.flush();
            }
            String endData = "END";
            dos.write(endData.getBytes());
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(FaceLib.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String predict(byte[] img) {
        String idUser = "-1";
        ArrayList<Integer> imgLength = new ArrayList<>();
        imgLength.add(img.length);
        sendCommand("predict", "null", imgLength);
        String replyServer = receiveData();
        System.out.println(replyServer);
        ArrayList<BufferedImage> listBuff = new ArrayList<>();
        listBuff.add(convertFromByteToImage(img));
        sendData(listBuff);
        result = receiveData().trim();
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(FaceLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        idUser = result;
        System.out.println(result);
        return idUser;
    }
    
    private BufferedImage convertFromByteToImage(byte [] b){
        BufferedImage img = null;
        if (b != null) {
            InputStream in = new ByteArrayInputStream(b);
            try {
                img = ImageIO.read(in);
            } catch (IOException ex) {
                Logger.getLogger(FaceLib.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return img;
    }

}
