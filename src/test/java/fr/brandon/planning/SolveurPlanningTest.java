package fr.brandon.planning;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import solver.constraints.ICF;
import solver.variables.IntVar;
import solver.variables.VF;
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

		planningTest.displayResult();

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
		int cpt = 0;
		for (int iService = 0; iService < planningTest.getNbServices(); iService++) {
			for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
				if (planningTest.getVD()[iInterne]) {
					for (int t = 0; t < planningTest.getNbJours() - 1; t++) {
						if (t % SolveurPlanning.NB_JOURS_SEMAINE == SolveurPlanning.VENDREDI)
							if (planningTest.getX()[iService][iInterne][t]
									.getValue() == 1) {
								for (int jService = 0; jService < planningTest
										.getNbServices(); jService++) {
									cpt += planningTest.getX()[jService][iInterne][t + 2]
											.getValue();
								}
								assertEquals(
										"L'interne "
												+ iInterne
												+ "accepte la règle VD mais elle n'est pas respectée",
										1, cpt);
								cpt = 0;
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
		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			for (int t = 0; t < planningTest.getNbJours(); t++) {
				if (planningTest.getIndispoForte()[iInterne][t]) {
					for (int iService = 0; iService < planningTest
							.getNbServices(); iService++)
						assertEquals("l'indisponibilité forte de l'interne "
								+ iInterne + "n'est pas respectée", 0,
								planningTest.getX()[iService][iInterne][t]
										.getValue());
				}
			}
		}
	}

	/**
	 * Test Aptitude
	 */
	@Test
	public void estCapableTest() {
		for (int iService = 0; iService < planningTest.getNbServices(); iService++)
			for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
				for (int t = 0; t < planningTest.getNbJours(); t++) {
					if (!planningTest.getAptitude()[iService][iInterne][t]) {
						assertEquals("l'interne " + iInterne
								+ " travaille mais n'a pas l'aptitude", 0,
								planningTest.getX()[iService][iInterne][t]
										.getValue());
					}
				}
			}
	}

	/**
	 * Test gardeEnsemble
	 */
	@Test
	public void gardeEnsembleTest() {
		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			for (int jInterne = 0; jInterne < planningTest.getNbInternes(); jInterne++) {
				if (!planningTest.getPeutTravailEnsemble()[iInterne][jInterne]
						&& (iInterne != jInterne)) {
					for (int t = 0; t < planningTest.getNbJours(); t++) {
						for (int iService = 0; iService < planningTest
								.getNbServices(); iService++) {
							if (planningTest.getX()[iService][iInterne][t]
									.getValue() == 1)
								for (int jService = 0; jService < planningTest
										.getNbServices(); jService++) {

									assertEquals(
											"Deux internes travaillent ensemble alors qu'ils ne peuvent pas",
											0,
											planningTest.getX()[jService][jInterne][t]
													.getValue());
								}
						}
					}
				}
			}
		}
	}

	/**
	 * Test des groupements d'astreinte
	 */
	@Test
	public void groupementAstreintesTest() {
		for (int iAstreinte = 0; iAstreinte < planningTest.getNbAstreintes(); iAstreinte++) {
			for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
				for (int t = 0; t < planningTest.getNbJours() - 2; t++) {
					if (planningTest.getY()[iAstreinte][iInterne][t].getValue() == 1) {

						switch (t % SolveurPlanning.NB_JOURS_SEMAINE) {
						case 0: // lundi
							assertEquals(
									"Les astreintes ne sont pas groupées comme souhaité",
									1,
									planningTest.getY()[iAstreinte][iInterne][t + 1]
											.getValue());
							break;
						case 1: // mardi
							assertEquals(
									"Les astreintes ne sont pas groupées comme souhaité",
									1,
									planningTest.getY()[iAstreinte][iInterne][t - 1]
											.getValue());
							break;
						case 2: // mercredi
							assertEquals(
									"Les astreintes ne sont pas groupées comme souhaité",
									1,
									planningTest.getY()[iAstreinte][iInterne][t + 1]
											.getValue());
							break;
						case 3: // jeudi
							assertEquals(
									"Les astreintes ne sont pas groupées comme souhaité",
									1,
									planningTest.getY()[iAstreinte][iInterne][t - 1]
											.getValue());
							break;
						case 4: // vendredi
							assertEquals(
									"Les astreintes ne sont pas groupées comme souhaité",
									1,
									planningTest.getY()[iAstreinte][iInterne][t + 1]
											.getValue());
							assertEquals(
									"Les astreintes ne sont pas groupées comme souhaité",
									1,
									planningTest.getY()[iAstreinte][iInterne][t + 2]
											.getValue());
							break;
						case 5: // samedi
							assertEquals(
									"Les astreintes ne sont pas groupées comme souhaité",
									1,
									planningTest.getY()[iAstreinte][iInterne][t + 1]
											.getValue());
							assertEquals(
									"Les astreintes ne sont pas groupées comme souhaité",
									1,
									planningTest.getY()[iAstreinte][iInterne][t - 1]
											.getValue());
							break;
						case 6: // dimanche
							assertEquals(
									"Les astreintes ne sont pas groupées comme souhaité",
									1,
									planningTest.getY()[iAstreinte][iInterne][t - 1]
											.getValue());
							assertEquals(
									"Les astreintes ne sont pas groupées comme souhaité",
									1,
									planningTest.getY()[iAstreinte][iInterne][t - 2]
											.getValue());
							break;
						default:
							System.out.println("Erreur.");
						}

					}
				}
			}
		}
	}

	/**
	 * Test tjrsUneAstreinte
	 */
	@Test
	public void tjrsUneAstreinteTest() {
		int nbAstreinte = 0;
		for (int t = 0; t < planningTest.getNbJours(); t++) {
			for (int iAstreinte = 0; iAstreinte < planningTest
					.getNbAstreintes(); iAstreinte++) {
				for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
					if (planningTest.getY()[iAstreinte][iInterne][t].getValue() == 1)
						nbAstreinte++;
				}
				assertEquals("Le service " + iAstreinte
						+ " n'a pas le nombre requis de gardes", 1, nbAstreinte);
				nbAstreinte = 0;
			}
		}
	}

	/**
	 * Test incompatibleGardeAstreinte
	 */
	@Test
	public void incompatibleGardeAstreinteTest() {
		for (int iService = 0; iService < planningTest.getNbServices(); iService++) {
			for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
				for (int t = 0; t < planningTest.getNbJours(); t++) {
					if (planningTest.getX()[iService][iInterne][t].getValue() == 1)
						for (int iAstreinte = 0; iAstreinte < planningTest
								.getNbAstreintes(); iAstreinte++) {
							assertEquals(
									"L'interne cumule garde et astreinte",
									0,
									planningTest.getY()[iAstreinte][iInterne][t]
											.getValue());

						}

				}
			}
		}
	}

	/**
	 * Test equilibre Samu
	 */
	@Test
	public void equilibreSamuTest() {
		int nbGardes = 0;
		int nbGardesTheorique = planningTest.getNbJours()
				/ planningTest.getNbInternes();
		int nbGardesEcart = planningTest.getNbGardeEcart();
		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			for (int t = 0; t < planningTest.getNbJours(); t++) {
				for (int iService = 0; iService < planningTest.getNbServices(); iService++) {
					if (planningTest.getNomServices()[iService] == "SAMU") {
						if (planningTest.getX()[iService][iInterne][t]
								.getValue() == 1)
							nbGardes++;
					}
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
	 * Test enchainement désagréables
	 */
	@Test
	public void enchainementDesagreableTest() {

		int garde[][] = new int[planningTest.getNbInternes()][planningTest.getNbJours()];

		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			for (int t = 0; t < planningTest.getNbJours(); t++) {
				for (int iService = 0; iService < planningTest.getNbServices(); iService++) {

					garde[iInterne][t] += planningTest.getX()[iService][iInterne][t].getValue();
				}
			}
		}

		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			for (int t = 0; t < planningTest.getNbJours() - 4; t++) {

				if ((garde[iInterne][t] == 1) && (garde[iInterne][t + 2] == 1))

					assertEquals(
							"L'interne "
									+ iInterne
									+ " subit un enchainement désagréable le jour "
									+ t, 0, garde[iInterne][t + 4]);

			}

		}
	}

	/**
	 * Test equilibre JSD
	 */
	@Test
	public void equilibreJSDTest() {
		int nbJourTheorique = (planningTest.getNbJours()
				* planningTest.getNbServices() / SolveurPlanning.NB_JOURS_SEMAINE)
				/ planningTest.getNbInternes();

		// On va regrouper chaque jour dans un tableau qui lui est propre afin
		// d'y appliquer les contraintes plus simplement
		int[] jeudis = new int[planningTest.getNbInternes()];
		int[] samedis = new int[planningTest.getNbInternes()];
		int[] dimanches = new int[planningTest.getNbInternes()];
		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			for (int iService = 0; iService < planningTest.getNbServices(); iService++) {
				for (int t = 0; t < planningTest.getNbJours(); t++) {
					switch (t % SolveurPlanning.NB_JOURS_SEMAINE) {
					case SolveurPlanning.JEUDI:
						jeudis[iInterne] += planningTest.getX()[iService][iInterne][t]
								.getValue();
						break;
					case SolveurPlanning.SAMEDI:
						samedis[iInterne] += planningTest.getX()[iService][iInterne][t]
								.getValue();
						break;
					case SolveurPlanning.DIMANCHE:
						dimanches[iInterne] += planningTest.getX()[iService][iInterne][t]
								.getValue();
						break;
					default:

					}
				}
			}
		}

		// le nombre de garde effectué le jeudi/samedi/dimanche par chaque
		// interne est comptabilisé, ce nombre ne doit pas trop s'écarter de la
		// moyenne théorique
		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			assertTrue("probleme de jeudi", jeudis[iInterne] <= nbJourTheorique
					+ planningTest.getNbGardeEcart());
			assertTrue("probleme de jeudi", jeudis[iInterne] >= nbJourTheorique
					- planningTest.getNbGardeEcart());
			assertTrue(
					"probleme de Samedi",
					samedis[iInterne] <= nbJourTheorique
							+ planningTest.getNbGardeEcart());
			assertTrue(
					"probleme de Samedi",
					samedis[iInterne] >= nbJourTheorique
							- planningTest.getNbGardeEcart());
			assertTrue(
					"probleme de Dimanche",
					dimanches[iInterne] <= nbJourTheorique
							+ planningTest.getNbGardeEcart());
			assertTrue(
					"probleme de Dimanche",
					dimanches[iInterne] >= nbJourTheorique
							- planningTest.getNbGardeEcart());

		}
	}

	/**
	 * Test Equilibre des astreintes
	 */
	@Test
	public void equilibreAstreinteTest() {
		int nbAstreintes = 0;
		int nbAstreintesTheorique = planningTest.getNbAstreintesTheorique();
		int nbAstreinteEcart = planningTest.getNbAstreintesEcart();
		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			for (int t = 0; t < planningTest.getNbJours(); t++) {
				for (int iAstreinte = 0; iAstreinte < planningTest
						.getNbAstreintes(); iAstreinte++) {
					if (planningTest.getY()[iAstreinte][iInterne][t].getValue() == 1)
						nbAstreintes++;
				}

			}

			assertTrue("L'interne " + iInterne
					+ " s'éloigne trop de la moyenne d'astreinte théorique",
					nbAstreintes <= nbAstreintesTheorique + nbAstreinteEcart);
			assertTrue("L'interne " + iInterne
					+ " s'éloigne trop de la moyenne d'astreinte théorique",
					nbAstreintes >= nbAstreintesTheorique - nbAstreinteEcart);
			nbAstreintes = 0;
		}
	}

	/**
	 * Test indisponibilité souples
	 */
	@Test
	public void indisponibiliteSoupleTest() {
		int[] nbIndispo = new int[planningTest.getNbInternes()];
		int[] nbIndispoRespectees = new int[planningTest.getNbInternes()];
		boolean respecte;

		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			for (int t = 0; t < planningTest.getNbJours(); t++) {
				if (planningTest.getIndispoSouple()[iInterne][t]) {
					nbIndispo[iInterne]++;
					respecte = true;
					for (int iService = 0; iService < planningTest
							.getNbServices(); iService++) {
						if (planningTest.getX()[iService][iInterne][t]
								.getValue() == 1) {
							respecte = false;
						}
					}
					if (respecte)
						nbIndispoRespectees[iInterne]++;
				}
			}
		}

		// On va considerer que la fonction respecter ses objectifs
		// lorsqu'au moins 50% des indisponibilités souples son respectées

		for (int iInterne = 0; iInterne < planningTest.getNbInternes(); iInterne++) {
			assertTrue(
					"l'interne "
							+ iInterne
							+ " n'a pas suffisament d'insiponibilités souples respectées",
					nbIndispoRespectees[iInterne] >= nbIndispo[iInterne] / 2);
		}
	}

}
