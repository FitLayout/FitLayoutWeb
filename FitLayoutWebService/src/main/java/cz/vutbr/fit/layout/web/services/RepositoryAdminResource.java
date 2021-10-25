/**
 * RepositoryAdminResource.java
 *
 * Created on 15. 4. 2021, 20:50:38 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.web.data.RepositoryInfo;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.data.UserInfo;
import cz.vutbr.fit.layout.web.ejb.MailerService;
import cz.vutbr.fit.layout.web.ejb.StorageService;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * 
 * @author burgetr
 */
@Path("repository")
@Tag(name = "admin", description = "Repository administration")
public class RepositoryAdminResource
{
    @Inject
    private UserService userService;
    @Inject
    private StorageService storage;
    @Inject
    private MailerService mailer;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "listRepositories", summary = "Gets a list of available repositories for current user.")
    @APIResponse(responseCode = "200", description = "List of repository information",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = RepositoryInfo.class)))    
    public Response listRepositories()
    {
        var user = userService.getUser();
        if (user != null && !user.isGuest() && !user.isAnonymous())
            return Response.ok(storage.getRepositories(user)).build();
        else
            return Response.ok(List.of()).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "createRepository", summary = "Creates a new repository.")
    @APIResponse(responseCode = "200", description = "The new repository description",
            content = @Content(schema = @Schema(ref = "RepositoryInfo")))    
    public Response createRepository(RepositoryInfo data)
    {
        if (data != null)
        {
            try {
                UserInfo user = userService.getUser();
                if (!user.isAnonymous() && user.getEmail() != null)
                    data.setEmail(user.getEmail()); //force user e-mail for the repository (when available)
                storage.createRepository(userService.getUser(), data);
                return Response.ok(data).build();
            } catch (RepositoryException e) {
                return Response.serverError()
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(e.getMessage()))
                        .build();
            }
        }
        else
        {
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage("Bad repository data"))
                    .build();
        }
    }
    
    @GET
    @Path("/{repoId}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "getRepositoryInfo", summary = "Gets information about a repository identified by its ID.")
    @APIResponse(responseCode = "200", description = "Selected repository information",
            content = @Content(schema = @Schema(ref = "RepositoryInfo")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found")    
    public Response getRepositoryInfo(@PathParam("repoId") String repositoryId)
    {
        if (repositoryId != null)
        {
            try {
                var info = storage.getRepositoryInfo(userService.getUser(), repositoryId);
                if (info != null)
                    return Response.ok(info).build();
                else
                    return Response.status(Status.NOT_FOUND)
                            .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                            .build();
            } catch (RepositoryException e) {
                return Response.serverError()
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(e.getMessage()))
                        .build();
            }
        }
        else
        {
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage("Repository id missing"))
                    .build();
        }
    }
    
    @PUT
    @Path("/{repoId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "updateRepositoryInfo", summary = "Gets information about a repository identified by its ID.")
    @APIResponse(responseCode = "200", description = "The updated repository description",
            content = @Content(schema = @Schema(ref = "RepositoryInfo")))    
    @APIResponse(responseCode = "400", description = "Invalid service parametres",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found")    
    public Response updateRepositoryInfo(@PathParam("repoId") String repositoryId, RepositoryInfo data)
    {
        if (repositoryId != null)
        {
            try {
                var info = storage.updateRepository(userService.getUser(), repositoryId, data);
                if (info != null)
                    return Response.ok(info).build();
                else
                    return Response.status(Status.NOT_FOUND)
                            .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                            .build();
            } catch (RepositoryException e) {
                return Response.serverError()
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(e.getMessage()))
                        .build();
            }
        }
        else
        {
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage("Repository id missing"))
                    .build();
        }
    }
    
    @DELETE
    @Path("/{repoId}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "deleteRepository", summary = "Deletes a repository identified by its ID.")
    @APIResponse(responseCode = "200", description = "Repository deleted",
            content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found")    
    public Response deleteRepository(@PathParam("repoId") String repositoryId)
    {
        if (repositoryId != null)
        {
            try {
                storage.deleteRepository(userService.getUser(), repositoryId);
                return Response.ok(new ResultValue(null)).build();
            } catch (RepositoryException e) {
                return Response.serverError()
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(e.getMessage()))
                        .build();
            }
        }
        else
        {
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage("Repository id missing"))
                    .build();
        }
    }
    
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    @Operation(operationId = "listAllRepositories", summary = "Gets a list of all available repositories (admin only).")
    @APIResponse(responseCode = "200", description = "List of repository information",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = RepositoryInfo.class)))    
    @APIResponse(responseCode = "403", description = "Not authorized")    
    @SecurityRequirement(name = "jwt")
    public Response listAllRepositories()
    {
        List<RepositoryInfo> list = storage.getAllRepositories();
        return Response.ok(list).build();
    }
    
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "status", summary = "Gets overall storage status.")
    @APIResponse(responseCode = "200", description = "Selected repository information",
            content = @Content(schema = @Schema(ref = "StorageStatus")))    
    public Response getStatus()
    {
        return Response.ok(storage.getStatus(userService.getUser())).build();
    }
    
    @GET
    @Path("/remind/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "sendRepositoriesReminder", summary = "Sends an e-mail reminder containing all repositories that have the given e-mail assigned")
    @APIResponse(responseCode = "200", description = "E-mail sent (or nothing set if no repository is assigned to the given e-mail",
            content = @Content(schema = @Schema(ref = "ResultValue")))    
    public Response sendRepositoriesReminder(@PathParam("email") String email)
    {
        if (email != null)
        {
            var list = storage.getRepositoriesForEmail(email);
            if (!list.isEmpty())
            {
                try {
                    mailer.sendRepositoryInfo(email, list);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Response.serverError()
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ResultErrorMessage("E-mail sending failed"))
                            .build();
                }
            }
        }
        return Response.ok(new ResultValue("ok")).build();
    }
    
}
