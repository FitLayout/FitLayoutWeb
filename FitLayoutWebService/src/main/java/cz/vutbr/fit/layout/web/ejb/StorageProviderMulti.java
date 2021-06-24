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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.XSD;
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
import cz.vutbr.fit.layout.rdf.RESOURCE;
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
    
    private static final String SEP = "-";
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
        List<RepositoryInfo> repos = getRepositoryList(user);
        int cnt = repos.size();
        return new StorageStatus(false, true, cnt, -1);
    }

    @Override
    public List<RepositoryInfo> getRepositoryList(UserInfo user)
    {
        if (isReady())
        {
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
        {
            updateAccessDate(repoId);
            return RDFStorage.create(repo);
        }
        else
            return null;
    }

    @Override
    public RDFArtifactRepository getArtifactRepository(UserInfo user, String repoId)
    {
        final RDFStorage storage = getStorage(user, repoId);
        if (storage != null)
        {
            updateAccessDate(repoId);
            return new RDFArtifactRepository(storage);
        }
        else
            return null;
    }

    @Override
    public void createRepository(UserInfo user, RepositoryInfo info)
            throws RepositoryException
    {
        if (info != null)
        {
            final UUID uuid = UUID.randomUUID();
            info.setId(uuid.toString());
            final String repoId = user.getUserId() + SEP + info.getId();
            
            createRepositoryWithId(repoId, user.getUserId(), info);
        }
        else
            throw new RepositoryException("Illegal repository data");
    }

    private void createRepositoryWithId(String id, String owner, RepositoryInfo info)
    {
        log.info("Creating repository {}", id);
        final RepositoryConfig conf;
        if (info.getDescription() == null)
            conf = new RepositoryConfig(id, new SailRepositoryConfig(new NativeStoreConfig()));
        else
            conf = new RepositoryConfig(id, info.getDescription(), new SailRepositoryConfig(new NativeStoreConfig()));
        manager.addRepositoryConfig(conf);
        Repository repo = manager.getRepository(id);
        
        final IRI iri = repoIRI(info.getId());
        metadata.add(iri, REPOSITORY.uuid, vf.createLiteral(info.getId()));
        metadata.add(iri, REPOSITORY.version, vf.createLiteral(METAFILE_VERSION));
        metadata.add(iri, REPOSITORY.createdOn, vf.createLiteral(new Date()));
        metadata.add(iri, REPOSITORY.accessedOn, vf.createLiteral(new Date()));
        if (info.getExpires() != null)
            metadata.add(iri, REPOSITORY.expires, vf.createLiteral(info.getExpires()));
        if (info.getEmail() != null)
            metadata.add(iri, REPOSITORY.email, vf.createLiteral(info.getEmail()));
        if (owner != null)
            metadata.add(iri, REPOSITORY.owner, vf.createLiteral(owner));
        if (info.getDescription() != null)
            metadata.add(iri, REPOSITORY.name, vf.createLiteral(info.getDescription()));
        saveMetadata();
        
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
                    deleteRepositoryWithId(id, repoId);
                }
                else
                    throw new RepositoryException("The repository does not exist");
            }
        }
        else
            throw new RepositoryException("Illegal repository data");
    }

    private void deleteRepositoryWithId(String id, String uuid)
    {
        log.info("Deleting repository {}", id);
        manager.removeRepository(id);
        final IRI iri = repoIRI(uuid);
        metadata.remove(iri, null, null);
    }
    
    private RepositoryInfo findRepository(String uuid)
    {
        Optional<IRI> repoIri = Models.subjectIRI(metadata.filter(null, REPOSITORY.uuid, vf.createLiteral(uuid)));
        if (repoIri.isPresent())
            return loadRepositoryInfo(repoIri.get());
        else
            return null;
    }
    
    private RepositoryInfo loadRepositoryInfo(IRI repositoryIri)
    {
        RepositoryInfo ret = new RepositoryInfo();
        for (Statement st : metadata.filter(repositoryIri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            
            if (REPOSITORY.uuid.equals(pred))
            {
                ret.setId(value.stringValue());
            }
            else if (REPOSITORY.accessedOn.equals(pred))
            {
                if (value instanceof Literal)
                    ret.setAccessedOn(((Literal) value).calendarValue().toGregorianCalendar().getTime());
            }
            else if (REPOSITORY.createdOn.equals(pred))
            {
                if (value instanceof Literal)
                    ret.setCreatedOn(((Literal) value).calendarValue().toGregorianCalendar().getTime());
            }
            else if (REPOSITORY.expires.equals(pred))
            {
                if (value instanceof Literal)
                    ret.setExpires(((Literal) value).calendarValue().toGregorianCalendar().getTime());
            }
            else if (REPOSITORY.email.equals(pred))
            {
                ret.setEmail(value.stringValue());
            }
            else if (REPOSITORY.owner.equals(pred))
            {
                ret.setOwner(value.stringValue());
            }
            else if (REPOSITORY.name.equals(pred))
            {
                ret.setDescription(value.stringValue());
            }
        }
        return ret;
    }
    
    private RepositoryInfo findUserRepository(String userId, String repoId)
    {
        final var info = manager.getRepositoryInfo(userId + SEP + repoId);
        if (info != null)
        {
            RepositoryInfo ret = findRepository(repoId);
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
                ret.add(findRepository(id));
            }
        }
        return ret;
    }

    private void updateAccessDate(String uuid)
    {
        final IRI iri = repoIRI(uuid);
        metadata.remove(iri, REPOSITORY.accessedOn, null);
        metadata.add(iri, REPOSITORY.accessedOn, vf.createLiteral(new Date()));
        saveMetadata();
    }
    
    @Override
    public void close()
    {
        manager.shutDown();
        manager = null;
    }
    
    //=====================================================================================
    
    private IRI repoIRI(String uuid)
    {
        return vf.createIRI("urn:uuid:" + uuid);
    }
    
    private Model emptyMetadata()
    {
        Model ret = new LinkedHashModel();
        ret.setNamespace("rr", REPOSITORY.NAMESPACE);
        ret.setNamespace("xsd", XSD.NAMESPACE);
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
                ret = Rio.parse(is, RESOURCE.NAMESPACE, RDFFormat.TURTLE);
                return ret;
            } catch (IOException e) {
                log.error("Couldn't load metadata: {}", e.getMessage());
            }
        }
        if (ret == null)
            ret = emptyMetadata();
        return ret;
    }
    
    private void saveMetadata()
    {
        saveMetadata(metadata);
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
