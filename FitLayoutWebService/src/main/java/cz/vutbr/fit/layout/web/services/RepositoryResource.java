/**
 * RepositoryResource.java
 *
 * Created on 24. 3. 2021, 17:07:02 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.rdf4j.query.BindingSet;

import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.rdf.StorageException;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.data.SelectQueryResult;
import cz.vutbr.fit.layout.web.ejb.StorageService;

/**
 * Direct repository querying.
 * 
 * @author burgetr
 */
@Path("repository")
public class RepositoryResource
{
    @Inject
    private StorageService storage;

    @POST
    @Consumes(Serialization.SPARQL_QUERY)
    @Produces(MediaType.APPLICATION_JSON)
    public Response repositoryQuery(String query)
    {
        try {
            final List<BindingSet> bindings = storage.getStorage().executeSafeTupleQuery(query);
            final SelectQueryResult result = new SelectQueryResult(bindings);
            return Response.ok(new ResultValue(result)).build();
        } catch (StorageException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
}
