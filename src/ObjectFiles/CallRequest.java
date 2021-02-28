package ObjectFiles;

import java.io.Serializable;
import java.net.InetAddress;

public class CallRequest implements Serializable
{
    private String targetUser;// the username of person to whom we are calling
    private String callerUser;// the username of person who is calling
    private InetAddress inetAddress;// inetAddress of caller

    public int getPort() {
        return Port;
    }

    public void setPort(int port) {
        Port = port;
    }

    private int Port;
    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public String getCallerUser() {
        return callerUser;
    }

    public void setCallerUser(String callerUser) {
        this.callerUser = callerUser;
    }

    public CallRequest(String targetUser, String callerUser, InetAddress inetAddress,int Port) {
        this.targetUser = targetUser;
        this.callerUser = callerUser;
        this.inetAddress = inetAddress;
        this.Port = Port;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }
}
