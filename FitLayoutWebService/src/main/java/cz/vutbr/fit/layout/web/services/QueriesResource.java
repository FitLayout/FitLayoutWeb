/**
 * QueriesResource.java
 *
 * Created on 29. 5. 2023, 12:41:24 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.ArrayList;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.SavedQuery;
import cz.vutbr.fit.layout.rdf.StorageException;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.data.SavedQueryDTO;
import cz.vutbr.fit.layout.web.ejb.StorageService;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * Saved query management endpoint.
 * 
 * @author burgetr
 */
@Path("r/{repoId}/query")
@Tag(name = "query", description = "Saved query management")
public class QueriesResource
{
    @Inject
    private UserService userService;
    @Inject
    private StorageService storage;

    @PathParam("repoId")
    @Parameter(description = "The ID of the artifact repository to use", required = true)
    private String repoId;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "list", summary = "Obtains all the saved queries")
    @APIResponse(responseCode = "200", description = "Saved query list")    
    public Response getList()
    {
        try {
            RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                var queryMap = repo.getSavedQueries();
                var ret = new ArrayList<SavedQueryDTO>(queryMap.size());
                for (SavedQuery q : queryMap.values())
                {
                    var dq = new SavedQueryDTO(q);
                    dq.setId(decodeQueryURI(q.getIri()));
                    ret.add(dq);
                }
                return Response.ok(new ResultValue(ret)).build();
            }
            else
            {
                return Response.status(Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                        .build();
            }
        } catch (StorageException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "getQuery", summary = "Obtains a saved query")
    @APIResponse(responseCode = "200", description = "A saved query")    
    public Response getQuery(@PathParam("id") Long id)
    {
        try {
            RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                var queryMap = repo.getSavedQueries();
                if (id == null)
                    id = -1L;
                IRI iri = repo.getIriFactory().createSavedQueryURI(id);
                if (queryMap.containsKey(iri))
                {
                    var q = queryMap.get(iri);
                    var dq = new SavedQueryDTO(q);
                    dq.setId(decodeQueryURI(q.getIri()));
                    return Response.ok(new ResultValue(dq)).build();
                }
                else
                {
                    return Response.status(Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ResultErrorMessage("No such query"))
                            .build();
                }
            }
            else
            {
                return Response.status(Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                        .build();
            }
        } catch (StorageException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "addQuery", summary = "Adds a saved a query")
    @APIResponse(responseCode = "200", description = "Query saved")    
    public Response addQuery(SavedQueryDTO query)
    {
        try {
            RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                if (query.getTitle() != null && query.getQueryString() != null)
                {
                    SavedQuery newQuery = repo.getSavedQueryByTitle(query.getTitle()); // is there already a query with the same title
                    if (newQuery != null)
                        newQuery.setQueryString(query.getQueryString()); // yes - update query string
                    else
                        newQuery = new SavedQuery(query.getTitle(), query.getQueryString()); // no - create new query
                    
                    repo.saveQuery(newQuery);
                    // return the new query
                    var ret = new SavedQueryDTO(newQuery);
                    ret.setId(decodeQueryURI(newQuery.getIri()));
                    return Response.ok(new ResultValue(ret)).build();
                }
                else
                {
                    return Response.status(Status.BAD_REQUEST)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ResultErrorMessage("Invalid query specification"))
                            .build();
                }
            }
            else
            {
                return Response.status(Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                        .build();
            }
        } catch (StorageException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "deleteQuery", summary = "Deletes a saved query")
    @APIResponse(responseCode = "200", description = "Query deleted")    
    public Response deleteQuery(@PathParam("id") Long id)
    {
        try {
            RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                var queryMap = repo.getSavedQueries();
                if (id == null)
                    id = -1L;
                IRI iri = repo.getIriFactory().createSavedQueryURI(id);
                if (queryMap.containsKey(iri))
                {
                    repo.deleteSavedQuery(iri);
                    return Response.ok().build();
                }
                else
                {
                    return Response.status(Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ResultErrorMessage("No such query"))
                            .build();
                }
            }
            else
            {
                return Response.status(Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                        .build();
            }
        } catch (StorageException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
    private long decodeQueryURI(IRI iri)
    {
        final String loc = iri.getLocalName();
        if (loc != null && loc.startsWith("query-"))
        {
            String num = loc.substring(6);
            return Long.valueOf(num);
        }
        else
            return 0;
    }

    
}
