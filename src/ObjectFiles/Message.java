package ObjectFiles;

import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable
{
    private String from;
    private String to;
    private String content;
    private Timestamp SentTime;
    private Timestamp ReceivedTime;
    private Timestamp SeenTime;

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Message(String from, String to, String content, Timestamp sentTime, Timestamp receivedTime, Timestamp seenTime) {
        this.from = from;
        this.to = to;
        this.content = content;
        SentTime = sentTime;
        ReceivedTime = receivedTime;
        SeenTime = seenTime;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getSentTime() {
        return SentTime;
    }

    public void setSentTime(Timestamp sentTime) {
        SentTime = sentTime;
    }

    public Timestamp getReceivedTime() {
        return ReceivedTime;
    }

    public void setReceivedTime(Timestamp receivedTime) {
        ReceivedTime = receivedTime;
    }

    public Timestamp getSeenTime() {
        return SeenTime;
    }

    public void setSeenTime(Timestamp seenTime) {
        SeenTime = seenTime;
    }
}
