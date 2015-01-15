package fr.brandon.planning;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        SolveurPlanning test = new SolveurPlanning();
        test.ajoutContrainte();
        
        System.out.println( "Hello World!" );
        
    }
}
