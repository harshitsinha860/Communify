package client.CallThreads;

import ObjectFiles.AudioPacket;

import javax.sound.sampled.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class AudioReceiver implements Runnable
{
    public AudioReceiver(String targetUser, BufferedPlayer bp) {
        target = targetUser;
    this.bp = bp;
    }
    private String target;
    private BufferedPlayer bp;
    int stopper;
    @Override
    public void run() throws NullPointerException{
        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
        SourceDataLine speakers;
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            speakers.open(format);
            speakers.start();
            int port = 8189;
            if(target.equals("Name"))
                port = 8188;
            DatagramSocket ds = new DatagramSocket(port);
            getBp().setSpeakers(speakers);
            // adjust condition of loop for extent of microphone use
            while (stopper==0) {
                //  i++;
                byte[] data2 = new byte[10400];

                DatagramPacket dp = new DatagramPacket(data2,data2.length);
                // System.out.printf("Receiving");
                ds.receive(dp);
                // System.out.println(dp.getData().length);
                ByteArrayInputStream bais = new ByteArrayInputStream(data2);
                ObjectInputStream ois = new ObjectInputStream(bais);
                AudioPacket ap = (AudioPacket)ois.readObject();
                getBp().addAudioPacket(ap);
                //byte data[]=ap.getAudioData();
                //speakers.write(data, 0, data.length);
                // System.out.println("Audio "+ap.counter);
            }
            speakers.drain();
            speakers.close();
            //microphone.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public BufferedPlayer getBp() {
        return bp;
    }

    public void setBp(BufferedPlayer bp) {
        this.bp = bp;
    }
}
