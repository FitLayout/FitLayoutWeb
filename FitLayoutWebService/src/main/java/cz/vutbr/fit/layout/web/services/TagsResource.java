/**
 * TagsResource.java
 *
 * Created on 4. 1. 2022, 18:23:25 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.TagInfo;
import cz.vutbr.fit.layout.web.ejb.StorageService;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * 
 * @author burgetr
 */
@Path("r/{repoId}/tags")
@Tag(name = "tags", description = "Tag operations")
public class TagsResource
{
    @Inject
    private UserService userService;
    @Inject
    private StorageService storage;

    @PathParam("repoId")
    @Parameter(description = "The ID of the artifact repository to use", required = true)
    private String repoId;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "getTags", summary = "Reads a list of tags defined in the repository")
    @APIResponse(responseCode = "200", description = "The list of tags",
            content = @Content(schema = @Schema(ref = "TagInfo")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
        content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response getTags()
    {
        final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
        if (repo != null)
        {
            Collection<cz.vutbr.fit.layout.model.Tag> rdfTags = repo.getTags();
            List<TagInfo> tags = rdfTags.stream().map(t -> new TagInfo(t)).collect(Collectors.toList());
            return Response.ok(tags).build();
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
