package fr.brandon.planning;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for simple SolveurPlanning.
 */
public class SolveurPlanningTest {
	protected static SolveurPlanning planningTest;

	public SolveurPlanningTest() {
	}

	@BeforeClass
	public static void setUpClass() {
		// On prépare les test en créant une instance d'un planning
		int nbServices = 3;
		int nbAstreinte = 1;
		int nbInternes = 10;
		int nbJours = 15;
		int diffGardes = 1;
		int diffAstreinte = 2;

		String[] nomServices = { "MAT", "SAMU", "BLOC" };
		String[] nomAstreintes = { "AST" };
		boolean[] respectVD = new boolean[nbInternes];
		for (int i = 0; i < nbInternes; i++) {
			respectVD[i] = true;
		}
		boolean[][] indispoForte = new boolean[nbInternes][nbJours];
		for (int i = 0; i < nbInternes; i++) {
			for (int t = 0; t < nbJours; t++) {
				indispoForte[i][t] = false;
			}
		}

		boolean[][] indispoSouple = new boolean[nbInternes][nbJours];
		for (int i = 0; i < nbInternes; i++) {
			for (int t = 0; t < nbJours; t++) {
				indispoSouple[i][t] = false;
			}
		}

		indispoSouple[0][0] = true;
		indispoSouple[0][1] = true;
		indispoSouple[0][2] = true;
		indispoSouple[0][3] = true;
		indispoSouple[0][4] = true;
		indispoSouple[0][5] = true;
		indispoSouple[0][6] = true;
		indispoSouple[0][7] = true;
		indispoSouple[0][8] = true;

		indispoSouple[1][7] = true;
		indispoSouple[1][8] = true;
		indispoSouple[1][9] = true;
		indispoSouple[1][10] = true;
		indispoSouple[1][11] = true;
		indispoSouple[1][12] = true;
		indispoSouple[1][13] = true;
		indispoSouple[1][14] = true;

		boolean[][] peutTravaillerEnsemble = new boolean[nbInternes][nbInternes];
		for (int i = 0; i < nbInternes; i++) {
			for (int j = 0; j < nbInternes; j++) {
				peutTravaillerEnsemble[i][j] = true;
			}
		}

		peutTravaillerEnsemble[0][1] = false;
		peutTravaillerEnsemble[0][2] = false;
		peutTravaillerEnsemble[1][0] = false;
		peutTravaillerEnsemble[2][0] = false;

		// peutTravaillerEnsemble[0][3] = false;
		// peutTravaillerEnsemble[0][4] = false;
		// peutTravaillerEnsemble[0][5] = false;
		// peutTravaillerEnsemble[0][j] = false;

		boolean[][][] aptitude = new boolean[nbServices][nbInternes][nbJours];
		for (int i = 0; i < nbServices; i++) {
			for (int j = 0; j < nbInternes; j++) {
				for (int t = 0; t < nbJours; t++) {
					aptitude[i][j][t] = true;
				}
			}
		}

		planningTest = new SolveurPlanning(nbServices, nbAstreinte, nbInternes,
				nbJours, diffGardes, diffAstreinte, nomServices, nomAstreintes,
				respectVD, indispoForte, indispoSouple, peutTravaillerEnsemble,
				aptitude);

		planningTest.initialisation();
		planningTest.solve();
		
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
		
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of auPlus1GardeJour
	 */
	@Test
	public void auPlus1GardeJourTest() {
		int nbGardes = 0;
		for (int t = 0; t < planningTest.getNbJours(); t++) {
			for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
				for (int iService = 0; iService < planningTest.getNbServices(); iService++) {
					if (planningTest.getX()[iService][iInterne][t].getValue() == 1)
						nbGardes++;
				}
				assertTrue("L'interne " + iInterne
						+ " travaille dans deux services le même jour",
						nbGardes <= 1);
				nbGardes = 0;
			}
		}
	}

