/**
 * ArtifactResource.java
 *
 * Created on 9.9.2020, 17:48:26 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.Response.Status;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.rdf.BoxModelBuilder;
import cz.vutbr.fit.layout.rdf.Serialization;
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
        System.out.println("INIT");
        //initialize the services
        sm = ServiceManager.create();
        CSSBoxTreeProvider provider = new CSSBoxTreeProvider();
        sm.addArtifactService(provider);
        //use RDF storage as the artifact repository
        sm.setArtifactRepository(storage.getStorage());
        System.out.println("Services: " + sm.findArtifactSevices().keySet());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listArtifacts()
    {
        Collection<IRI> list = storage.getStorage().getArtifactIRIs();
        return Response.ok(list).build();
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
            sm.setServiceParams(op, params.getParams());
            Artifact page = ((ArtifactService) op).process(null);
            
            storage.getStorage().addArtifact(page);
            
            return Response.ok(page.getIri()).build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND).entity("{\"error\": \"No such service\"}").build();
        }
    }

    
}
