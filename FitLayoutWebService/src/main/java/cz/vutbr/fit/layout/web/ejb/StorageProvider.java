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
import cz.vutbr.fit.layout.web.data.UserInfo;

/**
 * An interface of a provider that is able to create and open RDF repositories for users.
 * 
 * @author burgetr
 */
public interface StorageProvider
{

    public boolean isReady();
    
    public StorageStatus getStorageStatus(UserInfo user);
    
    public List<RepositoryInfo> getRepositoryList(UserInfo user);
    
    public RepositoryInfo getRepositoryInfo(UserInfo user, String repoId);

    public RDFStorage getStorage(UserInfo user, String repoId);

    public RDFArtifactRepository getArtifactRepository(UserInfo user, String repoId);
    
    public void createRepository(UserInfo user, RepositoryInfo info)
        throws RepositoryException;
    
    public void deleteRepository(UserInfo user, String repoId)
        throws RepositoryException;

    public void close();

}