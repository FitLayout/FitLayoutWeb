/**
 * ParametrizedServiceDescr.java
 *
 * Created on 12. 3. 2021, 18:40:04 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.Service;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.impl.ParameterString;

/**
 * A generic service description.
 * 
 * @author burgetr
 */
@Schema(name="ParametrizedServiceDescr", description="Description of a parametrized service")
public class ParametrizedServiceDescr
{
    @Schema(description = "Service ID", required = true)
    public String id;
    @Schema(description = "Service name", required = true)
    public String name;
    @Schema(description = "Service description", required = true)
    public String description;
    @Schema(description = "Type IRI of the input artifacts", required = false)
    public String consumes;
    @Schema(description = "Type IRI of the produced artifacts", required = true)
    public String produces;
    @Schema(description = "Service category used for grouping services", required = false)
    public String category;
    @Schema(description = "Descriptions of accepted parameters", required = false)
    public Map<String, ParamDescr> params;
    
    public ParametrizedServiceDescr(Service service)
    {
        id = service.getId();
        name = service.getName();
        description = service.getDescription();
        category = service.getCategory();
        
        if (service instanceof ParametrizedOperation)
            addParams((ParametrizedOperation) service);
    }

    private void addParams(ParametrizedOperation op)
    {
        params = new LinkedHashMap<>();
        for (Parameter p : op.getParams())
        {
            final ParamDescr descr;
            if (p instanceof ParameterBoolean)
                descr = new ParamBooleanDescr((ParameterBoolean) p);
            else if (p instanceof ParameterInt)
                descr = new ParamIntDescr((ParameterInt) p);
            else if (p instanceof ParameterFloat)
                descr = new ParamFloatDescr((ParameterFloat) p);
            else if (p instanceof ParameterString)
                descr = new ParamStringDescr((ParameterString) p);
            else
                descr = new ParamDescr(p, "unknown");
            params.put(p.getName(), descr);
        }
    }
}
