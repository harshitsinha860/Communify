package server;
import javafx.util.Pair;
import server.Handlers.CallAudioHandler;
import server.Handlers.CallFramesHandler;
import server.Handlers.ClientHandler;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

public class Server
{
    public ArrayList<Pair<String, Socket>> activelist;
    public ArrayList<Pair<ObjectInputStream, ObjectOutputStream>> activeUserStreams;
    public MessageManager msh;
    public ArrayList<ClientHandler> handlers;
    public ArrayList<Thread> handlerThreads;
    public ArrayList<Pair<String,String> > currentCalls;
    public static void main(String[] args) throws Exception
    {
        //server ip would be in this case 127.0.0.1:8188
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        String url = "jdbc:mysql://127.0.0.1:3306/app";
        Connection connection = DriverManager.getConnection(url,"root","root");
        //String q = "SET SQL_SAFE_UPDATES = 0";
        //PreparedStatement ps = connection.prepareStatement(q);
        //ps.executeUpdate();
        //System.out.println("Succesfull");
        Server server=new Server();
        server.handlers=new ArrayList<>();
        server.handlerThreads=new ArrayList<>();
        server.activelist=new ArrayList<Pair<String, Socket>>();
        server.currentCalls=new ArrayList<Pair<String, String>>();
        server.activeUserStreams=new ArrayList<>();
        server.msh = new MessageManager(server);
        server.msh.connection=connection;
        CallFramesHandler ch = new CallFramesHandler();
        ch.server=server;
        Thread callFramesHandlerThread = new Thread(ch);
        callFramesHandlerThread.start();
        CallAudioHandler ca = new CallAudioHandler();
        ca.server=server;
        Thread callAudioHandlerThread = new Thread(ca);
        callAudioHandlerThread.start();
        try
        {
            ServerSocket ss = new ServerSocket(8188);
            while(true)
            {
                Socket sc = ss.accept();
                ObjectInputStream ois = new ObjectInputStream(sc.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(sc.getOutputStream());
                ClientHandler chi = new ClientHandler(sc,server,server.msh,oos,ois,connection);
                Thread t = new Thread(chi);
                chi.ois = ois;
                chi.oos = oos;
                t.start();
                server.handlerThreads.add(t);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    public boolean isOnline(String username)
    {
        for(int i = 0; i < activelist.size(); i++)
        {
            if(activelist.get(i).getKey().equals(username))
            {
                return true;
            }
        }
        return false;
    }
    public Pair<ClientHandler,Thread> getHandler(String username)
    {
        for(int i=0;i<activelist.size();i++)
        {
            if(activelist.get(i).getKey().equals(username))
            {
                return new Pair(handlers.get(i),handlerThreads.get(i));
            }
        }
        return null;
    }
    public void addCall(String user1,String user2)
    {
        currentCalls.add(new Pair<>(user1,user2));
    }
    public boolean isInCall(String user1)
    {
        for(int i=0;i<currentCalls.size();i++)
        {
            if(currentCalls.get(i).getKey().equals(user1)||currentCalls.get(i).getValue().equals(user1))
                return true;
        }
        return false;
    }
}
