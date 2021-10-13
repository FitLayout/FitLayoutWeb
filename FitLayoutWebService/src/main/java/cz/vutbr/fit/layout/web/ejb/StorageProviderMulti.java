/**
 * StorageProviderMulti.java
 *
 * Created on 14. 4. 2021, 17:46:22 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.rdf4j.IsolationLevels;
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
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
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
    
    private static final String SEP = "-";
    private static final String META_REPOSITORY = "SYSTEM-FITLAYOUT";
    private static final int METAFILE_VERSION = 1;
    
    private String configPath;
    private LocalRepositoryManager manager;
    private ValueFactory vf;
    

    public StorageProviderMulti(String configPath)
    {
        this.configPath = configPath;
        vf = SimpleValueFactory.getInstance();
        init();
    }

    private void init()
    {
        final String path = configPath.replace("$HOME", System.getProperty("user.home"));
        final File baseDir = new File(path);
        manager = new LocalRepositoryManager(baseDir);
        manager.init();
    }
    
    public boolean isReady()
    {
        return (manager != null && manager.isInitialized());
    }
    
    @Override
    public void touch(UserInfo user, String repoId)
    {
        updateAccessDate(repoId);
    }

    @Override
    public StorageStatus getStorageStatus(UserInfo user)
    {
        dumpMetadata();
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
    public List<RepositoryInfo> getRepositoryListAll()
    {
        if (isReady())
        {
            return findAllRepositories();
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
        Repository repo = manager.getRepository(user.getUserId() + SEP + repoId);
        //if the repository is not defined for the user, try to find it for anonymous guest
        if (!user.isAnonymous() && repo == null)
            repo = manager.getRepository(UserInfo.ANONYMOUS_USER + SEP + repoId);
        
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
        Model metadata = new LinkedHashModel();
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
        addMetadata(metadata);
        
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
        try (RepositoryConnection con = getMetaRepository().getConnection()) {
            con.remove(iri, null, null);
        }
    }
    
    private RepositoryInfo findRepository(String uuid)
    {
        try (RepositoryConnection con = getMetaRepository().getConnection()) {
            RepositoryResult<Statement> statements = con.getStatements(null, REPOSITORY.uuid, vf.createLiteral(uuid));
            Optional<IRI> repoIri = Models.subjectIRI(statements);
            if (repoIri.isPresent())
                return loadRepositoryInfo(con, repoIri.get());
            else
                return null;
        }
    }
    
    private RepositoryInfo loadRepositoryInfo(RepositoryConnection con, IRI repositoryIri)
    {
        RepositoryInfo ret = new RepositoryInfo();
        final RepositoryResult<Statement> statements = con.getStatements(repositoryIri, null, null);
        for (Statement st : statements)
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
        var info = manager.getRepositoryInfo(userId + SEP + repoId);
        //if the repository is not defined for the user, try to find it for anonymous guest
        if (info == null && !UserInfo.ANONYMOUS_USER.equals(userId))
            info = manager.getRepositoryInfo(UserInfo.ANONYMOUS_USER + SEP + repoId);
        
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
                RepositoryInfo repoInfo = findRepository(id);
                if (repoInfo != null)
                    ret.add(repoInfo);
                else
                    log.debug("Repository {} has no metadata", info.getId()); // repository exists in manager but has no metadata in meta-storage (orphaned?)
            }
        }
        return ret;
    }

    private List<RepositoryInfo> findAllRepositories()
    {
        final var infos = manager.getAllRepositoryInfos();
        final List<RepositoryInfo> ret = new ArrayList<>(infos.size());
        for (var info : infos)
        {
            int i = info.getId().indexOf(SEP);
            if (i != -1)
            {
                String id = info.getId().substring(i + 1);
                RepositoryInfo repoInfo = findRepository(id);
                if (repoInfo != null)
                    ret.add(repoInfo);
                else
                    log.info("Repository {} has no metadata", info.getId()); // repository exists in manager but has no metadata in meta-storage (orphaned?)
            }
        }
        return ret;
    }

    private void updateAccessDate(String uuid)
    {
        final IRI iri = repoIRI(uuid);
        try (RepositoryConnection con = getMetaRepository().getConnection()) {
            con.begin(IsolationLevels.SERIALIZABLE);
            con.remove(iri, REPOSITORY.accessedOn, null);
            con.add(iri, REPOSITORY.accessedOn, vf.createLiteral(new Date()));
            con.commit();
        }
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
    
    private Repository getMetaRepository()
    {
        Repository repo = manager.getRepository(META_REPOSITORY);
        if (repo == null)
        {
            final RepositoryConfig conf = new RepositoryConfig(META_REPOSITORY, new SailRepositoryConfig(new NativeStoreConfig()));
            manager.addRepositoryConfig(conf);
            repo = manager.getRepository(META_REPOSITORY);
            if (repo != null)
            {
                // initialize the connection with default metadata
                try (RepositoryConnection con = repo.getConnection()) {
                    con.add(emptyMetadata());
                }
            }
            else
                log.error("Internal error: Couldn't create metadata repository");
        }
        return repo;
    }
    
    private void addMetadata(Model metadata)
    {
        log.debug("Adding metadata");
        final Repository repo = getMetaRepository();
        if (repo != null)
        {
            try (RepositoryConnection con = repo.getConnection()) {
                con.add(metadata);
            } catch (RepositoryException e) {
                log.error("Could not save metadata: {}", e.getMessage());
            }
        } 
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
    
    private void dumpMetadata()
    {
        try (RepositoryConnection con = getMetaRepository().getConnection()) {
            RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, System.out);
            con.prepareGraphQuery(QueryLanguage.SPARQL,
                "CONSTRUCT {?s ?p ?o } WHERE {?s ?p ?o } ").evaluate(writer);
        }        
    }
    
}
