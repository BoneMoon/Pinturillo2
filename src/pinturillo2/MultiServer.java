/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinturillo2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author josed
 */
public class MultiServer {
    int pontuacao = 0;
    static Vector<ClientHandler> ar = new Vector<>();
    static int i = 1;
    
    Timer timer1 = new Timer();
    Timer timer2 = new Timer();

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(1234);
        Socket s;
        System.out.println("à espera de conexões...");
        while (true) {

            s = ss.accept();
            System.out.println("Jogador" + i + "Conectou:" + s);
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            ClientHandler mtch = new ClientHandler(s, "Jogador" + " " + i, dis, dos);

            Thread t = new Thread(mtch);
            
            ar.add(mtch);

            t.start();
            i++;
        }
    }

    private static class ClientHandler implements Runnable {

        final DataInputStream dis;
        final DataOutputStream dos;
        final Socket s;
        private String nome;
        boolean isLoggedin;

        private ClientHandler(Socket s, String string, DataInputStream dis, DataOutputStream dos) {
            this.s = s;
            this.dis = dis;
            this.dos = dos;
            this.nome = string;
            this.isLoggedin = true;
        }

        @Override
        public void run() {
            for (int a = 0; a < ar.size(); a++) {
                if (!ar.get(a).nome.equalsIgnoreCase(this.nome)) {
                    try {
                        ar.get(a).dos.writeUTF("new-cl: " + this.nome);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(MultiServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    this.dos.writeUTF("new-cl: " + ar.get(a).nome);
                } catch (IOException ex) {
                    Logger.getLogger(MultiServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            String recebido;
            while (true) {
                try {
                    recebido = this.dis.readUTF();
                    System.out.println(recebido);
                    if (recebido.equals("logout")) {
                        this.isLoggedin = false;
                        this.s.close();
                        ar.remove(this);
                        for (ClientHandler mc : ar) {
                            mc.dos.writeUTF(this.nome + " desconectou-se.");
                        }
                        System.out.println(this.nome + " desconectou-se.");
                        break;
                    }

                    for (ClientHandler mc : ar) {
                        mc.dos.writeUTF(this.nome + " : " + recebido);
                    }
                } catch (IOException e) {
                }
            }
            try {
                this.dis.close();
                this.dos.close();
            } catch (IOException e) {
            }
        }
    }
    
}
