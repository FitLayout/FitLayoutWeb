/**
 * StorageProviderMulti.java
 *
 * Created on 14. 4. 2021, 17:46:22 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.web.data.RepositoryInfo;
import cz.vutbr.fit.layout.web.data.StorageStatus;
import cz.vutbr.fit.layout.web.data.UserInfo;
import cz.vutbr.fit.layout.web.ontology.REPOSITORY;

/**
 * Storage provider implementation for a multi storage using a repository manager.
 * 
 * @author burgetr
 */
public class StorageProviderMulti implements StorageProvider
{
    private static Logger log = LoggerFactory.getLogger(StorageProviderMulti.class);
    
    private static final String DEFAULT_REPOSITORY = "default";
    
    private static final String SEP = "-";
    private static final int REPOSITORY_LIMIT = 4;
    private static final int REPOSITORY_EXPIRATION_HOURS = 12;
    private static final String METAFILE = "flrepos.ttl";
    private static final int METAFILE_VERSION = 1;
    
    private boolean autoCreateDefault;
    private String configPath;
    private LocalRepositoryManager manager;
    private Model metadata;
    private ValueFactory vf;
    

    public StorageProviderMulti(String configPath)
    {
        this.configPath = configPath;
        autoCreateDefault = false;
        vf = SimpleValueFactory.getInstance();
        init();
    }

    public boolean isAutoCreateDefault()
    {
        return autoCreateDefault;
    }

    public void setAutoCreateDefault(boolean autoCreateDefault)
    {
        this.autoCreateDefault = autoCreateDefault;
    }

    private void init()
    {
        final String path = configPath.replace("$HOME", System.getProperty("user.home"));
        final File baseDir = new File(path);
        manager = new LocalRepositoryManager(baseDir);
        manager.init();
        metadata = loadMetadata();
    }
    
    public boolean isReady()
    {
        return (manager != null && manager.isInitialized());
    }
    
    @Override
    public StorageStatus getStorageStatus(UserInfo user)
    {
        checkDefaultRepository(user);
        List<RepositoryInfo> repos = getRepositoryList(user);
        int cnt = repos.size();
        return new StorageStatus(false, cnt < REPOSITORY_LIMIT, cnt, REPOSITORY_LIMIT - cnt);
    }

    @Override
    public List<RepositoryInfo> getRepositoryList(UserInfo user)
    {
        if (isReady())
        {
            checkDefaultRepository(user);
            return findUserRepositories(user.getUserId());
        }
        else
            return List.of();
    }

    @Override
    public RepositoryInfo getRepositoryInfo(UserInfo user, String repoId)
    {
        if (isReady())
        {
            checkDefaultRepository(user);
            return findUserRepository(user.getUserId(), repoId);
        }
        else
            return null;
    }

    @Override
    public RDFStorage getStorage(UserInfo user, String repoId)
    {
        final Repository repo = manager.getRepository(user.getUserId() + SEP + repoId);
        if (repo != null)
            return RDFStorage.create(repo);
        else
            return null;
    }

    @Override
    public RDFArtifactRepository getArtifactRepository(UserInfo user, String repoId)
    {
        final RDFStorage storage = getStorage(user, repoId);
        if (storage != null)
            return new RDFArtifactRepository(storage);
        else
            return null;
    }

    @Override
    public void createRepository(UserInfo user, RepositoryInfo info)
            throws RepositoryException
    {
        if (info != null && info.getId() != null)
        {
            if (user.isAnonymous() || user.isGuest())
            {
                if (DEFAULT_REPOSITORY.equals(info.getId()))
                {
                    createRepositoryWithId(user.getUserId() + SEP + DEFAULT_REPOSITORY, info.getDescription(), user.getExpires());
                }
                else
                    throw new RepositoryException("Repository creation not allowed");
            }
            else
            {
                final String id = user.getUserId() + SEP + info.getId();
                var rinfo = manager.getRepositoryInfo(id);
                if (rinfo == null)
                {
                    createRepositoryWithId(id, info.getDescription(), null);
                }
                else
                    throw new RepositoryException("The repository already exists");
            }
        }
        else
            throw new RepositoryException("Illegal repository data");
    }

