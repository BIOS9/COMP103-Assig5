/* Code for COMP103 - 2018T2, Assignment 5
 * Name: Matthew Corfiatis
 * Username: CorfiaMatt
 * ID: 300447277
 */

/**
 * Stores information about dimensions and offsets of branches in the organisation tree
 */
public class Measurement
{
    public double min = 0, max = 0, offset = 0;

    public Measurement(double min, double max)
    {
        this.min = min;
        this.max = max;
    }
}