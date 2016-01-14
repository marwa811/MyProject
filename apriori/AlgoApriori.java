package apriori;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.List;


/**
 * The apriori algorithm for ontology selection
 * <br/><br/>  
 */
public class AlgoApriori {

	// the current level k
	protected int k; 

	// variables for statistics
	protected int totalCandidateCount = 0; // number of candidate generated during last execution
	protected long startTimestamp; // start time of last execution
	protected long endTimestamp; // end time of last execution
	private int databaseSize;
        //variable concerninig ontologies
        private Vector ontologyIndex;
	private Vector updatedOntologyNames;	
	//Lists of datasets and pareto sets
	private List<double[]> database = null;
        private List<double[]> ParetoSet = null;
	
	// object to write the output file (if the user wants to write to a file)
	BufferedWriter writer = null; 
        
        
	/**
	 * Default constructor
	 */
	public AlgoApriori() {
		
	}

	/**
	 * Method to run the algorithm
	 * @param input  the path of an input file
	 * @param output the path of an input if the result should be saved to a file. 
         */
	public Vector runAlgorithm(String input, String output, Vector ont_names) throws IOException {
			
		// record the start time
		startTimestamp = System.currentTimeMillis();
		
		// set the number of candidate found to zero
		totalCandidateCount = 0;
		// reset the utility for checking the memory usage
		MemoryLogger.getInstance().reset();
                
		// READ THE INPUT FILE
		// variable to count the number of transactions
		databaseSize = 0;
                
                //Vector of the final sets of ontologies 
                Vector finalOntologies=new Vector();
		database = new ArrayList<>(); // the database in memory (intially empty)
		
		// scan the database to load it into memory and count the support of each single item at the same time
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line;
		// for each line (transactions) until the end of the file
		while (((line = reader.readLine()) != null)) { 
			if (line.isEmpty() == true ||
					line.charAt(0) == '#' || line.charAt(0) == '%'
							|| line.charAt(0) == '@') {
				continue;
			}
			// split the line according to spaces
			String[] lineSplited = line.split(" ");
			
			// create an array of int to store the items in this transaction
			double transaction[] = new double[lineSplited.length];
			
			// for each item in this line (transaction)
			for (int i=0; i< lineSplited.length; i++) { 
				// transform this item from a string to double
				Double item=Double.parseDouble(lineSplited[i]);
				// store the item in the memory representation of the database
				transaction[i] = item;
			}                        
			// add the transaction to the database
			database.add(transaction);
      			// increase the number of transaction
			databaseSize++;
		}
		// close the input file
		reader.close();
                OntologyEvaluationSet dataSet;
                dataSet = new OntologyEvaluationSet(ont_names,database);
                System.out.println("The Selected Ontologies:");
                System.out.println("------------------------------");
                dataSet.printDataSet();

		// we start looking for ontologySet of size 1
		k = 1;
                ParetoSet = new ArrayList<>();
                ParetoSet=getSkyline(database);
                
                // ontologyOne contains the names of Ontologies in ontologySet of size 1
                // candidateOne contains the evaluation scores of the pareto ontologies             
                updatedOntologyNames=new Vector();
                for (int i=0; i<ontologyIndex.size(); i++)
                {
                    updatedOntologyNames.add(ont_names.elementAt((int)ontologyIndex.elementAt(i)));
                }
                OntologyEvaluationSet ParetoSet1;
                ParetoSet1 = new OntologyEvaluationSet(updatedOntologyNames,ParetoSet);
                System.out.println();
                System.out.println("Pareto Set of Size 1: ");
                System.out.println("-------------------------------");                
                ParetoSet1.printDataSet();
                
		
		// If no frequent item, the algorithm stops!
		if(ParetoSet.size() == 0){
			// close the output file if we used it
			if(writer != null){
				writer.close();
			}
                        finalOntologies=updatedOntologyNames;
			return finalOntologies; 
		}
		
		// Now we will perform a loop to find all Pareto sets of size > 1
		// starting from size k = 2.
		// The loop will stop when no candidates can be generated 
                //or if K is specified to a certain value
		OntologyEvaluationSet level = null;
		k = 2;
                OntologyEvaluationSet candidatesK;
                OntologyEvaluationSet paretoSet2 = null;
               do{
                  MemoryLogger.getInstance().checkMemory();
                 //level 2
                  if(k==2){
                    candidatesK = generateCandidate2(ParetoSet1);
                    System.out.println();
                    System.out.println("Candidate Sets of Size 2: ");
                    System.out.println("-------------------------------");                
                    candidatesK.printDataSet();
                    ParetoSet= new ArrayList<>();
                    ontologyIndex=new Vector();
                    ParetoSet=getSkyline(candidatesK.evaluation_values);
                    updatedOntologyNames=new Vector();                    
                    for (int i=0; i<ontologyIndex.size(); i++)
                    {
                    updatedOntologyNames.add(candidatesK.ontology_names.elementAt((int)ontologyIndex.elementAt(i)));
                    }
                    paretoSet2 = new OntologyEvaluationSet(updatedOntologyNames,ParetoSet);
                    System.out.println();
                    System.out.println("Pareto Sets of Size 2: ");
                    System.out.println("-------------------------------");                
                    paretoSet2.printDataSet();
                    k++;
                }
                  // if level k>2
                  else
                  {
                    candidatesK = generateCandidateSizeK(paretoSet2);
                    System.out.println();
                    System.out.println("Candidate Sets of Size "+k+": ");
                    System.out.println("-------------------------------");                
                    candidatesK.printDataSet();
                    ParetoSet= new ArrayList<>();
                    ontologyIndex=new Vector();
                    ParetoSet=getSkyline(candidatesK.evaluation_values);
                    updatedOntologyNames=new Vector();                    
                    for (int i=0; i<ontologyIndex.size(); i++)
                    {
                    updatedOntologyNames.add(candidatesK.ontology_names.elementAt((int)ontologyIndex.elementAt(i)));
                    }
                    paretoSet2 = new OntologyEvaluationSet(updatedOntologyNames,ParetoSet);
                    System.out.println();
                    System.out.println("Pareto Sets of Size "+k+": ");
                    System.out.println("-------------------------------");                
                    paretoSet2.printDataSet();
                    k++;
                  }
			 
               }while(k<6); 
            return finalOntologies;
        }    
        
