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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.web.data.RepositoryInfo;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.ejb.MailerService;
import cz.vutbr.fit.layout.web.ejb.StorageService;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * 
 * @author burgetr
 */
@Path("repository")
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
    public Response listRepositories()
    {
        var user = userService.getUser();
        if (user != null && !user.isGuest() && !user.isAnonymous())
            return Response.ok(new ResultValue(storage.getRepositories(user))).build();
        else
            return Response.ok(new ResultValue(List.of())).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response createRepository(RepositoryInfo data)
    {
        if (data != null)
        {
            try {
                storage.createRepository(userService.getUser(), data);
                return Response.ok(new ResultValue(data)).build();
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
    public Response getRepositoryInfo(@PathParam("repoId") String repositoryId)
    {
        if (repositoryId != null)
        {
            try {
                var info = storage.getRepositoryInfo(userService.getUser(), repositoryId);
                if (info != null)
                    return Response.ok(new ResultValue(info)).build();
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
    public Response getAllRepositories()
    {
        List<RepositoryInfo> list = storage.getAllRepositories();
        return Response.ok(new ResultValue(list)).build();
    }
    
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getStatus()
    {
        return Response.ok(new ResultValue(storage.getStatus(userService.getUser()))).build();
    }
    
    @GET
    @Path("/remind/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
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
                    return Response.serverError()
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ResultErrorMessage(e.getMessage()))
                            .build();
                }
            }
        }
        return Response.ok(new ResultValue("ok")).build();
    }
    
}
