package fr.brandon.planning;

import solver.Solver;
import solver.constraints.ICF;
import solver.constraints.LCF;
import solver.search.strategy.ISF;
import solver.variables.VF;
import solver.variables.IntVar;

public class SolveurPlanning {
	private Solver solveur;
	private IntVar test;
	private IntVar test2;
	
	public SolveurPlanning() {
		solveur = new Solver();
    }
	
	public void ajoutContrainte() {
		test = VF.enumerated("test", 0 , 100 , solveur);
		test2 = VF.enumerated("test2", 0 , 100 , solveur);
		ICF.arithm(test, "=", test2);
	}

}
