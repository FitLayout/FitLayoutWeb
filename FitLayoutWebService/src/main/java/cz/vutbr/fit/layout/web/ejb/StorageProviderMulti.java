/**
 * StorageProviderMulti.java
 *
 * Created on 14. 4. 2021, 17:46:22 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.web.data.RepositoryInfo;
import cz.vutbr.fit.layout.web.data.StorageStatus;
import cz.vutbr.fit.layout.web.data.UserInfo;

/**
 * Storage provider implementation for a multi storage using a repository manager.
 * 
 * @author burgetr
 */
public class StorageProviderMulti implements StorageProvider
{
    private static Logger log = LoggerFactory.getLogger(StorageProviderMulti.class);
    
    private static final String SEP = "-";
    private static final int REPOSITORY_LIMIT = 4;
    
    private String configPath;
    private RepositoryManager manager;

    public StorageProviderMulti(String configPath)
    {
        this.configPath = configPath;
        init();
    }

    private void init()
    {
        File baseDir = new File(configPath);
        manager = new LocalRepositoryManager(baseDir);
        manager.init();
    }
    
    public boolean isReady()
    {
        return (manager != null && manager.isInitialized());
    }
    
    @Override
    public StorageStatus getStorageStatus(String userId)
    {
        List<RepositoryInfo> repos = getRepositoryList(userId);
        int cnt = repos.size();
        return new StorageStatus(false, cnt < REPOSITORY_LIMIT, cnt, REPOSITORY_LIMIT - cnt);
    }

    @Override
    public List<RepositoryInfo> getRepositoryList(String userId)
    {
        if (isReady())
        {
            final var infos = manager.getAllRepositoryInfos();
            final List<RepositoryInfo> ret = new ArrayList<>(infos.size());
            for (var info : infos)
            {
                log.debug("Found: {}", info.getId());
                if (info.getId().startsWith(userId + SEP))
                {
                    String id = info.getId().substring(userId.length() + 1);
                    ret.add(new RepositoryInfo(id, info.getDescription()));
                }
            }
            return ret;
        }
        else
            return List.of();
    }

    @Override
    public RDFStorage getStorage(String userId, String repoId)
    {
        final Repository repo = manager.getRepository(userId + SEP + repoId);
        if (repo != null)
            return RDFStorage.create(repo);
        else
            return null;
    }

    @Override
    public RDFArtifactRepository getArtifactRepository(String userId, String repoId)
    {
        final RDFStorage storage = getStorage(userId, repoId);
        if (storage != null)
            return new RDFArtifactRepository(storage);
        else
            return null;
    }

    @Override
    public void createRepository(String userId, RepositoryInfo info)
            throws RepositoryException
    {
        if (info != null && info.getId() != null)
        {
            if (UserInfo.GUEST_USER.equals(userId))
            {
                if ("default".equals(info.getId()))
                {
                    createRepositoryWithId(userId + SEP + "default", info.getDescription());
                }
                else
                    throw new RepositoryException("Repository creation not allowed");
            }
            else
            {
                final String id = userId + SEP + info.getId();
                var rinfo = manager.getRepositoryInfo(id);
                if (rinfo == null)
                {
                    createRepositoryWithId(id, info.getDescription());
                }
                else
                    throw new RepositoryException("The repository already exists");
            }
        }
        else
            throw new RepositoryException("Illegal repository data");
    }

    private void createRepositoryWithId(String id, String description)
    {
        log.info("Creating repository {}", id);
        final RepositoryConfig conf;
        if (description == null)
            conf = new RepositoryConfig(id, new SailRepositoryConfig(new NativeStoreConfig()));
        else
            conf = new RepositoryConfig(id, description, new SailRepositoryConfig(new NativeStoreConfig()));
        manager.addRepositoryConfig(conf);
        Repository repo = manager.getRepository(id);
        log.info("Created {}", repo);
    }
    
    @Override
    public void close()
    {
        manager.shutDown();
        manager = null;
    }
    
}
