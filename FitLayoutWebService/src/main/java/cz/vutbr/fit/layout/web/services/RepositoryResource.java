/**
 * RepositoryResource.java
 *
 * Created on 24. 3. 2021, 17:07:02 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.eclipse.rdf4j.rio.helpers.NTriplesUtil;

import cz.vutbr.fit.layout.api.IRIDecoder;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.rdf.SparqlQueryResult;
import cz.vutbr.fit.layout.rdf.StorageException;
import cz.vutbr.fit.layout.web.data.BooleanQueryResult;
import cz.vutbr.fit.layout.web.data.QuadrupleData;
import cz.vutbr.fit.layout.web.data.Result;
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
    private static final String TEXT_BOOLEAN = "text/boolean";

    /** Maximal value of a query limit. */
    private static final long MAX_QUERY_LIMIT = 2048;
    
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
    @Path("/selectQuery")
    @Consumes(Serialization.SPARQL_QUERY)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "selectQuery", summary = "Executes a SPARQL SELECT query on the underlying RDF repository")
    @APIResponse(responseCode = "200", description = "SPARQL select query result (bindings)",
        content = @Content(schema = @Schema(ref = "SelectQueryResult")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "500", description = "Query evaluation error",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response selectQuery(String query,
            @DefaultValue("100") @QueryParam("limit") long limit,
            @DefaultValue("0") @QueryParam("offset") long offset,
            @DefaultValue("false") @QueryParam("offset") boolean distinct)
    {
        try {
            final RDFStorage rdfst = storage.getStorage(userService.getUser(), repoId);
            if (rdfst != null)
            {
                if (limit > MAX_QUERY_LIMIT) limit = MAX_QUERY_LIMIT;
                final List<BindingSet> bindings = rdfst.executeSparqlTupleQuery(query, distinct, limit, offset);
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
    
    @POST
    @Path("/updateQuery")
    @Consumes(Serialization.SPARQL_QUERY)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "updateQuery", summary = "Executes a SPARQL UPDATE query on the underlying RDF repository")
    @APIResponse(responseCode = "200", description = "Update performed",
        content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "500", description = "Query evaluation error",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response updateQuery(String query)
    {
        try {
            final RDFStorage rdfst = storage.getStorage(userService.getUser(), repoId);
            if (rdfst != null)
            {
                rdfst.execSparqlUpdate(query);
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
    
    @POST
    @Path("/query")
    @Consumes(Serialization.SPARQL_QUERY)
    @Produces({Serialization.JSONLD, Serialization.TURTLE, Serialization.RDFXML,
        Serialization.NTRIPLES, Serialization.NQUADS,
        MediaType.TEXT_XML, MediaType.APPLICATION_JSON, TEXT_BOOLEAN})
    @PermitAll
    @Operation(operationId = "query", summary = "Executes any SPARQL query on the underlying RDF repository")
    @APIResponse(responseCode = "200", description = "SELECT query result (tuple query)",
        content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "SelectQueryResult")))    
    @APIResponse(responseCode = "200-JSONLD", description = "CONSTRUCT query result (graph query) serialized in JSON-LD", 
        content = @Content(mediaType = Serialization.JSONLD))    
    @APIResponse(responseCode = "200-TURTLE", description = "CONSTRUCT query result (graph query) serialized in TURTLE",
        content = @Content(mediaType = Serialization.TURTLE))   
    @APIResponse(responseCode = "200-RDFXML", description = "CONSTRUCT query result (graph query) serialized in RDF/XML",
        content = @Content(mediaType = Serialization.RDFXML))
    @APIResponse(responseCode = "200-NTRIPLES", description = "CONSTRUCT query result (graph query) serialized in N-TRIPLES",
        content = @Content(mediaType = Serialization.NTRIPLES))
    @APIResponse(responseCode = "200-NQUADS", description = "CONSTRUCT query result (graph query) serialized in N-QUADS",
        content = @Content(mediaType = Serialization.NQUADS))
    @APIResponse(responseCode = "200-JSON", description = "ASK query result (boolean query) serialized in JSON",
        content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "200-TEXT", description = "ASK query result (boolean query) serialized in text",
        content = @Content(mediaType = TEXT_BOOLEAN))
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "500", description = "Query evaluation error",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response query(String query,
            @DefaultValue("100") @QueryParam("limit") long limit,
            @DefaultValue("0") @QueryParam("offset") long offset,
            @DefaultValue("false") @QueryParam("offset") boolean distinct,
            @HeaderParam("Accept") String accept)
    {
        try {
            final RDFStorage rdfst = storage.getStorage(userService.getUser(), repoId);
            if (rdfst != null)
            {
                if (limit > MAX_QUERY_LIMIT) limit = MAX_QUERY_LIMIT;
                final SparqlQueryResult result = rdfst.executeSparqlQuery(query, distinct, limit, offset);
                switch (result.getType())
                {
                    case TUPLE:
                        final SelectQueryResult tRes = new SelectQueryResult(result.getTupleResult());
                        return Response.ok(tRes).type(MediaType.APPLICATION_JSON).build();
                    case GRAPH:
                        final List<Statement> graph = result.getGraphResult();
                        final String mime = (accept == null || MediaType.WILDCARD.equals(accept)) ? Serialization.NTRIPLES : accept;
                        if (Serialization.rdfFormats.contains(mime))
                        {
                            StreamingOutput stream = new StreamingOutput() {
                                @Override
                                public void write(OutputStream os) throws IOException, WebApplicationException {
                                    Serialization.statementsToStream(graph, os, mime);
                                }
                            };
                            return Response.ok(stream).type(accept).build();
                        }
                        else
                        {
                            return Response.status(Status.NOT_ACCEPTABLE)
                                    .type(MediaType.APPLICATION_JSON)
                                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NOT_ACCEPTABLE))
                                    .build();
                        }
                    case BOOLEAN:
                        boolean val = result.getBooleanResult();
                        if (accept == null || MediaType.WILDCARD.equals(accept) || MediaType.APPLICATION_JSON.equals(accept))
                            return Response.ok(new BooleanQueryResult(val)).type(MediaType.APPLICATION_JSON).build();
                        else if (TEXT_BOOLEAN.equals(accept))
                            return Response.ok(String.valueOf(val)).type(TEXT_BOOLEAN).build();
                        else
                            return Response.status(Status.NOT_ACCEPTABLE)
                                    .type(MediaType.APPLICATION_JSON)
                                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NOT_ACCEPTABLE))
                                    .build();
                }
                return Response.serverError().build(); //should not happen
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
                final String query = "SELECT ?p ?v WHERE { <" + String.valueOf(iri) + "> ?p ?v } LIMIT " + limit;
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
                final String query = "SELECT ?v ?p WHERE { ?v ?p <" + String.valueOf(iri) + "> } LIMIT " + limit;
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
                    return Response.ok(new ResultValue(String.valueOf(type))).build();
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
                final String query = "SELECT ?p ?v WHERE { <" + String.valueOf(iri) + "> ?p ?v }";
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
    
    @POST
    @Path("/addQuads")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "addQuadruples", summary = "Adds a list of new quadruple to the repository")
    @APIResponse(responseCode = "200", description = "The quadruples added",
        content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "400", description = "Invalid quadruple specification",
        content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response addQuadruples(List<QuadrupleData> quads)
    {
        if (quads != null)
        {
            try {
                final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
                if (repo != null)
                {
                    final IRIDecoder dec = repo.getIriDecoder();
                    int addCnt = 0;
                    int skipCnt = 0;
                    for (QuadrupleData quad : quads)
                    {
                        if (quad.isOk())
                        {
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
                            addCnt++;
                        }
                        else
                            skipCnt++;
                    }
                    return Response.ok(new ResultValue("Added " + addCnt + " quads, " + skipCnt + " skipped")).build();
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
                    .entity(new ResultErrorMessage("A list of quadruples required"))
                    .build();
        }
    }
    
    @POST
    @Path("/deleteQuads")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "deleteQuadruples", summary = "Removes a list of quadruples from the repository")
    @APIResponse(responseCode = "200", description = "Quadruples removed",
        content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "400", description = "Invalid quadruple specification",
        content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response deleteQuadruples(List<QuadrupleData> quads)
    {
        if (quads != null)
        {
            try {
                final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
                if (repo != null)
                {
                    final IRIDecoder dec = repo.getIriDecoder();
                    int addCnt = 0;
                    int skipCnt = 0;
                    for (QuadrupleData quad : quads)
                    {
                        if (quad.isOk())
                        {
                            final IRI sIri = dec.decodeIri(quad.getS());
                            final IRI pIri = dec.decodeIri(quad.getP());
                            final IRI aIri = dec.decodeIri(quad.getArtifact());
                            if (quad.getO() != null)
                            {
                                final IRI oIri = dec.decodeIri(quad.getO());
                                repo.getStorage().remove(sIri, pIri, oIri, aIri);
                            }
                            else if (quad.getValue() != null)
                            {
                                repo.getStorage().removeValue(sIri, pIri, quad.getValue(), aIri);
                            }
                            addCnt++;
                        }
                        else
                            skipCnt++;
                    }
                    return Response.ok(new ResultValue("Removed " + addCnt + " quads, " + skipCnt + " skipped")).build();
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
                    .entity(new ResultErrorMessage("A list of quadruples required"))
                    .build();
        }
    }
    
    //========================================================================================================
    
    @GET
    @Path("/namespaces")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "getNamespaces", summary = "Gets a list of namespace declarations that have been defined for the repository")
    @APIResponse(responseCode = "200", description = "Namespace query result",
        content = @Content(schema = @Schema(ref = "SelectQueryResult")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
        content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response getNamespaces()
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                final List<Namespace> nss = repo.getStorage().getNamespaces();
                //transform to a binding set
                final var vf = repo.getStorage().getValueFactory();
                List<BindingSet> bindings = nss.stream()
                        .map((ns) -> new ListBindingSet(List.of("prefix", "namespace"),
                                                        vf.createLiteral(ns.getPrefix()), 
                                                        vf.createLiteral(ns.getName())))
                        .collect(Collectors.toList());
                //send result
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
    
    @DELETE
    @Path("/namespaces")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "clearNamespaces", summary = "Removes all namespace declarations from the repository")
    @APIResponse(responseCode = "200", description = "Namespaces removed",
        content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
        content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response clearNamespaces()
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                repo.getStorage().clearNamespaces();
                return Response.ok(new Result(Result.OK)).build();
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
    @Path("/namespaces/{prefix}")
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    @Operation(operationId = "getNamespace", summary = "Gets a namespace URI for the given prefix")
    @APIResponse(responseCode = "200", description = "Namespace URI",
        content = @Content(mediaType = "text/plain"))    
    @APIResponse(responseCode = "404", description = "Repository or namespace with the given ID not found",
        content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response getNamespace(@PathParam("prefix") String prefix)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                final String ns = repo.getStorage().getNamespace(prefix);
                if (ns != null)
                    return Response.ok(ns).build();
                else
                    return Response.status(Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                            .build();
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
    @Path("/namespaces/{prefix}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "deleteNamespace", summary = "Removes a namespace definition for the given prefix")
    @APIResponse(responseCode = "200", description = "Namespace deleted",
        content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository or namespace with the given ID not found",
        content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response deleteNamespace(@PathParam("prefix") String prefix)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                final String ns = repo.getStorage().getNamespace(prefix);
                if (ns != null)
                {
                    repo.getStorage().deleteNamespace(prefix);
                    return Response.ok(new Result(Result.OK)).build();
                }
                else
                    return Response.status(Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                            .build();
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
    
    @PUT
    @Path("/namespaces/{prefix}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "addNamespace", summary = "Adds a namespace definition for the given prefix")
    @APIResponse(responseCode = "200", description = "Namespace added",
        content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
        content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response addNamespace(@PathParam("prefix") String prefix, String body)
    {
        if (prefix != null && body != null)
        {
            try {
                final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
                if (repo != null)
                {
                    repo.getStorage().addNamespace(prefix, body.trim());
                    return Response.ok(new Result(Result.OK)).build();
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
                    .entity(new ResultErrorMessage("prefix or namespace is missing"))
                    .build();
        }
    }
    
    //========================================================================================================
    
    @GET
    @Path("/statements")
    @Produces({Serialization.JSONLD, Serialization.TURTLE, Serialization.RDFXML, Serialization.NTRIPLES, Serialization.NQUADS})
    @PermitAll
    @Operation(operationId = "getStatements", summary = "Gets all RDF statements from the repository")
    @APIResponses(value = {
        @APIResponse(responseCode = "200-JSONLD", description = "RDF statements serialized in JSON-LD", 
                content = @Content(mediaType = Serialization.JSONLD)),    
        @APIResponse(responseCode = "200-TURTLE", description = "RDF statements serialized in TURTLE",
                content = @Content(mediaType = Serialization.TURTLE)),   
        @APIResponse(responseCode = "200-RDFXML", description = "RDF statements serialized in RDF/XML",
                content = @Content(mediaType = Serialization.RDFXML)),
        @APIResponse(responseCode = "200-NTRIPLES", description = "RDF statements serialized in N-Triples",
            content = @Content(mediaType = Serialization.NTRIPLES)),
        @APIResponse(responseCode = "200-NQUADS", description = "RDF statements serialized in N-Quads",
                content = @Content(mediaType = Serialization.NQUADS)),
        @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "ResultErrorMessage")))    
    })
    public Response getStatements(@HeaderParam("Accept") String accept,
            @QueryParam("subj") String subj,
            @QueryParam("pred") String pred,
            @QueryParam("obj") String obj,
            @QueryParam("context") String context)
    {
        final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
        if (repo != null)
        {
            try {
                final ValueFactory vf = repo.getStorage().getValueFactory();
                Resource ssubj = (subj == null) ? null : NTriplesUtil.parseResource(subj, vf);
                IRI spred = (pred == null) ? null : NTriplesUtil.parseURI(pred, vf);
                Value sobj = (obj == null) ? null : NTriplesUtil.parseValue(obj, vf);
                StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream os) throws IOException, WebApplicationException {
                        if (context == null)
                        {
                            Serialization.statementsToStream(repo.getStorage().getRepository(), os, accept,
                                    ssubj, spred, sobj);
                        }
                        else
                        {
                            IRI contextIri = repo.getIriDecoder().decodeIri(context);
                            Serialization.statementsToStream(repo.getStorage().getRepository(), os, accept,
                                    ssubj, spred, sobj, contextIri);
                        }
                    }
                };
                return Response.ok(stream)
                        .type(accept)
                        .build();
                
            } catch (IllegalArgumentException e) {
                return Response.status(Status.BAD_REQUEST).entity(new ResultErrorMessage(e.getMessage())).build();
            } catch (StorageException e) {
                return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
            }
        }        
        else
        {
            return Response.status(Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                    .build();
        }
    }
    
    @DELETE
    @Path("/statements")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "deleteStatements", summary = "Deletes statements from the repository")
    @APIResponse(responseCode = "200", description = "Statements deleted",
            content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "400", description = "Invalid service parametres",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response deleteStatements(@QueryParam("subj") String subj,
            @QueryParam("pred") String pred,
            @QueryParam("obj") String obj,
            @QueryParam("context") String context)
    {
        final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
        if (repo != null)
        {
            try {
                final ValueFactory vf = repo.getStorage().getValueFactory();
                Resource ssubj = (subj == null) ? null : NTriplesUtil.parseResource(subj, vf);
                IRI spred = (pred == null) ? null : NTriplesUtil.parseURI(pred, vf);
                Value sobj = (obj == null) ? null : NTriplesUtil.parseValue(obj, vf);
                if (context == null)
                {
                    repo.getStorage().removeStatements(ssubj, spred, sobj);
                }
                else
                {
                    IRI contextIri = repo.getIriDecoder().decodeIri(context);
                    repo.getStorage().removeStatements(ssubj, spred, sobj, contextIri);
                }   
                return Response.ok(new ResultValue(null)).build();
                
            } catch (IllegalArgumentException e) {
                return Response.status(Status.BAD_REQUEST).entity(new ResultErrorMessage(e.getMessage())).build();
            } catch (StorageException e) {
                return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
            }
        }        
        else
        {
            return Response.status(Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                    .build();
        }
    }
    
    @POST
    @Path("/statements")
    @Consumes({Serialization.JSONLD, Serialization.TURTLE, Serialization.RDFXML, Serialization.NTRIPLES, Serialization.NQUADS})
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "addStatements", summary = "Imports statements to the repository")
    @APIResponse(responseCode = "200", description = "Statements added",
            content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "400", description = "Invalid service parametres",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response addStatements(InputStream istream,
            @QueryParam("context") String context,
            @QueryParam("baseURI") String baseURI,
            @HeaderParam("Content-Type") String mimeType)
    {
        final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
        if (repo != null)
        {
            try {
                if (context == null)
                    context = "http://fake.url/import";
                IRI contextIri = repo.getIriDecoder().decodeIri(context);
                if (baseURI != null)
                    repo.getStorage().importStream(istream, Serialization.getFormatForMimeType(mimeType), contextIri);
                else
                    repo.getStorage().importStream(istream, Serialization.getFormatForMimeType(mimeType), contextIri, baseURI);
                return Response.ok(new ResultValue(null)).build();
            } catch (IllegalArgumentException e) {
                return Response.status(Status.BAD_REQUEST).entity(new ResultErrorMessage(e.getMessage())).build();
            } catch (StorageException e) {
                return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
            }
        }        
        else
        {
            return Response.status(Status.NOT_FOUND)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                    .build();
        }
    }
    
    //========================================================================================================
    
    @GET
    @Path("/contexts")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "getContexts", summary = "Gets a list of contexts that have been defined for the repository")
    @APIResponse(responseCode = "200", description = "Context query result",
        content = @Content(schema = @Schema(ref = "SelectQueryResult")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
        content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response getContexts()
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                final List<Resource> nss = repo.getStorage().getContexts();
                //transform to a binding set
                final var vf = repo.getStorage().getValueFactory();
                List<BindingSet> bindings = nss.stream()
                        .map((ctx) -> new ListBindingSet(List.of("contextID"),
                                                        vf.createIRI(String.valueOf(ctx))))
                        .collect(Collectors.toList());
                //send result
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
    
    //========================================================================================================
    
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
