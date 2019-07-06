/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinturillo2;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author josed
 */
public class FXMLDocumentController implements Initializable {
    
    private Game game;
    
    /**
     *
     */
    public Cliente serverCon;
    
    Set<String> listClientsString = new HashSet<String>();
    
    /**
     * label que vai fazer dizer quem é a desenhar
     *
     */
    @FXML
    public Label indicator_drawer_lbl;
    
    /**
     *Lista View
     */
    @FXML
    public ListView<String> clienteList;
    
    /**
     * Canvas onde vamos desenhar
     */
    @FXML
    public Canvas canvas;
    
    GraphicsContext gc;
    
    /**
     *Slider para mudarmos o stroke
     */
    @FXML
    public Slider slide;
    
    /**
     *Color Pickeer para mudarmos de cor
     */
    @FXML
    public ColorPicker cp;
    
    /**
     * Botão para limpar o canvas 
     *
     */
    @FXML
    public Button btlimpa;
    
    /**
     * Label que vai fazer a contagem decrescente do timer
     *
     */
    @FXML
    public Label remain_time_lbl;
    
   @FXML
    private void onSliderDragged (){
        double value = slide.getValue();
        String str = String.format("%.1f", value);
        this.game.lineWidth = value;
    }

     
    @FXML
    private void onCanvasMouseDragged(MouseEvent event) {
        this.game.setOnMouseDragged(event);
    }
    
    @FXML
    private void onCanvasMousePressed(MouseEvent event) {
        this.game.setOnMousePressed(event);
    }
    
    @FXML
    private void setOnAction(){
       game.setColor(cp.getValue());       
    }
    
    @FXML
    private void setOnMouseClicked(){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            gc = canvas.getGraphicsContext2D();
            this.game = new Game(this);
            
            this.serverCon = new Cliente(s -> {
                Platform.runLater(() -> {
                    this.onNewFromServer(s);
                    this.game.onNewFromServer(s);
                });
            });
            this.serverCon.setDaemon(true);
            this.serverCon.start();
        } catch (IOException ex) {
        }
        
        this.canvas.setOnMouseReleased((e) -> this.game.onMouseReleased(e));
        
    }
    
    /**
     * String que recebe do servidor
     *
     * @param s String
     */
    public void onNewFromServer(String s) {

        String[] splited = s.split(":");
        
        if (splited.length == 2 && splited[0].equalsIgnoreCase("new-cl")) {
            if (!this.listClientsString.contains(splited[1].trim()))this.listClientsString.add(splited[1].trim());
            this.clienteList.getItems().clear();
            this.clienteList.getItems().addAll(this.listClientsString);
        }
        
        if (splited[0].equalsIgnoreCase("new_round")){
            int pj1 = Integer.parseInt(splited[1]);
            int pj2 = Integer.parseInt(splited[2]);
            
            this.listClientsString.clear();
            this.listClientsString.add("Jogador 1 " + pj1);
            this.listClientsString.add("Jogador 2 " + pj2);
            this.clienteList.getItems().clear();
            this.clienteList.getItems().addAll(this.listClientsString);
        }
    } 
    
}
