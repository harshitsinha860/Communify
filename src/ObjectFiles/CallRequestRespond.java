package ObjectFiles;

import java.io.Serializable;
import java.net.InetAddress;

public class CallRequestRespond extends CallRequest implements Serializable
{

    private String error;
    private boolean accept;
    public CallRequestRespond(InetAddress userInetAddress, int dedicatedPort,
                              String error, boolean accept,String callerUser,String targetUser)
    {
        super(targetUser,callerUser,userInetAddress,dedicatedPort);
        this.error = error;
        this.accept = accept;
    }
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isAccepted() {
        return accept;
    }

    public void setAcceptance(boolean accept) {
        this.accept = accept;
    }
}
