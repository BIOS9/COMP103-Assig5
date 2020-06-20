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
import java.awt.Color;

/**
 * Represents an employee in an organisation
 * Normal employees have initials and a role (and could have further information)
 * All employees (other than the CEO) have a manager, who is another employee
 * Any employee who is a manager will have a set of employees in their team.
 *  (If an employee has an empty team, then they aren't a manager.)
 * An employee object can also represent a vacant position in the organisation
 *  (a kind of "virtual employee"), represented by an employee with no name or role.
 *  A vacant position still has a manager and may have a team of employees under it.
 *  When a vacant position is filled, the initials and role will be filled in, turning
 *  the employee object into a regular employee.
 *
 * An employee is drawn as a rectangle containing their initials and role.
 * Every employee (except the CEO) will be drawn with a link to their manager.
 * The position where a team member of a manager is drawn is one layer down
 *  from the manager, at a horizontal position specified by
 *  the employee's offset - how far to the right (or left) of the manager.
 *  This means that if the manager is moved around on the screen, the team
 *  members (and their team members, and ....) will automatically move with them.
 */

public class Employee {

    // Fields
    private String initials;
    private String role;
    private Employee manager;
    private Set<Employee> team = new HashSet<Employee>();
    private double offset;  // horizontal offset relative to the manager
                            // negative value to the left of the manager, positive value to the right

    // constant for drawing a person
    public static final double WIDTH = 40;   // width of the box representing the employee
    public static final double HEIGHT = 30;  // height of the box representing the employee
    public static final double V_SEP = 20;   // vertical separation between layers
    public static final double ROOT_TOP = 50;
    public static final double ROOT_X = 550;
    public static final Color BACKGROUND = new Color(255, 255, 180);
    public static final Color V_BACKGROUND = new Color(250, 255, 255);
    
    // Constructors

    /**
     * Construct a new Employee object with the given initials and role
     */
    public Employee(String initials, String role) {
        this.initials = initials;
        this.role = role;
    }

    /**
     * Construct a new Employee object with the given initials, role, and offset
     * Useful for loading from file 
     */
    public Employee(String initials, String role, double offset) {
        this.initials = initials;
        this.role = role;
        this.offset = offset;
    }


    public String getInitials()
    {
        return initials;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String newRole)
    {
        role = newRole;
    }

    public double getOffset() {
        return offset;
    }

    // Adding and removing members of the team =========================

    /** [STEP 1:]
     * Add a new member to the team managed by this employee, and
     * ensure that the new team member has this employee as their manager
     */
    public void addToTeam(Employee empl){
        if (empl == null) return;
        team.add(empl);
        empl.manager = this;
    }

    /** [STEP 1:]
     * Remove a member of the team managed by this employee, and
     * ensure that the team member no longer has this employee as their manager
     */
    public void removeFromTeam(Employee empl){
        team.remove(empl);
        empl.manager = null;
    }

    // Simple getters and setters  ==================================
    
    /** Returns the manager of this Employee */
    public Employee getManager()  {return manager;}

    
    /** Returns the set of employees in the team, */
    public Set<Employee> getTeam() {
        return Collections.unmodifiableSet(team);
    }
    //Note: By returning an unmodifiable version of the team, other parts
    // of the program can access and loop through the team,
    // but cannot add or remove employee from the team

    /**
     * Returns true iff this employee is managing any other employees
     */
    public boolean isManager(){
        return (!team.isEmpty());
    }
    


    /**
     * True if the Employee is an empty position, needing to be filled.
     */
    public boolean isVacant(){
        return (initials == null);
    }

    /**
     * Clear initials of the employee,
     * but leave the manager and team members.
     * Effectively makes this an empty position within the hierarchy,
     */
    public void makeVacant(){
        initials = null;
    }

    /**
     * Fill a vacant position by copying the employee details (initials)
     * from emp into this employee object
     * If the position is not vacant, does nothing.
     */
    public void fillVacancy(Employee emp){
        if (isVacant()){
            this.initials = emp.initials;
        }
    }

    /**
     * Move the value offset so that the Employee will be drawn at position x 
     * Offset specifies where to draw the employee, relative to their manager.
     * Offset is the distance to the right (or left, if negative) of the manager's position
     */
    public void moveOffset(double x){
        if (manager == null) { // this must be the CEO
            offset = x - ROOT_X;
        }
        else {
            offset=x - manager.getX();
        }
    }

    /**
     * Set the offset value (horizontal position of employee relative to manager)
     * Only needed for constructing test hierarchy or loading from a file.
     */
    public void setOffset(double off){
        offset = off;
    }


    /**
     * Return the top of this employee box (internal use only)
     * Calculated 
     */
    private double getTop(){
        if (manager == null) { return ROOT_TOP; }   // this must be the top employee
        return manager.getTop() + HEIGHT + V_SEP;
    }

    /**
     * horizontal center of this employee box (internal use only)
     * Recursive method, to compute center from the offset and the center of the manager.
     */
    private double getX(){
        if (manager == null) {  // this must be the CEO
            return ROOT_X + offset;
        }
        else {
            return manager.getX() + offset;
        }
    }

    /**
     * Returns true iff the point (x,y) is on top of where
     *  this employee is currently drawn.
     */
    public boolean on(double x,double y){
        return (Math.abs(getX()-x)<=WIDTH/2 && (y >= getTop()) && (y <= getTop()+HEIGHT) );
    }
    
    /**
     * Returns a string containing the details of an employee.
     * if the initials are null, then will be given as "??"
     */
    public String toString() {
        return String.format("%-3s %s",
                             (initials==null?"??":initials),
                             (role==null?"":role));
    }

    /**
     * Returns a string containing the details of an employee, plus
     * offset and number of members of their team.
     * initials and role may be "NULL"
     * May be useful for saving to files
     */
    public String toStringFull() {
        return ((initials==null||initials.equals("")?"NULL":initials) +" "+
                (role==null||role.equals("")?"NULL":role) +" "+
                (int)(offset) +" "+team.size());
    }

    /**
     * Draw a box representing the Employee, and 
     * connect it to its manager (if there is a manager)
     */
        
    public void draw(){
        double left=getX()-WIDTH/2;
        double top=getTop();
        UI.setColor(isVacant()?V_BACKGROUND:BACKGROUND);
        UI.fillRect(left,top,WIDTH,HEIGHT);
        UI.setColor(Color.black);
        UI.drawRect(left,top,WIDTH,HEIGHT);
        UI.drawString((initials==null)?"??":initials, left+5, top+12);
        UI.drawString((role==null)?"--":role, left+5, top+26); 
        if (manager != null) {
            // vertical line
            double x1 = manager.getX();
            double y1 = manager.getTop() + HEIGHT;
            double yMid = y1 + V_SEP/2;

            double x2 = x1 + offset;
            double y2 = y1 + V_SEP;

            UI.drawLine(x1, y1, x1, yMid);    // vertical
            UI.drawLine(x1, yMid, x2, yMid);  // horizontal
            UI.drawLine(x2, yMid, x2, y2);    // vertical
        }
        
    }

}
