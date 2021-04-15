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

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.web.FLConfig;
import cz.vutbr.fit.layout.web.data.ResultErrorMessage;
import cz.vutbr.fit.layout.web.data.ResultValue;
import cz.vutbr.fit.layout.web.data.ServiceParams;
import cz.vutbr.fit.layout.web.ejb.StorageService;

/**
 * 
 * @author burgetr
 */
@Path("r/{repoId}/artifact")
public class ArtifactResource
{
    @Inject
    private StorageService storage;
    
    @PathParam("repoId")
    private String repoId;
    
    private String userId;
    
    
    /**
     * Retrieves information about a given artifact without its contents.
     * @param mimeType
     * @param iri the artifact IRI or {@code null} for all artifacts
     * @return
     */
    private Response getArtifactInfo(String iriValue, String mimeType)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userId, repoId);
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
        } catch (RepositoryException | ServiceException e) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ResultErrorMessage(e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/")
    @Produces(Serialization.JSONLD)
    public Response getArtifactsInfoJSON()
    {
        return getArtifactInfo(null, Serialization.JSONLD);
    }
    
    @GET
    @Path("/")
    @Produces(Serialization.TURTLE)
    public Response getArtifactsInfoTurtle()
    {
        return getArtifactInfo(null, Serialization.TURTLE);
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listArtifacts()
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userId, repoId);
            if (repo != null)
            {
                Collection<IRI> list = repo.getArtifactIRIs();
                List<String> stringList = list.stream().map(Object::toString).collect(Collectors.toList());
                return Response.ok(new ResultValue(stringList)).build();
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
    public Response create(ServiceParams params)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userId, repoId);
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
                        return Response.ok(new ResultValue(newArtifact.getIri().toString())).build();
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

    private Response getArtifact(String iriValue, String mimeType)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userId, repoId);
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
                            .entity(new ResultErrorMessage(ResultErrorMessage.E_NO_ARTIFACT + ": " + iri.toString()))
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
    @Produces(Serialization.JSONLD)
    public Response getArtifactJSON(@PathParam("iri") String iriValue)
    {
        return getArtifact(iriValue, Serialization.JSONLD);
    }
    
    @GET
    @Path("/item/{iri}")
    @Produces(Serialization.TURTLE)
    public Response getArtifactTurtle(@PathParam("iri") String iriValue)
    {
        return getArtifact(iriValue, Serialization.TURTLE);
    }
    
    @DELETE
    @Path("/item/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeArtifact(@PathParam("iri") String iriValue)
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userId, repoId);
            if (repo != null)
            {
                IRI iri = repo.getIriDecoder().decodeIri(iriValue);
                repo.removeArtifact(iri);
                return Response.ok(new ResultValue(iri.toString())).build();
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
    public Response removeAll()
    {
        try {
            final RDFArtifactRepository repo = storage.getArtifactRepository(userId, repoId);
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
    @Produces(Serialization.JSONLD)
    public Response getArtifactInfoJSON(@PathParam("iri") String iriValue)
    {
        return getArtifactInfo(iriValue, Serialization.JSONLD);
    }
    
    @GET
    @Path("/info/{iri}")
    @Produces(Serialization.TURTLE)
    public Response getArtifactInfoTurtle(@PathParam("iri") String iriValue)
    {
        return getArtifactInfo(iriValue, Serialization.TURTLE);
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
