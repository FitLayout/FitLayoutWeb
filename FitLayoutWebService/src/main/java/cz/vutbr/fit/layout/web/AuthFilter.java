/**
 * AuthFilter.java
 *
 * Created on 2. 5. 2021, 10:52:40 by burgetr
 */
package cz.vutbr.fit.layout.web;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * 
 * @author burgetr
 */
@Provider
public class AuthFilter implements ContainerRequestFilter
{
    @Inject
    private UserService userService;

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException
    {
        if (!userService.isAuthorized())
        {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage("not authorized")).build());
        }
    }
    
}
