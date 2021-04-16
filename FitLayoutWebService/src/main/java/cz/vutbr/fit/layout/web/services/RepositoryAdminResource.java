/**
 * RepositoryAdminResource.java
 *
 * Created on 15. 4. 2021, 20:50:38 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.ejb.StorageService;

/**
 * 
 * @author burgetr
 */
@Path("repository")
public class RepositoryAdminResource
{
    @Inject
    private StorageService storage;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listRepositories()
    {
        return Response.ok(new ResultValue(storage.getRepositories())).build();
    }
    
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus()
    {
        return Response.ok(new ResultValue(storage.getStatus())).build();
    }
    
}
