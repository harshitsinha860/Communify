package server.Handlers;

import ObjectFiles.*;
import javafx.util.Pair;
import server.MessageManager;
import server.Server;
import sun.net.ConnectionResetException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.*;

public class ClientHandler implements Runnable, Serializable
{
    public Socket sc;
    public MessageManager msh;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;
    public Connection connection;
    public Server server;
    public String username;
    public String password;
    int flag = 0;

    public ClientHandler(Socket sc, Server server, MessageManager msh, ObjectOutputStream oos, ObjectInputStream ois, Connection connection)
    {
        this.sc = sc;
        this.server = server;
        this.msh = msh;
        this.oos = oos;
        this.ois = ois;
        this.connection = connection;
    }
    public ObjectOutputStream find(String sender)
    {
        int i;
        for(i=0;i<server.activelist.size();i++)
        {
            if (server.activelist.get(i).getKey().equals(sender))
            {
                return server.activeUserStreams.get(i).getValue();
            }
        }
        return null;
    }
    public void logout(Timestamp time)
    {
        for(int i = 0; i < server.activelist.size(); i++)
        {
            if(server.activelist.get(i).getKey().equals(username))
            {
                server.activelist.remove(i);
                server.activeUserStreams.remove(i);
                server.handlers.remove(i);
                break;
            }
        }
        for(int i = 0; i < server.activelist.size(); i++)
        {
            server.handlers.get(i).broadcast(username,time,-2);
        }
        String query = "UPDATE UserTable SET LastSeen = '"+time+"' WHERE UserName='" + (username) + "'";
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void broadcast(String user,Timestamp time,int valid)
    {
        SystemMessage sm = new SystemMessage(user,valid,time);
        try {
            oos.writeObject(sm);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void callAlert(CallRequest cr)
    {
        try {
            oos.writeObject(cr);
            oos.flush();
            flag=1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    private void callRespond(CallRequestRespond obj) {
        try {
            oos.writeObject(obj);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run()
    {
        Timestamp time = null;
        Object obj = null;
        try
        {
            obj = ois.readObject();
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        if(obj instanceof user) {
            user temp = (user) obj;
            username = temp.username;
            password = temp.password;
            try {
                if (authenticate()) {
                    msh.oos = oos;
                    msh.remove(username);
                    System.out.println("All good from server side, user identity has been confirmed.");
                    while (true) {
                        try {
                            obj = ois.readObject();
                        } catch (ConnectionResetException e) {
                            e.printStackTrace();
                            break;
                        }
                        if (obj instanceof Message) {
                            Message ms = (Message) obj;
                            String receiver = ms.getTo();
                            System.out.println("----------------" + receiver);
                            ObjectOutputStream oosTo = find(receiver);
                            System.out.println(oosTo);
                            if (oosTo != null)// IF USER IS ONLINE
                            {
                                System.out.println("User is Active");
                                ms.setReceivedTime(ms.getSentTime());
                                oosTo.writeObject(ms);
                                oosTo.flush();
                                SystemMessage sm = new SystemMessage(ms.getTo(), 1, ms.getSentTime());
                                oos.writeObject(sm);
                                oos.flush();
                            } else// IF USER IS OFFLINE
                            {
                                msh.insert(receiver, ms);
                            }
                        }
                        else if (obj instanceof SystemMessage)
                        {
                            SystemMessage sm = (SystemMessage) obj;
                            if (sm.valid == -1) {// SystemMessage object containing information of logout
                                time = sm.time;
                                break;
                            }
                            String receiver = sm.sender;
                            System.out.println("----------------" + receiver);
                            ObjectOutputStream oosTo = find(receiver);
                            System.out.println(oosTo);
                            if (oosTo != null)// IF USER IS ONLINE
                            {
                                System.out.println("User is Active");
                                sm.sender = username;
                                //for person who receives seen receipt
                                // will have sender as
                                // person who sends this to him
                                oosTo.writeObject(sm);
                                oosTo.flush();
                            }
                            else// IF USER IS OFFLINE
                            {
                                sm.sender = username;
                                //for person who receives seen receipt will have sender as
                                // person who sends this to him
                                msh.insert(receiver, sm);
                            }
                        }
                        else if((obj instanceof CallRequestRespond))
                        {
                            System.out.println("Call Respond from "+((CallRequestRespond) obj).getTargetUser()+" to "+((CallRequestRespond) obj).getCallerUser());
                            Pair<ClientHandler,Thread> cht = server.getHandler(((CallRequestRespond) obj).getCallerUser());
                            cht.getKey().callRespond((CallRequestRespond)obj);
                            if(((CallRequestRespond) obj).isAccepted())
                                server.addCall(((CallRequestRespond) obj).getCallerUser(),((CallRequestRespond) obj).getTargetUser());
                        }
                        else if(obj instanceof CallRequest)
                        {
                            // generate Alert Request to user if he is online
                            System.out.println("Call Request from "+((CallRequest) obj).getCallerUser()+" to "+((CallRequest) obj).getTargetUser());
                            Pair<ClientHandler,Thread> cht = server.getHandler(((CallRequest) obj).getTargetUser());
                            if(cht.getKey()==null||server.isInCall(((CallRequest) obj).getTargetUser()))// Target user is not online
                            {
                                oos.writeObject(new CallRequestRespond(InetAddress.getLocalHost(),0000,"User Not Online",false,((CallRequest) obj).getCallerUser(),((CallRequest) obj).getTargetUser()));
                                oos.flush();
                            }
                            else {
                                cht.getKey().callAlert((CallRequest) obj);
                            }
                        }
                    }
                }
                System.out.println("Logging Out");
                logout(time);
                this.oos.close();
                this.ois.close();
                this.sc.close();
            } catch (IOException | SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else
        {
            signupclass temp=(signupclass)obj;
            username = temp.user;
            System.out.println("User " + username + " Has successfully signed up.");
            try {
                msh.insertuser(temp);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean authenticate() throws ClassNotFoundException,SQLException
    {
        String query = "SELECT Password FROM UserTable WHERE UserName='" + (username) + "'";
        PreparedStatement preStat = connection.prepareStatement(query);
        ResultSet rs = preStat.executeQuery(query);
        if (rs.next())
        {
            String CheckPassword = rs.getString("Password");
            if (CheckPassword.equals(password))
            {
                authentication auth = new authentication(true,"Successful");
                try {
                    oos.writeObject(auth);
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server.activelist.add(new Pair<String,Socket>(username,sc));
                server.activeUserStreams.add(new Pair<>(ois,oos));
                server.handlers.add(this);
                /**
                 * Now we will fetch lastseen of all the registered users and store it!
                 */
                String query2 = "SELECT * FROM UserTable ";
                PreparedStatement ps = connection.prepareStatement(query2);
                ResultSet rs2 = ps.executeQuery();
                while(rs2.next()) {
                    try {
                        oos.writeObject(new SystemMessage(rs2.getString("Username"), -2, rs2.getTimestamp("LastSeen")));
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //informing all the other active users that i have logged in.
                for(int i=0;i<server.handlers.size();i++) {
                    if (!server.handlers.get(i).equals(this)) {
                        server.handlers.get(i).broadcast(username, null, -3);
                    }
                }
                //this covers some other cases which have been somehow not covered above!
                for(int i=0;i<server.activelist.size();i++)
                {
                    if(!server.activelist.get(i).getKey().equals(username))
                    {
                        try
                        {
                            oos.writeObject(new SystemMessage(server.activelist.get(i).getKey(),-3,null));
                            oos.flush();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                return true;
            }
            else
            {
                authentication auth = new authentication(false,"Invalid Login Credentials");
                try {
                    oos.writeObject(auth);
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }
        else
            return false;
    }
}
