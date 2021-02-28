package ObjectFiles;
import java.io.Serializable;

public class user implements Serializable {
    public String username;
    public String password;

    public user(String username, String password) {
        this.username = username;
        this.password = password;
    }
}