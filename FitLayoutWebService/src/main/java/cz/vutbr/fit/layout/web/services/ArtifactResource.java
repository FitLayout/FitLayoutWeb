/**
 * ArtifactResource.java
 *
 * Created on 9.9.2020, 17:48:26 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.rdf.model.RDFArtifact;
import cz.vutbr.fit.layout.web.FLConfig;
import cz.vutbr.fit.layout.web.StreamOutput;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.data.ServiceParams;
import cz.vutbr.fit.layout.web.ejb.StorageService;
import cz.vutbr.fit.layout.web.ejb.UserService;

/**
 * 
 * @author burgetr
 */
@Path("r/{repoId}/artifact")
@Tag(name = "artifact", description = "Artifact operations")
public class ArtifactResource
{
    private static Logger log = LoggerFactory.getLogger(ArtifactResource.class);

    @Inject
    private UserService userService;
    @Inject
    private StorageService storage;
    
    @PathParam("repoId")
    @Parameter(description = "The ID of the artifact repository to use", required = true)
    private String repoId;
    
    /**
     * Retrieves information about a given artifact without its contents.
     * @param mimeType
     * @param iri the artifact IRI or {@code null} for all artifacts
     * @return
     */
    private Response serializeArtifactInfo(String iriValue, String mimeType)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                Collection<IRI> list;
                if (iriValue == null)
                {
                    list = repo.getArtifactIRIs();
                }
                else
                {
                    IRI iri = repo.getIriDecoder().decodeIri(iriValue);
                    list = new ArrayList<>(1);
                    list.add(iri);
                }
                Model graph = getArtifactModel(repo, list);
                StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream os) throws IOException, WebApplicationException {
                        Serialization.modelToStream(graph, os, mimeType);
                    }
                };
                return Response.ok(stream)
                        .type(mimeType)
                        .build();
            }
            else
            {
                return Response.status(Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        } catch (RepositoryException | ServiceException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/")
    @Produces({Serialization.JSONLD, Serialization.TURTLE, Serialization.RDFXML})
    @PermitAll
    @Operation(operationId = "getArtifactsInfo", summary = "Retrieves information about all artifacts in the repository (artifact contents not included).")
    @APIResponses(value = {
            @APIResponse(responseCode = "200-JSONLD", description = "The artifacts RDF description serialized in JSON-LD", 
                    content = @Content(mediaType = Serialization.JSONLD)),    
            @APIResponse(responseCode = "200-TURTLE", description = "The artifacts RDF description serialized in TURTLE",
                    content = @Content(mediaType = Serialization.TURTLE)),   
            @APIResponse(responseCode = "200-RDFXML", description = "The artifacts RDF description serialized in RDF/XML",
                    content = @Content(mediaType = Serialization.RDFXML)),
            @APIResponse(responseCode = "400", description = "Invalid service parametres",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "ResultErrorMessage"))),
            @APIResponse(responseCode = "404", description = "Repository with the given ID not found or could not be serialized",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "ResultErrorMessage")))
        })
    public Response getArtifactsInfo(@HeaderParam("Accept") String accept)
    {
        return serializeArtifactInfo(null, accept);
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "listArtifacts", summary = "Gets a list of artifact IRIs.")
    @APIResponse(responseCode = "200", description = "List of artifact IRIs",
            content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = String.class)))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response listArtifacts()
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                Collection<IRI> list = repo.getArtifactIRIs();
                List<String> stringList = list.stream().map(Object::toString).collect(Collectors.toList());
                return Response.ok(stringList).build();
            }
            else
            {
                return Response.status(Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                        .build();
            }
        } catch (RepositoryException | ServiceException e) {
            return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
        }
    }
    
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "createArtifact", summary = "Creates a new artifact by invoking a service.")
    @APIResponse(responseCode = "200", description = "The IRI of the new artifact created",
            content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "400", description = "Invalid service parametres",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    @APIResponse(responseCode = "404", description = "Repository or service with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response createArtifact(ServiceParams params)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                ServiceManager sm = FLConfig.createServiceManager(repo);
                ParametrizedOperation op = null;
                if (params.getServiceId() != null)
                    op = sm.findParmetrizedService(params.getServiceId());
                if (op != null)
                {
                    try {
                        checkStorageReady();
                        
                        //read the source artifact
                        Artifact sourceArtifact = null;
                        IRI sourceArtifactIri = null;
                        if (params.getParentIri() != null)
                            sourceArtifactIri = repo.getIriDecoder().decodeIri(params.getParentIri());
                        if (sourceArtifactIri != null)
                            sourceArtifact = repo.getArtifact(sourceArtifactIri); 
                        
                        //invoke the service
                        ServiceManager.setServiceParams(op, params.getParams());
                        Artifact newArtifact = ((ArtifactService) op).process(sourceArtifact);
                        repo.addArtifact(newArtifact);
                        return Response.ok(new ResultValue(String.valueOf(newArtifact.getIri()))).build();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        return Response.status(Status.BAD_REQUEST).entity(new ResultErrorMessage(e.getMessage())).build();
                    } catch (RepositoryException | ServiceException e) {
                        return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
                    }
                }
                else
                {
                    return Response.status(Status.NOT_FOUND).entity(new ResultErrorMessage(ResultErrorMessage.E_NO_SERVICE)).build();
                }
            }
            else
            {
                return Response.status(Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        } catch (RepositoryException | ServiceException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }

    /**
     * Serializes the artifact RDF model as obtained from the repository (without constructing
     * the java representation if the artifact). 
     * @param iriValue
     * @param mimeType
     * @return
     */
    private Response serializeArtifactModel(String iriValue, String mimeType)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                IRI iri = repo.getIriDecoder().decodeIri(iriValue);
                Model graph = repo.getArtifactModel(iri);
                if (!graph.isEmpty())
                {
                    StreamingOutput stream = new StreamingOutput() {
                        @Override
                        public void write(OutputStream os) throws IOException, WebApplicationException {
                            Serialization.modelToStream(graph, os, mimeType);
                        }
                    };
                    return Response.ok(stream)
                            .type(mimeType)
                            .build();
                }
                else
                {
                    return Response.status(Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_ARTIFACT + ": " + String.valueOf(iri)))
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
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        } catch (RepositoryException | ServiceException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
    /**
     * Serializes the Java representation of the artifact (if available).
     * 
     * @param iriValue
     * @param mimeType
     * @return
     */
    private Response serializeArtifact(String iriValue, String mimeType)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                IRI iri = repo.getIriDecoder().decodeIri(iriValue);
                Artifact a = repo.getArtifact(iri);
                if (a != null)
                {
                    if (a instanceof Page)
                    {
                        StreamingOutput stream = new StreamingOutput() {
                            @Override
                            public void write(OutputStream os) throws IOException, WebApplicationException {
                                StreamOutput.pageToStream((Page) a, os, mimeType);
                            }
                        };
                        return Response.ok(stream)
                                .type(mimeType)
                                .build();
                    }
                    else if (a instanceof AreaTree)
                    {
                        Artifact p = repo.getArtifact(((AreaTree) a).getPageIri());
                        if (p != null && p instanceof Page)
                        {
                            StreamingOutput stream = new StreamingOutput() {
                                @Override
                                public void write(OutputStream os) throws IOException, WebApplicationException {
                                    StreamOutput.areaTreeToStream((AreaTree) a, (Page) p, os, mimeType);
                                }
                            };
                            return Response.ok(stream)
                                    .type(mimeType)
                                    .build();
                        }
                        else
                        {
                            return Response.status(Status.NOT_FOUND)
                                    .type(MediaType.APPLICATION_JSON)
                                    .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_ARTIFACT + " (missing source page): " + String.valueOf(iri)))
                                    .build();
                        }
                    }
                    else
                    {
                        return Response.status(Status.NOT_FOUND)
                                .type(MediaType.APPLICATION_JSON)
                                .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_ARTIFACT + " (or couldn't be serialized): " + String.valueOf(iri)))
                                .build();
                    }
                }
                else
                {
                    return Response.status(Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_ARTIFACT + ": " + String.valueOf(iri)))
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
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        } catch (RepositoryException | ServiceException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/item/{iri}")
    @Produces({Serialization.JSONLD, Serialization.TURTLE, Serialization.RDFXML,
        MediaType.TEXT_XML, MediaType.TEXT_HTML, "image/png"})
    @PermitAll
    @Operation(operationId = "getArtifact", summary = "Gets a complete artifact including its contents identified by its IRI")
    @APIResponses(value = {
        @APIResponse(responseCode = "200-JSONLD", description = "The complete artifact RDF description serialized in JSON-LD", 
                content = @Content(mediaType = Serialization.JSONLD)),    
        @APIResponse(responseCode = "200-TURTLE", description = "The complete artifact RDF description serialized in TURTLE",
                content = @Content(mediaType = Serialization.TURTLE)),   
        @APIResponse(responseCode = "200-RDFXML", description = "The complete artifact RDF description serialized in RDF/XML",
                content = @Content(mediaType = Serialization.RDFXML)),
        @APIResponse(responseCode = "200-XML", description = "The complete artifact serialized in XML",
                content = @Content(mediaType = MediaType.TEXT_XML)),
        @APIResponse(responseCode = "200-HTML", description = "The complete artifact serialized in HTML",
                content = @Content(mediaType = MediaType.TEXT_HTML)),
        @APIResponse(responseCode = "200-PNG", description = "The complete artifact in a PNG image",
                content = @Content(mediaType = "image/png")),   
        @APIResponse(responseCode = "400", description = "Invalid IRI provided",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "ResultErrorMessage"))),    
        @APIResponse(responseCode = "404", description = "Repository or service with the given ID not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "ResultErrorMessage")))    
    })
    public Response getArtifact(@HeaderParam("Accept") String accept, @PathParam("iri") String iriValue)
    {
        switch (accept)
        {
            case Serialization.JSONLD:
            case Serialization.TURTLE:
            case Serialization.RDFXML:
                return serializeArtifactModel(iriValue, accept);
            default:
                return serializeArtifact(iriValue, accept);
        }
    }
    
    @DELETE
    @Path("/item/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "deleteArtifact", summary = "Deletes an artifact identified by its IRI")
    @APIResponse(responseCode = "200", description = "Artifact deleted",
            content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository or artifact with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response removeArtifact(@PathParam("iri") String iriValue)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                IRI iri = repo.getIriDecoder().decodeIri(iriValue);
                repo.removeArtifact(iri);
                return Response.ok(new ResultValue(String.valueOf(iri))).build();
            }
            else
            {
                return Response.status(Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).entity(new ResultErrorMessage(e.getMessage())).build();
        } catch (RepositoryException | ServiceException e) {
            return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
        }
    }
    
    @GET
    @Path("/refresh/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "refreshArtifact", summary = "Recomputes the computed artifact properties")
    @APIResponse(responseCode = "200", description = "Artifact recomputed",
            content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository or artifact with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response refreshArtifact(@PathParam("iri") String iriValue)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                IRI iri = repo.getIriDecoder().decodeIri(iriValue);
                // load the artifact
                Artifact art = repo.getArtifact(iri);
                if (art != null)
                {
                    // recompute values
                    if (art instanceof RDFArtifact)
                        ((RDFArtifact) art).recompute();
                    else
                        log.warn("Weird: {} is not an RDFArtifact?!", iri);
                    // replace the artifact
                    repo.replaceArtifact(iri, art);
                    return Response.ok(new ResultValue(String.valueOf(iri))).build();
                }
                else
                {
                    return Response.status(Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_ARTIFACT))
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
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).entity(new ResultErrorMessage(e.getMessage())).build();
        } catch (RepositoryException | ServiceException e) {
            return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
        }
    }
    
    @DELETE
    @Path("/clear")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Operation(operationId = "clear", summary = "Clears the repository - deletes all artifacts and metadata")
    @APIResponse(responseCode = "200", description = "Repository cleared",
            content = @Content(schema = @Schema(ref = "ResultValue")))    
    @APIResponse(responseCode = "404", description = "Repository with the given ID not found",
            content = @Content(schema = @Schema(ref = "ResultErrorMessage")))    
    public Response removeAll()
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userService.getUser(), repoId);
            if (repo != null)
            {
                repo.clear();
                return Response.ok(new ResultValue(null)).build();
            }
            else
            {
                return Response.status(Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_REPO))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).entity(new ResultErrorMessage(e.getMessage())).build();
        } catch (RepositoryException | ServiceException e) {
            return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
        }
    }
    
    @GET
    @Path("/info/{iri}")
    @Produces({Serialization.JSONLD, Serialization.TURTLE, Serialization.RDFXML})
    @PermitAll
    @Operation(operationId = "getArtifactInfo", summary = "Retrieves information about an artifact identified by its IRI (artifact content not included).")
    @APIResponses(value = {
            @APIResponse(responseCode = "200-JSONLD", description = "The artifact RDF description serialized in JSON-LD", 
                    content = @Content(mediaType = Serialization.JSONLD)),    
            @APIResponse(responseCode = "200-TURTLE", description = "The artifact RDF description serialized in TURTLE",
                    content = @Content(mediaType = Serialization.TURTLE)),   
            @APIResponse(responseCode = "200-RDFXML", description = "The artifact RDF description serialized in RDF/XML",
                    content = @Content(mediaType = Serialization.RDFXML)),
            @APIResponse(responseCode = "400", description = "Invalid IRI provided",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "ResultErrorMessage"))),    
            @APIResponse(responseCode = "404", description = "Repository or service with the given ID not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(ref = "ResultErrorMessage")))    
        })
    public Response getArtifactInfo(@HeaderParam("Accept") String accept, @PathParam("iri") String iriValue)
    {
        return serializeArtifactInfo(iriValue, accept);
    }
    
    //@GET
    //@Path("/nextId")
    /*public Response nextArtifactId()
    {
        try {
            checkStorageReady();
            long seq = storage.getStorage().getNextSequenceValue("page");
            return Response.ok(new ResultValue(seq)).build();
        } catch (RepositoryException e) {
            return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
        }
    }*/

    private void checkStorageReady() throws RepositoryException
    {
        if (!storage.isReady())
            throw new RepositoryException("Storage not ready");
    }
    
    private Model getArtifactModel(RDFArtifactRepository repo, Collection<IRI> subjects) throws RepositoryException 
    {
        Model model = new LinkedHashModel();
        try (RepositoryConnection con = repo.getStorage().getConnection())
        {
            for (Resource subject : subjects)
            {
                RepositoryResult<Statement> result = con.getStatements(subject, null, null, true);
                try {
                    while (result.hasNext())
                    {
                        Statement st = result.next();
                        if (!st.getPredicate().equals(BOX.pngImage)) //filter out png images in the model to save space
                            model.add(st);
                    }
                }
                finally {
                    result.close();
                }
            }
        }
        return model;
    }
    
}
