/**
 * StorageProvider.java
 *
 * Created on 14. 4. 2021, 17:35:32 by burgetr
 */

package cz.vutbr.fit.layout.web.ejb;

import java.util.List;

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
    
    public StorageStatus getStorageStatus();
    
    public List<RepositoryInfo> getRepositoryList();

    public RDFStorage getStorage(String userId, String repoId);

    public RDFArtifactRepository getArtifactRepository(String userId, String repoId);

    public void close();

}