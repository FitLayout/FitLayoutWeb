/**
 * AuthResource.java
 *
 * Created on 16. 4. 2021, 18:38:52 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.data.UserInfo;
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
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(summary = "Get current user information based on the credentials (Bearer JWT token) obtained")
    @APIResponse(responseCode = "200", description = "Current user information",
            content = @Content(schema = @Schema(type = SchemaType.OBJECT, implementation = UserInfo.class)))
    public Response getUserInfo()
    {
        return Response.ok(new ResultValue(userService.getUser())).build();
    }

}
