/**
 * StorageService.java
 *
 * Created on 23. 9. 2020, 21:07:05 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;

/**
 * 
 * @author burgetr
 */
@Singleton
public class StorageService
{
    private static Logger log = LoggerFactory.getLogger(StorageService.class);
    
    @Inject
    @ConfigProperty(name = "fitlayout.rdf.storage")
    String configStorage;

    @Inject
    @ConfigProperty(name = "fitlayout.rdf.server")
    String configServer;

    @Inject
    @ConfigProperty(name = "fitlayout.rdf.repository")
    String configRepository;

    @Inject
    @ConfigProperty(name = "fitlayout.rdf.path")
    String configPath;
    
    private boolean singleMode;
    private StorageProvider provider;
    
    @PostConstruct
    public void init()
    {
        singleMode = (!"multi".equals(configStorage));
        if (singleMode)
        {
            log.info("Initializing single mode repository");
            provider = new StorageProviderSingle(configStorage, configServer, configRepository, configPath);
        }
        else
        {
            log.info("Initializing multi mode repository");
            provider = new StorageProviderMulti(configPath);
        }
    }
    
    @PreDestroy
    public void destroy()
    {
        closeStorage();
    }
    
    //===============================================================================================
    
    public boolean isReady()
    {
        return provider.isReady();
    }
    
    public RDFStorage getStorage(String userId, String repoId)
    {
        return provider.getStorage(userId, repoId);
    }
    
    public RDFArtifactRepository getArtifactRepository(String userId, String repoId)
    {
        return provider.getArtifactRepository(userId, repoId);
    }

    public void closeStorage()
    {
        provider.close();
    }
    
}
