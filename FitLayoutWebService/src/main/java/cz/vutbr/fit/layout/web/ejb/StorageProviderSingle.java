/**
 * StorageProviderSingle.java
 *
 * Created on 14. 4. 2021, 17:35:32 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import java.util.List;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.web.data.RepositoryInfo;

/**
 * Storage provider implementation for a single storage mode.
 * 
 * @author burgetr
 */
public class StorageProviderSingle implements StorageProvider
{
    private static Logger log = LoggerFactory.getLogger(StorageProviderSingle.class);
    
    private static String DEFAULT_REPOSITORY = "default";

    private String configStorage;
    private String configServer;
    private String configRepository;
    private String configPath;
    
    private RDFStorage storage;
    private RDFArtifactRepository artifactRepository;
    
    public StorageProviderSingle(String configStorage, String configServer,
            String configRepository, String configPath)
    {
        this.configStorage = configStorage;
        this.configServer = configServer;
        this.configRepository = configRepository;
        this.configPath = configPath;
        init();
    }

    private void init()
    {
        storage = createLocalStorage();
        if (storage != null)
            artifactRepository = createLocalArtifactRepository(storage);
    }
    
    @Override
    public boolean isReady()
    {
        return storage != null;
    }
    
    @Override
    public List<RepositoryInfo> getRepositoryList()
    {
        final RepositoryInfo info = new RepositoryInfo(DEFAULT_REPOSITORY, "The default preconfigured repository");
        return List.of(info);
    }

    @Override
    public RDFStorage getStorage(String userId, String repoId)
    {
        if (DEFAULT_REPOSITORY.equals(repoId))
            return storage;
        else
            return null;
    }
    
    @Override
    public RDFArtifactRepository getArtifactRepository(String userId, String repoId)
    {
        if (DEFAULT_REPOSITORY.equals(repoId) && storage != null)
            return artifactRepository;
        else
            return null;
    }
    
    @Override
    public void close()
    {
        if (storage != null)
        {
            log.info("Closing storage");
            storage.close();
        }
    }
    
    //==================================================================================
    
    private RDFStorage createLocalStorage()
    {
        RDFStorage storage = null;
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
        return storage;
    }
    
    private RDFArtifactRepository createLocalArtifactRepository(RDFStorage storage)
    {
        return new RDFArtifactRepository(storage);
    }
    
}
