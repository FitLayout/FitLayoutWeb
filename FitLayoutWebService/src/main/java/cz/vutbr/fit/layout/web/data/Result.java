/**
 * Result.java
 *
 * Created on 3. 10. 2020, 21:22:25 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

/**
 * A base class for all JAX-RS results.
 *  
 * @author burgetr
 */
public class Result
{
    //standard statuses
    public static final String OK = "ok";
    public static final String ERROR = "error";
    
    String status;

    
    public Result(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
