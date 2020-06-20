// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2018T2, Assignment 5
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import ecs100.*;
import java.util.*;
import java.io.*;


/** GeneSearch   */

public class GeneSearch{

    
    private List<Character> data;    // the genome data to search in
    private List<Character> pattern; // the pattern to search for
    private String patternString;         // the pattern to search for (as a String)
    private int maxErrors = 1;            // number of mismatching characters allowed

    private int quickSearchLength = 3;
    private Map<String, Collection<Integer>> locationMap = null; //Map for quick exact search
    private String quickSearchPattern = null;

    /**
     * Construct a new GeneSearch object
     */
    public GeneSearch(){
        setupGUI();
        loadData();
    }

    /**
     * Initialise the interface
     */
    public void setupGUI(){
        UI.addTextField("Search Pattern", this::setSearchPattern);
        UI.addButton("ExactSearch", this::exactSearch);
        UI.addButton("Approx Search", this::approximateSearch);
        UI.addSlider("# mismatches allowed", 1, 5, maxErrors,
                (double n)->{maxErrors = (int)n;});
        UI.addSlider("Length of quick search Pattern", 3, 10, 3,
                (double n)->{quickSearchLength = (int)n; UI.println("Quick search pattern length set to " + (int)n); });
        UI.addTextField("Quick Search Pattern", (String s) -> { quickSearchPattern = s; });
        UI.addButton("Generate search map", this::generateMap);
        UI.addButton("Quick ExactSearch", this::quickExactSearch);
        UI.addButton("Quit", UI::quit);
        UI.setDivider(1.0);
    }



    public void setSearchPattern(String v){
        patternString = v.toUpperCase();
        pattern = new ArrayList<Character>();
        for (int i=0; i<v.length(); i++){
            pattern.add(patternString.charAt(i));
        }
        UI.println("Search pattern is now "+ pattern);
    }

    /**
     * Search for all occurrences of the pattern in the data,
     * reporting the position of each occurrence and the total number of matches
     */    
    public void exactSearch(){
        if (pattern==null){UI.println("No pattern");return;}
        UI.println("===================\nExact searching for "+patternString);
        int matchCount = 0;
        for(int i = 0; i < data.size(); ++i)
        {
            for(int j = 0; j < pattern.size(); ++j)
            {
                if(i + j >= data.size()) break; //Ensure the search stays within the bounds of the array
                if(data.get(i + j) != pattern.get(j)) //Check the pattern
                    break;

                if(j == pattern.size() - 1) //If we have reached the end of the pattern and matched all characters
                {
                    UI.println("Exact match at " + i);
                    matchCount++;
                }
            }
        }
        UI.println(matchCount + " matches found");
    }


    /**
     * Search for all approximate occurrences of the pattern in the data,
     * (pattern is the same as a sub sequence of the data except for at most
     *  maxErrors characters that differ.)
     * Reports the position and data sequence of each occurrence and
     *  the total number of matches
     */    
    public void approximateSearch(){
        if (pattern==null){UI.println("No pattern");return;}
        UI.println("===================");
        UI.printf("searching for %s, %d mismatches allowed\n", patternString, maxErrors);
        int matchCount = 0;
        for(int i = 0; i < data.size(); ++i)
        {
            int errors = 0;
            for(int j = 0; j < pattern.size(); ++j)
            {
                if(i + j >= data.size()) break; //Ensure the search stays within the bounds of the array
                if(data.get(i + j) != pattern.get(j)) //Check the pattern
                {
                    if (errors < maxErrors) //Check error allowance
                        errors++;
                    else
                        break;
                }

                if(j == pattern.size() - 1) //If we have reached the end of the pattern and matched all characters
                {
                    UI.println("Approx match at " + i);
                    matchCount++;
                }
            }
        }
        UI.println(matchCount + " matches found with " + maxErrors + " mismatches allowed.");
    }

