package ObjectFiles;
import java.io.Serializable;

public class signupclass implements Serializable
{
    public String user,password;
    public signupclass(String user,String password)
    {
        this.user=user;
        this.password=password;
    }
}
