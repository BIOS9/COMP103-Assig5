// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2018T2, Assignment 5
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

import ecs100.*;
import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.io.*;

/** <description of class OrganisationChart>
 */

public class OrganisationChart {

    // Fields
    private Employee organisation;          // the root of the current organisational chart
    private Employee selectedEmployee = null; // the employee selected by pressing the mouse
    private Employee newPerson = null;      // the employee constructed from the data
                                            //  the user entered
    private double initialOffset = 0; //Initial offset of the person being moved

    private String newInitials = null;      // the data the user entered
    private String newRole = null;

    // constants for the layout
    public static final double NEW_LEFT = 10; // left of the new person Icon
    public static final double NEW_TOP = 10;  // top of the new person Icon

    public static final double ICON_X = 40;   // position and size of the retirement icon
    public static final double ICON_Y = 90;   
    public static final double ICON_RAD = 20;

    public static final double LAYOUT_MARGIN = 10;
    /**
     * Construct a new OrganisationChart object
     * Set up the GUI
     */
    public OrganisationChart() {
        this.setupGUI();
        organisation = new Employee(null, "CEO");   // Set the root node of the organisation
        redraw();
    }

    /**
     * Set up the GUI (buttons and mouse)
     */
    public void setupGUI(){
        UI.setMouseMotionListener( this::doMouse );
        UI.addTextField("Initials", (String v)-> {newInitials=v; redraw();});
        UI.addTextField("Role", (String v)-> {newRole=v; redraw();});
        UI.addButton("Load test tree",  this::makeTestTree);
        UI.addButton("Organise", ()->{
            layoutTree(organisation);});
        UI.addButton("Save Tree", this::saveTree);
        UI.addButton("Load Tree", this::loadTree);
        UI.addButton("Add user", this::showAddEmplWindow);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1100,500);
        UI.setDivider(0);
    }

    /**
     * Most of the work is initiated by the mouse.
     * The action depends on where the mouse is pressed:
     *   on an employee in the tree, or
     *   the new person
     * and where it is released:
     *   on another employee in the tree,
     *   on the retirement Icon, or
     *   empty space
     * An existing person will be moved around in the tree, retired, or repositioned.
     * The new person will be added into the tree;
     */
    public void doMouse(String action, double x, double y){
        if (action.equals("pressed")){
            if (onNewIcon(x, y)) {
                // get the new person
                newPerson = new Employee(newInitials, newRole);
                selectedEmployee = null;
            }
            else {
                // find the selected employee
                selectedEmployee = findEmployee(x, y, organisation);
                if(selectedEmployee != null)
                    initialOffset = selectedEmployee.getOffset();
                newPerson = null;
            }
        }
        else if (action.equals("released")){
            Employee targetEmployee = findEmployee(x, y, organisation); 

            // acting on an employee in the tree
            if (selectedEmployee != null) {
                if (onRetirementIcon(x, y) ){
                    // moving employee to retirement 
                    retirePerson(selectedEmployee);
                }
                else if (targetEmployee == null || targetEmployee==selectedEmployee) { 
                    // repositioning employee
                    selectedEmployee.moveOffset(x);
                }
                else {
                    // Moving existing employee around in the hierarchy.
                    moveEmployee(selectedEmployee, targetEmployee);  
                }
            }
            // acting on the new person
            else if (newPerson != null) {  
                if (targetEmployee != null ) {
                    // Moving new person to hierarchy.
                    addNewPerson(newPerson, targetEmployee);
                    newInitials=null;
                    newRole=null;
                }
            }
            this.redraw();
        }
        else if(action.equals("dragged"))
        {
            if (selectedEmployee != null) {
                Employee targetEmployee = fnidBottomEmployee(x, y, organisation);

                if (onRetirementIcon(x, y) ){
                    selectedEmployee.setOffset(initialOffset); //Move back to start position
                }
                else if (targetEmployee == null || targetEmployee==selectedEmployee) {
                    // repositioning employee
                    selectedEmployee.moveOffset(x); //Move the employee to the mouse
                }
                else {
                    selectedEmployee.setOffset(initialOffset); //Move back to start position
                }
            }
            this.redraw();
        }
    }

    private void saveTree()
    {
        if(organisation != null)
        {
            String fileName = UIFileChooser.save(); //Get file name
            if(fileName == null) return;

            String err = FileManager.saveTree(new File(fileName), organisation); //Save the tree and get result

            //If errors occurred, print them out
            if(err != null)
                UI.println("Error saving tree: " + err);
        }
    }

    /**
     * Loads an organisation tree from a file
     */
    private void loadTree()
    {
        String fileName = UIFileChooser.open(); //Get file name
        if(fileName == null) return;
        organisation = FileManager.loadTree(new File(fileName)); //Load organisation
        redraw();
    }

    /**
     * Find and return an employee that is currently placed over the position (x,y). 
     * Must do a recursive search of the subtree whose root is the given employee.
     * Returns an Employee if it finds one,
     * Returns null if it doesn't.
     * [Completion:] If (x,y) is on two employees, it should return the top one.
     */
    private Employee findEmployee(double x, double y, Employee empl){
        if (empl.on(x, y)) {   // base case: (x,y) is on root of subtree
            return empl;  
        }
        else {  // look further in the subtree
            Employee onEmpl = null;
            for(Employee subEmpl : empl.getTeam())
            {
                Employee temp = findEmployee(x, y, subEmpl);
                if(temp != null)
                    onEmpl = temp;
            }
            return onEmpl; //Returns latest instance of an employee under the mouse
        }
    }

    /**
     * Finds the bottom most employee at the cursor position.
     * Used to find if the cursor is over any employee when dragging
     * @param x Cursor X
     * @param y Cursor Y
     * @param empl Root node to search under
     * @return Employee found if any. Can return null
     */
    private Employee fnidBottomEmployee(double x, double y, Employee empl){
        if (empl.on(x, y)) {   // base case: (x,y) is on root of subtree
            return empl;
        }
        else {  // look further in the subtree
            for(Employee subEmpl : empl.getTeam())
            {
                Employee temp = findEmployee(x, y, subEmpl);
                if(temp != null) //Returns earliest instance of an employee under the mouse
                    return temp;
            }
        }
        return null;
    }

    /**
     * Add the new employee to the target
     * [STEP 2:] If target is not vacant, add new employee to the target's team
     * [STEP 3:] If target is vacant, fill the position with the initials of new employee
     * [COMPLETION:] If the newPerson has a role but no initials, change the role of the target.
     */
    public void addNewPerson(Employee newEmpl, Employee target){
        if ((newEmpl == null) || (target == null)){return;}   //invalid arguments.
        if(target.isVacant())
        {
            target.fillVacancy(newEmpl);
        }
        else
        {
            if(newEmpl.getInitials() == null)
                target.setRole(newEmpl.getRole());
            else
                target.addToTeam(newEmpl);
        }
    }


    /** Move a current employee (empl) to a new position (target)
     *  [STEP 2:] If the target is not vacant, then
     *    add the employee to the team of the target,
     *    (bringing the whole subtree of the employee with them)
     *  [STEP 3:] If the target is vacant, then
     *     make the employee fill the vacancy, and
     *     if the employee was a manager, then make their old position vacant, but
     *     if they were not a manager, just remove them from the tree.
     *        [COMPLETION:]
     *   Moving the CEO is a problem, and shouldn't be allowed. 
     *   In general, moving any employee to a target that is in the
     *   employee's subtree is a problem and should not be allowed. (Why?)
     */
    private void moveEmployee(Employee empl, Employee target) {
        if ((empl == null) || (target == null)){return;}   //invalid arguments.
        if(inSubtree(target, empl)) return; //Check if the target node is under the employee

        if(target.isVacant())
        {
            target.fillVacancy(empl);

            if(empl.isManager())
                empl.makeVacant();
            else
                empl.getManager().removeFromTeam(empl);
        }
        else {
            empl.getManager().removeFromTeam(empl);
            target.addToTeam(empl);
        }
    }
    /** STEP 3
     * Retire an employee.
     * If they are a manager or the CEO, then make the position vacant
     *  (leaving the employee object in the tree, but no initials)
     * If they are not a manager, then remove them from the tree completely.
     */
    public void retirePerson(Employee empl){
        if(empl.isManager()) //If they have any sub tree items
        {
            empl.makeVacant();
        }
        else
        {
            if(empl.getManager() != null)
                empl.getManager().removeFromTeam(empl);
        }
    }

    public void layoutTree(Employee employee)
    {
        measurePos(employee);
        redraw();
    }

    /**
     * Returns the organised width of a node and all its child nodes in a tree
     * @param employee Employee node to measure
     * @return The dimensions and offset of the nodes
     */
    public Measurement measurePos(Employee employee)
    {
        if(!employee.isManager())
            return new Measurement(-Employee.WIDTH / 2, Employee.WIDTH / 2); //Dimensions relative to centre of node

        Measurement lastMeasurement = null;

        //Organise team
        for(Employee employee1 : employee.getTeam())
        {
            if(lastMeasurement == null) { //First employee in the team
                employee1.setOffset(0);
                lastMeasurement = measurePos(employee1);
            }
            else
            {
                Measurement currentMeasurement = measurePos(employee1); //Recursive measurement
                currentMeasurement.offset = lastMeasurement.offset + lastMeasurement.max + Math.abs(currentMeasurement.min) + LAYOUT_MARGIN;
                employee1.setOffset(currentMeasurement.offset);
                lastMeasurement = currentMeasurement; //Save last measurement
            }
        }

        //Find the final dimensions of the node after organising the children
        double max=0, min=0;
        for(Employee employee1 : employee.getTeam()) {
            double emin = employee1.getOffset() - (Employee.WIDTH / 2); //Left most edge of node
            double emax = employee1.getOffset() + (Employee.WIDTH / 2); //Right most edge of node

            //Find the most extreme values
            if(emin < min) min = emin;
            if(emax > max) max = emax;
        }

        return new Measurement(min, max);
    }

    /** (COMPLETION)
     *        Return true if person is in the subtree, and false otherwise
     *        Uses == to determine node equality
     *  Check if person is the same as the root of subTree
     *  if not, check if in any of the subtrees of the team members of the root
     *   (recursive call, which must return true if it finds the person)
    */
    private boolean inSubtree(Employee person, Employee subTree) {
        if (subTree==organisation) { return true; }  // first simple case!!
        if (person==subTree)       { return true; }  // second simple case!!

        //Depth first search
        for(Employee subEmpl : subTree.getTeam())
        {
            if(inSubtree(person, subEmpl)) //Recursive call
                return true;
        }
    
        return false;
    }
    


    // Drawing the tree  =========================================
    /**
     * Redraw the chart.
     */
    private void redraw() {
        UI.clearGraphics();
        drawTree(organisation);
        drawNewIcon();
        drawRetireIcon();
    }

    /** [STEP 1:]
     *  Recursive method to draw all nodes in a subtree, given the root node.
     *        (The provided code just draws the root node;
     *         you need to make it draw all the nodes.)
     */
    private void drawTree(Employee empl) {
        empl.draw();

        //Draw all children
        for(Employee child : empl.getTeam())
            drawTree(child); //Recursive call
    }

    // OTHER DRAWING METHODS =======================================
    /**
     * Redraw the new Person box
     */
    private void drawNewIcon(){
        UI.setColor((newInitials==null)?Employee.V_BACKGROUND:Employee.BACKGROUND);
        UI.fillRect(NEW_LEFT,NEW_TOP,Employee.WIDTH, Employee.HEIGHT);
        UI.setColor(Color.black);
        UI.drawRect(NEW_LEFT,NEW_TOP,Employee.WIDTH, Employee.HEIGHT);
        UI.drawString((newInitials==null)?"--":newInitials, NEW_LEFT+5, NEW_TOP+12);
        UI.drawString((newRole==null)?"--":newRole, NEW_LEFT+5, NEW_TOP+26); 
    }

    /**
     * Redraw the retirement Icon
     */
    private void drawRetireIcon(){
        UI.setColor(Color.red);
        UI.setLineWidth(5);
        UI.drawOval(ICON_X-ICON_RAD, ICON_Y-ICON_RAD, ICON_RAD*2, ICON_RAD*2);
        double off = ICON_RAD*0.68;
        UI.drawLine((ICON_X - off), (ICON_Y - off), (ICON_X + off), (ICON_Y + off));
        UI.setLineWidth(1);
        UI.setColor(Color.black);
    }

    /** is the mouse position on the New Person box */
    private boolean onNewIcon(double x, double y){
        return ((x >= NEW_LEFT) && (x <= NEW_LEFT + Employee.WIDTH) &&
                (y >= NEW_TOP) && (y <= NEW_TOP + Employee.HEIGHT));
    }

    /** is the mouse position on the retirement icon */
    private boolean onRetirementIcon(double x, double y){
        return (Math.abs(x - ICON_X) < ICON_RAD) && (Math.abs(y - ICON_Y) < ICON_RAD);
    }
    

    // Testing ==============================================
    /**
     * Makes an initial tree so you can test your program
     */
    private void makeTestTree(){
        organisation = new Employee("AA", "CEO");
        Employee aa = new Employee("AS", "VP1");
        Employee bb = new Employee("BV", "VP2");
        Employee cc = new Employee("CW", "VP3");
        Employee dd = new Employee("DM", "VP4");
        Employee a1 = new Employee("AF", "AL");
        Employee a2 = new Employee("AH", "AL");
        Employee b1 = new Employee("BK", "AS");
        Employee b2 = new Employee("BL", "DPA");
        Employee d1 = new Employee("CX", "DBP");
        Employee d2 = new Employee("CY", "SEP");
        Employee d3 = new Employee("CZ", "MSP");

        organisation.addToTeam(aa); aa.setOffset(-160);
        organisation.addToTeam(bb); bb.setOffset(-50);
        organisation.addToTeam(cc); cc.setOffset(15);
        organisation.addToTeam(dd); dd.setOffset(120);

        aa.addToTeam(a1); a1.setOffset(-25);
        aa.addToTeam(a2); a2.setOffset(25);
        bb.addToTeam(b1); b1.setOffset(-25);
        bb.addToTeam(b2); b2.setOffset(25);
        dd.addToTeam(d1); d2.setOffset(-50);
        dd.addToTeam(d2); 
        dd.addToTeam(d3); d3.setOffset(50);

        this.redraw();
    }

    //* Test for printing out the tree structure, indented text */
    private void printTree(Employee empl, String indent){
        UI.println(indent+empl+ " " +
                   (empl.getManager()==null?"noM":"hasM") + " " +
                   empl.getTeam().size()+" reports");
        String subIndent = indent+"  ";
        for (Employee tm : empl.getTeam()){
            printTree(tm, subIndent);
        }
    }

    /**
     * Uses Java Swing to display a window where the user can create a new employee.
     */
    public void showAddEmplWindow()
    {
        JFrame frame = new JFrame();
        frame.setPreferredSize(new Dimension(300, 200));

        JPanel container = new JPanel(new GridBagLayout()); //New container that uses grid bag layout
        GridBagConstraints c = new GridBagConstraints(); //Create position constraint object for child elements

        JTextField initialsBox = new JTextField();
        JComboBox<String> roleBox = new JComboBox<>(new String[] {"CEO", "CTO", "CO", "VP", "AL", "AS", "DPA", "DBP", "SEP", "MSP"}); //Creates combo box with preset roles
        JSpinner roleNumberBox = new JSpinner();
        JCheckBox noInitialCheckBox = new JCheckBox("Role only/No initials");

        JPanel rolePanel = new JPanel();
        rolePanel.add(roleBox);
        rolePanel.add(roleNumberBox);

        //Event handler to disable then initials input when the check box is checked
        noInitialCheckBox.addItemListener((ItemEvent e) -> {
            if(e.getStateChange() == ItemEvent.DESELECTED)
                initialsBox.setEnabled(true);
            else if(e.getStateChange() == ItemEvent.SELECTED)
                initialsBox.setEnabled(false);
        });

        //Set dimensions
        initialsBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        rolePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        roleNumberBox.setMinimumSize(new Dimension(1, 1)); //Prevents glitch with 0 min size
        roleNumberBox.setPreferredSize(new Dimension(40, 22));
        noInitialCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel titleLabel = new JLabel("Add employee:");
        JLabel initialLabel = new JLabel("Initials:"); //new label for nickname
        JLabel roleLabel = new JLabel("Role:"); //new label for username


        //Labels
        c.insets = new Insets(3,3,3,3); //add padding
        c.fill = GridBagConstraints.NONE; //Dont autosize label
        c.anchor = GridBagConstraints.WEST; //Align left
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0; //Dont weight size because its not autosize
        container.add(titleLabel, c);
        c.gridy = 1;
        container.add(initialLabel, c);
        c.gridy = 2;
        container.add(roleLabel, c);

        //Inputs
        c.fill = GridBagConstraints.HORIZONTAL; //Scale input boxes
        c.gridx = 1;
        c.weightx = 1; //Weight auto size to max available space
        c.gridy = 1;
        container.add(initialsBox, c);
        c.gridy = 2;
        container.add(rolePanel, c);
        c.gridy = 3;
        container.add(noInitialCheckBox, c);

        //Buttons
        c.gridx = 1;
        JButton submitButton = new JButton("Add");
        c.gridy = 5;
        container.add(submitButton, c);

        //Event listener for add button click
        submitButton.addActionListener((ActionEvent) -> {
            //Set initials
            if(noInitialCheckBox.isSelected())
                newInitials = null;
            else
                newInitials = initialsBox.getText();

            //Set role
            newRole = (String)roleBox.getSelectedItem();

            //If role number is not 0, append it to the new role
            int roleNum = (int)roleNumberBox.getValue();
            if(roleNum != 0)
                newRole += roleNum;

            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //Close window

            redraw(); //Redraw the organisation window
        });


        c.gridx = 0;
        JButton closeButton = new JButton("Cancel");
        container.add(closeButton, c);

        //Event listener for cancel button click
        closeButton.addActionListener((ActionEvent) -> {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)); //Close window
        });


        frame.add(container);
        frame.pack(); //Pack the GUI elements into the window
        frame.setVisible(true); //Make the window visible
    }

    // Main
    public static void main(String[] arguments) {
        new OrganisationChart();
    }        

}