        // this function computes sky line for a list of doubles
        //and output list of points not dominated 
        List<double[]> getSkyline(List<double[]> input){
           List<double[]> output = new ArrayList<>();
           boolean[] nondominated=new boolean[input.size()];
           ontologyIndex=new Vector();
           
           for(int i=0; i<nondominated.length; i++)
               nondominated[i]=true;
           double[] t1= input.get(0);
           for(int i=0; i<input.size();i++)
           {
               if (nondominated[i]==true){
               for(int j=i+1; j<input.size();j++)
               {
               if(dominate(input.get(i),input.get(j))==1)
                   nondominated[j]=false;
               else if(dominate(input.get(i),input.get(j))==2)
               { 
                   nondominated[i]=false;
                   break;
               }
               else if(dominate(input.get(i),input.get(j))==3)
                   continue;
            }
               }
               else continue;
           }
           for(int i=0; i<input.size();i++)
               if(nondominated[i]==true){
                   output.add(input.get(i));
                   ontologyIndex.add(i);
               }
           return output;
        }        
        
        //check for domination of two sets 
        int dominate(double[] t1, double[] t2){
        int i=0, first=0, second=0;
        do{
            if(t1[i]>=t2[i])
                first++;
            else
                second++;
            i++;
        }while(i<t1.length);
        
        if(first==t1.length)   //t1 dominates t2
            return 1;
        if(second==t1.length)      //t2 dominates t1 
            return 2;
        if(first>0&& first<t1.length && second>0 && second<t1.length)
            return 3;       //no one dominate the other
        else return 0;
        }
                
	/**
	 * This method generates candidates of ontologySet of size 2 based on
	 * ontologySet of size 1.
	 */
	private OntologyEvaluationSet generateCandidate2(OntologyEvaluationSet ParetoSet1) {
            
            Vector ontologyNames=new Vector();
            List<double[]> eval_scores;
            eval_scores = new ArrayList<>();
            OntologyEvaluationSet candidates = null;
	    int no=ParetoSet1.evaluation_values.get(1).length;
            double[] temp1=new double[no];
            double[] temp2=new double[no];
            double[] result;
            
            //combine ontology names
            for (int i = 0; i < ParetoSet1.ontology_names.size(); i++) {
                String O1 = ParetoSet1.ontology_names.elementAt(i).toString();
		temp1=ParetoSet1.evaluation_values.get(i);
                        
                for (int j = i + 1; j < ParetoSet1.ontology_names.size(); j++) {
                    String O2 = (String) ParetoSet1.ontology_names.elementAt(j).toString();
                    temp2=ParetoSet1.evaluation_values.get(j);

                    ontologyNames.add(O1+" , "+O2);
                    //get the average of the evaluation scores
                    result=new double[no];
                    for(int h=0; h<no; h++)    
                     result[h]=temp1[h]/2+temp2[h]/2;                    
                    eval_scores.add(result); 
		}
            }
            // Create a new OntologyEvaluationSet of size 2
            candidates= new OntologyEvaluationSet(ontologyNames,eval_scores);
            return candidates;
	}

