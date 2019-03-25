package com.cyandr.robot;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class OntTime {

    static String Ns = "www.cyandr.think";


    OntDocumentManager manager = OntDocumentManager.getInstance();
    OntModelSpec spec=new OntModelSpec(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
    OntModel model = ModelFactory.createOntologyModel( );

    Resource r=model.getResource(Ns+"Paper");
    OntClass paper=r.as(OntClass.class);

   
}
