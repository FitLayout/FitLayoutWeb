/**
 * StorageProvider.java
 *
 * Created on 14. 4. 2021, 17:35:32 by burgetr
 */

package cz.vutbr.fit.layout.web.ejb;

import java.util.List;

import org.eclipse.rdf4j.repository.RepositoryException;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.web.data.RepositoryInfo;
import cz.vutbr.fit.layout.web.data.StorageStatus;

/**
 * An interface of a provider that is able to create and open RDF repositories for users.
 * 
 * @author burgetr
 */
public interface StorageProvider
{

    public boolean isReady();
    
    public StorageStatus getStorageStatus(String userId);
    
    public List<RepositoryInfo> getRepositoryList(String userId);
    
    public RepositoryInfo getRepositoryInfo(String userId, String repoId);

    public RDFStorage getStorage(String userId, String repoId);

    public RDFArtifactRepository getArtifactRepository(String userId, String repoId);
    
    public void createRepository(String userId, RepositoryInfo info)
        throws RepositoryException;
    
    public void deleteRepository(String userId, String repoId)
        throws RepositoryException;

    public void close();

}