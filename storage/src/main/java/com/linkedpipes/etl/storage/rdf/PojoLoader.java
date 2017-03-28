package com.linkedpipes.etl.storage.rdf;

import com.linkedpipes.etl.storage.BaseException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import java.util.Collection;

/**
 * Class used to load data from RDF to POJO.
 */
public class PojoLoader {

    /**
     * TODO Replace with IllegalArgumentException
     */
    public static class CantLoadException extends BaseException {

        public CantLoadException(String message, Object... args) {
            super(message, args);
        }

        public CantLoadException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Interface for loadable entities.
     */
    public interface Loadable {

        /**
         * Set the resource IRI. Is called before any call of the load
         * functions.
         *
         * @param iri
         */
        default void loadIri(String iri) {
            // No-operation here.
        }

        default Loadable load(String predicate, Value value)
                throws CantLoadException {
            // No-operation here.
            return null;
        }

        default Loadable load(String predicate, Value value, Resource graph)
                throws CantLoadException {
            // Default implementation call the variant that ignores the graphs.
            return load(predicate, value);
        }

    }

    private PojoLoader() {
    }

    /**
     * Load given resource into given object.
     *
     * @param statements
     * @param resource
     * @param graph
     * @param instance
     */
    public static void loadFromResource(Collection<Statement> statements,
            Resource resource, Resource graph, Loadable instance)
            throws CantLoadException {
        instance.loadIri(resource.stringValue());
        for (Statement s : statements) {
            boolean sameGraph = graph == null || graph.equals(s.getContext());
            if (sameGraph && s.getSubject().equals(resource)) {
                final Loadable newObject = instance.load(
                        s.getPredicate().stringValue(), s.getObject(), graph);
                // Load new object if created.
                if (newObject != null) {
                    loadFromResource(statements, (Resource) s.getObject(),
                            graph, newObject);
                }
            }
        }
    }

    /**
     * Load first instance of given type to given object.
     *
     * @param statements
     * @param type
     * @param instance
     */
    public static void loadOfType(Collection<Statement> statements, IRI type,
            Loadable instance) throws CantLoadException {
        for (Statement s : statements) {
            // Check the statement identify object of given type.
            if (RDF.TYPE.equals(s.getPredicate()) &&
                    type.equals(s.getObject())) {
                loadFromResource(statements, s.getSubject(), s.getContext(),
                        instance);
                return;
            }
        }
    }

}
