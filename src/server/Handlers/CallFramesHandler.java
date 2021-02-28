package server.Handlers;

import ObjectFiles.FramePacket;
import server.Server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class CallFramesHandler implements Runnable
{
    public Server server;

    @Override
    public void run()
    {
        DatagramSocket ds=null;
        DatagramSocket ds_sender=null;
        try {
            ds = new DatagramSocket(20002);
            ds_sender = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while(true)
        {
            byte[] data = new byte[200000];
            DatagramPacket dp=new DatagramPacket(data,data.length);
            try {
                ds.receive(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bais);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FramePacket fp = null;
            try {
                fp = (FramePacket)ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            DatagramPacket send_data= new DatagramPacket(dp.getData(),dp.getLength(),server.getHandler(fp.getDestinationUsername()).getKey().sc.getInetAddress(),9890);
            try {
                ds_sender.send(send_data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
