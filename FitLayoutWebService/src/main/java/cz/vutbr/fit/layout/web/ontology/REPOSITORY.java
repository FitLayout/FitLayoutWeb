package cz.vutbr.fit.layout.web.ontology;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * FitLayout Repository Metadata.
 * <p>
 * FITLayout internal repository metadata..
 * <p>
 * Namespace REPOSITORY.
 * Prefix: {@code <http://fitlayout.github.io/ontology/repository.owl#>}
 */
public class REPOSITORY {

	/** {@code http://fitlayout.github.io/ontology/repository.owl#} **/
	public static final String NAMESPACE = "http://fitlayout.github.io/ontology/repository.owl#";

	/** {@code repository} **/
	public static final String PREFIX = "repository";

	/**
	 * {@code http://fitlayout.github.io/ontology/repository.owl#accessedOn}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#accessedOn">accessedOn</a>
	 */
	public static final IRI accessedOn;

	/**
	 * {@code http://fitlayout.github.io/ontology/repository.owl#createdOn}.
	 * <p>
	 * Creation date/time for the repository
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#createdOn">createdOn</a>
	 */
	public static final IRI createdOn;

	/**
	 * {@code http://fitlayout.github.io/ontology/repository.owl#email}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#email">email</a>
	 */
	public static final IRI email;

	/**
	 * {@code http://fitlayout.github.io/ontology/repository.owl#expires}.
	 * <p>
	 * Creation date/time for the repository
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#expires">expires</a>
	 */
	public static final IRI expires;

	/**
	 * {@code http://fitlayout.github.io/ontology/repository.owl#name}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#name">name</a>
	 */
	public static final IRI name;

	/**
	 * {@code http://fitlayout.github.io/ontology/repository.owl#owner}.
	 * <p>
	 * An identification of the service that created an artifact.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#owner">owner</a>
	 */
	public static final IRI owner;

	/**
	 * {@code http://fitlayout.github.io/ontology/repository.owl#Repository}.
	 * <p>
	 * An artifact repository
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#Repository">Repository</a>
	 */
	public static final IRI Repository;

	/**
	 * {@code http://fitlayout.github.io/ontology/repository.owl#uuid}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#uuid">uuid</a>
	 */
	public static final IRI uuid;

	/**
	 * {@code http://fitlayout.github.io/ontology/repository.owl#version}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#version">version</a>
	 */
	public static final IRI version;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();

		accessedOn = factory.createIRI(REPOSITORY.NAMESPACE, "accessedOn");
		createdOn = factory.createIRI(REPOSITORY.NAMESPACE, "createdOn");
		email = factory.createIRI(REPOSITORY.NAMESPACE, "email");
		expires = factory.createIRI(REPOSITORY.NAMESPACE, "expires");
		name = factory.createIRI(REPOSITORY.NAMESPACE, "name");
		owner = factory.createIRI(REPOSITORY.NAMESPACE, "owner");
		Repository = factory.createIRI(REPOSITORY.NAMESPACE, "Repository");
		uuid = factory.createIRI(REPOSITORY.NAMESPACE, "uuid");
		version = factory.createIRI(REPOSITORY.NAMESPACE, "version");
	}

	private REPOSITORY() {
		//static access only
	}

}
