/**
 * AuthResource.java
 *
 * Created on 16. 4. 2021, 18:38:52 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * 
 * @author burgetr
 */
@Path("/auth")
public class AuthResource
{
    @Inject
    private UserService userService;
    
    @GET
    @Path("userInfo")
    public Response getUserInfo()
    {
        return Response.ok(new ResultValue(userService.getUser())).build();
    }
    
}
