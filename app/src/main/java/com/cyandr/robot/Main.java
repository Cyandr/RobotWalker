package com.cyandr.robot;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.vocabulary.VCARD;

import java.util.function.Consumer;

public class Main {


    RobotApp app;

    // some definitions
    private static String personURI = "http://somewhere/xh2";
    private static String fullName = "Xinhui Yan2";

    public static void main(String[] args) {

        OntModel model=null;
        // create an empty Model

        model = ModelFactory.createOntologyModel();

        // create the resource
        Resource yxh = model.createResource(personURI);
        yxh.addProperty(VCARD.FN, fullName);
        yxh.addProperty(VCARD.Given,"Yan");
        yxh.addProperty(VCARD.NAME,"Xinhui");
        yxh.addProperty(VCARD.Country,"China");
        yxh.addProperty(VCARD.EMAIL,"Cyandr@qq.com");

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
        try ( RDFConnection conn = conn0 ) {


            Consumer<ResultSet> resultSetConsumer= resultSet -> {
                String email=";";
                Binding binding=resultSet.nextBinding();

                for (String str :resultSet.getResultVars())
                {
                    System.out.println(str);
                    if (binding!=null)
                    {
                        Var vv= Var.alloc(str);
                        if(binding.contains(vv))
                            RobotApp.showText(binding.get(vv).toString());
                       // binding=resultSet.nextBinding();
                    }


                }

             /*   String namespace=VCARD.EMAIL.toString();
                System.out.println(model1.getProperty(namespace,email));
                System.out.println(email);*/
            };
            conn.put(model);
            conn.queryResultSet(query,resultSetConsumer);
        }catch (Exception e)
        {

            e.printStackTrace();
        }
        RobotApp.showText("Hello World!");


    }
}
