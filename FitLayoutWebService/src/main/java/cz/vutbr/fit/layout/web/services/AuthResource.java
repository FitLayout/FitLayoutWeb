/**
 * AuthResource.java
 *
 * Created on 16. 4. 2021, 18:38:52 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.security.Principal;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.data.UserInfo;

/**
 * 
 * @author burgetr
 */
@Path("/auth")
public class AuthResource
{
    @Inject
    private Principal principal;
    
    @GET
    @Path("userInfo")
    public Response getUserInfo()
    {
        UserInfo info = new UserInfo(principal);
        return Response.ok(new ResultValue(info)).build();
    }
    
}
