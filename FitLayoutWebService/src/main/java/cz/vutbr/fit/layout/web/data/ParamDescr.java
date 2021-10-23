/**
 * ParamDescr.java
 *
 * Created on 28. 2. 2021, 18:35:11 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import cz.vutbr.fit.layout.api.Parameter;

/**
 * 
 * @author burgetr
 */
@Schema(name="ParamDescr", description="Service parameter description")
public class ParamDescr
{
    @Schema(description="Name of the parameter", required = true)
    public String name;
    @Schema(description="Type of the parameter {bolean, int, float, string}", required = true)
    public String type;

    public ParamDescr(Parameter param, String type)
    {
        name = param.getName();
        this.type = type; 
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }
}