    public Collection<Integer> exactSearchManual(char[] matchPattern){
        ArrayList<Integer> matches = new ArrayList<>();
        for(int i = 0; i < data.size(); ++i)
        {
            for(int j = 0; j < matchPattern.length; ++j)
            {
                if(i + j >= data.size()) break; //Ensure the search stays within the bounds of the array
                if(data.get(i + j) != matchPattern[j]) //Check the pattern
                    break;

                if(j == matchPattern.length - 1) //If we have reached the end of the pattern and matched all characters
                    matches.add(i);
            }
        }
        return matches;
    }


    /**
     * Load gene data from file into ArrayList of characters
     */
    public void loadData(){
        data = new ArrayList<Character>(1000000);
        try{
            Scanner sc = new Scanner(new File("acetobacter_pasteurianus.txt"));
            while (sc.hasNext()){
                String line = sc.nextLine();
                for (int i=0; i<line.length(); i++){
                    data.add(line.charAt(i));
                }
            }
            sc.close();
            UI.println("read "+data.size()+" letters");
        }
        catch(IOException e){UI.println("Fail: " + e);}
    }

    /**
     * Searches for exact matches using the map of locations
     */
    public void quickExactSearch() {
        if(locationMap == null)
        {
            UI.println("You must generate the search map before you can use the quick exactSearch.");
            return;
        }
        if(quickSearchPattern == null || quickSearchPattern.length() == 0 || !quickSearchPattern.matches("(?i)^([ACTG]+)$")) //Ensures valid pattern using regex for A, C, T, G
        {
            UI.println("Please enter a valid quick search pattern.");
            UI.println("Pattern cannot be empty and can only contain A, T, G, C");
            return;
        }
        if(quickSearchPattern.length() != quickSearchLength)
        {
            UI.println("You must use a pattern that has the same length as the pattern length for the generated map");
            return;
        }

        Collection<Integer> locations = locationMap.get(quickSearchPattern.toUpperCase()); //Get match locations
        UI.println(locations.size() + " exact matches found for pattern " + quickSearchPattern);
        for(Integer loc : locations) //Print match locations
        {
            UI.println("Exact match at " + loc);
        }
        UI.println(locations.size() + " exact matches found!");
    }

    /**
     * Generates a map of all possible subsequences for the length specified by the user.
     */
    public void generateMap()
    {
        UI.println("Building map...");
        locationMap = new HashMap<>();

        ArrayList<String> patterns = generateSubsequences(quickSearchLength); //Get arraylist of all possible patterns for the length
        UI.println("Generated " + patterns.size() + " patterns.");
        UI.println("Searching for patterns...");

        //Search for each pattern and put the results in the map
        for(String pattern : patterns)
        {
            Collection<Integer> locations = exactSearchManual(pattern.toCharArray());
            locationMap.put(pattern, locations);
        }

        UI.println("Search complete! You may now use the quick exact search.");
    }


    /**
     * Generates all possible subsequence patterns for a given length
     * @param length Length of subsequences to generate. Larger length means more possibilities
     * @return Arraylist of all patterns
     */
    public ArrayList<String> generateSubsequences(int length)
    {
        ArrayList<String> subsequences = new ArrayList<>();
        generateSubsequences(length, "", subsequences, 0);
        return subsequences;
    }

    /**
     * Generates all possible subsequence patterns for a given length
     * @param length Length of subsequences to generate
     * @param current Current position in the tree, used since this method os recursive
     * @param subsequences ArrayList to add new sequences to
     * @param depth Current depth in the tree
     */
    public void generateSubsequences(int length, String current, ArrayList<String> subsequences, int depth)
    {
        if(length < 1) return; //Invalid length

        final char[] bases = new char[] { 'A', 'C', 'T', 'G' };

        for(char c : bases)
        {
            String subCurrent = current + c;

            if(length - 1 == depth)
                subsequences.add(subCurrent);
            else
                generateSubsequences(length, subCurrent, subsequences, depth + 1); //Recursively generate the rest of the string
        }
    }

    public static void main(String[] arguments){
        new GeneSearch();
    }        


}
