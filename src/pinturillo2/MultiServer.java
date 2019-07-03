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
import java.util.concurrent.ThreadLocalRandom;
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
  
  
    private ClientHandler getNextPlayer() {
        if(nowPlaying == null) {
            for (ClientHandler clientHandler : ar) {
                if (clientHandler.nome.equalsIgnoreCase("Jogador 1")) return clientHandler;
            }
        }
        
        if(nowPlaying.nome.equalsIgnoreCase("Jogador 1")){
            for (ClientHandler clientHandler : ar) {
                if (clientHandler.nome.equalsIgnoreCase("Jogador 2")) return clientHandler;
            }
        }
        
        if(nowPlaying.nome.equalsIgnoreCase("Jogador 2")){
            for (ClientHandler clientHandler : ar) {
                if (clientHandler.nome.equalsIgnoreCase("Jogador 1")) return clientHandler;
            }
        }
        
        return null;
        
    }
    
    private String getRandomWord() {
        return palavras[ThreadLocalRandom.current().nextInt(0, palavras.length)];
    }

    private TimerTask createNewTimerTask() {
        return new TimerTask() {
        @Override
            public void run() {
                ar.forEach(cl -> cl.send("no_gess_winner"));
                timesPlayed.put(nowPlaying.nome, timesPlayed.get(nowPlaying.nome) + 1);
                maybeStartNewRound();
            }
        };
    }
    
        private void maybeStartNewRound() {
        boolean maybeNextRound = false;
        for (Map.Entry<String, Integer> entry : timesPlayed.entrySet()) {
            if(entry.getValue() < 3){
                maybeNextRound = true;
                break;
            } 
        }
        
        if(!maybeNextRound){
            ar.forEach(cl -> cl.send("end_game:" + this.pontuacoes.get("Jogador 1") + ":" + this.pontuacoes.get("Jogador 2")));
            return;
        }
        
        ar.forEach(cl -> cl.send("new_round" + ":" + pontuacoes.get("Jogador 1") + ":" + pontuacoes.get("Jogador 2")));
        starGame();
        
    }

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
            for (int a = 0; a < this.server.ar.size(); a++) {
                if (!this.server.ar.get(a).nome.equalsIgnoreCase(this.nome)) {
                    try {
                        this.server.ar.get(a).dos.writeUTF("new-cl: " + this.nome);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(MultiServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    this.dos.writeUTF("new-cl: " + this.server.ar.get(a).nome);
                } catch (IOException ex) {
                    Logger.getLogger(MultiServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
            while (true) {
                try {
                    String recebido = this.dis.readUTF();
                    this.handleIncomming(recebido);
                    
                } catch (IOException e) {
                }
            }
        }
         
        public void send(String dados) {
            try {
                this.dos.writeUTF(dados);
                this.dos.flush();
            } catch (IOException ex) {
                Logger.getLogger(MultiServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void handleIncomming(String recebido) {
            String[] splitedData = recebido.split(":");
            
            if (splitedData[0].equalsIgnoreCase("drawing")) {
                ar
                    .stream()
                    .filter(cl -> !cl.equals(this))
                    .forEach(cl -> cl.send(recebido));
            }
            
            if (splitedData[0].equalsIgnoreCase("gess")) {
                String gess = splitedData[1];
                
                if (!palavraSelecionada.equalsIgnoreCase(gess)) return;
                
                corrTimerTask.cancel();
                pontuacoes.put(this.nome, pontuacoes.get(this.nome) + 10);
                pontuacoes.put(nowPlaying.nome, pontuacoes.get(nowPlaying.nome) + 5);
                timesPlayed.put(nowPlaying.nome, timesPlayed.get(nowPlaying.nome) + 1);
                maybeStartNewRound();
            }
        }
    }
    
    
}
