/**
 * RepositoryAdminResource.java
 *
 * Created on 15. 4. 2021, 20:50:38 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import javax.annotation.security.PermitAll;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response listRepositories()
    {
        return Response.ok(new ResultValue(storage.getRepositories(userService.getUser()))).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response createRepository(RepositoryInfo data)
    {
        if (data != null && data.id != null)
        {
            try {
                storage.createRepository(userService.getUser(), data);
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
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getStatus()
    {
        return Response.ok(new ResultValue(storage.getStatus(userService.getUser()))).build();
    }
    
}
