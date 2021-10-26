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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
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
@Tag(name = "repository", description = "RDF repository opertations")
public class RepositoryResource
{
    @Inject
    private UserService userService;
    @Inject
    private StorageService storage;

    @PathParam("repoId")
    @Parameter(description = "The ID of the artifact repository to use", required = true)
    private String repoId;
    
    @GET
    @Path("/touch")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "touch", summary = "Updates the last access time of the given repository to current time")
    @APIResponse(responseCode = "200", description = "Repository touched",
            content = @Content(schema = @Schema(ref = "ResultValue")))    
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
    @Operation(operationId = "repositoryQuery", summary = "Executes a SPARQL SELECT query on the underlying RDF repository")
    @APIResponse(responseCode = "200", description = "SPARQL query result",
        content = @Content(schema = @Schema(ref = "SelectQueryResult")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "500", description = "Query evaluation error",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response repositoryQuery(String query)
    {
        try {
            final RDFStorage rdfst = storage.getStorage(userService.getUser(), repoId);
            if (rdfst != null)
            {
                final List<BindingSet> bindings = rdfst.executeSafeTupleQuery(query);
                final SelectQueryResult result = new SelectQueryResult(bindings);
                return Response.ok(result).build();
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
    @Operation(operationId = "querySubject", summary = "Gets all triples for the given subject IRI")
    @APIResponse(responseCode = "200", description = "Select query result assigning (p)redicate and (v)alue",
        content = @Content(schema = @Schema(ref = "SelectQueryResult")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
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
                return Response.ok(result).build();
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
    @Operation(operationId = "queryObject", summary = "Gets all triples for the given object IRI")
    @APIResponse(responseCode = "200", description = "Select query result assigning (v)alue and (p)redicate",
        content = @Content(schema = @Schema(ref = "SelectQueryResult")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
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
                return Response.ok(result).build();
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
    @Operation(operationId = "getSubjectValue", summary = "Gets the property value for the given subject and property IRIs")
    @APIResponse(responseCode = "200", description = "Select query result assigning (p)redicate and (v)alue",
        content = @Content(schema = @Schema(ref = "ResultBinding")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
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
                    return Response.ok(ret).build();
                }
                else if (val instanceof Literal)
                {
                    final var ret = new SelectQueryResult.LiteralBinding(val.stringValue(), ((Literal) val).getDatatype());
                    return Response.ok(ret).build();
                }
                else
                {
                    final var ret = new SelectQueryResult.NullBinding();
                    return Response.ok(ret).build();
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
    @Operation(operationId = "getSubjectType", summary = "Gets the assigned rdf:type IRI for the given subject IRI")
    @APIResponse(responseCode = "200", description = "Type IRI or 'unknown'",
        content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
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
    @Operation(operationId = "describeSubject", summary = "Gets the RDF description for the given subject IRI")
    @APIResponse(responseCode = "200", description = "Subject description",
        content = @Content(schema = @Schema(ref = "SubjectDescriptionResult")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
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
                return Response.ok(result).build();
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
    @Operation(operationId = "addQuadruple", summary = "Adds a new quadruple to the repository")
    @APIResponse(responseCode = "200", description = "The quadruple added",
        content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "400", description = "Invalid quadruple specification",
        content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
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
            return Response.status(Status.BAD_REQUEST)
                    .entity(new ResultErrorMessage("Must provide {s, p, (o | value), artifact}"))
                    .build();
        }
        
    }
    
    @GET
    @Path("/checkRepo")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "checkRepo", summary = "Checks the repository whether it exists and is properly initialized")
    @APIResponse(responseCode = "200", description = "Repository check result",
        content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
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
    @Operation(operationId = "initRepo", summary = "Initializes an empty repository with the necessary RDF metadata (schemas)")
    @APIResponse(responseCode = "200", description = "Repository initialized",
            content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response initRepo()
    {
        final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
        if (repo != null)
        {
            if (repo.isInitialized())
                return Response.ok(new ResultValue("ok")).build();
            else
                return Response.serverError().entity(new ResultErrorMessage("error during repository initialization")).build();
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
