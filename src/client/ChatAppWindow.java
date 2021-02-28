package client;

import client.ChatThread.ClientReceiver;
import client.controllers.ChatController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

public class ChatAppWindow extends Application
{
    public ObjectOutputStream oos;
    public ObjectInputStream ois;
    public Socket socket;
    public String username;
    public Stage window;
    public FXMLLoader loader;
    public ChatController controller;
    public AnchorPane display;
    public Connection connection;
    public ClientReceiver reciever;
    @Override
    public void start(Stage primaryStage)  throws IOException
    {
        loader = new FXMLLoader(getClass().getResource("FXMLFiles/chat.fxml"));
        display = loader.load();
        controller = loader.getController();
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        String url = "jdbc:mysql://127.0.0.1:3306/app";
        try
        {
            connection = DriverManager.getConnection(url,"root","root");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        url = "https://wallpaperaccess.com/full/1261663.jpg";
        BackgroundImage myBI= new BackgroundImage(new Image(url,800,600,false,true), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        display.setBackground(new Background(myBI));
        reciever = new ClientReceiver();
        reciever.ois=ois;//inputstream of client
        reciever.oos = oos;
        reciever.controller=controller;
        reciever.connection=connection;
        reciever.username=username;
        Thread t = new Thread(reciever);

        controller.ois = ois;
        controller.oos = oos;
        controller.socket = socket;
        controller.connection = connection;
        controller.username = username;
        controller.window = primaryStage;
        controller.currentStage = primaryStage;
        controller.WindowPane = display;
        controller.FriendStatus = new ArrayList<>();
        controller.refresh();
        t.start();

        window = primaryStage;
        window.setScene(new Scene(display,1200,600));
        window.setTitle(username + "'s " + "Chat Window");
        window.show();
    }
}
