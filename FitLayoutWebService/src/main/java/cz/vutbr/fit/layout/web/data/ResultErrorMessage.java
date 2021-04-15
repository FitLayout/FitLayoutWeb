/**
 * ResultError.java
 *
 * Created on 3. 10. 2020, 21:20:25 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

/**
 * A result containing an error message.
 * 
 * @author burgetr
 */
public class ResultErrorMessage extends Result
{
    //standard messages
    public static final String E_NO_SERVICE = "No such service";
    public static final String E_NO_ARTIFACT = "No such artifact";
    public static final String E_NO_REPO = "No such repository";
    
    String message;

    public ResultErrorMessage(String status, String message)
    {
        super(status);
        this.message = message;
    }

    public ResultErrorMessage(String message)
    {
        super(Result.ERROR);
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }


}
