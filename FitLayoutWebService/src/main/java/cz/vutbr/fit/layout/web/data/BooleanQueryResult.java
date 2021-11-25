/**
 * BooleanQueryResult.java
 *
 * Created on 25. 11. 2021, 12:02:04 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import javax.json.bind.annotation.JsonbProperty;

import cz.vutbr.fit.layout.web.data.SelectQueryResult.ResultHeader;

/**
 * 
 * @author burgetr
 */
public class BooleanQueryResult
{
    public ResultHeader head;
    @JsonbProperty("boolean")
    public Boolean value;
    
    public BooleanQueryResult(boolean value)
    {
        this.head = new ResultHeader();
        this.value = value;
    }

    public ResultHeader getHead()
    {
        return head;
    }

    public Boolean getValue()
    {
        return value;
    }
}
