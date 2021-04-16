/**
 * StorageProviderMulti.java
 *
 * Created on 14. 4. 2021, 17:46:22 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.web.data.RepositoryInfo;
import cz.vutbr.fit.layout.web.data.StorageStatus;

/**
 * Storage provider implementation for a multi storage using a repository manager.
 * 
 * @author burgetr
 */
public class StorageProviderMulti implements StorageProvider
{
    private static Logger log = LoggerFactory.getLogger(StorageProviderMulti.class);

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
    public StorageStatus getStorageStatus()
    {
        return new StorageStatus(true, false, 1, 0);
    }

    @Override
    public List<RepositoryInfo> getRepositoryList()
    {
        if (isReady())
        {
            final var infos = manager.getAllRepositoryInfos();
            final List<RepositoryInfo> ret = new ArrayList<>(infos.size());
            for (var info : infos)
                ret.add(new RepositoryInfo(info.getId(), info.getDescription()));
            return ret;
        }
        else
            return List.of();
    }

    @Override
    public RDFStorage getStorage(String userId, String repoId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RDFArtifactRepository getArtifactRepository(String userId, String repoId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close()
    {
        // TODO Auto-generated method stub
        
    }
    
    
}
