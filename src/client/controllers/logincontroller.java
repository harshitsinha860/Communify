package client.controllers;

import ObjectFiles.authentication;
import ObjectFiles.user;
import client.ChatAppWindow;
import client.signup;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class logincontroller
{
    public Stage window;
    public TextField ServerIP;
    public TextField PortNo;
    public Socket socket;
    public TextField name;
    public TextField pass;
    public Button userLogin;
    public void Login(ActionEvent actionEvent) throws Exception
    {
        window = (Stage)name.getScene().getWindow();
        socket = new Socket(ServerIP.getText(),Integer.parseInt(PortNo.getText()));
        System.out.println("Connected to server");
        user data = new user(name.getText(),pass.getText());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(data);
        oos.flush();
        System.out.println("Waiting for Approval!");
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        try
        {
            System.out.println("Authentication");
            Object temp=ois.readObject();
            authentication a = (authentication) temp;
            if(a.auth)
            {
                ChatAppWindow ChattingWindow = new ChatAppWindow();
                ChattingWindow.oos=oos;
                ChattingWindow.ois=ois;
                ChattingWindow.socket=socket;
                ChattingWindow.username=name.getText();
                ChattingWindow.start(window);
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Authentication Failed");
                alert.setHeaderText("Please Check Your Login Credentials");
                alert.setContentText(a.Error);
                alert.show();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public void signUp(ActionEvent actionEvent)
    {
        try
        {
            signup signup = new signup();
            signup.start(window);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
