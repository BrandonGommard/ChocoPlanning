package fr.brandon.planning;

/**
 * 
 * @author Brandon Gommard
 */
public class App 
{
    public static void main( String[] args )
    {
    	int nbServices = 3;
    	int nbAstreinte = 1;
    	int nbInternes = 10;
    	int nbJours = 15;
    	int diffGardes = 1;
    	int diffAstreinte = 2;
    	
    	String[] nomServices = {"MAT","SAMU","BLOC"};
        String[] nomAstreintes = {"AST"};
        boolean[] respectVD = new boolean[nbInternes];
        for(int i=0; i<nbInternes ; i++){
        	respectVD[i] = true;
        }
        boolean[][] indispoForte = new boolean[nbInternes][nbJours];
        for(int i=0; i<nbInternes ; i++){
        	for(int t=0; t<nbJours ; t++){
        		indispoForte[i][t] = false;
        	}
        }
        
        boolean[][] indispoSouple = new boolean[nbInternes][nbJours];
        for(int i=0; i<nbInternes ; i++){
        	for(int t=0; t<nbJours ; t++){
        		indispoSouple[i][t] = false;
        	}
        }
        boolean[][] peutTravaillerEnsemble = new boolean[nbInternes][nbInternes];
        for(int i=0; i<nbInternes ; i++){	
        	for(int j=0; j<nbInternes ; j++){	
        		peutTravaillerEnsemble[i][j] = true;
        	}
        }
        
        peutTravaillerEnsemble[0][1] = false;
        peutTravaillerEnsemble[0][2] = false;
        peutTravaillerEnsemble[1][0] = false;
        peutTravaillerEnsemble[2][0] = false;
        
        //peutTravaillerEnsemble[0][3] = false;
        //peutTravaillerEnsemble[0][4] = false;
        //peutTravaillerEnsemble[0][5] = false;
        //peutTravaillerEnsemble[0][j] = false;
        
        boolean[][][] aptitude = new boolean[nbServices][nbInternes][nbJours];
        for(int i=0; i<nbServices ; i++){
        	for(int j=0; j<nbInternes ; j++){
        		for(int t=0; t<nbJours ; t++){	
                	aptitude[i][j][t] = true;
                }
        	}
        }
        
        
        SolveurPlanning test = new SolveurPlanning(nbServices,nbAstreinte,nbInternes,nbJours,diffGardes, diffAstreinte, nomServices,nomAstreintes, 
        										   respectVD, indispoForte, indispoSouple, peutTravaillerEnsemble, aptitude);
        
        test.initialisation();
        test.ajoutDesContraintes();
        test.solve();     
       
        test.displayResult();

    }
}
