package cz.vutbr.fit.layout.web.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/api")
public interface PingResourceClient {

    @GET
    @Path("/service/ping")
    Response ping();
    
}