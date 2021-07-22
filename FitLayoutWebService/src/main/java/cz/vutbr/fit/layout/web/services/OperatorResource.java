/**
 * OperatorResource.java
 *
 * Created on 12. 3. 2021, 18:34:37 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.web.FLConfig;
import cz.vutbr.fit.layout.web.data.ParametrizedServiceDescr;
import cz.vutbr.fit.layout.web.data.ResultValue;

/**
 * Area operator management and configuration.
 * 
 * @author burgetr
 */
@Path("operator")
public class OperatorResource
{
    private ServiceManager sm;
    
    
    @PostConstruct
    public void init()
    {
        sm = FLConfig.createServiceManager(null);
    }
    
    @GET
    @PermitAll
    public Response listOperators()
    {
        var ops = sm.findAreaTreeOperators();
        var result = new ArrayList<ParametrizedServiceDescr>();
        for (AreaTreeOperator op : ops.values())
        {
            result.add(new ParametrizedServiceDescr(op));
        }
        return Response.ok(new ResultValue(result)).build();
    }

}
