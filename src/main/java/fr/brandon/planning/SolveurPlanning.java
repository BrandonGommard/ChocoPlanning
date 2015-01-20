package fr.brandon.planning;

import solver.Solver;
import solver.constraints.ICF;
import solver.constraints.LCF;
import solver.search.strategy.ISF;
import solver.variables.BoolVar;
import solver.variables.VF;
import solver.variables.IntVar;

public class SolveurPlanning {
	private int nbInternes;
    private int nbJours;
    private int nbServices;
    private int nbGardeEcart;
    private int nbAstreintes;
    
    private String[] nomServices;
    private String[] nomAstreintes;
    
    //// x correspond au tableau 3d des gardes dans chaque service
    //// y correspond au tableau 3d des Astreintes
    private IntVar[][][] x;
    private IntVar[][][] y;
    private Solver solveur;
	
    /**
     * Constructeur de la classe Planning, contenant les differentes données necessaires à la planification
     * @param services nombres de services à planifier
     * @param astreintes nombres de services d'astreintes 
     * @param internes nombres d'internes participant à la planification
     * @param jours période de temps de la planification
     * @param difGardes différence de nombre de gardes autorisé entre le plus présent et le moins présent des internes
     */
    public SolveurPlanning(int services, int astreintes, int internes, int jours, int diffGardes, String[] nomService, String[] nomAstreinte){
        this.nbServices = services;
        this.nbAstreintes = astreintes;
        this.nbInternes = internes;
        this.nbJours = jours;
        this.nbGardeEcart = diffGardes;
        this.nomServices = nomService;
        this.nomAstreintes = nomAstreinte;
        
        System.out.println("Objet planning créé avec les paramètres suivants :");
        System.out.println("Nombre de Services : \t" + nbServices);
        System.out.println("Nombre d'Astreintes : \t" + nbAstreintes);
        System.out.println("Nombre d'Internes : \t" + nbInternes);
        System.out.println("Nombre de Jours : \t" + nbJours);
        System.out.println("Ecart max toléré : \t" + nbGardeEcart);
    }
    
    //Appel du Constructeur par défault
	public SolveurPlanning() {

		this(3,1,5,15,2,  new String[]{"SAMU", "MAT", "BLOC"}, new String[]{"AST"});
		
    }
	
	
	/**
     * On récupère le nom des différents services, il faudra les passer au moment de la création des variables
     * @param noms 
     */
    public void setNomServices(String[] noms){
        this.nomServices = noms;
    }
    
    public String[] getNomServices(){
        return this.nomServices;
    }
    
    public void setNomAstreintes(String[] noms){
        this.nomAstreintes = noms;
    }
    
    public String[] getNomAstreintes(){
        return this.nomAstreintes;
    }
    
    
public void initialisation(){
        
        
        
        this.solveur = new Solver("Planning");
        this.x = new IntVar[nbServices][][];
        this.y = new IntVar[nbAstreintes][][];
                
        //mat bloc samu
        for(int iService=0; iService < nbServices ; iService++){
            this.x[iService] = VF.boundedMatrix(nomServices[iService], nbInternes , nbJours,0,1, solveur);
        }
        
        //AST 
        for(int iAstreinte=0 ; iAstreinte<nbAstreintes ; iAstreinte++){
            this.y[iAstreinte] = VF.boundedMatrix(nomAstreintes[iAstreinte], nbInternes, nbJours,0,1, solveur);
        }
        
        ajoutDesContraintes();
        
    }
    
    public void ajoutDesContraintes(){
        
        auPlus1GardeJour();        
       // tjrs1PersonneDeGarde();        
       // equilibreGarde(); 
        
    }
    
    /**
     * auPlus1GardeJour ajoute la contrainte sur chaque jour qu'une personne ne peut être que dans un seul service
     * @param m Modele
     * @param x tableau des gardes
     */
    private void auPlus1GardeJour() { 
        
        //On change l'ordre des dimensions du tableau 3D afin de lui appliquer les contraintes plus simplement
        IntVar[][][] nouveauX = new IntVar[nbInternes][nbJours][nbServices];
        
        for(int iService=0; iService < nbServices ; iService++){
            for(int iIntern=0 ; iIntern < nbInternes ; iIntern++){
                for(int t=0 ; t<nbJours ; t++){
                    nouveauX[iIntern][t][iService] = x[iService][iIntern][t];
                }
            }
        }
        
        for(int iIntern=0 ; iIntern<nbInternes ; iIntern++){
            for(int t=0 ; t<nbJours ; t++){
                solveur.post(ICF.sum(nouveauX[iIntern][t], "<=", VF.fixed(1, solveur)));
            }
        }
    }
	
	/*public void ajoutContrainte() {
		test = VF.enumerated("test", 0 , 100 , solveur);
		test2 = VF.enumerated("test2", 50 , 100 , solveur);
		test3 = VF.enumerated("test3", 40 , 70 , solveur);
		
		solveur.post(
		ICF.arithm(test, ">", test2),
		ICF.arithm(test3, "-", test2, "=", 5 )
		);
	}*/
	
	
	public void solve(){
		
		IntVar[] toutesLesGardes = new IntVar[nbServices*nbInternes*nbJours];
		int cpt = 0;
		
		//Pour chaque service et chaque interne on va appliquer la stratégie suivante
		for(int iService=0 ; iService < nbServices ; iService++){
			for(int iInterne=0 ; iInterne < nbInternes ; iInterne++){
				for(int t=0 ; t < nbJours ; t++){
					toutesLesGardes[cpt] = x[iService][iInterne][t];
					cpt++;
				}
			}
		}
		for(int iService=0 ; iService < nbServices ; iService++){
			for(int iInterne=0 ; iInterne < nbInternes ; iInterne++){
				solveur.set(ISF.custom(ISF.minDomainSize_var_selector(), ISF.max_value_selector(),toutesLesGardes));
			}
		}
		
		if (solveur.findSolution()) {
            //for (int i = 0; i < 100; i++) {
			//System.out.println("test = " + test.getValue());
			
           // }
            System.out.println("Solution trouvée en " + solveur.getMeasures().getTimeCount() + " secondes");        
        } else {
            System.out.println("Pas de solution pour ce problème, en "
                    + solveur.getMeasures().getTimeCount() + solveur.getMeasures().getSolutionCount() + " secondes");
        }
	}
	
	
	
	public void displayResult(){
        int sommeDeGardes;
        int[][] nbGardes = new int[nbServices][nbInternes];
        for(int i=0; i< nbInternes ; i++){
            for(int t=0; t< nbJours ; t++){
                System.out.print("[");
                for(int j=0; j< nbServices ; j++){
                    System.out.print((x[j][i][t]).getValue());
                    nbGardes[j][i] += x[j][i][t].getValue();
                }
                System.out.print("] ");
            }
            sommeDeGardes =0;
            for(int k=0; k<nbServices ; k++){
                System.out.print(" service" + k + ": " + nbGardes[k][i]);
                sommeDeGardes += nbGardes[k][i];
            }
            System.out.print(" Total: ");
            System.out.println(sommeDeGardes);
            
        }
         System.out.println("");
        
    }

}
