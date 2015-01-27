package fr.brandon.planning;


import java.util.Random;

import solver.Solver;
import solver.constraints.ICF;
import solver.constraints.LCF;
import solver.search.strategy.ISF;
import solver.variables.VF;
import solver.variables.IntVar;
import fr.brandon.planning.Semaine;

public class SolveurPlanning {
	private int nbInternes;
    private int nbJours;
    private int nbServices;
    private int nbGardeEcart;
    private int nbAstreintes;
    private int nbAstreintesEcart;
    
    //true si il respecte, false sinon
    private boolean[] VD;
    //true si il a une indispo, false sinon
    private boolean[][] indispoForte;
    private boolean[][] indispoSouple;
    private boolean[][][] aptitude;
    private boolean[][] peutTravailEnsemble;
    
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
    public SolveurPlanning(int services, int astreintes, int internes, int jours, int diffGardes, int diffAstreinte, String[] nomService, String[] nomAstreinte , 
    					   boolean[] respectVD, boolean[][] indispoForte, boolean[][] indispoSouple, boolean[][] peutTravaillerEnsemble, boolean[][][] aptitude){
        this.nbServices = services;
        this.nbAstreintes = astreintes;
        this.nbInternes = internes;
        this.nbJours = jours;
        this.nbGardeEcart = diffGardes;
        this.nbAstreintesEcart = diffAstreinte;
        this.nomServices = nomService;
        this.nomAstreintes = nomAstreinte;
        this.VD = respectVD;
        this.indispoForte = indispoForte;
        this.indispoSouple = indispoSouple;
        this.peutTravailEnsemble = peutTravaillerEnsemble;
        this.aptitude = aptitude;
        
        System.out.println("Objet planning créé avec les paramètres suivants :");
        System.out.println("Nombre de Services : \t" + nbServices);
        System.out.println("Nombre d'Astreintes : \t" + nbAstreintes);
        System.out.println("Nombre d'Internes : \t" + nbInternes);
        System.out.println("Nombre de Jours : \t" + nbJours);
        System.out.println("Ecart max toléré : \t" + nbGardeEcart);
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
    
    /**
     * fonction d'initialisation du solveur et des variables
     */
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
        
    }
    
    public void ajoutDesContraintes(){
        
        auPlus1GardeJour();        
        tjrs1PersonneDeGarde();        
        equilibreGarde();
        reposLendemain();
        enchainementVD();
        indisponibiliteForte();
        estCapable();
        gardeEnsemble();
        
        groupementAstreintes();
        
        tjrsUneAstreinte();
        incompatibleGardeAstreinte();
        equilibreSamu();
        enchainementDesagreable();
        equilibreJSD();
        
        equilibreAstreinte();
        indisponibiliteSouple();
    }
    
    /**
     * auPlus1GardeJour ajoute la contrainte sur chaque jour qu'une personne ne peut être que dans un seul service
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
    
    /**
     * tjrs1PersonneDeGarde ajoute la contrainte sur chaque service qui doit avoir une personne de garde chaque jour
     * @param m Modele
     * @param x tableau des gardes
     */
     private void tjrs1PersonneDeGarde(){
        
        //On change l'ordre des dimensions du tableau 3D afin de lui appliquer les contraintes plus simplement
        IntVar[][][] nouveauX = new IntVar[nbServices][nbJours][nbInternes];
        
        for(int iService=0; iService < nbServices ; iService++){
            for(int iIntern=0 ; iIntern < nbInternes ; iIntern++){
                for(int t=0 ; t<nbJours ; t++){
                    nouveauX[iService][t][iIntern] = x[iService][iIntern][t];
                }
            }
        }
               
        for(int iService=0 ; iService<nbServices ; iService++){
            for(int t=0 ; t<nbJours ; t++){
                solveur.post(ICF.sum(nouveauX[iService][t], "=", VF.fixed(1, solveur)));
            }
        }     
     }
     
