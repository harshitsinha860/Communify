package server;

import ObjectFiles.Message;
import ObjectFiles.SystemMessage;
import ObjectFiles.signupclass;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.time.LocalDateTime;

public class MessageManager
{
    public Server server;
    public ObjectOutputStream oos;
    public Connection connection;
    public MessageManager(Server ss)
    {
        server = ss;
    }
    public void insertuser(signupclass sc) throws SQLException
    {
        String q1 = "INSERT INTO UserTable VALUES (?,?,?)";
        PreparedStatement ps = connection.prepareStatement(q1);
        ps.setString(1,sc.user);
        ps.setString(2,sc.password);
        ps.setString(3, String.valueOf(new Timestamp(System.currentTimeMillis())));
        ps.executeUpdate();
        String table = sc.user + "Table";
        q1 = "CREATE TABLE " + table + " ( Sender varchar(11),Valid int(255),Message text(2000),Time timestamp)";
        ps = connection.prepareStatement(q1);
        ps.executeUpdate();
    }
    public void insert(String receiver,Object obj) throws ClassNotFoundException, SQLException
    {
        int valid = -1;
        String table,sender,content;
        sender = content = null;
        Timestamp time=null;
        if(obj instanceof Message)
        {
            Message message=(Message)obj;
            valid=0;// someone has sent me this msg!
            sender = message.getFrom();
            content = message.getContent();
            time = message.getSentTime();
        }
        else if(obj instanceof SystemMessage) {
            SystemMessage sm = (SystemMessage) obj;
            valid = sm.valid;
            sender = sm.sender;
            time = sm.time;
            content = null;
        }
        table=receiver+"Table";
        System.out.println(table+"---------------");
        System.out.println(sender+" /"+content);
        String query1 = "INSERT INTO "+(table)+" VALUES (?, ?, ?, ?)";
        PreparedStatement preStat = connection.prepareStatement(query1);
        preStat.setString(1, sender);
        preStat.setInt(2, valid);
        preStat.setString(3, content);
        preStat.setTimestamp(4, time);
        preStat.executeUpdate();
    }
    public ObjectOutputStream find(String sender)
    {
        for(int i = 0; i < server.activelist.size(); i++)
        {
            if(server.activelist.get(i).getKey().equals(sender))
            {
                return server.activeUserStreams.get(i).getValue();
            }
        }
        return null;
    }

    public void remove(String username) throws SQLException, IOException, ClassNotFoundException {
        String table = username + "Table";
        String query = "SELECT * FROM " + (table) + "";
        PreparedStatement preStat = connection.prepareStatement(query);
        ResultSet result = preStat.executeQuery();
        while (result.next())
        {
            String sender = result.getString("Sender");
            int valid = result.getInt("Valid");
            Timestamp time = result.getTimestamp("Time");
            String content = result.getString("Message");
            System.out.println("Name - " + sender);
            System.out.println("content - " + content);
            System.out.println("valid - " + valid);
            if (valid == 1 || valid == 2)
            // Message Sent is Received or Seen
            // 1 Received Time
            // 2 Seen Time
            {
                SystemMessage sm = new SystemMessage(sender ,valid, time);
                oos.writeObject(sm);
                //USER GET INFO OF RECEIVING  AND SEEN TIME
                oos.flush();
            }
            else
            {
                ObjectOutputStream oosTo = find(sender);
                Message ms = new Message(sender,username,content,time,null,null);
                oos.writeObject(ms);
                //Sent message to User
                oos.flush();
                LocalDateTime datetime1 = LocalDateTime.now();//To get Local time
                time = Timestamp.valueOf(datetime1);
                //System.out.println(datetime1);
                if (oosTo != null)// if Sender is online
                {
                    System.out.println("User is Active");
                    SystemMessage sm = new SystemMessage(username ,1,time);
                    oosTo.writeObject(sm);//Sender get Info of receiving time
                    oosTo.flush();
                }
                else//if sender is offline
                {
                    SystemMessage sm=new SystemMessage(username,1,time);
                    //Sender get Receiving time of his message
                    insert(sender,sm);
                }
            }
        }
        query = "TRUNCATE " + (table) + "";
        preStat = connection.prepareStatement(query);
        preStat.executeUpdate();
    }
}
