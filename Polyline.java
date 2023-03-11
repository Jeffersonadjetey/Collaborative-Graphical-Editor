import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**Author: Matthew Currie, Jefferson Adjetey
 * Date: 6/1/2021
 * CS10 Spring 2021
 */

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
// serializable class allows for client server interaction
public class Polyline implements Shape, Serializable {
	// TODO: YOUR CODE HERE
	public int x1, y1; // starting points of polyline shape
	public static ArrayList<Point> arr; // arraylist of points within the polyline
	private Color color;

	public Polyline(int x1, int y1, Color color){ // initial point of polyline
		this.x1 = x1;
		this.y1 = y1;
		this.color = color;
	}

	public Polyline(int x1, int y1, ArrayList<Point> pointArrayList, Color color){ // full polyline with a list of its points
		this.x1 = x1; this.y1 = y1;
		arr = pointArrayList; // set the static list to the list of points passed in
		this.color = color;

	}


	@Override
	public void moveBy(int dx, int dy) {
		for(int i=0;i<arr.size()-1;i++){ // iterate through list of points within the polyline to update location
			int tempx = (int)arr.get(i).getX();
			int tempy = (int)arr.get(i).getY();
			tempx += dx;
			tempy += dy;
			arr.get(i).setLocation( (double) tempx, (double) tempy);
		}

	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public boolean contains(int x, int y) {
		for(int i=0;i<arr.size()-1;i++){// for each line in the polyline, check if the given point is contained
			if(Segment.pointToSegmentDistance(x, y, (int)arr.get(i).getX(), (int)arr.get(i).getY(), (int)arr.get(i+1).getX(), (int)arr.get(i+1).getY()) <= 3){
				return true;
			}
		} return false;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		for(int i=0;i<arr.size()-1;i++){ // for each point, draw a line from it to the next point in the list
			g.drawLine((int)arr.get(i).getX(), (int)arr.get(i).getY(), (int)arr.get(i+1).getX(), (int)arr.get(i+1).getY() );
		}
	}

	@Override
	public String toString() {
		return "polyline "+x1+" "+y1+"  " + arr + color.getRGB();
	}

}