     /**
      * equilibreGarde s'assure que chaque personne aura a peu près le même nombre de garde que les autres
      */
     private void equilibreGarde(){       
         int nbGardesTheorique = (nbJours*nbServices) / nbInternes ;
         System.out.println("nombre de gardes théorique: " + nbGardesTheorique);
         
         // On va regrouper toutes les gardes de chaque interne dans un tableau afin de pouvoir faire la somme plus facilement
         // On pourra ensuite comparer cette somme à la valeur attendue
         IntVar[][] nbGardesInterne = new IntVar[nbInternes][nbServices*nbJours];
         int cpt = 0;
         for(int iInterne=0 ; iInterne < nbInternes ; iInterne++){
        	 for(int iService=0 ; iService < nbServices ; iService++){
        		 for(int t=0 ; t < nbJours ; t++){
        			 nbGardesInterne[iInterne][cpt] = x[iService][iInterne][t];
        			 cpt++;
        		 }
        	 }
        	 cpt = 0;
         }
         
         // le nombre de garde effectué par chaque interne, moins le nombre de garde théorique ne doit pas s'écarter de plus de nbgardeEcart
         for(int iInterne = 0; iInterne < nbInternes ; iInterne++){
        	 solveur.post(ICF.sum(nbGardesInterne[iInterne], "<=", VF.fixed(nbGardesTheorique+nbGardeEcart, solveur)));
        	 solveur.post(ICF.sum(nbGardesInterne[iInterne], ">=", VF.fixed(nbGardesTheorique-nbGardeEcart, solveur)));
        	 
         }
         
         //IntegerExpressionVariable[][] nbGardes = new IntegerExpressionVariable[nbInternes][nbServices];
         //for(int i=0 ; i<nbInternes ; i++){
                        
             //for(int j=0; j<nbServices ; j++){
              //  nbGardes[i][j] = sum(x[j][i]) ; 
             //}
             
             //problème en faisant comme ça, on ne regarde pas la difference entre tout le monde
             //mais l'écart par rapport au nombre de garde theorique -> on peut s'ecarter de 0 1 2 3 du nombre de garde theorique
             //mais l'écart entre le pire et le meilleur pourra etre de 1*2 2*2 3*2
             //m.addConstraint(leq(minus( sum(nbGardes[i]) , nbGardesTheorique ) , nbGardeEcart));
            // m.addConstraint(leq(minus( nbGardesTheorique , sum(nbGardes[i]) ) , nbGardeEcart));
             
             //sinon faire boucle sur tout le monde, trouver le pire et le meilleur, faire la diff et comparer a la valeur voulue
             //max(sum(nbgardes[i]) - min(sum(nbgardes[i]) < nbgardes ecart ?
             //très lent, choco ne trouve pas de solution quand il y a beaucoup d'internes
             
         }      
     
    /**
     * reposLendemain s'assure que chaque personne aura un jour de repos le lendemain de sa garde
     */
     private void reposLendemain(){
    	 
    	//On change l'ordre des dimensions du tableau 3D afin de lui appliquer les contraintes plus simplement
         IntVar[][][] nouveauX = new IntVar[nbInternes][nbJours][nbServices];
         
         for(int iService=0; iService < nbServices ; iService++){
             for(int iIntern=0 ; iIntern < nbInternes ; iIntern++){
                 for(int t=0 ; t<nbJours ; t++){
                     nouveauX[iIntern][t][iService] = x[iService][iIntern][t];
                 }
             }
         }
    	 
    	 
    	 for(int iService=0 ; iService < nbServices ; iService++){
        	 for(int iInterne=0 ; iInterne < nbInternes ; iInterne++){
        		 for(int t=0 ; t < nbJours -1 ; t++){
        			 
        			 solveur.post(LCF.ifThen(
        					 			ICF.arithm( nouveauX[iInterne][t][iService], "=", 1),
        					 				ICF.sum(nouveauX[iInterne][t+1], "=", VF.fixed(0, solveur))	   
        					 				)
        					 	 );
        			 
        			 
        			  //solveur.post(LCF.ifThen(ICF.arithm( x[iService][iInterne][t], "=", 1), ICF.arithm(x[iService][iInterne][t+1], "=", 0)));
        			  //solveur.post(LCF.ifThen(ICF.arithm( x[iService][iInterne][t], "=", 1), ICF.arithm(x[(iService+1)%nbServices][iInterne][t+1], "=", 0)));
        			  //solveur.post(LCF.ifThen(ICF.arithm( x[iService][iInterne][t], "=", 1), ICF.arithm(x[(iService+2)%nbServices][iInterne][t+1], "=", 0)));
        		 }
        	 }
         }
     }
    
