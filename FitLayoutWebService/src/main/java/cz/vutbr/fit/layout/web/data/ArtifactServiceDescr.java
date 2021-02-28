/**
 * ArtifactServiceDescr.java
 *
 * Created on 28. 2. 2021, 18:32:13 by burgetr
 */
package cz.vutbr.fit.layout.web.data;

import java.util.HashMap;
import java.util.Map;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.impl.ParameterString;

/**
 * 
 * @author burgetr
 */
public class ArtifactServiceDescr
{
    public String id;
    public String consumes;
    public String produces;
    public Map<String, ParamDescr> params;
    
    public ArtifactServiceDescr(ArtifactService service)
    {
        id = service.getId();
        if (service.getConsumes() != null)
            consumes = service.getConsumes().toString();
        if (service.getProduces() != null)
            produces = service.getProduces().toString();
        
        if (service instanceof ParametrizedOperation)
            addParams((ParametrizedOperation) service);
    }

    private void addParams(ParametrizedOperation op)
    {
        params = new HashMap<>();
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
