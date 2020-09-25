/**
 * AdminResource.java
 *
 * Created on 25. 9. 2020, 14:19:17 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import java.io.IOException;
import java.util.Scanner;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFParseException;

import cz.vutbr.fit.layout.web.ejb.StorageService;

/**
 * 
 * @author burgetr
 */
@Path("admin")
public class AdminResource
{
    private static String[] owls = new String[] {"render.owl", "segmentation.owl", "fitlayout.owl", "mapping.owl"};

    @Inject
    private StorageService storage;

    
    @GET
    @Path("/initRepo")
    public Response initRepo()
    {
        int cnt = 0;
        //load the ontologies
        for (String owl : owls)
        {
            String owlFile = loadResource("/rdf/" + owl);
            try
            {
                storage.getStorage().importXML(owlFile);
                cnt++;
            } catch (RDFParseException e) {
                e.printStackTrace();
            } catch (RepositoryException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Response.ok(cnt).build();
    }
    
    private static String loadResource(String filePath)
    {
        try (Scanner scanner = new Scanner(AdminResource.class.getResourceAsStream(filePath), "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.next();
        }
    }

    
}