    /**
     * enchainementVD s'assure que lorsque quelqu'un interne qui souhaite respecter la regle VD 
     * est de garde le vendredi, il le soit le Dimanche également
     */
     private void enchainementVD(){
    	 IntVar[][][] nouveauX = new IntVar[nbInternes][nbJours][nbServices];
         
         for(int iService=0; iService < nbServices ; iService++){
             for(int iIntern=0 ; iIntern < nbInternes ; iIntern++){
                 for(int t=0 ; t<nbJours ; t++){
                     nouveauX[iIntern][t][iService] = x[iService][iIntern][t];
                 }
             }
         }
         
    	 for(int iService=0 ; iService < nbServices ; iService++){
        	 for(int iInterne=0 ; iInterne < nbInternes ; iInterne++){
        		 if(VD[iInterne]){
	        		 for(int t=0 ; t < nbJours -1 ; t++){
	        			 //Attention bien s'assurer que le décompte des jours commence le lundi
	        			 //Sinon ajouter un décalage
	        			 if(t % Semaine.NB_JOURS_SEMAINE.toInt() == Semaine.VENDREDI.toInt())
		        			 solveur.post(LCF.ifThen(
	        					 			ICF.arithm( x[iService][iInterne][t], "=", 1),
	        					 			ICF.sum(nouveauX[iInterne][t+2], "=", VF.fixed(1, solveur))
	        					 					)
	        					 		 );
	        			 if(t%7 == 6)
		        			 solveur.post(LCF.ifThen(
	        					 			ICF.arithm( x[iService][iInterne][t], "=", 1),
	        					 			ICF.sum(nouveauX[iInterne][t-2], "=", VF.fixed(1, solveur))
	        					 					)
	        					 		 );
	        		 }
        		 }
        	 }
         }
     }
    
     /**
     * indisponiibliteForte s'assure que lorsqu'un interne a une indisponibilité forte, elle soit respectée
      */
     private void indisponibiliteForte(){
    	for(int iInterne=0; iInterne< nbInternes ; iInterne++){
    		for(int t=0 ; t< nbJours ; t++){
    			if(indispoForte[iInterne][t]){
    				for(int iService=0; iService< nbServices; iService++)
    					solveur.post(ICF.arithm(x[iService][iInterne][t], "=", 0));
    			}
    		}
    	}  	
    }
    
    /**
     * estCapable s'assure qu'un interne ai la capacité de réaliser une garde pour lui affecter
     */
     private void estCapable(){
    	for(int iService=0; iService< nbServices; iService++)
	    	for(int iInterne=0; iInterne< nbInternes ; iInterne++){
	    		for(int t=0 ; t< nbJours ; t++){
	    			if(!aptitude[iService][iInterne][t]){
	    				solveur.post(ICF.arithm(x[iService][iInterne][t], "=", 0));
	    			}
	    		}
	    	}  
    }
	
     /**
      * gardeEnsemble s'assure que les internes peuvent être affectés à des gardes le même jour
      */
     private void gardeEnsemble(){
    	 IntVar[][][] nouveauX = new IntVar[nbInternes][nbJours][nbServices];
         
         for(int iService=0; iService < nbServices ; iService++){
             for(int iIntern=0 ; iIntern < nbInternes ; iIntern++){
                 for(int t=0 ; t<nbJours ; t++){
                     nouveauX[iIntern][t][iService] = x[iService][iIntern][t];
                 }
             }
         }
         
    	 for(int iInterne=0 ; iInterne< nbInternes ; iInterne++){
    		 for(int jInterne=iInterne+1 ; jInterne< nbInternes ; jInterne++){
        		 if((!peutTravailEnsemble[iInterne][jInterne])){

        			 for(int iService=0 ; iService<nbServices ; iService++){
        				 for(int t=0 ; t<nbJours ; t++){
        					 solveur.post(LCF.ifThen(ICF.arithm(x[iService][iInterne][t], "=", 1), 
	  				  				 ICF.sum(nouveauX[jInterne][t], VF.fixed(0, solveur))
	  				  				 
	  				  		)
					 );
        					 solveur.post(LCF.ifThen(ICF.arithm(x[iService][jInterne][t], "=", 1), 
	  				  				 ICF.sum(nouveauX[iInterne][t], VF.fixed(0, solveur))
	  				  				 
	  				  		)
					 );
        					/* solveur.post(LCF.ifThen(ICF.arithm(x[iService][iInterne][t], "=", 1), 
				  				      LCF.and(ICF.arithm(x[0][jInterne][t], "=", VF.fixed(0, solveur)),
		  				  				      ICF.arithm(x[1][jInterne][t], "=", VF.fixed(0, solveur)),
		  				  				      ICF.arithm(x[2][jInterne][t], "=", VF.fixed(0, solveur))
				  				    		  )
				  				   )
		  				      );
        					 solveur.post(LCF.ifThen(ICF.arithm(x[iService][jInterne][t], "=", 1), 
				  				      LCF.and(ICF.arithm(x[0][iInterne][t], "=", VF.fixed(0, solveur)),
		  				  				      ICF.arithm(x[1][iInterne][t], "=", VF.fixed(0, solveur)),
		  				  				      ICF.arithm(x[2][iInterne][t], "=", VF.fixed(0, solveur))
				  				    		  )
				  				   )
		  				      );*/
        				 }
        			 }
        		 }
        	 }
    	 }
     }

