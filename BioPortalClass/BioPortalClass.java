package BioPortalClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BioPortalClass {

    static final String REST_URL = "http://data.bioontology.org";
    static final String API_KEY = "e7f879a2-fbb2-4f44-a85b-d7fd867d1e2b";
    static final ObjectMapper mapper = new ObjectMapper();

    public Vector access_bioportal() throws Exception {
        ArrayList<String> terms = new ArrayList<String>();     
        Vector ontologynames=new Vector();
        double[][] criteriaValues= new double[25][4];
    
        String currentDir = System.getProperty("user.dir");
        Scanner in = null;
        try {
            in = new Scanner(new FileReader(currentDir + "/src/Files/classes_search_terms.txt"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BioPortalClass.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (in.hasNextLine()) {
            terms.add(in.nextLine());
        }

//Recommender code
        ArrayList<JsonNode> searchResults = new ArrayList<JsonNode>();
        String allterms="";
        for (String term : terms) 
        {
            allterms+=term;
            System.out.println(allterms);
        }
        JsonNode searchResult = jsonToNode(get(REST_URL + "/recommender?input=blood,experiment,test?input_type=2?wc=1.0"));
        searchResults.add(searchResult);
        
        double random = new Random().nextDouble();
        double result1;
         
        for (JsonNode result : searchResults) {
           for (int k=0; k<result.size(); k++){
              ontologynames.add(result.get(k).findValue("acronym"));
                            
              //get the evaluation criteria values from NCBO Recommender API
              criteriaValues[k][0]=(Double)result.get(k).findValue("coverageResult").findValue("normalizedScore").asDouble();
              criteriaValues[k][1]=(Double)result.get(k).findValue("specializationResult").findValue("normalizedScore").asDouble();
              criteriaValues[k][2]=(Double)result.get(k).findValue("acceptanceResult").findValue("normalizedScore").asDouble();
              criteriaValues[k][3]=(Double)result.get(k).findValue("detailResult").findValue("normalizedScore").asDouble();                     
            }
        }
        //write the results (evaluation scores for the selected ontologies) in Transactions.txt file
        try {
            try (PrintStream out = new PrintStream(new FileOutputStream
                (currentDir + "/src/Transactions.txt"))) {
                for (int k=0; k<ontologynames.size() ; k++){
                    for(int c=0; c<4 ; c++)
                    {
                        out.print(criteriaValues[k][c]+" ");
                    }
                    out.println();
                }
                out.close();
            }

        } catch (FileNotFoundException e) {
        } 
        return ontologynames;
    }

    private static JsonNode jsonToNode(String json) {
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    private static String get(String urlToGet) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlToGet);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "apikey token=" + API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

