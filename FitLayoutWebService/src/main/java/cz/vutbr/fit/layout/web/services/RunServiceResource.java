/**
 * RunServiceResource.java
 *
 * Created on 31. 10. 2023, 9:22:41 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.StorageException;
import cz.vutbr.fit.layout.web.algorithm.JsonPageCreator;
import cz.vutbr.fit.layout.web.data.Result;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.ejb.StorageService;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * Endpoints for running different analytic services on a repository.
 * 
 * @author burgetr
 */
@Path("r/{repoId}/run")
@Tag(name = "runService", description = "Running different analytic services on a repository")
public class RunServiceResource
{
    @Inject
    private UserService userService;
    @Inject
    private StorageService storage;

    @PathParam("repoId")
    @Parameter(description = "The ID of the artifact repository to use", required = true)
    private String repoId;

    @POST
    @Path("/renderJson")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @Operation(operationId = "renderJsonPage", summary = "Creates a new Page artifact from a JSON page description (e.g. obtained from a puppeteer backend or a client browser extension).")
    public Response renderJsonPage(InputStream istream,
            @HeaderParam("Accept") String accept)
    {
        final boolean jsonRequired = MediaType.APPLICATION_JSON.equals(accept);
        try {
            
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                JsonPageCreator builder = new JsonPageCreator();
                Page page = builder.renderInputStream(istream, "UTF-8");
                //page.setCreatorParams("...");
                repo.addArtifact(page);
                return createOkResponse(page, jsonRequired);
            }
            else
            {
                return createErrorResponse(Status.NOT_FOUND, ResultErrorMessage.E_NO_REPO, jsonRequired);
            }
            
        } catch (StorageException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        } catch (IOException | RuntimeException e) {
            //e.printStackTrace();
            return createErrorResponse(Status.BAD_REQUEST, e.getMessage(), jsonRequired);
        }

    }
    
    private Response createOkResponse(Page page, boolean jsonRequired)
    {
        if (jsonRequired) 
        {
            var result = new Result(Result.OK);
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(result)
                    .build();
        }
        else
        {
            String message = "ok, " + page.getWidth() + " x " + page.getHeight() + " : " + page.getTitle() + " " + page.getIri();
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.TEXT_HTML)
                    .entity(createOkMessage(message))
                    .build();
        }
    }
    
    private Response createErrorResponse(Status status, String message, boolean jsonRequired)
    {
        if (jsonRequired) 
        {
            var result = new ResultErrorMessage(message);
            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(result)
                    .build();
        }
        else
        {
            return Response.status(status)
                    .type(MediaType.TEXT_HTML)
                    .entity(createErrorMessage(message))
                    .build();
        }
    }
    
    private String createOkMessage(String message)
    {
        return "<div>" + message + "</div>";
    }
    
    private String createErrorMessage(String error)
    {
        return "<div>" + error + "</div>";
    }
    
}