     /**
      * 
      */
     private void groupementAstreintes(){
    	 for(int iAstreinte=0 ; iAstreinte<nbAstreintes ; iAstreinte++){
    		 for(int iInterne=0 ; iInterne<nbInternes ; iInterne++){
    			 for(int t=0; t<nbJours-2 ; t++){
 
    				 //pas nécessaire de mettre les contraintes sur les autres jours
    				 switch (t % Semaine.NB_JOURS_SEMAINE.toInt())
    				 {
    				   case 0: //lundi
    					   solveur.post(ICF.arithm(y[iAstreinte][iInterne][t], "=", y[iAstreinte][iInterne][t+1]));
    				     break;
    				   case 1: //mardi
    					   //solveur.post(ICF.arithm(y[iAstreinte][iInterne][t], "=", y[iAstreinte][iInterne][t-1]));
      				     break;
      				   case 2: //mercredi
      					   solveur.post(ICF.arithm(y[iAstreinte][iInterne][t], "=", y[iAstreinte][iInterne][t+1]));
      				     break;
      				   case 3: //jeudi
      					   //solveur.post(ICF.arithm(y[iAstreinte][iInterne][t], "=", y[iAstreinte][iInterne][t-1]));
    				     break;
    				   case 4: //vendredi
    					   solveur.post(ICF.arithm(y[iAstreinte][iInterne][t], "=", y[iAstreinte][iInterne][t+1]));
    					   solveur.post(ICF.arithm(y[iAstreinte][iInterne][t], "=", y[iAstreinte][iInterne][t+2]));
    				     break;
    				   case 5: //samedi
    					   //solveur.post(ICF.arithm(y[iAstreinte][iInterne][t], "=", y[iAstreinte][iInterne][t-1]));
    					   //solveur.post(ICF.arithm(y[iAstreinte][iInterne][t], "=", y[iAstreinte][iInterne][t+1]));
      				     break;
      				   case 6: //dimanche
      					   //solveur.post(ICF.arithm(y[iAstreinte][iInterne][t], "=", y[iAstreinte][iInterne][t-1]));
      					   //solveur.post(ICF.arithm(y[iAstreinte][iInterne][t], "=", y[iAstreinte][iInterne][t-2]));
      				     break;
    				   default:
    				     System.out.println("Erreur.");
    				 }
    				 
    			 }
    		 }
    	 }
     }
     
     /**
      * 
      */
     private void tjrsUneAstreinte(){
    	//On change l'ordre des dimensions du tableau 3D afin de lui appliquer les contraintes plus simplement
         IntVar[][][] nouveauY = new IntVar[nbServices][nbJours][nbInternes];
         
         for(int iAstreinte=0; iAstreinte < nbAstreintes ; iAstreinte++){
             for(int iIntern=0 ; iIntern < nbInternes ; iIntern++){
                 for(int t=0 ; t<nbJours ; t++){
                     nouveauY[iAstreinte][t][iIntern] = y[iAstreinte][iIntern][t];
                     
                 }
             }
         }
                
         for(int iAstreinte=0; iAstreinte < nbAstreintes ; iAstreinte++){
             for(int t=0 ; t<nbJours ; t++){
                 solveur.post(ICF.sum(nouveauY[iAstreinte][t], "=", VF.fixed(1, solveur)));
             }
         }   
     }
     
