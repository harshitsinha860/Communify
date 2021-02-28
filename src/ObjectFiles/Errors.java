package ObjectFiles;

import java.io.Serializable;

public class Errors implements Serializable
{
    public String errormessage;

    public Errors(String errormessage) {
        this.errormessage = errormessage;
    }
}
