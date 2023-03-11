import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

/**Author: Matthew Currie, Jefferson Adjetey
 * Date: 6/1/2021
 * CS10 Spring 2021
 */
/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, updated Fall 2016
 */
// serializable class allows for client server interaction
public class Rectangle implements Shape, Serializable {
	private int x1;
	private int x2;
	private int y1;
	private int y2;
	private Color color;

	// recreates rectangle

	public void setCorners(int x1, int y1, int x2, int y2){
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
	}

	// rectangle with a single point

	public Rectangle(int x, int y, Color color){
		this.x1 = x;    this.x2 = x;
		this.y1 = y;    this.y2 = y;
		this.color = color;
	}

	// rectangle with two corners

	public Rectangle (int x1, int y1, int x2, int y2, Color color){
		setCorners(x1, y1, x2, y2);
		this.color = color;
	}

	@Override
	public void moveBy(int dx, int dy) {
		x1 += dx;
		y1 += dy;
		x2 += dx;
		y2 += dy;

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
	//check if set of points is within a given rectangle
	public boolean contains(int x, int y) {
		if ((x >= x1 && x <= x2) && (y >= y1 && y <= y2)){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x1, y1, x2-x1, y2-y1);
	}

	public String toString() {
		return "ellipse "+x1+" "+y1+" "+x2+" "+y2+" "+color.getRGB();
	}
}