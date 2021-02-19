/**
 * AdminResource.java
 *
 * Created on 25. 9. 2020, 14:19:17 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.ejb.StorageService;

/**
 * 
 * @author burgetr
 */
@Path("admin")
public class AdminResource
{
    @Inject
    private StorageService storage;

    
    @GET
    @Path("/checkRepo")
    public Response checkRepo()
    {
        Value val = storage.getStorage().getPropertyValue(BOX.Page, RDF.TYPE);
        if (val != null)
            return Response.ok(new ResultValue("Repository metadata is present.")).build();
        else
            return Response.ok(new ResultErrorMessage("Repository has not been initialized. Use /initRepo to fix this.")).build();
    }
    
    @GET
    @Path("/initRepo")
    public Response initRepo()
    {
        if (storage.getArtifactRepository().isInitialized())
            return Response.ok(new ResultValue("ok")).build();
        else
            return Response.serverError().entity("error during repository initialization").build();
    }
    
}
