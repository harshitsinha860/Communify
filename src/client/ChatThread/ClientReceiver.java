package client.ChatThread;

import ObjectFiles.*;
import client.controllers.ChatController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;

public class ClientReceiver implements Runnable
{
    public ObjectOutputStream oos;
    public ObjectInputStream ois;
    public ChatController controller;
    public Connection connection;
    public String username;
    @Override
    public void run()
    {
        while(true)
        {
            Object obj  = null;
            try
            {
                obj = ois.readObject();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            if(obj instanceof Message)
            {
                Message temp = (Message)obj;
                temp.setReceivedTime(new Timestamp(System.currentTimeMillis()));
                //Receiver time is current time
                String q ="INSERT INTO Local"+username+"Chats VALUES('"+(temp.getFrom())+"','"+
                        (temp.getTo())+"','"+(temp.getContent())+"',"+(temp.getSentTime()==null?"null":("'"+temp.getSentTime()+"'"))+","+
                        (temp.getReceivedTime()==null?"'2021-01-01 00:00:00'":("'"+temp.getReceivedTime()+"'"))+","
                        +(temp.getSeenTime()==null?"'2021-01-01 00:00:00'":("'"+temp.getSeenTime()+"'"))+")";
                try {
                    PreparedStatement ps = connection.prepareStatement(q);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                /*
                    Quickly update my msg box as we are chatting with the person who has sent us the msg.
                 */
                if(temp.getFrom().equals(controller.currentUser.getText()))
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            System.out.println("Changing UI with talking person");
                            //
                            try {
                                controller.display(controller.currentUser.getText());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //controller.addMessageToDisplay(temp);
                                //controller.seenMessagesof(temp.getFrom());
                            //}
                            //catch (IOException e) {
                              //  e.printStackTrace();
                            //}
                        }
                    });
                }
                //Add the msg to my chats
                controller.chats.add(temp);
                /*
                * This feature below is when I get/send msg to/from someone and
                * he hasnt added me to his friends or i havent added him to my friends
                * so automatically I/he will be added to his/mine friends
                * */
                if(!controller.friends.contains(temp.getFrom()))
                {
                    Platform.runLater(new Runnable()//To perform UI work from different Thread
                    {
                        @Override
                        public void run() {
                            System.out.println(temp.getFrom());
                            controller.addChat(temp.getFrom());
                        }
                    });
                }
            }
            else if(obj instanceof SystemMessage)
            {
                SystemMessage temp = (SystemMessage) obj;
                //System.out.println(temp.valid);
                if(temp.valid==1)// recieved Time
                {
                    String q="UPDATE Local"+username+"Chats SET ReceivedTime = '"+temp.time+"' WHERE ReceivedTime = " +
                            "'2021-01-01 00:00:00' AND Receiver ='"+temp.sender+"'";
                    //System.out.println(q);
                    try {
                        PreparedStatement ps = connection.prepareStatement(q);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }
                else if(temp.valid==2)// seen Time
                {
                    String q="UPDATE Local"+username+"Chats SET SeenTime = '"+temp.time+"' WHERE SeenTime = " +
                            "'2021-01-01 00:00:00' AND Receiver='"+temp.sender+"'";
                    try {
                        PreparedStatement ps = connection.prepareStatement(q);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                else if(temp.valid == -2) // someone logged out
                {
                    int flag=0;
                    for(int i=0;i<controller.FriendStatus.size();i++)
                    {
                        if(controller.FriendStatus.get(i).getUser().equals(temp.sender))
                        {
                            flag=1;
                            controller.FriendStatus.get(i).setValid(0);
                            controller.FriendStatus.get(i).setTime(temp.time);
                            break;
                        }
                    }
                    if(flag==0)
                    {
                        //System.out.println(temp.sender);
                        controller.FriendStatus.add(new Status(temp.sender,temp.time,0));
                    }
                    if(controller.currentUser.getText().equals(temp.sender))
                    {
                        Platform.runLater(new Runnable()//To perform UI work from different Thread
                        {
                            @Override
                            public void run() {
                                System.out.println("Changing Status Of CurrentUSer");
                                controller.currentUserStatus.setText("Last Seen "+temp.time.toString());
                            }
                        });
                    }
                }
                else if(temp.valid==-3)// someone logged in
                {
                    int flag=0;
                    for(int i=0;i<controller.FriendStatus.size();i++)
                    {
                        if(controller.FriendStatus.get(i).getUser().equals(temp.sender))
                        {
                            flag=1;
                            controller.FriendStatus.get(i).setValid(1);
                            break;
                        }
                    }
                    if(flag==0)
                    {
                        controller.FriendStatus.add(new Status(temp.sender,temp.time,1));
                    }
                    if(controller.currentUser.getText().equals(temp.sender))
                    {
                        Platform.runLater(new Runnable()//To perform UI work from different Thread
                        {
                            @Override
                            public void run() {
                                controller.currentUserStatus.setText("Online");
                            }
                        });
                    }
                }
                /**
                 * ASAP I send a msg to someone and when he sees it this piece of code
                 * is responsible for updating my read receipts(seentime/receivetime) in the gui
                 */
                if(temp.valid==1||temp.valid==2)
                {
                    Platform.runLater(new Runnable()//To perform UI work from different Thread
                    {
                        @Override
                        public void run() {
                            //System.out.println("Received receiving time refreshing");
                            for(int i=0;i<controller.chats.size();i++)
                            {
                                //System.out.println(controller.chats.get(i).getTo());
                                //System.out.println(temp.sender);
                                //System.out.println(controller.chats.get(i).getFrom());
                                //System.out.println(controller.chats.get(i).getReceivedTime());
                                if(temp.valid==1&&((controller.chats.get(i).getTo().equals(temp.sender)||controller.chats.get(i).getFrom().equals(temp.sender))&&controller.chats.get(i).getReceivedTime()==null))// check for proper object
                                {
                                    //System.out.println("changed the value");
                                    //System.out.println(temp.time);
                                    controller.chats.get(i).setReceivedTime(temp.time);// update received time in message object
                                }
                                else if(temp.valid==2&&((controller.chats.get(i).getTo().equals(temp.sender)||controller.chats.get(i).getFrom().equals(temp.sender))&&controller.chats.get(i).getSeenTime()==null))// check for proper object
                                {
                                    //System.out.println("changed the value");
                                    controller.chats.get(i).setSeenTime(temp.time);// update received time in message object
                                }
                            }
                            /*try {
                                System.out.println("Updating Display");
                                controller.display(temp.sender);// update in UI
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/
                        }
                    });
                }
            }
            else if(obj instanceof Errors)
            {
                Errors temp = (Errors)obj;
                System.out.println(temp.errormessage);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Error");
                alert.setContentText(temp.errormessage);
                alert.show();
            }
            else if(obj instanceof CallRequestRespond)
            {
                CallRequestRespond crr = (CallRequestRespond) obj;
                if(!crr.isAccepted())
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Call Disconnected");
                            alert.setHeaderText(crr.getError());
                            alert.setContentText("You Can Retry Connecting Call");
                            alert.show();
                        }
                    });
                }
                else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            controller.StartVideoChat(new CallRequest(controller.currentUser.getText(),controller.username,crr.getInetAddress(),crr.getPort()));
                        }
                    });
                }
            }
            else if(obj instanceof CallRequest)
            {
                CallRequest finalObj = (CallRequest) obj;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Call Request Received from "+ ((CallRequest) finalObj).getCallerUser());
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Alert Call Request Received From user - "+finalObj.getCallerUser(), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                        alert.showAndWait();
                        if (alert.getResult() == ButtonType.YES)
                        {
                            InetAddress localhost=null;
                            try {
                                localhost = InetAddress.getLocalHost();
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            try
                            {
                                int port = 9890;
                                CallRequestRespond crr =
                                        new CallRequestRespond(InetAddress.getByName(localhost.getHostAddress()),port,
                                                "Successful",true,finalObj.getCallerUser(),finalObj.getTargetUser());
                                oos.writeObject(crr);
                                oos.flush();
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            controller.StartVideoChat(finalObj);
                        }
                        else if(alert.getResult() == ButtonType.NO||alert.getResult() == ButtonType.CANCEL)
                        {
                            CallRequestRespond crr = null;
                            try {
                                crr = new CallRequestRespond(InetAddress.getLocalHost(),0000,"Call Rejected By User",false,finalObj.getCallerUser(),finalObj.getTargetUser());
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            try {
                                oos.writeObject(crr);
                                oos.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }
}
