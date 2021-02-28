package ObjectFiles;

import java.io.Serializable;
import java.sql.Timestamp;

public class AudioPacket implements Serializable
{
    private byte[] audioData;
    private Timestamp timestamp;

    public String getDestination_user() {
        return destination_user;
    }

    public void setDestination_user(String destination_user) {
        this.destination_user = destination_user;
    }

    public AudioPacket(byte[] audioData, Timestamp timestamp, String destination_user) {
        this.audioData = audioData;
        this.timestamp = timestamp;
        this.destination_user = destination_user;
    }

    private String destination_user;

    public byte[] getAudioData() {
        return audioData;
    }

    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
