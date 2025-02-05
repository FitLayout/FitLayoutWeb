/**
 * StorageService.java
 *
 * Created on 23. 9. 2020, 21:07:05 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import java.util.List;
import java.util.stream.Collectors;

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
import cz.vutbr.fit.layout.web.data.UserInfo;

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
    
    private boolean singleMode;
    private StorageProvider provider;
    
    @PostConstruct
    public void init()
    {
        switch (configStorage)
        {
            case "multi":
                log.info("Initializing multi mode repository in {}", configPath);
                provider = new StorageProviderMulti(configPath);
                singleMode = false;
                break;
            case "localdir":
                log.info("Initializing local mode repository in {}", configPath);
                provider = new StorageProviderLocal(configPath);
                singleMode = false;
                break;
            default:
                log.info("Initializing single mode repository");
                provider = new StorageProviderSingle(configStorage, configServer, configRepository, configPath);
                singleMode = true;
                break;
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
    
    public void touch(UserInfo user, String repoId)
    {
        provider.touch(user, repoId);
    }
    
    public StorageStatus getStatus(UserInfo user)
    {
        return provider.getStorageStatus(user);
    }
    
    public List<RepositoryInfo> getAllRepositories()
    {
        return provider.getRepositoryListAll();
    }
    
    public List<RepositoryInfo> getRepositoriesForEmail(String email)
    {
        return provider.getRepositoryListAll()
                .stream()
                .filter(r -> (r.getEmail() != null && email.trim().equals(r.getEmail())))
                .collect(Collectors.toList());
    }
    
    public List<RepositoryInfo> getRepositories(UserInfo user)
    {
        return provider.getRepositoryList(user);
    }
    
    public RepositoryInfo getRepositoryInfo(UserInfo user, String repoId)
    {
        return provider.getRepositoryInfo(user, repoId);
    }
    
    public RDFStorage getStorage(UserInfo user, String repoId)
    {
        return provider.getStorage(user, repoId);
    }
    
    public RDFArtifactRepository getArtifactRepository(UserInfo user, String repoId)
    {
        return provider.getArtifactRepository(user, repoId);
    }

    public void createRepository(UserInfo user, RepositoryInfo data)
        throws RepositoryException
    {
        provider.createRepository(user, data);
    }
    
    public RepositoryInfo updateRepository(UserInfo user, String repoId, RepositoryInfo info)
            throws RepositoryException
    {
        return provider.updateRepository(user, repoId, info);
    }
    
    public void deleteRepository(UserInfo user, String repoId)
            throws RepositoryException
    {
        provider.deleteRepository(user, repoId);
    }

    public boolean isRepoReadOnly(UserInfo user, String repoId)
    {
        var info = getRepositoryInfo(user, repoId);
        return (info.getReadOnly() != null && info.getReadOnly() == true);
    }
    
    public void closeStorage()
    {
        provider.close();
    }
    
}
