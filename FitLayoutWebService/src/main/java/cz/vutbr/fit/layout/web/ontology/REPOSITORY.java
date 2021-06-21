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
	 * {@code http://fitlayout.github.io/ontology/repository.owl#createdOn}.
	 * <p>
	 * Creation date/time for the repository
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#createdOn">createdOn</a>
	 */
	public static final IRI createdOn;

	/**
	 * {@code http://fitlayout.github.io/ontology/repository.owl#expires}.
	 * <p>
	 * Creation date/time for the repository
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#expires">expires</a>
	 */
	public static final IRI expires;

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
	 * {@code http://fitlayout.github.io/ontology/repository.owl#version}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/repository.owl#version">version</a>
	 */
	public static final IRI version;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();

		createdOn = factory.createIRI(REPOSITORY.NAMESPACE, "createdOn");
		expires = factory.createIRI(REPOSITORY.NAMESPACE, "expires");
		owner = factory.createIRI(REPOSITORY.NAMESPACE, "owner");
		Repository = factory.createIRI(REPOSITORY.NAMESPACE, "Repository");
		version = factory.createIRI(REPOSITORY.NAMESPACE, "version");
	}

	private REPOSITORY() {
		//static access only
	}

}
