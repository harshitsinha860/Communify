package client;

import client.controllers.logincontroller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class login extends Application {
    Stage window;
    FXMLLoader loader;
    logincontroller controller;
    AnchorPane MainDisplay;
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        loader = new FXMLLoader(getClass().getResource("FXMLFiles/login.fxml"));
        MainDisplay = loader.load();
        controller = loader.getController();
        controller.window = primaryStage;
        String url = "https://wallpaperaccess.com/full/1261663.jpg";
        BackgroundImage myBI= new BackgroundImage(new Image(url,800,600,false,true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT);
        MainDisplay.setBackground(new Background(myBI));
        window = primaryStage;
        window.setScene(new Scene(MainDisplay,800,600));
        window.setTitle("Welcome | Login Page");
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
