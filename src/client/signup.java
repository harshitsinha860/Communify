package client;
import client.controllers.signupcontroller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class signup extends Application
{
    Stage window;
    signupcontroller controller;
    FXMLLoader loader;
    AnchorPane Display;
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        loader = new FXMLLoader(getClass().getResource("FXMLFiles/signup.fxml"));
        Display = loader.load();
        controller = loader.getController();
        controller.window = primaryStage;
        window = primaryStage;
        window.setScene(new Scene(Display,800,600));
        window.setTitle("Signup page");
        window.show();
    }
}