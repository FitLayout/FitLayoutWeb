/**
 * ArtifactResource.java
 *
 * Created on 9.9.2020, 17:48:26 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.rdf4j.model.IRI;

/**
 * 
 * @author burgetr
 */
@Path("artifact")
public class ArtifactResource extends BaseStorageResource
{

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listArtifacts()
    {
        Set<IRI> list = getStorage().getArtifactIRIs();
        return Response.ok(list).build();
    }
    
}
