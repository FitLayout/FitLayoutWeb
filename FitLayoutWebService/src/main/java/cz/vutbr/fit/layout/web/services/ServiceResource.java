
package cz.vutbr.fit.layout.web.services;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.rdf4j.model.Model;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.rdf.BoxModelBuilder;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.web.FLConfig;
import cz.vutbr.fit.layout.web.data.ArtifactServiceDescr;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ServiceParams;

/**
 * Global service listing and invocation. This maintains a global configuration common
 * for all repositories.
 * 
 * @author burgetr
 */
@Path("service")
@Tag(name = "service", description = "Global service discovery and invocation")
public class ServiceResource
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
    @Operation(operationId = "getServiceList", summary = "Gets a list of available artifact services.")
    @APIResponse(responseCode = "200", description = "List of service descriptions",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ArtifactServiceDescr.class)))    
    public Response getServiceList()
    {
        var services = sm.findArtifactSevices().values();
        var result = new ArrayList<ArtifactServiceDescr>();
        for (ArtifactService serv : services)
        {
            result.add(new ArtifactServiceDescr(serv));
        }
        return Response.ok(result).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({Serialization.JSONLD, Serialization.TURTLE, Serialization.RDFXML, Serialization.NTRIPLES, Serialization.NQUADS})
    @PermitAll
    @Operation(operationId = "invoke", summary = "Invokes a service and returns the resulting artifact")
    @APIResponse(responseCode = "200", description = "The complete artifact data")    
    public Response invoke(@HeaderParam("Accept") String accept, ServiceParams params)
    {
        return invoke(params, accept);
    }

    //===========================================================================================
    
    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "getServiceConfig", summary = "Gets the default configuration of an artifact service.")
    @APIResponse(responseCode = "200", description = "Service configuration",
            content = @Content(schema = @Schema(ref = "ServiceParams")))    
    public Response getServiceConfig(@QueryParam("id") String serviceId)
    {
        ParametrizedOperation op = sm.findParmetrizedService(serviceId);
        if (op != null)
        {
            Map<String, Object> p = ServiceManager.getServiceParams(op);
            ServiceParams params = new ServiceParams(serviceId, p);
            return Response.ok(params).build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND).entity(new ResultErrorMessage(ResultErrorMessage.E_NO_SERVICE)).build();
        }
    }
    
    //===========================================================================================
    
    @GET
    @Path("/ping")
    @PermitAll
    @Operation(operationId = "ping", summary = "Returns 'ok'.")
    public String ping()
    {
        return "ok";
    }
    
    //===========================================================================================

    public Response invoke(ServiceParams params, String mimeType)
    {
        ParametrizedOperation op = null;
        if (params.getServiceId() != null)
            op = sm.findParmetrizedService(params.getServiceId());
        
        if (op != null)
        {
            ServiceManager.setServiceParams(op, params.getParams());
            Artifact page = ((ArtifactService) op).process(null);
            
            if (page != null)
            {
                BoxModelBuilder builder = new BoxModelBuilder(((RDFArtifactRepository) sm.getArtifactRepository()).getIriFactory());
                Model graph = builder.createGraph(page);
                
                StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream os) throws IOException, WebApplicationException {
                        Serialization.modelToStream(graph, os, mimeType);
                    }
                };
                
                return Response.ok(stream).build();
            }
            else
            {
                return Response.ok().build(); // no artifact produced
            }
        }
        else
        {
            return Response.status(Status.NOT_FOUND).entity(new ResultErrorMessage(ResultErrorMessage.E_NO_SERVICE)).build();
        }
    }
    
}
