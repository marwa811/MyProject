package ontologyselectionproject;

import BioPortalClass.BioPortalClass;
import apriori.AlgoApriori;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Vector;

public class OntologySelectionProject {
    
    public static void main(String[] args) throws IOException, Exception {
        String currentDir = System.getProperty("user.dir");        
        String input = currentDir + "/src/Transactions.txt";
	String output = ".//output.txt";
                
//get the Ontologies and evaluation criteria values from NCBO Recommender service		
		BioPortalClass bioportal=new BioPortalClass();
                Vector ont_names= new Vector();
                ont_names=bioportal.access_bioportal();
		
// Applying the Apriori algorithm
		AlgoApriori apriori = new AlgoApriori();
		apriori.runAlgorithm(input, output,ont_names);
		apriori.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = OntologySelectionProject.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
    }
    

