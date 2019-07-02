/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinturillo2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author josed
 */
public class MultiServer {
    HashMap<String, Integer> pontuacoes = new HashMap<>();
    HashMap<String, Integer> timesPlayed = new HashMap<>();
    String palavraSelecionada = null;
    ClientHandler nowPlaying = null;
    CopyOnWriteArrayList<ClientHandler> ar = new CopyOnWriteArrayList<>();
    static int i = 1;
    
    String[] palavras = {"Aniverário", "Suécia", "Coroa", "Holanda", "Ovelha", "Serpente", "Melancia", "Ásia", "Acampamento", "Voleibol", "Árvore", "Circo", "Ramo", 
        "Natal", "Leopardo", "Talheres", "Barril", "Biscoito", "Arco-íris", "Osso", "Computador", "Ninho", "Chama", "Volante", "Cereja"};
    
    Timer timer1 = new Timer();
    TimerTask corrTimerTask = this.createNewTimerTask();

public MultiServer() throws IOException {
    
        
    ServerSocket ss = new ServerSocket(1234);
    Socket s;
    System.out.println("à espera de conexões...");
    while (true) {

        s = ss.accept();
            
        if(ar.size()>= 2) continue;
            System.out.println("Jogador" + i + "Conectou:" + s);
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

        ClientHandler mtch = new ClientHandler(this, s, "Jogador" + " " + i , dis, dos);

        Thread t = new Thread(mtch);
        t.setDaemon(true);
        ar.add(mtch);
            
        t.start();
        i++;
            
        this.pontuacoes.put(mtch.nome, 0);
        this.timesPlayed.put(mtch.nome, 0);
        if(ar.size() == 2) this.starGame(); 
        }
    }

 public static void main(String[] args) throws IOException {
        MultiServer s = new MultiServer();
    }
 
  private void starGame() {
        String nextWord = this.getRandomWord();
        this.palavraSelecionada = nextWord;
        
        ClientHandler ch = this.getNextPlayer();
        this.nowPlaying = ch;
        this.nowPlaying.send("turn:" + this.palavraSelecionada);
        
        if (this.corrTimerTask == null) {
            this.corrTimerTask = this.createNewTimerTask();
        } else {
            this.corrTimerTask.cancel();
            this.corrTimerTask = this.createNewTimerTask();
        }
        
        this.timer1.schedule(corrTimerTask, 20 * 1000);
        ar.forEach(cl -> cl.send("startgame"));
    }
  
  //PÕE CÓDIGO AQUI

    private class ClientHandler implements Runnable {

                final DataInputStream dis;
        final DataOutputStream dos;
        final Socket s;
        private String nome;
        boolean isLoggedin;
        MultiServer server;

        private ClientHandler(MultiServer server, Socket s, String string, DataInputStream dis, DataOutputStream dos) {
            this.s = s;
            this.dis = dis;
            this.dos = dos;
            this.nome = string;
            this.isLoggedin = true;
            this.server = server;
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
