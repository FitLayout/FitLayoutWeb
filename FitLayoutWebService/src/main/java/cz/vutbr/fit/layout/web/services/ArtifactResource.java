/**
 * ArtifactResource.java
 *
 * Created on 9.9.2020, 17:48:26 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.web.ResultValue;
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
        //initialize the services
        sm = ServiceManager.create();
        CSSBoxTreeProvider provider = new CSSBoxTreeProvider();
        sm.addArtifactService(provider);
        //use RDF storage as the artifact repository
        sm.setArtifactRepository(storage.getArtifactRepository());
        System.out.println("Services: " + sm.findArtifactSevices().keySet());
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listArtifacts()
    {
        Collection<IRI> list = storage.getArtifactRepository().getArtifactIRIs();
        List<String> stringList = list.stream().map(Object::toString).collect(Collectors.toList());
        return Response.ok(new ResultValue(stringList)).build();
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
                sm.setServiceParams(op, params.getParams());
                Artifact page = ((ArtifactService) op).process(null);
                storage.getArtifactRepository().addArtifact(page);
                return Response.ok(new ResultValue(page.getIri().toString())).build();
            } catch (RepositoryException | ServiceException e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        }
        else
        {
            return Response.status(Status.NOT_FOUND).entity("{\"error\": \"No such service\"}").build();
        }
    }

    @GET
    @Path("/nextId")
    public Response nextArtifactId()
    {
        try {
            checkStorageReady();
            long seq = storage.getStorage().getNextSequenceValue("page");
            return Response.ok(new ResultValue(seq)).build();
        } catch (RepositoryException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private void checkStorageReady() throws RepositoryException
    {
        if (!storage.isReady())
            throw new RepositoryException("Storage not ready");
    }
    
}
