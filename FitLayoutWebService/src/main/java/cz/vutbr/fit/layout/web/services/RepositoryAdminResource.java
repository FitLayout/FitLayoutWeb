/**
 * RepositoryAdminResource.java
 *
 * Created on 15. 4. 2021, 20:50:38 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.security.Principal;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.web.data.RepositoryInfo;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.data.UserInfo;
import cz.vutbr.fit.layout.web.ejb.StorageService;

/**
 * 
 * @author burgetr
 */
@Path("repository")
public class RepositoryAdminResource
{
    @Inject
    private Principal principal;
    @Inject
    private StorageService storage;
    private UserInfo user;

    @PostConstruct
    public void init()
    {
        user = new UserInfo(principal);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listRepositories()
    {
        return Response.ok(new ResultValue(storage.getRepositories(user.getUserId()))).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRepository(RepositoryInfo data)
    {
        if (data != null && data.id != null)
        {
            try {
                storage.createRepository(user.getUserId(), data);
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
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus()
    {
        return Response.ok(new ResultValue(storage.getStatus(user.getUserId()))).build();
    }
    
}
