package fr.brandon.planning;



/**
 * 
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	String[] nomServices = {"MAT","SAMU","BLOC"};
        String[] nomAstreintes = {"AST"};
        
        SolveurPlanning test = new SolveurPlanning(3,1,5,15,2, nomServices,nomAstreintes);
        
        test.initialisation();
        test.ajoutDesContraintes();
        test.solve();     
       
        test.displayResult();

    }
}
