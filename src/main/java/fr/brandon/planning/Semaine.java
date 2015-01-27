package fr.brandon.planning;

public enum Semaine {
	  LUNDI    (0),
	  MARDI    (1),
	  MERCREDI (2),
	  JEUDI    (3),
	  VENDREDI (4),
	  SAMEDI   (5),
	  DIMANCHE (6),
	  NB_JOURS_SEMAINE(7);
	  
	    private final int code;

	    private Semaine(int code) {
	        this.code = code;
	    }

	    public int toInt() {
	        return code;
	    }
}
