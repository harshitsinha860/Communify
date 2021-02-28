package ObjectFiles;


import java.io.Serializable;
import java.sql.Timestamp;

public class SystemMessage implements Serializable
{
    public String sender;
    public int valid;
    public Timestamp time;
    public SystemMessage(String sender, int valid, Timestamp time) {
        this.sender=sender;
        this.valid=valid;
        this.time=time;
    }
}
