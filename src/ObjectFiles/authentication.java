package ObjectFiles;

import java.io.Serializable;

public class authentication implements Serializable
{
    public boolean auth;
    public String Error;

    public authentication(boolean auth, String error) {
        this.auth = auth;
        Error = error;
    }
}
