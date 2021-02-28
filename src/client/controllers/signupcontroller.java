package client.controllers;

import ObjectFiles.signupclass;
import client.login;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class signupcontroller
{
    public Socket soc;
    public TextField username;
    public TextField password;
    public TextField ServerIP;
    public TextField PortNo;
    public Button Signup;
    public Stage window;

    public void Sign(ActionEvent actionEvent) throws Exception {
        Stage window = (Stage) password.getScene().getWindow();
        soc = new Socket(ServerIP.getText(), Integer.parseInt(PortNo.getText()));
        System.out.println("Connected to server");
        signupclass data = new signupclass(username.getText(),password.getText());
        ObjectOutputStream oos = new ObjectOutputStream(soc.getOutputStream());
        oos.writeObject(data);
        oos.flush();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = "jdbc:mysql://127.0.0.1:3306/app";
        try
        {
            Connection connection = DriverManager.getConnection(url, "root", "root");
            String q="CREATE TABLE `Local"+username.getText()+"Chats` " +
                    "(\n" +
                    "  `Sender` varchar(15) NOT NULL,\n" +
                    "  `Receiver` varchar(15) NOT NULL,\n" +
                    "  `Message` text NOT NULL,\n" +
                    "  `SentTime` timestamp NULL DEFAULT '2021-01-01 00:00:00',\n" +
                    "  `ReceivedTime` timestamp NULL DEFAULT '2021-01-01 00:00:00',\n" +
                    "  `SeenTime` timestamp NULL DEFAULT '2021-01-01 00:00:00'\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
            PreparedStatement ps=connection.prepareStatement(q);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        login lw = new login();
        lw.start(window);
    }
}
