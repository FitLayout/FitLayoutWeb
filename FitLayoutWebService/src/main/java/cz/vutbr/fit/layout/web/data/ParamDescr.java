/**
 * ParamDescr.java
 *
 * Created on 28. 2. 2021, 18:35:11 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import cz.vutbr.fit.layout.api.Parameter;

/**
 * 
 * @author burgetr
 */
@Schema(name="ParamDescr", description="Service parameter description",
        discriminatorProperty = "type",
        discriminatorMapping = {
                @DiscriminatorMapping(value = "int", schema = ParamIntDescr.class),
                @DiscriminatorMapping(value = "float", schema = ParamFloatDescr.class),
                @DiscriminatorMapping(value = "string", schema = ParamStringDescr.class),
                @DiscriminatorMapping(value = "boolean", schema = ParamBooleanDescr.class),
        })
public class ParamDescr
{
    @Schema(description="Name of the parameter", required = true)
    public String name;
    @Schema(description="Text description of the parameter", required = false)
    public String description;
    @Schema(description="Type of the parameter {bolean, int, float, string}", required = true)
    public String type;

    public ParamDescr(Parameter param, String type)
    {
        name = param.getName();
        description = param.getDescription();
        this.type = type; 
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getType()
    {
        return type;
    }
}