    private void createRepositoryWithId(String id, String description, Date expires)
    {
        log.info("Creating repository {}", id);
        final RepositoryConfig conf;
        if (description == null)
            conf = new RepositoryConfig(id, new SailRepositoryConfig(new NativeStoreConfig()));
        else
            conf = new RepositoryConfig(id, description, new SailRepositoryConfig(new NativeStoreConfig()));
        manager.addRepositoryConfig(conf);
        Repository repo = manager.getRepository(id);
        
        final IRI iri = repoIRI(id);
        metadata.add(iri, REPOSITORY.createdOn, vf.createLiteral(new Date()));
        if (expires != null)
            metadata.add(iri, REPOSITORY.expires, vf.createLiteral(expires));
        
        log.info("Created {}", repo);
    }
    
    @Override
    public void deleteRepository(UserInfo user, String repoId)
            throws RepositoryException
    {
        if (repoId != null)
        {
            if (user.isAnonymous() || user.isGuest())
            {
                throw new RepositoryException("Repository deletion not allowed");
            }
            else
            {
                final String id = user.getUserId() + SEP + repoId;
                var rinfo = manager.getRepositoryInfo(id);
                if (rinfo != null)
                {
                    deleteRepositoryWithId(id);
                }
                else
                    throw new RepositoryException("The repository does not exist");
            }
        }
        else
            throw new RepositoryException("Illegal repository data");
    }

    private void deleteRepositoryWithId(String id)
    {
        log.info("Deleting repository {}", id);
        manager.removeRepository(id);
    }
    
    private RepositoryInfo findUserRepository(String userId, String repoId)
    {
        final var info = manager.getRepositoryInfo(userId + SEP + repoId);
        if (info != null)
        {
            String id = info.getId().substring(userId.length() + 1);
            var ret = new RepositoryInfo(id, info.getDescription());
            return ret;
        }
        else
            return null;
    }

    private List<RepositoryInfo> findUserRepositories(String userId)
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

    private void checkDefaultRepository(UserInfo user)
            throws RepositoryException
    {
        if (autoCreateDefault)
        {
            final var infos = findUserRepositories(user.getUserId());
            if (infos.size() == 0)
            {
                RepositoryInfo defaultInfo = new RepositoryInfo(DEFAULT_REPOSITORY, "Default repository");
                createRepository(user, defaultInfo);
            }
        }
    }
    
    @Override
    public void close()
    {
        manager.shutDown();
        manager = null;
    }
    
    //=====================================================================================
    
    private Date getExpirationDate()
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, REPOSITORY_EXPIRATION_HOURS);
        return c.getTime();
    }
    
    private IRI repoIRI(String id)
    {
        return vf.createIRI(REPOSITORY.NAMESPACE, "r-" + id);
    }
    
    private Model emptyMetadata()
    {
        Model ret = new LinkedHashModel();
        ret.add(REPOSITORY.Repository, REPOSITORY.version, vf.createLiteral(METAFILE_VERSION));
        ret.add(REPOSITORY.Repository, REPOSITORY.createdOn, vf.createLiteral(new java.util.Date()));
        return ret;
    }
    
    private Model loadMetadata()
    {
        Model ret = null;
        final File mdfile = manager.resolvePath(METAFILE);
        if (mdfile.exists())
        {
            try (InputStream is = new FileInputStream(mdfile)) {
                ret = Rio.parse(is, null, RDFFormat.TURTLE);
                return ret;
            } catch (IOException e) {
                log.error("Couldn't load metadata: {}", e.getMessage());
            }
        }
        if (ret == null)
            ret = new LinkedHashModel();
        return ret;
    }
    
    private void saveMetadata(Model model)
    {
        final File mdfile = manager.resolvePath(METAFILE);
        try (OutputStream os = new FileOutputStream(mdfile)) {
            Rio.write(model, os, RDFFormat.TURTLE);
        } catch (IOException e) {
            log.error("Couldn't save metadata: {}", e.getMessage());
        }
    }
    
}
