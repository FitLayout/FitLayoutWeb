/**
 * RepositoryResource.java
 *
 * Created on 24. 3. 2021, 17:07:02 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;

import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.rdf.StorageException;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.data.SelectQueryResult;
import cz.vutbr.fit.layout.web.data.SubjectDescriptionResult;
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
    
    @GET
    @Path("/subject/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response querySubject(@PathParam("iri") String iriValue, @DefaultValue("100") @QueryParam("limit") int limit)
    {
        try {
            final IRI iri = storage.getArtifactRepository().getIriDecoder().decodeIri(iriValue);
            final String query = "SELECT ?p ?v WHERE { <" + iri.toString() + "> ?p ?v } LIMIT " + limit;
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
    
    @GET
    @Path("/object/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryObject(@PathParam("iri") String iriValue, @DefaultValue("100") @QueryParam("limit") int limit)
    {
        try {
            final IRI iri = storage.getArtifactRepository().getIriDecoder().decodeIri(iriValue);
            final String query = "SELECT ?v ?p WHERE { ?v ?p <" + iri.toString() + "> } LIMIT " + limit;
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
    
    @GET
    @Path("/subject/{subjIri}/{propertyIri}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSubjectValue(@PathParam("subjIri") String subjIriValue, @PathParam("propertyIri") String propertyIriValue)
    {
        try {
            final IRI subjIri = storage.getArtifactRepository().getIriDecoder().decodeIri(subjIriValue);
            final IRI propertyIri = storage.getArtifactRepository().getIriDecoder().decodeIri(propertyIriValue);
            final Value val = storage.getStorage().getPropertyValue(subjIri, propertyIri);
            if (val instanceof IRI)
            {
                final var ret = new SelectQueryResult.IriBinding((IRI) val);
                return Response.ok(new ResultValue(ret)).build();
            }
            else if (val instanceof Literal)
            {
                final var ret = new SelectQueryResult.LiteralBinding(val.stringValue(), ((Literal) val).getDatatype());
                return Response.ok(new ResultValue(ret)).build();
            }
            else
            {
                final var ret = new SelectQueryResult.NullBinding();
                return Response.ok(new ResultValue(ret)).build();
            }
        } catch (StorageException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/type/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSubjectType(@PathParam("iri") String iriValue)
    {
        try {
            final IRI iri = storage.getArtifactRepository().getIriDecoder().decodeIri(iriValue);
            final IRI type = storage.getStorage().getSubjectType(iri);
            if (type != null)
                return Response.ok(new ResultValue(type.toString())).build();
            else
                return Response.ok(new ResultValue("unknown")).build();
        } catch (StorageException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/describe/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response describeSubject(@PathParam("iri") String iriValue)
    {
        try {
            final IRI iri = storage.getArtifactRepository().getIriDecoder().decodeIri(iriValue);
            final String query = "SELECT ?p ?v WHERE { <" + iri.toString() + "> ?p ?v }";
            final List<BindingSet> bindings = storage.getStorage().executeSafeTupleQuery(query);
            final SubjectDescriptionResult result = new SubjectDescriptionResult(bindings);
            return Response.ok(new ResultValue(result)).build();
        } catch (StorageException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
}
