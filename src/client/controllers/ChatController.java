package client.controllers;

import ObjectFiles.CallRequest;
import ObjectFiles.Message;
import ObjectFiles.Status;
import ObjectFiles.SystemMessage;
import client.ChatThread.ClientReceiver;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.*;
import java.util.*;

public class ChatController
{
    public Stage currentStage;
    public ObjectOutputStream oos;
    public ObjectInputStream ois;
    public Socket socket;
    public String username;
    public Stage window;
    public ClientReceiver reciever;
    public Connection connection;
    public AnchorPane WindowPane;
    public GridPane gridPane;
    public ScrollPane scrollPane;
    public VBox VerticalPane;
    public HBox lowerHBox;
    public Button Send;
    public TextArea textBox;
    public Label currentUser; // user with whom we are chatting
    public Label currentUserStatus;
    public Button Logout;
    public Button refresh;
    public VBox AllChats;
    public Button AddFriend;
    public ArrayList<Message> chats;
    public ArrayList<String> friends;
    public ArrayList<Status> FriendStatus;

    public void addChat(String username)
    {
        for(int i = 0; i < friends.size(); i++)
        {
            if(friends.get(i).equals(username))
                return;
        }
        Button name = new Button(username);
        AllChats.getChildren().add(name);
        friends.add(username);
        name.setOnMouseClicked(e -> {
            seenMessagesof(username);
        });
    }
    public void seenMessagesof(String friend)
    {
        Timestamp seenTime = new Timestamp(System.currentTimeMillis());
        //fetchChatsFriend(friend);
        //System.out.println("displaying" + friend + "chats");
        String q ="UPDATE Local"+username+"Chats SET SeenTime = '"+seenTime.toString()+"' WHERE SeenTime = '2021-01-01 00:00:00' AND Sender ='"+friend+"'";
        try
        {
            PreparedStatement ps = connection.prepareStatement(q);
            ps.executeUpdate();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        for(int i = 0; i < chats.size(); i++)
        {
            //System.out.println(chats.get(i).getContent() + " " + chats.get(i).getSeenTime());
            if((chats.get(i).getTo().equals(friend) || chats.get(i).getFrom().equals(friend)) && (chats.get(i).getSeenTime() == null))
            {
                System.out.println("changed the value");
                chats.get(i).setSeenTime(seenTime);
            }
        }
        try
        {
            display(friend);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        SystemMessage sm = new SystemMessage(friend,2,seenTime);
        try {
            oos.writeObject(sm);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Displays Current Chats of user with username person displayed
    public void display(String username) throws IOException
    {
        //System.out.println(chats.size());
        VerticalPane.getChildren().clear();
        for(int i = 0; i < chats.size(); i++)
        {
            if(chats.get(i).getFrom().equals(username) || chats.get(i).getTo().equals(username))
            {
                addMessageToDisplay(chats.get(i));
            }
        }
        for(int i = 0; i < FriendStatus.size(); i++)
        {
            if(FriendStatus.get(i).getUser().equals(username))
            {
                if(FriendStatus.get(i).getValid()==1)
                {
                    currentUserStatus.setText("Online");
                }
                else
                {
                    currentUserStatus.setText("Last Seen "+FriendStatus.get(i).getTime());
                }
                break;
            }
        }
        currentUser.setText(username);
    }
    public void fetchAllChats()
    {
        if(chats != null)
            chats.clear();
        String q = "SELECT * FROM Local" + username + "Chats";
        PreparedStatement ps = null;
        try
        {
            ps = connection.prepareStatement(q);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Timestamp rt = rs.getTimestamp("ReceivedTime");
                Timestamp st = rs.getTimestamp("SeenTime");
                if (rt == null || rs.getTimestamp("ReceivedTime").toString().equals("2021-01-01 00:00:00"))
                    rt = null;
                if (st == null || rs.getTimestamp("SeenTime").toString().equals("2021-01-01 00:00:00"))
                    st = null;
                chats.add(new Message(rs.getString("Sender"), rs.getString("Receiver"), rs.getString("Message"), rs.getTimestamp("SentTime"), rt, st));
                //System.out.println("rt is" + " " + rt);
                //System.out.println("st is" + " " + st);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void logout() throws IOException{
        Timestamp time = new Timestamp(System.currentTimeMillis());
        SystemMessage log = new SystemMessage(username,-1,time);
        oos.writeObject(log);
        oos.flush();
        System.exit(1);
    }
    // Kind of initilization!
    public void refresh()
    {
        scrollPane.setPrefWidth(WindowPane.getPrefWidth());
        scrollPane.setPrefHeight(WindowPane.getPrefHeight());
        textBox.clear();
        VerticalPane.getChildren().clear();
        AllChats.getChildren().clear();
        Send.setOnMouseClicked(e -> sendMessage());
        refresh.setOnMouseClicked(e -> {
            refresh();
        });
        Logout.setOnMouseClicked(e -> {
            try {
                logout();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        currentUser.setText("Name");
        currentUserStatus.setText("");
        /*if(friends != null)
        {
            for(int i = 0; i < friends.size(); i++)
                System.out.println(friends.get(i));
        }
        if(FriendStatus != null) {
            for (int i = 0; i < FriendStatus.size(); i++)
                System.out.println(FriendStatus.get(i).getUser());
        }*/
        chats = new ArrayList<>();
        friends= new ArrayList<>();
        AddFriend.setOnMouseClicked(e-> addNewFriendChat());
        fetchAllChats();//To fetch all chat of user from Local_Database
        //System.out.println(currentUser);
        //String q = "SELECT Sender FROM Local" + username + "Chats";
        //PreparedStatement ps = null;
        /*try
        {
           /* ps = connection.prepareStatement(q);
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                String fr = rs.getString("Sender");
                if ((!friends.contains(fr)) && !(fr.equals(username)))
                {
                    friends.add(fr);
                    addChat(fr);
                }
            }
            for (int i = 0; i < chats.size(); i++)
            {
                try
                {
                    if (chats.get(i).getFrom().equals(currentUser.getText()) || chats.get(i).getTo().equals(currentUser
                            .getText()))
                        addMessageToDisplay(chats.get(i));
                    if((!friends.contains(chats.get(i).getFrom()))&&(!chats.get(i).getFrom().equals(username)))
                    {
                        friends.add(chats.get(i).getFrom());
                        addChat(chats.get(i).getFrom());
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        } */
    }

    public void sendMessage()
    {
        Message msg = new Message(username,currentUser.getText(),textBox.getText(),new Timestamp(System.currentTimeMillis()),null,null);
        textBox.clear();
        //System.out.println(currentUser.getText());
        if(currentUser.getText().equals("Name"))
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Select a user");
            alert.setContentText("Please select a user before typing any message");
            alert.show();
            return;
        }
        if(!currentUser.equals("Name"))
        {
            chats.add(msg);
            try {
                addMessageToDisplay(msg);
                insertIntoDatabase(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                System.out.println(msg.getContent());
                oos.writeObject(msg);
                oos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addNewFriendChat()
    {
        TextInputDialog dialog = new TextInputDialog("Enter the username of friend");
        dialog.setTitle("UserName Input");
        dialog.setHeaderText("Username Of Friend");
        Optional<String>result = dialog.showAndWait();
        if(result.isPresent())
        {
            PreparedStatement ps = null;
            ResultSet rs = null;
            int flag = 0;
            try
            {
                String q = "SELECT * FROM usertable";
                ps = connection.prepareStatement(q);
                rs = ps.executeQuery();
                while(rs.next())
                {
                    if(rs.getString("Username").equals(result.get()))
                    {
                        flag = 1;
                        break;
                    }
                }
                if(flag == 1)
                {
                    if (!result.get().equals(username))
                        addChat(result.get());
                }
                else
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Select a valid user");
                    alert.setContentText("Entered user is not found in the database");
                    alert.show();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void insertIntoDatabase(Message temp)
    {
        String q=
                "INSERT INTO Local"+username+"Chats VALUES('"+(temp.getFrom())+"','"+(temp.getTo())+"','"+(temp.getContent())+"',"+(temp.getSentTime()==null?"null":("'"+temp.getSentTime()+"'"))+","+(temp.getReceivedTime()==null?"'2021-01-01 00:00:00'":("'"+temp.getReceivedTime()+"'"))+","+(temp.getSeenTime()==null?"'2021-01-01 00:00:00'":("'"+temp.getSeenTime()+"'"))+")";
        try {
            PreparedStatement ps = connection.prepareStatement(q);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addMessageToDisplay(Message msg) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLFiles/MessageDisplay.fxml"));
        VBox vbox = loader.load();
        MessageDisplayController mdc = loader.getController();
        mdc.MessageContent.setText(msg.getContent());
        mdc.SenderName.setText(msg.getFrom());
        mdc.TimeContent.setText((msg.getSentTime()).toString());
        mdc.ReadReceipts.setOnMouseClicked(e-> showDetails(msg));
        vbox.setPrefWidth(300);
        VerticalPane.getChildren().add(vbox);
    }
    private void showDetails(Message mesg)//To tell Receive time and Seen time
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Time Receipts");
        alert.setHeaderText("Type       Time");
        String text="SentTime\t"+(mesg.getSentTime()).toString()+"\n";
        if(mesg.getReceivedTime()!=null)
            text+=("ReceivedTime\t"+(mesg.getReceivedTime()).toString())+"\n";
        else
            text+=("ReceivedTime\t"+"NOT RECEIVED\n");
        if(mesg.getSeenTime()!=null)
            text+=("SeenTime\t"+(mesg.getSeenTime()).toString())+"\n";
        else
            text+=("SeenTime\t"+"NOT SEEN\n");
        alert.setContentText(text);
        alert.show();
    }

    public void CallClicked() throws UnknownHostException
    {
        CallRequest cr = new CallRequest(currentUser.getText(),username, InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()),9890);
        try {
            oos.writeObject(cr);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void StartVideoChat(CallRequest finalObj)
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLFiles/vidchat.fxml"));
        AnchorPane anchorPane = null;
        try
        {
            anchorPane = (AnchorPane) fxmlLoader.load();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        String url = "https://wallpaperaccess.com/full/1261663.jpg";
        BackgroundImage myBI= new BackgroundImage(new Image(url,800,600,false,true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT);
        anchorPane.setBackground(new Background(myBI));
        vidcontroller controller = fxmlLoader.getController();
        controller.setTargetInetAddress(finalObj.getInetAddress());
        controller.setTargetPort(finalObj.getPort());
        controller.targetUser = currentUser.getText();
        controller.setCurrentStage(new Stage());
        currentStage.setTitle("Video Chat Window");
        currentStage.setScene(new Scene(anchorPane,600,400));
        // display window now
        currentStage.show();
        controller.StartVideoCall();
    }
}