package client.CallThreads;
import ObjectFiles.AudioPacket;
import ObjectFiles.FramePacket;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import javax.sound.sampled.SourceDataLine;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.Queue;

import static java.lang.Thread.sleep;

public class BufferedPlayer implements Runnable
{
    private Queue<FramePacket> frameQueue;
    private Queue<AudioPacket> audioQueue;
    private ImageView imageView;
    private Label loadingLabel;
    private long delta;
    private SourceDataLine speakers;
    public BufferedPlayer(String targetUser, ImageView imageView, Label loadingLabel, long delta) {
        this.setFrameQueue(new LinkedList<>());
        this.setAudioQueue(new LinkedList<>());
        this.setImageView(imageView);
        this.setLoadingLabel(loadingLabel);
        this.setDelta(delta);
    }
    public void addFramePacket(FramePacket fp)
    {
        getFrameQueue().add(fp);
    }
    public void addAudioPacket(AudioPacket ap){
        getAudioQueue().add(ap);}
    private long getDifference(FramePacket fp,AudioPacket ap)
    {
        return Math.abs(fp.getTimestamp().getTime()-ap.getTimestamp().getTime());
    }
    private boolean getSign(FramePacket fp,AudioPacket ap)
    {
        if(fp.getTimestamp().getTime()-ap.getTimestamp().getTime()>=0)
            return true;
        else
            return false;
    }

    @Override
    public void run() throws NullPointerException
    {
        try {
            sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(true)
        {

            try
            {
                System.out.println(getFrameQueue().size()+" "+getAudioQueue().size());
                while(getAudioQueue().size()==0||getFrameQueue().size()==0)
                {
                    System.out.println(getFrameQueue().size()+" "+getAudioQueue().size());
                    sleep(2);// do nothing
                }
                while(getFrameQueue().size() == 0)
                {
                    sleep(2);
                }
                if(getDifference(getFrameQueue().peek(), getAudioQueue().peek())< getDelta())
                {
                    ByteArrayInputStream bais = new ByteArrayInputStream(getFrameQueue().peek().getFrameData());
                    BufferedImage bi = ImageIO.read(bais);
                    Image image = SwingFXUtils.toFXImage(bi, null);
                    getImageView().setImage(image);
                    getFrameQueue().remove();
                    //byte data[]= getAudioQueue().peek().getAudioData();
                    //getSpeakers().write(data, 0, data.length);
                    //System.out.println("Audio "+ap.counter);
                }
                else if(getDifference(getFrameQueue().peek(), getAudioQueue().peek())> getDelta()&&getSign
                 (getFrameQueue().peek(), getAudioQueue().peek()))
                {
                    getFrameQueue().remove();
                }
                else
                    getAudioQueue().remove();
            }
            catch(Exception e)
            {
                System.out.println("Buffered Player Error");
                e.printStackTrace();
            }
        }
    }

    public Queue<FramePacket> getFrameQueue() {
        return frameQueue;
    }

    public void setFrameQueue(Queue<FramePacket> frameQueue) {
        this.frameQueue = frameQueue;
    }

    public Queue<AudioPacket> getAudioQueue() {
        return audioQueue;
    }

    public void setAudioQueue(Queue<AudioPacket> audioQueue) {
        this.audioQueue = audioQueue;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public Label getLoadingLabel() {
        return loadingLabel;
    }

    public void setLoadingLabel(Label loadingLabel) {
        this.loadingLabel = loadingLabel;
    }

    public long getDelta() {
        return delta;
    }

    public void setDelta(long delta) {
        this.delta = delta;
    }

    public SourceDataLine getSpeakers() {
        return speakers;
    }

    public void setSpeakers(SourceDataLine speakers) {
        this.speakers = speakers;
    }
}
