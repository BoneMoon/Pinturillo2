/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinturillo2;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 *
 * @author kevin
 */
public class Cliente extends Thread {
    InetAddress ip;
    Socket s;
    DataInputStream dis;
    DataOutputStream dos;
    Consumer<String> onNewFromServer;

    public Thread sendMessage = new Thread(new Runnable(){
        @Override
        public void run() {
            while(true){
                String msg = Le.umaString();
                try {
                    dos.writeUTF("gess" + ":" + msg);
                    dos.flush();
                } catch (IOException e) {
                }
            }
        }
        
    });

    public Thread readMessage = new Thread(new Runnable(){
        @Override
        public void run() {
            while(true){
                try {
                    String recebido = dis.readUTF();
                    onNewFromServer.accept(recebido);
                } catch (Exception e) {
                }      
            }
        }           
    });
    
    public Cliente(Consumer<String> onNewFromServer) throws UnknownHostException, IOException {
        this.ip = InetAddress.getByName("localhost");
        this.s = new Socket(ip, 1234);
        this.dis = new DataInputStream(s.getInputStream());
        this.dos = new DataOutputStream(s.getOutputStream());
        this.onNewFromServer = onNewFromServer;
        
        this.sendMessage.setDaemon(true);
        this.readMessage.setDaemon(true);
        this.sendMessage.start();
        this.readMessage.start();
    }
    
    public void send(String data) {
        try {
            dos.writeUTF(data);
            dos.flush();
        } catch (IOException ex) {
        }
    }
    
}
