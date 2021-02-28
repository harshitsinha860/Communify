package client.controllers;

import client.CallThreads.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.net.InetAddress;

public class vidcontroller
{
    public String targetUser;
    public Button StopCall;// to stop or end call
    public ImageView MyVideoView;
    public ImageView CallerVideoView;
    public Label loadingLabel;
    private InetAddress TargetInetAddress;
    private int TargetPort;
    public InetAddress getTargetInetAddress() {
        return TargetInetAddress;
    }
    public Stage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }
    private Stage currentStage;
    public void StopCallClicked()
    {
        //Just include the oos,ois of the user and start a chat window from here..
    }
    public void StartVideoCall()
    {
        VideoSender vs=null;
        VideoReceiver vr=null;
        BufferedPlayer bp=null;
        AudioReceiver ar = null;
        AudioSender as = null;
        try {
            System.out.println("Initilizing Handling Objects ...");
            bp = new BufferedPlayer(targetUser,CallerVideoView,loadingLabel,1000);
            vs = new VideoSender(targetUser,TargetInetAddress,TargetPort,MyVideoView);
            vr = new VideoReceiver(targetUser,CallerVideoView,bp);
            ar = new AudioReceiver(targetUser,bp);
            as = new AudioSender(targetUser,TargetInetAddress,TargetPort);
            System.out.println("All Objects Initialized .");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread videoSenderThread = new Thread(vs);
        Thread videoreceiverThread = new Thread(vr);
        Thread audioSenderThread = new Thread(as);
        Thread audioReceiverThread = new Thread(ar);
        Thread bufferedPlayerThread = new Thread(bp);
        bufferedPlayerThread.start();
        videoSenderThread.start();
        audioSenderThread.start();
        videoreceiverThread.start();
        audioReceiverThread.start();
    }
    public void setTargetInetAddress(InetAddress targetInetAddress) {
        TargetInetAddress = targetInetAddress;
    }

    public int getTargetPort() {
        return TargetPort;
    }
    public void setTargetPort(int targetPort) {
        TargetPort = targetPort;
    }

}