     /**
      * 
      */
     private void incompatibleGardeAstreinte(){
    	//On change l'ordre des dimensions du tableau 3D afin de lui appliquer les contraintes plus simplement
         IntVar[][][] nouveauY = new IntVar[nbInternes][nbJours][nbAstreintes];
         
         for(int iAstreinte=0; iAstreinte < nbAstreintes ; iAstreinte++){
             for(int iIntern=0 ; iIntern < nbInternes ; iIntern++){
                 for(int t=0 ; t<nbJours ; t++){
                	 nouveauY[iIntern][t][iAstreinte] = y[iAstreinte][iIntern][t];
                 }
             }
         }
         
         
         for(int iService=0; iService < nbServices ; iService++){
             for(int iIntern=0 ; iIntern < nbInternes ; iIntern++){
                 for(int t=0 ; t<nbJours ; t++){
                	 solveur.post(LCF.ifThen(ICF.arithm(x[iService][iIntern][t], "=", VF.fixed(1, solveur)), 
                			 					   ICF.sum(nouveauY[iIntern][t], "=", VF.fixed(0, solveur))));
                 }
             }
         }
    	 
     }
     
     /**
      * 
      */
     private void equilibreSamu(){
    	 int nbGardesTheorique = nbJours / nbInternes ;
         System.out.println("nombre de gardes SAMU théorique: " + nbGardesTheorique);
         
         // On va regrouper toutes les gardes de chaque interne dans un tableau afin de pouvoir faire la somme plus facilement
         // On pourra ensuite comparer cette somme à la valeur attendue
         IntVar[][] nbGardesInterne = new IntVar[nbInternes][nbJours];
         int cpt = 0;
         for(int iInterne=0 ; iInterne < nbInternes ; iInterne++){
        	 for(int iService=0 ; iService < nbServices ; iService++){
        		 for(int t=0 ; t < nbJours ; t++){
        			 if(nomServices[iService] == "SAMU"){
        				 nbGardesInterne[iInterne][cpt] = x[iService][iInterne][t];
        			 	 cpt++;
        			 }
        		 }
        	 }
        	 cpt = 0;
         }
         
         // le nombre de garde effectué par chaque interne, moins le nombre de garde théorique ne doit pas s'écarter de plus de nbgardeEcart
         // revoir la valeur de nbGardeEcart
         for(int iInterne = 0; iInterne < nbInternes ; iInterne++){
        	 solveur.post(ICF.sum(nbGardesInterne[iInterne], "<=", VF.fixed(nbGardesTheorique+nbGardeEcart, solveur)));
        	 solveur.post(ICF.sum(nbGardesInterne[iInterne], ">=", VF.fixed(nbGardesTheorique-nbGardeEcart, solveur)));
        	 
         }
     }
     
     /**
      * enchainementDesagreable permettra d'éviter les enchainements de plusieurs gardes à la suite
      */
     private void enchainementDesagreable(){
    	 
    	 IntVar[][][] nouveauX = new IntVar[nbInternes][nbJours][nbServices];
         
         for(int iService=0; iService < nbServices ; iService++){
             for(int iIntern=0 ; iIntern < nbInternes ; iIntern++){
                 for(int t=0 ; t<nbJours ; t++){
                     nouveauX[iIntern][t][iService] = x[iService][iIntern][t];
                 }
             }
         }   
    	 
    	 for(int iService=0 ; iService< nbServices ; iService++){
    		 for(int iInterne=0 ; iInterne<nbInternes ; iInterne++){
    			 for(int t=0 ; t<nbJours-4 ; t++){
    				 solveur.post(LCF.ifThen(LCF.and(ICF.sum(nouveauX[iInterne][t], "=", VF.fixed(1, solveur)), 
    						 						 ICF.sum(nouveauX[iInterne][t+2], "=", VF.fixed(1, solveur))
    						 						), 
    						 				ICF.arithm(x[iService][iInterne][t+4], "=", 0)
    						 				)
    						 	 );
    			 }
    		 }
    	 }
    	 
    	 
     }
     
     /**
      * equilibreJSD va s'assurer que le nombre de jeudi samedi et dimanche sont équilibrés
      */
     private void equilibreJSD(){
    	 
     }
     
