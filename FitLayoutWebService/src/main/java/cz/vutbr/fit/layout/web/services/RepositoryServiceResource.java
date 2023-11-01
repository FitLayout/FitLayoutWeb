/**
 * RepositoryServiceResource.java
 *
 * Created on 17. 1. 2022, 11:07:04 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
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
import cz.vutbr.fit.layout.web.ejb.StorageService;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * Service management specific for individual repositories.
 * 
 * @author burgetr
 */
@Path("r/{repoId}/service")
@Tag(name = "repositoryService", description = "Repository-specific service config and invocation")
public class RepositoryServiceResource
{
    @Inject
    private UserService userService;
    @Inject
    private StorageService storage;

    @PathParam("repoId")
    @Parameter(description = "The ID of the artifact repository to use", required = true)
    private String repoId;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "repositoryGetServiceList", summary = "Gets a list of available artifact services for the repository.")
    @APIResponse(responseCode = "200", description = "List of service descriptions",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ArtifactServiceDescr.class)))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response getRepoServiceList()
    {
        final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
        if (repo != null)
        {
            ServiceManager sm = FLConfig.createServiceManager(repo);
            var services = sm.findArtifactSevices().values();
            var result = new ArrayList<ArtifactServiceDescr>();
            for (ArtifactService serv : services)
            {
                result.add(new ArtifactServiceDescr(serv));
            }
            return Response.ok(result).build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                    .build();
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({Serialization.JSONLD, Serialization.TURTLE, Serialization.RDFXML, Serialization.NTRIPLES, Serialization.NQUADS})
    @PermitAll
    @Operation(operationId = "repositoryInvoke", summary = "Invokes a service and returns the resulting artifact")
    @APIResponse(responseCode = "200", description = "The complete artifact data")    
    @APIResponse(responseCode = "404", description = "Repository or service with the given ID not found",
                content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response invoke(@HeaderParam("Accept") String accept, ServiceParams params)
    {
        final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
        if (repo != null)
        {
            ServiceManager sm = FLConfig.createServiceManager(repo);
            return invoke(sm, params, accept);
        }
        else
        {
            return Response.status(Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                    .build();
        }
    }

    //===========================================================================================
    
    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "getRepoServiceConfig", summary = "Gets the default configuration of an artifact service.")
    @APIResponse(responseCode = "200", description = "Service configuration",
            content = @Content(schema = @Schema(ref = "ServiceParams")))    
    @APIResponse(responseCode = "404", description = "Repository or service with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response getServiceConfig(@QueryParam("id") String serviceId)
    {
        final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
        if (repo != null)
        {
            ServiceManager sm = FLConfig.createServiceManager(repo);
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
        else
        {
            return Response.status(Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                    .build();
        }
    }
    
    //===========================================================================================

    public Response invoke(ServiceManager sm, ServiceParams params, String mimeType)
    {
        ParametrizedOperation op = null;
        if (params.getServiceId() != null)
            op = sm.findParmetrizedService(params.getServiceId());
        
        if (op != null)
        {
            ServiceManager.setServiceParams(op, params.getParams());
            Artifact page = ((ArtifactService) op).process(null);
            
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
            return Response.status(Status.NOT_FOUND).entity(new ResultErrorMessage(ResultErrorMessage.E_NO_SERVICE)).build();
        }
    }
    
}
