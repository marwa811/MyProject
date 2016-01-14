/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apriori;

import java.util.List;
import java.util.Vector;

/**
 *
 * @author sony
 */
public class OntologyEvaluationSet {
    public Vector ontology_names= new Vector();
    public List<double[]> evaluation_values = null;

    public OntologyEvaluationSet(Vector ont_names, List<double[]> database) {
        this.ontology_names = ont_names;
        this.evaluation_values = database;
    }
 
    public void OntEvalSet(Vector ontology_names, List<double[]> evaluation_values) {
        this.ontology_names = ontology_names;
        this.evaluation_values = evaluation_values;
    }
    
    public Vector getOntologyNames() {
        return ontology_names;
    }

    public List<double[]> getEvaluationValues() {
        return evaluation_values;
    }
    
    public void printDataSet(){        
        int i=0;
        for(double[] k: this.evaluation_values){
            System.out.print(this.ontology_names.elementAt(i)+" ");
            i++;
            for (int j=0; j< k.length;j++){
                System.out.print(k[j]+" ");}
            System.out.println();
            }     
    }
}
