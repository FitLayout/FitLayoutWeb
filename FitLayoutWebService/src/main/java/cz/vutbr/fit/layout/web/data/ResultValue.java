/**
 * ResultValue.java
 *
 * Created on 26. 9. 2020, 13:25:07 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * A JAX-RS result containing any serializable object under 'result'.
 * 
 * @author burgetr
 */
@Schema(name = "ResultValue", description = "Result of an operation conaining an optional result value")
public class ResultValue extends Result
{
    Object result;

    public ResultValue(String status, Object result)
    {
        super(status);
        this.result = result;
    }

    public ResultValue(Object result)
    {
        super(Result.OK);
        this.result = result;
    }

    public Object getResult()
    {
        return result;
    }

    public void setResult(Object result)
    {
        this.result = result;
    }

}
