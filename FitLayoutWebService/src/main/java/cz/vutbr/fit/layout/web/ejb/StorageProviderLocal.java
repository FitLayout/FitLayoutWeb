/**
 * StorageProviderLocal.java
 *
 * Created on 15. 1. 2025, 12:22:01 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.web.data.RepositoryInfo;
import cz.vutbr.fit.layout.web.data.StorageStatus;
import cz.vutbr.fit.layout.web.data.UserInfo;

/**
 * Storage provider implementation for the "local" storage mode. The repositories are in subdirectories
 * of the configured path and cannot be added, updated, or deleted.
 * 
 * @author burgetr
 */
public class StorageProviderLocal implements StorageProvider
{
    private static Logger log = LoggerFactory.getLogger(StorageProviderLocal.class);

    private String configPath;
    private boolean storageReady;
    
    private Map<String, RDFStorage> storageMap;
    private Map<String, RDFArtifactRepository> repositoryMap;

    public StorageProviderLocal(String configPath)
    {
        this.configPath = configPath;
        storageMap = new HashMap<>();
        repositoryMap = new HashMap<>();
        init();
    }
    
    private void init()
    {
        // check if the storage path exists and is accessible
        storageReady = true;
        try
        {
            final String path = configPath.replace("$HOME", System.getProperty("user.home"));
            if (!new java.io.File(path).exists())
            {
                log.error("Storage path {} does not exist", path);
                storageReady = false;
            }
            else
            {
                configPath = path;
            }
        }
        catch (Exception e)
        {
            log.error("Error checking storage path {}", configPath, e);
            storageReady = false;
        }
    }
    
    @Override
    public boolean isReady()
    {
        return storageReady;
    }
    
    @Override
    public void touch(UserInfo user, String repoId)
    {
    }

    @Override
    public StorageStatus getStorageStatus(UserInfo user)
    {
        return new StorageStatus(true, false, 1, 0);
    }

    @Override
    public List<RepositoryInfo> getRepositoryList(UserInfo user)
    {
        return getRepositoryListAll();
    }

    @Override
    public List<RepositoryInfo> getRepositoryListAll()
    {
        // find all subdirectories in the storage path
        File[] files = new File(configPath).listFiles();
        List<RepositoryInfo> repos = new ArrayList<>();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    repos.add(getRepositoryInfo(null, file.getName()));
                }
            }
        }
        return repos;
    }

    @Override
    public RepositoryInfo getRepositoryInfo(UserInfo user, String repoId)
    {
        final String path = getRepositoryPath(repoId);
        if (isReady() && new File(path).exists())
            return new RepositoryInfo(repoId, sanitizeName(repoId));
        else
            return null;
    }

    @Override
    public RDFStorage getStorage(UserInfo user, String repoId)
    {
        final String path = getRepositoryPath(repoId);
        if (isReady() && new File(path).exists())
        {
            RDFStorage storage = storageMap.get(repoId);
            if (storage == null)
            {
                storage = openLocalStorage(repoId);
                storageMap.put(repoId, storage);
            }
            return storage;
        }
        else
            return null;
    }
    
    @Override
    public RDFArtifactRepository getArtifactRepository(UserInfo user, String repoId)
    {
        RDFStorage storage = getStorage(user, repoId);
        if (storage != null)
        {
            RDFArtifactRepository repository = repositoryMap.get(repoId);
            if (repository == null)
            {
                repository = createLocalArtifactRepository(storage);
                repositoryMap.put(repoId, repository);
            }
            return repository;
        }
        else
            return null;
    }
    
    @Override
    public void createRepository(UserInfo user, RepositoryInfo info)
        throws RepositoryException
    {
        throw new RepositoryException("Repository creation is disabled");
    }

    @Override
    public RepositoryInfo updateRepository(UserInfo user, String repoId, RepositoryInfo info)
            throws RepositoryException
    {
        // nothing can be updated with the local repository
        return getRepositoryInfo(user, repoId);
    }

    @Override
    public void deleteRepository(UserInfo user, String repoId)
        throws RepositoryException
    {
        throw new RepositoryException("Repository deletion is disabled");
    }

    @Override
    public void close()
    {
        if (isReady())
        {
            for (Map.Entry<String, RDFStorage> entry : storageMap.entrySet())
            {
                log.info("Closing storage {}", entry.getKey());
                entry.getValue().close();
            }
            storageMap.clear();
        }
    }
    
    //==================================================================================
    
    private RDFStorage openLocalStorage(String repositoryName)
    {
        RDFStorage storage = null;
        try {
            // remove characters that could cause issues in file paths
            final String path = getRepositoryPath(repositoryName);
            storage = RDFStorage.createNative(path);
            log.info("Using rdf4j file storage in {}", path);
        } catch (RepositoryException e) {
            storage = null;
        }
        return storage;
    }
    
    private RDFArtifactRepository createLocalArtifactRepository(RDFStorage storage)
    {
        return new RDFArtifactRepository(storage);
    }
    
    private String getRepositoryPath(String repositoryName)
    {
        return new StringBuilder(configPath).append(File.separator).append(sanitizeName(repositoryName)).toString();
    }
    
    private String sanitizeName(String name)
    {
        return name.replaceAll("[^a-zA-Z0-9_-]", "");
    }

}
