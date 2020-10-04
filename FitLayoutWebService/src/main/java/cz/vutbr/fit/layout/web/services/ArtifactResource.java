/**
 * ArtifactResource.java
 *
 * Created on 9.9.2020, 17:48:26 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
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
import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.Artifact;
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
@Path("artifact")
public class ArtifactResource
{
    @Inject
    private StorageService storage;
    private ServiceManager sm;
    
    
    @PostConstruct
    public void init()
    {
        sm = FLConfig.createServiceManager(storage.getArtifactRepository());
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listArtifacts()
    {
        try {
            Collection<IRI> list = storage.getArtifactRepository().getArtifactIRIs();
            List<String> stringList = list.stream().map(Object::toString).collect(Collectors.toList());
            return Response.ok(new ResultValue(stringList)).build();
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
        ParametrizedOperation op = sm.findParmetrizedService(params.getServiceId());
        if (op != null)
        {
            try {
                checkStorageReady();
                
                //read the source artifact
                Artifact sourceArtifact = null;
                IRI sourceArtifactIri = null;
                if (params.getParentIri() != null)
                    sourceArtifactIri = storage.getStorage().decodeIri(params.getParentIri());
                if (sourceArtifactIri != null)
                    sourceArtifact = storage.getArtifactRepository().getArtifact(sourceArtifactIri); 
                
                //invoke the service
                sm.setServiceParams(op, params.getParams());
                Artifact newArtifact = ((ArtifactService) op).process(sourceArtifact);
                storage.getArtifactRepository().addArtifact(newArtifact);
                return Response.ok(new ResultValue(newArtifact.getIri().toString())).build();
            } catch (IllegalArgumentException e) {
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

    @GET
    @Path("/get/{iri}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getArtifact(@PathParam("iri") String iriValue)
    {
        try {
            IRI iri = storage.getStorage().decodeIri(iriValue);
            Model graph = storage.getArtifactRepository().getArtifactModel(iri);
            if (!graph.isEmpty())
            {
                StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream os) throws IOException, WebApplicationException {
                        Serialization.modelToJsonLDStream(graph, os);
                    }
                };
                return Response.ok(stream).build();
            }
            else
            {
                return Response.status(Status.NOT_FOUND).entity(
                        new ResultErrorMessage(ResultErrorMessage.E_NO_ARTIFACT + ": " + iri.toString())).build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Status.BAD_REQUEST).entity(new ResultErrorMessage(e.getMessage())).build();
        } catch (RepositoryException | ServiceException e) {
            return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
        }
    }
    
    //@GET
    //@Path("/nextId")
    public Response nextArtifactId()
    {
        try {
            checkStorageReady();
            long seq = storage.getStorage().getNextSequenceValue("page");
            return Response.ok(new ResultValue(seq)).build();
        } catch (RepositoryException e) {
            return Response.serverError().entity(new ResultErrorMessage(e.getMessage())).build();
        }
    }

    private void checkStorageReady() throws RepositoryException
    {
        if (!storage.isReady())
            throw new RepositoryException("Storage not ready");
    }
    
}
