/**Author: Matthew Currie, Jefferson Adjetey
 * Date: 6/1/2021
 * CS10 Spring 2021
 */

// Class to maintain sketches

import java.io.Serializable;
import java.util.*;
import java.awt.*;

// serializable class allows for client server interaction
public class Sketch implements Serializable {
    private int id = 1;
    private NavigableMap<Integer, Shape> shapes;  // navigable map allows us to access elements in descending or ascending order

    // constructor for synchronized navigable map
    public Sketch(){
        this.shapes = Collections.synchronizedNavigableMap(new TreeMap<Integer, Shape>()) ;
        // map has shape id as key and shape as value
    }

    // assign id to added shapes
    public synchronized void putShape(Shape shape){
        shapes.put(id, shape);
        id++;
    }
    // remove shape with with a given id
    public synchronized void removeShape(int id){
        shapes.remove(id);
    }

    // return id of a shape with which the mouse is pointing to
    public int getID(Point p){
        for (Integer shapeID : shapes.descendingKeySet()) { // for each shape in the keyset starting from the top
            if (shapes.get(shapeID).contains((int) p.getX(), (int) p.getY())) { // check if the shape contains the x and y of point P
                return shapeID; // return shape's id
            }
        }
        return -1; // if there is no shape return -1 since ids start at 0
    }

    // synchronized getter
    public synchronized NavigableMap<Integer, Shape> getShapes(){
        return shapes;
    }

    // synchronized draw method
    public synchronized void draw(Graphics g){
        for (Integer shapeID: shapes.navigableKeySet()){ //navigable keySet allows drawing of shapes based on first drawn
            shapes.get(shapeID).draw(g);
        }
    }

}