	/**
	 * Test de tjrs1PersonneDeGarde
	 */
	@Test
	public void tjrs1PersonneDeGardeTest() {
		int nbGardes = 0;
		for (int t = 0; t < planningTest.getNbJours(); t++) {
			for (int iService = 0; iService < planningTest.getNbServices(); iService++) {
				for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
					if (planningTest.getX()[iService][iInterne][t].getValue() == 1)
						nbGardes++;
				}
				assertEquals("Le service " + iService
						+ " n'a pas le nombre requis de gardes", 1, nbGardes);
				nbGardes = 0;
			}
		}
	}

	/**
	 * Test de equilibreGarde
	 */
	@Test
	public void equilibreGardeTest() {
		int nbGardes = 0;
		int nbGardesTheorique = planningTest.getNbGardesTheorique();
		int nbGardesEcart = planningTest.getNbGardeEcart();
		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			for (int t = 0; t < planningTest.getNbJours(); t++) {
				for (int iService = 0; iService < planningTest.getNbServices(); iService++) {
					if (planningTest.getX()[iService][iInterne][t].getValue() == 1)
						nbGardes++;
				}

			}
			assertTrue("L'interne " + iInterne
					+ " s'éloigne trop de la moyenne de garde théorique",
					nbGardes <= nbGardesTheorique + nbGardesEcart);
			assertTrue("L'interne " + iInterne
					+ " s'éloigne trop de la moyenne de garde théorique",
					nbGardes >= nbGardesTheorique - nbGardesEcart);
			nbGardes = 0;
		}
	}

	/**
	 * Test de ReposLendemain
	 */
	@Test
	public void ReposLendemainTest() {
		for (int iService = 0; iService < planningTest.getNbServices(); iService++) {
			for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
				for (int t = 0; t < planningTest.getNbJours() - 1; t++) {
					if (planningTest.getX()[iService][iInterne][t].getValue() == 1) {
						assertEquals("L'interne " + iInterne
								+ "travaille deux jours de suite", 0,
								planningTest.getX()[iService][iInterne][t + 1]
										.getValue());
					}
				}
			}
		}
	}

	/**
	 * Test enchainementVD
	 */
	@Test
	public void enchainementVDTest() {
		planningTest.displayResult();
		int cpt = 0;
		for (int iService = 0; iService < planningTest.getNbServices(); iService++) {
			for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
				if (planningTest.getVD()[iInterne]) {
					for (int t = 0; t < planningTest.getNbJours() - 1; t++) {
						if (t % SolveurPlanning.NB_JOURS_SEMAINE == SolveurPlanning.VENDREDI)
							if(planningTest.getX()[iService][iInterne][t].getValue() == 1){
								for(int jService=0 ; jService<planningTest.getNbServices() ; jService++){
									cpt += planningTest.getX()[jService][iInterne][t + 2].getValue();
								}
							assertEquals("L'interne " + iInterne + "accepte la règle VD mais elle n'est pas respectée",
											1 , cpt);
							cpt=0;
							}
					}
				}
			}
		}
	}
	
	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void indisponibiliteForteTest() {
		for(int iInterne=0; iInterne< planningTest.getNbInternes() ; iInterne++){
    		for(int t=0 ; t< planningTest.getNbJours() ; t++){
    			if(planningTest.getIndispoForte()[iInterne][t]){
    				for(int iService=0; iService< planningTest.getNbServices() ; iService++)
    					assertEquals("l'indisponibilité forte de l'interne " + iInterne + "n'est pas respectée",
    									0, planningTest.getX()[iService][iInterne][t]);
    			}
    		}
    	} 
	}

	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void estCapable(){
		  assertTrue(false);
	  }
	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void gardeEnsemble() {
		assertTrue(false);
	  }
	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void groupementAstreintes() {
		assertTrue(false);
	  }
	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void tjrsUneAstreinte() {
		assertTrue(false);
	 }
	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void incompatibleGardeAstreinte() {
		assertTrue(false);
	 }
	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void equilibreSamu() {
		assertTrue(false);
	  }
	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void enchainementDesagreable() {
		assertTrue(false);
	 }
	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void equilibreJSD() {
		assertTrue(false);
	  }
	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void equilibreAstreinte() {
		assertTrue(false);
	 }
	/**
	 * Test Indisponibilité forte
	 */
	@Test
	public void indisponibiliteSouple() {
		assertTrue(false);
	  }
	 
	 

}
