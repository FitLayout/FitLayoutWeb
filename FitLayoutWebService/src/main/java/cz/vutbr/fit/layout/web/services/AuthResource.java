/**
 * AuthResource.java
 *
 * Created on 16. 4. 2021, 18:38:52 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * 
 * @author burgetr
 */
@Path("/auth")
@Tags(value = @Tag(name = "auth", description = "User authorization"))
public class AuthResource
{
    @Inject
    private UserService userService;
    
    @GET
    @Path("userInfo")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "getUserInfo", summary = "Get current user information based on the credentials (Bearer JWT token) obtained")
    @SecurityRequirement(name = "jwt", scopes = {})
    @APIResponse(responseCode = "200", description = "Current user information",
            content = @Content(schema = @Schema(ref="UserInfo")))
    public Response getUserInfo()
    {
        return Response.ok(userService.getUser()).build();
    }

}
