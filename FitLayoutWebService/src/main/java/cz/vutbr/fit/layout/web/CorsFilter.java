/**
 * CorsFilter.java
 *
 * Created on 15. 12. 2020, 20:54:25 by burgetr
 */
package cz.vutbr.fit.layout.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * 
 * @author burgetr
 */
@Provider
public class CorsFilter implements ContainerResponseFilter
{
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException
    {
        final var headers = responseContext.getHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        
        // accept CORS handshake
        if ("OPTIONS".equals(requestContext.getRequest().getMethod())) 
        {
            responseContext.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }
}
