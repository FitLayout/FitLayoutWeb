package airhacks.service.ping.boundary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("ping")
public interface PingResourceClient {

    @GET
    Response ping();
    
}