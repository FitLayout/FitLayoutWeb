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
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @ConfigProperty(name = "storageType")
    String configStorage;

    @Inject
    @ConfigProperty(name = "rdfServer")
    String configServer;

    @Inject
    @ConfigProperty(name = "rdfRepository")
    String configRepository;

    @Inject
    @ConfigProperty(name = "rdfPath")
    String configPath;

    private RDFStorage storage;
    
    @PostConstruct
    public void init()
    {
        getStorage();
    }
    
    @PreDestroy
    public void destroy()
    {
        closeStorage();
    }
    
    //===============================================================================================
    
    public boolean isReady()
    {
        return storage != null;
    }
    
    public RDFStorage getStorage()
    {
        if (storage == null)
        {
            try {
                String path = configPath.replace("$HOME", System.getProperty("user.home"));
                switch (configStorage)
                {
                    case "memory":
                        storage = RDFStorage.createMemory(path);
                        log.info("Using rdf4j memory storage in {}", path);
                        break;
                    case "native":
                        storage = RDFStorage.createNative(path);
                        log.info("Using rdf4j native storage in {}", path);
                        break;
                    case "http":
                        storage = RDFStorage.createHTTP(configServer, configRepository);
                        log.info("Using rdf4j remote HTTP storage on {} / {}", configServer, configRepository);
                        break;
                    default:
                        log.error("Unknown storage type in config file: {}", configStorage);
                }
            } catch (RepositoryException e) {
                storage = null;
            }
        }
        return storage;
    }
    
    public void closeStorage()
    {
        if (storage != null)
        {
            log.info("Closing storage");
            storage.close();
        }
    }

}
