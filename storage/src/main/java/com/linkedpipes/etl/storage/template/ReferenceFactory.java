package com.linkedpipes.etl.storage.template;

import com.linkedpipes.etl.storage.BaseException;
import com.linkedpipes.etl.storage.rdf.RdfUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Create a component content that can be loaded as a component reference.
 */
final class ReferenceFactory {

    private ReferenceFactory() {
    }

    /**
     * Create a component in given directory from given RDF.
     *
     * @param templateRdf
     * @param configurationRdf
     * @param destination
     * @param iriAsString
     * @param templates
     */
    public static void create(Collection<Statement> templateRdf,
            Collection<Statement> configurationRdf, File destination,
            String iriAsString, TemplateManager templates)
            throws BaseException {
        final Resource sourceResource = RdfUtils.find(templateRdf,
                ReferenceTemplate.TYPE);
        if (sourceResource == null) {
            throw new BaseException("Missing resource of reference type");
        }
        final ValueFactory vf = SimpleValueFactory.getInstance();
        final IRI iri = vf.createIRI(iriAsString);
        // We need to create a definition and an interface
        final Collection<Statement> interfaceRdf = new ArrayList<>(4);
        final Collection<Statement> definitionRdf = new ArrayList<>(4);
        definitionRdf.add(vf.createStatement(iri,
                RDF.TYPE, ReferenceTemplate.TYPE, iri));
        String templateIri = null;
        // Parse template - update and create an definition.
        for (Statement statement : templateRdf) {
            // Add some predicates to the definition.
            switch (statement.getPredicate().stringValue()) {
                case "http://linkedpipes.com/ontology/configurationGraph":
                    // Skip
                    continue;
                case "http://linkedpipes.com/ontology/template":
                    // Also store to definition.
                    templateIri = statement.getObject().stringValue();
                    definitionRdf.add(vf.createStatement(iri,
                            statement.getPredicate(),
                            statement.getObject(),
                            iri
                    ));
                    break;
            }
            //
            interfaceRdf.add(vf.createStatement(iri,
                    statement.getPredicate(),
                    statement.getObject(),
                    iri
            ));
        }

        // Add a reference to the configuration.
        final Resource configIri = vf.createIRI(iriAsString + "/configuration");
        definitionRdf.add(vf.createStatement(iri, vf.createIRI(
                "http://linkedpipes.com/ontology/configurationGraph"),
                configIri, iri));

        final BaseTemplate template = templates.getTemplates().get(templateIri);
        if (template == null) {
            throw new BaseException("Missing template reference.");
        }
        destination.mkdirs();
        // Component interface.
        RdfUtils.write(new File(destination, Template.INTERFACE_FILE),
                RDFFormat.TRIG, interfaceRdf);
        // Component definition.
        RdfUtils.write(new File(destination, Template.DEFINITION_FILE),
                RDFFormat.TRIG, definitionRdf);
        // Component configuration.
        final Collection<Statement> configRdf = RdfUtils.renameResources(
                configurationRdf, configIri.stringValue(), configIri);
        RdfUtils.write(new File(destination, Template.CONFIG_FILE),
                RDFFormat.TRIG, configRdf);
        // Component configuration description.
        final Collection<Statement> configDescRdf = RdfUtils.renameResources(
                template.getConfigDescRdf(),
                iriAsString + "/configuration/desc", configIri);
        RdfUtils.write(new File(destination, Template.CONFIG_DESC_FILE),
                RDFFormat.TRIG, configDescRdf);
    }

}
