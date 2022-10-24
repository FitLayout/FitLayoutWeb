/**
 * OperatorResource.java
 *
 * Created on 12. 3. 2021, 18:34:37 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.ArrayList;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.web.FLConfig;
import cz.vutbr.fit.layout.web.data.ParametrizedServiceDescr;

/**
 * Global area operator management and configuration.
 * 
 * @author burgetr
 */
@Path("operator")
@Tags(value = @Tag(ref = "service"))
public class OperatorResource
{
    private ServiceManager sm;
    
    
    @PostConstruct
    public void init()
    {
        sm = FLConfig.createServiceManager(null);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "listOperators", summary = "Gets a list of available area tree operator services.")
    @APIResponse(responseCode = "200", description = "List of service descriptions",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ParametrizedServiceDescr.class)))    
    public Response listOperators()
    {
        var ops = sm.findAreaTreeOperators();
        var result = new ArrayList<ParametrizedServiceDescr>();
        for (AreaTreeOperator op : ops.values())
        {
            result.add(new ParametrizedServiceDescr(op));
        }
        return Response.ok(result).build();
    }

}
