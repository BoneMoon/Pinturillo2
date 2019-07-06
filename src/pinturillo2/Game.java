/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinturillo2;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 *
 * @author josed
 */
public class Game {
    boolean myTurn = false;
    boolean gameHasStarted = false;
    double x0, y0;
    Color color = Color.BLACK;
    double lineWidth = 1;
    boolean isDrawing = false;
    FXMLDocumentController ctl;
    Timer t = new Timer();
    TimerTask currTimerTask = null;
    volatile int currTime = 0;
    
    /**
     * Set valor da cor
     *
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }
     
    /**
     * 
     * @param controller 
     */
    public Game(FXMLDocumentController controller) {
        this.ctl = controller;
    }
    
    /**
     * Quando o rato esta a ser pressionado
     *
     * @param e
     */
    public void setOnMousePressed(MouseEvent e) {
        if(!myTurn || !gameHasStarted) return;
        isDrawing = true;
        this.x0 = e.getX();
        this.y0 = e.getY();
    }
    
    /**
     * Quando o rato esta pressionado e esta a ser arrastado
     *
     * @param e
     */
    public void setOnMouseDragged(MouseEvent e) {
        if(!myTurn || !gameHasStarted) return;
        if(!isDrawing) return;
        drawLine(x0, y0, e.getX(), e.getY(), color, this.ctl.slide.getValue(), true);
        this.x0 = e.getX();
        this.y0 = e.getY();
    }
    
    /**
     * Quando o rato não esta a ser pressionado
     *
     * @param e
     */
    public void onMouseReleased(MouseEvent e) {
        if(!myTurn || !gameHasStarted) return;
        if (!isDrawing) return;
        isDrawing = true;
        drawLine(x0, y0, e.getX(), e.getY(), color, this.ctl.slide.getValue(), true);
    }
    
    /**
     * 
     * Desenha uma linha no canvas
     *
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param color
     * @param lw
     * @param emmit
     */
    public void drawLine(double x0, double y0, double x1, double y1, Color color, double lw, boolean emmit){
        this.ctl.gc.beginPath();
        this.ctl.gc.moveTo(x0, y0);
        this.ctl.gc.lineTo(x1, y1);
        this.ctl.gc.setStroke(color);
        this.ctl.gc.setLineWidth(lw);
        this.ctl.gc.stroke();
        this.ctl.gc.closePath();
        
        if (!emmit) return;
        
        double w = this.ctl.canvas.getWidth();
        double h = this.ctl.canvas.getHeight();
        
        try {
            this.ctl.serverCon.send("drawing" + ":" + (x0 / w) + ":" + (y0 / h) + ":" + (x1 / w) + ":" + (y1 / h) + ":" + color.toString() + ":" + lw);
        } catch (Exception e) {
            
        }
    }
    
    /**
     * String que recebe do servidor
     *
     * @param s
     */
    public void onNewFromServer(String s) {
        String[] splitedData = s.split(":");
        
        if (splitedData[0].equalsIgnoreCase("turn")) {
            this.myTurn = true;
            System.out.println("A palavra a desenhar é: " + splitedData[1]);
        }
        
        if(splitedData[0].equalsIgnoreCase("end_game")){
            this.currTimerTask.cancel();
            System.out.println("O JOGO ACABOU!");
            System.out.println("Jogador 1: " + splitedData[1]);
            System.out.println("Jogador 2: " + splitedData[2]);
            
            int pj1 = Integer.parseInt(splitedData[1]);
            int pj2 = Integer.parseInt(splitedData[2]);
            
            if (pj1 > pj2)  {
                System.out.println("Jogador 1 WINS");
            } else if (pj2 > pj1) {
                System.out.println("Jogador 2 WINS");
            } else {
                System.out.println("Empate");
            }
        }
        
        
        if (splitedData[0].equalsIgnoreCase("startgame")) {
            System.out.println("Jogo começou");
            gameHasStarted = true;
            
            if (myTurn) {
                this.ctl.indicator_drawer_lbl.setText("Artista");
            } else {
                this.ctl.indicator_drawer_lbl.setText("Bidente");
            }
            
            if (this.currTimerTask != null) {
                this.currTimerTask.cancel();
            }
            this.currTime = 20;
            this.currTimerTask = createNewTimeTimerTask();
            t.scheduleAtFixedRate(currTimerTask, 0, 1000);
        }
        
        if (splitedData[0].equalsIgnoreCase("new_round")) {
            System.out.println("new round");
            myTurn = false;
            gameHasStarted = false;
            this.ctl.gc.clearRect(0, 0, this.ctl.canvas.getWidth(), this.ctl.canvas.getHeight());
        }
        
        if (splitedData[0].equalsIgnoreCase("no_gess_winner")) {
            System.out.println("ninguem acertou.");
        }
        
        if (splitedData[0].equalsIgnoreCase("drawing")) {
            double w = this.ctl.canvas.getWidth();
            double h = this.ctl.canvas.getHeight();
        
            drawLine(Double.parseDouble(splitedData[1]) * w, Double.parseDouble(splitedData[2]) * h, Double.parseDouble(splitedData[3]) * w, Double.parseDouble(splitedData[4]) * h, Color.web(splitedData[5]), Double.parseDouble(splitedData[6]), false);
        }
    }
    
        private TimerTask createNewTimeTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    ctl.remain_time_lbl.setText("" + currTime);
                    currTime -= 1;
                });
            }
        };
    }
}