	/**
	 * Method to generate ontologySet of size k from frequent ontologySet of size K-1.
	 */
	protected OntologyEvaluationSet generateCandidateSizeK(OntologyEvaluationSet paretoSetK_1) {
            Vector ontologyNames=new Vector();
            List<double[]> eval_scores;
            eval_scores = new ArrayList<>();
            OntologyEvaluationSet candidates = null;
	    int no=paretoSetK_1.evaluation_values.get(1).length;
            double[] temp1=new double[no];
            double[] temp2=new double[no];
            double[] result;
            String[] splitted=null;
            int count=0;
            
            // combine ontology names
            for (int i = 0; i < paretoSetK_1.ontology_names.size(); i++) {
                String O1 = paretoSetK_1.ontology_names.elementAt(i).toString();
		temp1=paretoSetK_1.evaluation_values.get(i);
                        
                for (int j = i + 1; j < paretoSetK_1.ontology_names.size(); j++) {
                    String O2 = (String) paretoSetK_1.ontology_names.elementAt(j).toString();
                    splitted=new String[k-1];
                    splitted=O2.split(",");
                    temp2=paretoSetK_1.evaluation_values.get(j);
                    count=0;
                    String diff="";
                    for(int g=0;g<splitted.length;g++){
                    if(O1.contains(splitted[g]))
                      count++; 
                    else 
                      diff=" ,"+splitted[g];
                    }
                    String total="";
                    if(count==k-2)
                    {
                        total=O1+diff;
                        ontologyNames.add(total);
                    
                    
                //get the average of the evaluation scores
                    result=new double[no];
                    for(int h=0; h<no; h++)    
                     result[h]=temp1[h]/2+temp2[h]/2;                    
                    eval_scores.add(result); 
                    }
                }
            }
            // Create a new OntologyEvaluationSet of size k
            candidates= new OntologyEvaluationSet(ontologyNames,eval_scores);
            candidates=removeNonApriori(paretoSetK_1.ontology_names,candidates);
            return candidates;
	}

        /*
        To Apply the Apriori rule that all sub-sets of a Pareto superset
        must also be
        */
	OntologyEvaluationSet removeNonApriori(Vector namesK_1,OntologyEvaluationSet candidatesK){
        String[] splitted=null;
        Vector merged=null;
        int count;
        for(int i=0; i<candidatesK.ontology_names.size(); i++)
        {
           merged=new Vector();
           String s1=(String) candidatesK.ontology_names.elementAt(i);
           splitted=s1.split(" , ");
           merged=getMerged(splitted);
           count=0;
            for(int j=0;j<merged.size() ;j++)
            {
                if(namesK_1.contains(merged.elementAt(j)))
                    count++;
            }
            if(count<merged.size()){
                candidatesK.ontology_names.remove(i);
                candidatesK.evaluation_values.remove(i);
            }
        }
        return candidatesK;
       }
        
        Vector getMerged(String[] splitted)
        {
            Vector merged=new Vector();
            switch(k){
            case 3:
            for(int f=0;f<splitted.length;f++)
                for(int h=f+1;h<splitted.length;h++)
                    merged.add(splitted[f]+" , "+splitted[h]);
            case 4:
            for(int f=0;f<splitted.length;f++)
                for(int h=f+1;h<splitted.length;h++)
                    for(int l=h+1;l<splitted.length;l++)
                    merged.add(splitted[f]+" , "+splitted[h]+" , "+splitted[l]);
            case 5:
            for(int f=0;f<splitted.length;f++)
                for(int h=f+1;h<splitted.length;h++)
                    for(int l=h+1;l<splitted.length;l++)
                        for(int d=l+1;d<splitted.length;d++)
                        merged.add(splitted[f]+" , "+splitted[h]+" , "+splitted[l]+" , "+splitted[d]);
            }
        return merged;
        }
        
        /**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
            System.out.println();
            System.out.println();
            System.out.println("=============  APRIORI - STATS =============");
            System.out.println(" The algorithm stopped at size " + (k - 1));
            System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
            System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
            System.out.println("===================================================");
	}
}
