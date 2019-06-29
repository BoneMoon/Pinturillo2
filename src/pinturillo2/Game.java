/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinturillo2;

import java.util.Timer;
import java.util.TimerTask;
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
    
    public void setColor(Color color) {
        this.color = color;
    }
     
    public Game(FXMLDocumentController controller) {
        this.ctl = controller;
    }
    
    public void setOnMousePressed(MouseEvent e) {
        if(!myTurn || !gameHasStarted) return;
        isDrawing = true;
        this.x0 = e.getX();
        this.y0 = e.getY();
    }
    
    public void setOnMouseDragged(MouseEvent e) {
        if(!myTurn || !gameHasStarted) return;
        if(!isDrawing) return;
        drawLine(x0, y0, e.getX(), e.getY(), color, this.ctl.slide.getValue(), true);
        this.x0 = e.getX();
        this.y0 = e.getY();
    }
    
    public void onMouseReleased(MouseEvent e) {
        if(!myTurn || !gameHasStarted) return;
        if (!isDrawing) return;
        isDrawing = true;
        drawLine(x0, y0, e.getX(), e.getY(), color, this.ctl.slide.getValue(), true);
    }
    
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

    
}
