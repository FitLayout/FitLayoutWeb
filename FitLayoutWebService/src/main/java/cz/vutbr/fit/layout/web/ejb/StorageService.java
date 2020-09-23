/**
 * StorageService.java
 *
 * Created on 23. 9. 2020, 21:07:05 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
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
    
    
    public RDFStorage getStorage()
    {
        if (storage == null)
        {
            String path = configPath.replace("$HOME", System.getProperty("user.home"));
            System.err.println("PPATH="+path);
            switch (configStorage)
            {
                case "memory":
                    storage = RDFStorage.createMemory(configPath);
                    log.info("Using rdf4j memory storage in {}", path);
                    break;
                case "native":
                    storage = RDFStorage.createNative(configPath);
                    log.info("Using rdf4j native storage in {}", path);
                    break;
                case "http":
                    storage = RDFStorage.createHTTP(configServer, configRepository);
                    log.info("Using rdf4j remote HTTP storage on {} / {}", configServer, configRepository);
                    break;
                default:
                    log.error("Unknown storage type in config file: {}", configStorage);
            }
        }
        return storage;
    }

}
