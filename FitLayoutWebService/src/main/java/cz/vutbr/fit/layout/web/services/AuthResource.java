/**
 * AuthResource.java
 *
 * Created on 16. 4. 2021, 18:38:52 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.Set;
import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import cz.vutbr.fit.layout.web.JwtTokenGenerator;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * 
 * @author burgetr
 */
@Path("/auth")
public class AuthResource
{
    private static final long TOKEN_DURATION = 7200; // token duration in seconds
    
    @Inject
    private UserService userService;
    
    @GET
    @Path("userInfo")
    @PermitAll
    public Response getUserInfo()
    {
        return Response.ok(new ResultValue(userService.getUser())).build();
    }
    
    @GET
    @Path("getToken")
    @PermitAll
    public Response getToken()
    {
        try
        {
            UUID uuid = UUID.randomUUID();
            String token = JwtTokenGenerator.generateJWTString("/jwt-token.json", uuid.toString(), 
                    TOKEN_DURATION, Set.of("user"));
            return Response.ok(new ResultValue(token)).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ResultErrorMessage(e.getMessage())).build();
        }
    }
    
}
