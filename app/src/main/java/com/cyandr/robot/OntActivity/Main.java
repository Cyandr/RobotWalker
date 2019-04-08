package com.cyandr.robot.OntActivity;

import com.cyandr.robot.RobotApp;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                TestOntology();
            }
        }).start();

    }

    public static void TestOntology() {

        try {
            URL url = new URL("http://127.0.0.1:19092/fuseki/ConsumeTest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(200);
           // connection.setRequestProperty("Content-type", "application/x-javascript->json");//json格式数据
            connection.connect();
            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[1024];

            InputStream input = connection.getInputStream();
            while (input.read(buffer) != -1) {
                sb.append(new String(buffer, Charset.forName("utf-8")));
            }
            input.close();
            connection.disconnect();

            System.out.println(sb.toString());

            String string = sb.toString();
            //HttpAuthenticator authenticator = new SimpleAuthenticator("user", "password".toCharArray());
            QueryExecution queryExecution= QueryExecutionFactory.sparqlService("http://127.0.0.1:19092/fuseki/ConsumeTest",
                    "SELECT ?x ?c WHERE { ?x  <http://www.w3.org/2001/vcard-rdf/3.0#FN>  \"Xinhui Yan2\". ?x <http://www.w3.org/2001/vcard-rdf/3.0#Country> ?c. }","");




            ResultSet results = queryExecution.execSelect();
            List<Var> vars = queryExecution.getQuery().getProjectVars();

            while (results.hasNext()) {
                QuerySolution row = results.next();

                for (Var v : vars) {
                    RDFNode thing = row.get(v.getName());// 结果例如 ( ?X =
                    RobotApp.showText(v.getName() + "---" + thing.toString());
                }
            }
            queryExecution.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
