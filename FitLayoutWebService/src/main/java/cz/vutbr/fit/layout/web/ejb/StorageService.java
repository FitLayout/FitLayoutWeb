/**
 * StorageService.java
 *
 * Created on 23. 9. 2020, 21:07:05 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.web.data.RepositoryInfo;
import cz.vutbr.fit.layout.web.data.StorageStatus;

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
    @ConfigProperty(name = "fitlayout.rdf.server", defaultValue = "http://localhost:8080/rdf4j-server")
    String configServer;

    @Inject
    @ConfigProperty(name = "fitlayout.rdf.repository", defaultValue = "fitlayout")
    String configRepository;

    @Inject
    @ConfigProperty(name = "fitlayout.rdf.path", defaultValue = "$HOME/.fitlayout/storage")
    String configPath;
    
    @Inject
    @ConfigProperty(name = "fitlayout.rdf.autoCreateDefault", defaultValue = "false")
    boolean autoCreateDefault;
    
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
            log.info("Initializing multi mode repository in {}", configPath);
            provider = new StorageProviderMulti(configPath);
            ((StorageProviderMulti) provider).setAutoCreateDefault(autoCreateDefault);
        }
    }
    
    @PreDestroy
    public void destroy()
    {
        closeStorage();
    }
    
    //===============================================================================================
    
    public boolean isSingleMode()
    {
        return singleMode;
    }
    
    public boolean isReady()
    {
        return provider.isReady();
    }
    
    public StorageStatus getStatus(String userId)
    {
        return provider.getStorageStatus(userId);
    }
    
    public List<RepositoryInfo> getRepositories(String userId)
    {
        return provider.getRepositoryList(userId);
    }
    
    public RDFStorage getStorage(String userId, String repoId)
    {
        return provider.getStorage(userId, repoId);
    }
    
    public RDFArtifactRepository getArtifactRepository(String userId, String repoId)
    {
        return provider.getArtifactRepository(userId, repoId);
    }

    public void createRepository(String userId, RepositoryInfo data)
        throws RepositoryException
    {
        provider.createRepository(userId, data);
    }
    
    public void deleteRepository(String userId, String repoId)
            throws RepositoryException
    {
        provider.deleteRepository(userId, repoId);
    }
        
    public void closeStorage()
    {
        provider.close();
    }
    
}