     /**
      * 
      */
     private void equilibreAstreinte(){
    	 
    	 int nbAstreinteTheorique = nbJours / nbInternes ;
         System.out.println("nombre d'astreintes théorique: " + nbAstreinteTheorique);
         
         // On va regrouper toutes les gardes de chaque interne dans un tableau afin de pouvoir faire la somme plus facilement
         // On pourra ensuite comparer cette somme à la valeur attendue
         IntVar[][] nbAstreinteInterne = new IntVar[nbInternes][nbJours];
         int cpt = 0;
         for(int iInterne=0 ; iInterne < nbInternes ; iInterne++){
        	 for(int iAstreinte=0 ; iAstreinte < nbAstreintes ; iAstreinte++){
        		 for(int t=0 ; t < nbJours ; t++){
        			 
        				 nbAstreinteInterne[iInterne][cpt] = y[iAstreinte][iInterne][t];
        			 	 cpt++;
        			
        		 }
        	 }
        	 cpt = 0;
         }
         
         // le nombre d'astreinte effectué par chaque interne, moins le nombre d'astreinte théorique ne doit pas s'écarter de plus de nbAstreinteTheorique
         // revoir la valeur de nbGardeEcart
         for(int iInterne = 0; iInterne < nbInternes ; iInterne++){
        	 solveur.post(ICF.sum(nbAstreinteInterne[iInterne], "<=", VF.fixed(nbAstreinteTheorique+nbAstreintesEcart, solveur)));
        	 solveur.post(ICF.sum(nbAstreinteInterne[iInterne], ">=", VF.fixed(nbAstreinteTheorique-nbAstreintesEcart, solveur)));
        	 
         }
    	 
     }
     
     
     
     /**
      * 
      */
     private void indisponibiliteSouple(){
    	 
     }
     
     
	public void solve(){
		
		//On met toutes les gardes dans un seul tableau afin de pouvoir appliquer une stratégie de recherche sur tous les jours
		IntVar[] toutesLesGardes = new IntVar[nbServices*nbInternes*nbJours + nbAstreintes*nbInternes*nbJours];
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
		
		for(int iAstreinte =0 ; iAstreinte < nbAstreintes ; iAstreinte++){
			for(int iInterne=0 ; iInterne < nbInternes ; iInterne++){
				for(int t=0 ; t < nbJours ; t++){
					toutesLesGardes[cpt] = y[iAstreinte][iInterne][t];
					cpt++;
				}
			}
		}
		
		//Methode de Recherche
		for(int iService=0 ; iService < nbServices ; iService++){
			for(int iInterne=0 ; iInterne < nbInternes ; iInterne++){
				//solveur.set(ISF.custom(ISF.minDomainSize_var_selector(), ISF.min_value_selector(),toutesLesGardes));
				solveur.set(ISF.random_value(toutesLesGardes, new Random().nextLong()));
			}
		}
		
		if (solveur.findSolution()) {
            System.out.println("Solution trouvée en " + solveur.getMeasures().getTimeCount() + " secondes");        
        } else {
            System.out.println("Pas de solution pour ce problème, en "
                    + solveur.getMeasures().getTimeCount() + solveur.getMeasures().getSolutionCount() + " secondes");
        }
	}
	
	
	
	public void displayResult(){
        int sommeDeGardes, sommeDastreintes;
        int[][] nbGardes = new int[nbServices][nbInternes];
        int[][] nbAstr = new int[nbAstreintes][nbInternes];
        for(int i=0; i< nbInternes ; i++){
            for(int t=0; t< nbJours ; t++){
            	if(t%7 == 0)
                	System.out.print("   ");
                System.out.print("[");
                for(int j=0; j< nbServices ; j++){
                    System.out.print((x[j][i][t]).getValue());
                    nbGardes[j][i] += x[j][i][t].getValue();
                }
                System.out.print("] ");
                
            }
            sommeDeGardes =0;
            for(int k=0; k<nbServices ; k++){
                System.out.print(nomServices[k] + ": " + nbGardes[k][i] + " ");
                sommeDeGardes += nbGardes[k][i];
            }
            System.out.print(" Total: ");
            System.out.println(sommeDeGardes);
            
        }
         System.out.println("\n");
         
         
         
         for(int i=0; i< nbInternes ; i++){
             for(int t=0; t< nbJours ; t++){
             	if(t%7 == 0)
                 	System.out.print("   ");
                 System.out.print("[");
                 for(int j=0; j< nbAstreintes ; j++){
                     System.out.print((y[j][i][t]).getValue());
                     nbAstr[j][i] += y[j][i][t].getValue();
                 }
                 System.out.print("] ");
                 
             }
             sommeDastreintes =0;
             for(int k=0; k<nbAstreintes ; k++){
                 System.out.print(nomAstreintes[k] + ": " + nbAstr[k][i] + " ");
                 sommeDastreintes += nbAstr[k][i];
             }
             System.out.print(" Total: ");
             System.out.println(sommeDastreintes);
             
         }
          System.out.println("");
        
    }

}
