package ObjectFiles;

import java.sql.Timestamp;

public class Status
{
    public String user;
    public Timestamp time;
    public int valid;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getValid() {
        return valid;
    }

    public void setValid(int valid) {
        this.valid = valid;
    }

    public Status(String user, Timestamp time, int valid) {
        this.user = user;
        this.time = time;
        this.valid = valid;
    }
}
