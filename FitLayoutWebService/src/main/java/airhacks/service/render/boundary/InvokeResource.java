
package airhacks.service.render.boundary;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceManager;

/**
 *
 * @author burgetr
 */
@Path("invoke")
public class InvokeResource
{

    @Inject
    @ConfigProperty(name = "message")
    String message;

    @GET
    public String ping()
    {
        return "ok";
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response invoke(ServiceParams params)
    {
        ParametrizedOperation op = ServiceManager.findParmetrizedService(params.getServiceId());
        if (op != null)
        {
            ServiceManager.setServiceParams(op, params.getParams());
            return Response.ok("").build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND).entity("{\"error\": \"No such service\"}").build();
        }
    }

}
