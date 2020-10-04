/**
 * FLConfig.java
 *
 * Created on 3. 10. 2020, 23:09:57 by burgetr
 */
package cz.vutbr.fit.layout.web;

import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.segm.Provider;

/**
 * FitLayout configuration utilities.
 * 
 * @author burgetr
 */
public class FLConfig
{
    
    /**
     * Creates and configures a FitLayout ServiceManager instance.
     * @param repo the artifact repository to be used by the service manager or {@code null} when
     * no repository should be configured.
     * @return the created ServiceManager instance
     */
    public static ServiceManager createServiceManager(RDFArtifactRepository repo)
    {
        //initialize the services
        ServiceManager sm = ServiceManager.create();
        
        CSSBoxTreeProvider cssboxProvider = new CSSBoxTreeProvider();
        sm.addArtifactService(cssboxProvider);
        
        Provider segmProvider = new Provider();
        sm.addArtifactService(segmProvider);
        
        //use RDF storage as the artifact repository
        if (sm != null)
            sm.setArtifactRepository(repo);
        return sm;
    }

}
