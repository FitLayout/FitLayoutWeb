/**
 * RepositoryOperatorResource.java
 *
 * Created on 17. 1. 2022, 11:19:09 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.ArrayList;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.web.FLConfig;
import cz.vutbr.fit.layout.web.data.ParametrizedServiceDescr;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.ejb.StorageService;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * Global area operator management and configuration.
 * 
 * @author burgetr
 */
@Path("r/{repoId}/operator")
@Tag(name = "repositoryOperator", description = "Repository-specific operator config")
public class RepositoryOperatorResource
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
    @Operation(operationId = "repositoryListOperators", summary = "Gets a list of available area tree operator services for the repository.")
    @APIResponse(responseCode = "200", description = "List of service descriptions",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ParametrizedServiceDescr.class)))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response listOperators()
    {
        final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
        if (repo != null)
        {
            ServiceManager sm = FLConfig.createServiceManager(repo);
            var ops = sm.findAreaTreeOperators();
            var result = new ArrayList<ParametrizedServiceDescr>();
            for (AreaTreeOperator op : ops.values())
            {
                result.add(new ParametrizedServiceDescr(op));
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

}
