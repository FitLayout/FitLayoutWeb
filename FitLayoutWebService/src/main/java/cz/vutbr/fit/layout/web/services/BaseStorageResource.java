/**
 * BaseStorageResource.java
 *
 * Created on 9.9.2020, 17:17:09 by burgetr
 */
package cz.vutbr.fit.layout.web.services;

import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.rdf.RDFStorage;

/**
 * 
 * @author burgetr
 */
public class BaseStorageResource
{
    private static Logger log = LoggerFactory.getLogger(BaseStorageResource.class);

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
    
    
    protected RDFStorage getStorage()
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
