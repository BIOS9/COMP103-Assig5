/* Code for COMP103 - 2018T2, Assignment 5
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import ecs100.UI;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class FileManager {

    /**
     * Loads employee file into an Employee tree object
     * @param file File to read from
     * @return Loaded Employee tree
     */
    public static Employee loadTree(File file)
    {
        try
        {
            Scanner sc = new Scanner(file);
            return loadSubEmpl(sc);
        }
        catch (Exception ex)
        {
            UI.println("Error loading tree: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Recursive method that uses a scanner as a state variable to load a tree structure
     * @param sc Scanner that can read the tree
     * @return Loaded employee
     */
    private static Employee loadSubEmpl(Scanner sc)
    {
        ParseResult pr = new ParseResult(sc.nextLine()); //Get Employee for current Scanner line
        if(pr.childCount > 0) //If there are child nodes to load
        {
            //Recursively load child nodes
            for(int i = 0; i < pr.childCount; ++i)
                pr.employee.addToTeam(loadSubEmpl(sc));
        }
        return pr.employee;
    }

    /**
     * Saves an Employee tree to a file
     * @param file File to save to
     * @param root Organisation tree to save
     * @return Error value, null if no error
     */
    public static String saveTree(File file, Employee root)
    {
        try {
            String str = organiseTree(root, ""); //Get tree in String format

            //Save to file
            PrintWriter pw = new PrintWriter(file);
            pw.write(str);
            pw.close();
            return null;
        }
        catch (Exception ex)
        {
            return ex.getMessage();
        }
    }

    /**
     * Recursively converts an Employee tree into a string
     * @param empl Employee and team to convert
     * @param str Current tree in String format
     * @return Existing tree concatenated with the new conversion
     */
    private static String organiseTree(Employee empl, String str)
    {
        if(empl.isManager()) {
            //Add team to tree
            str += empl.getTeam().size() + " " + empl + " " + empl.getOffset() + "\r\n";
            for (Employee subEmp : empl.getTeam()) {
                str = organiseTree(subEmp, str);
            }
        }
        else
        {
            str += "0 " + empl + " " + empl.getOffset() +"\r\n"; //Add single leaf node
        }
        return str;
    }

    /**
     * Parses one line of the tree in String format into usable values
     */
    private static class ParseResult {
        public Employee employee;
        public int childCount;

        public ParseResult(String line)
        {
            Scanner sc = new Scanner(line);
            childCount = sc.nextInt();
            employee = new Employee(sc.next(), sc.next()); //Load employee initials and role
            employee.setOffset(sc.nextDouble());
            sc.close();
        }
    }

}
