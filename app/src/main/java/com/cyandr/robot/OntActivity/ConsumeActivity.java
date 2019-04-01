package com.cyandr.robot.OntActivity;

import com.cyandr.robot.MemoryWorld.*;
import com.cyandr.robot.MemoryWorld.MemoryObject;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.vocabulary.VCARD;

import java.util.HashMap;
import java.util.function.Consumer;

public class ConsumeActivity extends OntActivity {


    private static String ConsumeActURI = "http://com.cyandr/robot";
    private static String Prix = "Cyandr.Robot";

    People Who;
    Movement CostOrMake;
    Currency HowMuch;
    Location Where;
    Time When;
    MemoryObject What;


  public  ConsumeActivity(People people, Movement movement, Currency currency, Location location, Time time, MemoryObject what) {
        Who = people;
        CostOrMake = movement;
        HowMuch = currency;
        Where = location;
        When = time;
        What = what;

    }

    HashMap<String, ObjectProperty> RELS = new HashMap<>();

    void initRels(OntModel model) {

        RELS.put("who", model.createObjectProperty(Prix + "who"));
        RELS.put("movement", model.createObjectProperty(Prix + "movement"));
        RELS.put("howmuch", model.createObjectProperty(Prix + "howmuch"));
        RELS.put("where", model.createObjectProperty(Prix + "where"));

        RELS.put("when", model.createObjectProperty(Prix + "when"));

        RELS.put("object", model.createObjectProperty(Prix + "object"));

    }

    public  void generateRdfModel() {

        OntModel model = ModelFactory.createOntologyModel();
        initRels(model);
        OntClass consumeClass = model.createClass(Prix + ".ConsumeActivity");
        // create the resource
        Individual instance = model.createIndividual(Prix, consumeClass);

        Individual instanceWho = model.createIndividual(Prix, model.createClass(Prix + People.class.toString()));

        model.add(instance, RELS.get("who"), instanceWho);

        Individual instanceMove = model.createIndividual(Prix, model.createClass(Prix + Movement.class.toString()));

        model.add(instance, RELS.get("movement"), instanceMove);

        Individual instanceHowmuch = model.createIndividual(Prix, model.createClass(Prix + Currency.class.toString()));

        model.add(instance, RELS.get("howmuch"), instanceHowmuch);

        Individual instanceWhere = model.createIndividual(Prix, model.createClass(Prix + Location.class.toString()));

        model.add(instance, RELS.get("where"), instanceWhere);

        Individual instanceWhen = model.createIndividual(Prix, model.createClass(Prix + Time.class.toString()));

        model.add(instance, RELS.get("when"), instanceWhen);

        Individual instanceWhat = model.createIndividual(Prix, model.createClass(Prix + MemoryObject.class.toString()));

        model.add(instance,  RELS.get("object"), instanceWhat);

        //model.write();
        RDFConnection conn0 = RDFConnectionRemote.create()
                .destination("http://101.6.95.54:19092/fuseki/ConsumeTest")
                .queryEndpoint("query")
                .updateEndpoint("update")
                // Set a specific accept header; here, sparql-results+json (preferred) and text/tab-separated-values
                // The default is "application/sparql-results+json, application/sparql-results+xml;q=0.9, text/tab-separated-values;q=0.7, text/csv;q=0.5, application/json;q=0.2, application/xml;q=0.2, */*;q=0.1"
                .acceptHeaderSelectQuery("application/sparql-results+json, application/sparql-results+xml;q=0.9")
                .build();

        Query query = QueryFactory.create("SELECT ?x\n" +
                "WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#FN>  \"Xinhui Yan2\" }");
        //Update update= UpdateFactory.create();
        // Whether the connection can be reused depends on the details of the implementation.
        // See example 5.
        try (RDFConnection conn = conn0) {


            Consumer<ResultSet> resultSetConsumer = resultSet -> {
                String email = ";";
                Binding binding = resultSet.nextBinding();

                for (String str : resultSet.getResultVars()) {
                    System.out.println(str);
                    if (binding != null) {
                        Var vv = Var.alloc(str);
                        if (binding.contains(vv))
                            System.out.println(binding.get(vv));
                        // binding=resultSet.nextBinding();
                    }


                }

             /*   String namespace=VCARD.EMAIL.toString();
                System.out.println(model1.getProperty(namespace,email));
                System.out.println(email);*/
            };
            conn.put(model);
            conn.queryResultSet(query, resultSetConsumer);
        } catch (Exception e) {

            e.printStackTrace();
        }
        System.out.println("Hello World!");
    }
}
