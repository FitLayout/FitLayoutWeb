/**
 * RepositoryResource.java
 *
 * Created on 24. 3. 2021, 17:07:02 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.List;

import javax.annotation.security.PermitAll;
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
import javax.ws.rs.core.Response.Status;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;

import cz.vutbr.fit.layout.api.IRIDecoder;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.rdf.StorageException;
import cz.vutbr.fit.layout.web.data.QuadrupleData;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.data.SelectQueryResult;
import cz.vutbr.fit.layout.web.data.SubjectDescriptionResult;
import cz.vutbr.fit.layout.web.ejb.StorageService;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * Direct repository querying.
 * 
 * @author burgetr
 */
@Path("r/{repoId}/repository")
public class RepositoryResource
{
    @Inject
    private UserService userService;
    @Inject
    private StorageService storage;

    @PathParam("repoId")
    private String repoId;
    
    @GET
    @Path("/touch")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response touch()
    {
        storage.touch(userService.getUser(), repoId);
        return Response.ok(new ResultValue(null)).build();
    }
    
    @POST
    @Path("/query")
    @Consumes(Serialization.SPARQL_QUERY)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response repositoryQuery(String query)
    {
        try {
            final RDFStorage rdfst = storage.getStorage(userService.getUser(), repoId);
            if (rdfst != null)
            {
                final List<BindingSet> bindings = rdfst.executeSafeTupleQuery(query);
                final SelectQueryResult result = new SelectQueryResult(bindings);
                return Response.ok(new ResultValue(result)).build();
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
    @Path("/subject/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response querySubject(@PathParam("iri") String iriValue, @DefaultValue("100") @QueryParam("limit") int limit)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                final IRI iri = repo.getIriDecoder().decodeIri(iriValue);
                final String query = "SELECT ?p ?v WHERE { <" + iri.toString() + "> ?p ?v } LIMIT " + limit;
                final List<BindingSet> bindings = repo.getStorage().executeSafeTupleQuery(query);
                final SelectQueryResult result = new SelectQueryResult(bindings);
                return Response.ok(new ResultValue(result)).build();
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
    @Path("/object/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response queryObject(@PathParam("iri") String iriValue, @DefaultValue("100") @QueryParam("limit") int limit)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                final IRI iri = repo.getIriDecoder().decodeIri(iriValue);
                final String query = "SELECT ?v ?p WHERE { ?v ?p <" + iri.toString() + "> } LIMIT " + limit;
                final List<BindingSet> bindings = repo.getStorage().executeSafeTupleQuery(query);
                final SelectQueryResult result = new SelectQueryResult(bindings);
                return Response.ok(new ResultValue(result)).build();
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
    @Path("/subject/{subjIri}/{propertyIri}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getSubjectValue(@PathParam("subjIri") String subjIriValue, @PathParam("propertyIri") String propertyIriValue)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                final IRI subjIri = repo.getIriDecoder().decodeIri(subjIriValue);
                final IRI propertyIri = repo.getIriDecoder().decodeIri(propertyIriValue);
                final Value val = repo.getStorage().getPropertyValue(subjIri, propertyIri);
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
    @Path("/type/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getSubjectType(@PathParam("iri") String iriValue)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                final IRI iri = repo.getIriDecoder().decodeIri(iriValue);
                final IRI type = repo.getStorage().getSubjectType(iri);
                if (type != null)
                    return Response.ok(new ResultValue(type.toString())).build();
                else
                    return Response.ok(new ResultValue("unknown")).build();
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
    @Path("/describe/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response describeSubject(@PathParam("iri") String iriValue)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                final IRI iri = repo.getIriDecoder().decodeIri(iriValue);
                final String query = "SELECT ?p ?v WHERE { <" + iri.toString() + "> ?p ?v }";
                final List<BindingSet> bindings = repo.getStorage().executeSafeTupleQuery(query);
                final SubjectDescriptionResult result = new SubjectDescriptionResult(bindings);
                return Response.ok(new ResultValue(result)).build();
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
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response addQuadruple(QuadrupleData quad)
    {
        if (quad.isOk())
        {
            try {
                final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
                if (repo != null)
                {
                    final IRIDecoder dec = repo.getIriDecoder();
                    final IRI sIri = dec.decodeIri(quad.getS());
                    final IRI pIri = dec.decodeIri(quad.getP());
                    final IRI aIri = dec.decodeIri(quad.getArtifact());
                    if (quad.getO() != null)
                    {
                        final IRI oIri = dec.decodeIri(quad.getO());
                        repo.getStorage().add(sIri, pIri, oIri, aIri);
                    }
                    else if (quad.getValue() != null)
                    {
                        repo.getStorage().addValue(sIri, pIri, quad.getValue(), aIri);
                    }
                    return Response.ok(new ResultValue(null)).build();
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
        else
        {
            return Response.serverError().entity(new ResultErrorMessage("error", "Must provide {s, p, (o | value), artifact}")).build();
        }
        
    }
    
    @GET
    @Path("/checkRepo")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response checkRepo()
    {
        final RDFStorage rdfst = storage.getStorage(userService.getUser(), repoId);
        if (rdfst != null)
        {
            Value val = rdfst.getPropertyValue(BOX.Page, RDF.TYPE);
            if (val != null)
                return Response.ok(new ResultValue("Repository metadata is present.")).build();
            else
                return Response.ok(new ResultErrorMessage("Repository has not been initialized. Use /initRepo to fix this.")).build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                    .build();
        }
    }
    
    @GET
    @Path("/initRepo")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response initRepo()
    {
        final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
        if (repo != null)
        {
            if (repo.isInitialized())
                return Response.ok(new ResultValue("ok")).build();
            else
                return Response.serverError().entity("error during repository initialization").build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                    .build();
        }
    }
    
}
