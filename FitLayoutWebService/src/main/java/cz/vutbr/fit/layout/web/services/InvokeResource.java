
package cz.vutbr.fit.layout.web.services;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.rdf.BoxModelBuilder;
import cz.vutbr.fit.layout.rdf.RESOURCE;
import cz.vutbr.fit.layout.rdf.Serialization;

/**
 *
 * @author burgetr
 */
@Path("invoke")
public class InvokeResource
{
    private ServiceManager sm;
    
    
    @PostConstruct
    public void init()
    {
        System.out.println("INIT");
        sm = ServiceManager.create();
        CSSBoxTreeProvider provider = new CSSBoxTreeProvider();
        sm.addArtifactService(provider);
        System.out.println("Services: " + sm.findArtifactSevices().keySet());
    }
    
    @GET
    public String ping()
    {
        return "ok";
    }
    
    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServiceConfig(@QueryParam("id") String serviceId)
    {
        ParametrizedOperation op = sm.findParmetrizedService(serviceId);
        if (op != null)
        {
            Map<String, Object> p = sm.getServiceParams(op);
            ServiceParams params = new ServiceParams(serviceId, p);
            return Response.ok(params).build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND).entity("{\"error\": \"No such service\"}").build();
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response invoke(ServiceParams params)
    {
        ParametrizedOperation op = sm.findParmetrizedService(params.getServiceId());
        if (op != null)
        {
            sm.setServiceParams(op, params.getParams());
            Artifact page = ((ArtifactService) op).process(null);
            
            IRI pageIri = RESOURCE.createPageURI(page.getId());
            BoxModelBuilder builder = new BoxModelBuilder((Page) page, pageIri);
            Model graph = builder.getGraph();
            
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
            return Response.status(Status.NOT_FOUND).entity("{\"error\": \"No such service\"}").build();
        }
    }

}
