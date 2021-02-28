package ObjectFiles;

import java.io.Serializable;
import java.sql.Timestamp;

public class FramePacket implements Serializable
{
    private byte[] frameData;
    private Timestamp timestamp;

    public String getDestinationUsername() {
        return destinationUsername;
    }

    public void setDestinationUsername(String destinationUsername) {
        this.destinationUsername = destinationUsername;
    }

    public FramePacket(byte[] frameData, Timestamp timestamp, String destinationUsername) {
        this.frameData = frameData;
        this.timestamp = timestamp;
        this.destinationUsername = destinationUsername;
    }

    private String destinationUsername;

    public byte[] getFrameData() {
        return frameData;
    }

    public void setFrameData(byte[] frameData) {
        this.frameData = frameData;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
