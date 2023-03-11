import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

// Handles editor information to/from server

/**Author: Matthew Currie, Jefferson Adjetey
 * Date: 6/1/2021
 * CS10 Spring 2021
 */
/**
 * Handles communication to/from the server for the editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	ObjectInputStream clientInputStream;  // read object from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
			clientInputStream = new
					ObjectInputStream(sock.getInputStream());
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	// draws shape based on shapetype

	public static Shape drawShape(String shapeType, int x1, int y1, int x2, int y2, ArrayList<Point> arr, Color color){
		if (shapeType.equals("ellipse")){ // if shape type is ellipse, return an ellipse
			return new Ellipse(x1, y1, x2, y2, color);
		}
		else if (shapeType.equals("rectangle")){ // if shape type is rectangle, return a rectangle
			return new Rectangle(x1, y1, x2, y2, color);
		}
		else if (shapeType.equals("segment")){ // if shape type is segment, return a segment
			return new Segment(x1, y1, x2, y2, color);
		}

		else if (shapeType.equals("freehand")){ // if shape type is freehand, return a polyline
			return new Polyline(x1, y1, arr, color);
		}
		else{
			return null;
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			try{
				// read in the state of the sketch from the server
				Sketch sketch_state = (Sketch) clientInputStream.readObject();
				editor.setSketch(sketch_state);
			}
			catch(Exception e){ // print an error if we can't read in the state
				System.out.println(e.getMessage());
			}

			String line;
			while((line = in.readLine())!=null){
				String[] input = line.split("\\s+");
				if (input[0].equals("DRAW")){ // if in draw mode, parse string for the contents of the shape
					if (input[1].equals("polyline")){
						int x1 = Integer.parseInt(input[2]);
						int y1 = Integer.parseInt(input[3]);
						// unsure of how to parse arraylist of points
						int RGB = Integer.parseInt(input[5]);
						Color color = new Color(RGB);
						//Shape shape = drawShape(input[7], x1, y1, null, null,null, color); // create a new shape with the information
						//editor.getSketch().putShape(shape); // update the sketch

					}

					int x1 = Integer.parseInt(input[2]);
					int y1 = Integer.parseInt(input[3]);
					int x2 = Integer.parseInt(input[4]);
					int y2 = Integer.parseInt(input[5]);
					int RGB = Integer.parseInt(input[6]);

					Color color = new Color(RGB);
					Shape shape = drawShape(input[7], x1, y1, x2, y2,null, color); // create a new shape with the information
					editor.getSketch().putShape(shape); // update the sketch
				}


				else if(input[0].equals("MOVE")){ // if in move mode, parse string for information on how we will update
					                              // the shape's position
					int dx = Integer.parseInt(input[1]);
					int dy = Integer.parseInt(input[2]);
					int id = Integer.parseInt(input[3]);
					Shape shape = editor.getSketch().getShapes().get(id); // get shape being moved
					shape.moveBy(dx, dy); // update shape's position
				}


				else if(input[0].equals("DELETE")){ // if in delete mode, get the id of the shape
					int id = Integer.parseInt(input[1]);
					editor.getSketch().removeShape(id); // delete shape
				}


				else if(input[0].equals("RECOLOR")){ // if in recolor mode, get the id of the shape
					int RGB = Integer.parseInt(input[2]);
					int id =  Integer.parseInt(input[1]);
					Color c = new Color(RGB);
					if (editor.getSketch().getShapes().containsKey(id)) { // if there is a shape with the id, recolor it
						editor.getSketch().getShapes().get(id).setColor(c);
					}
				}
				editor.repaint();    // repaint canvas
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}	

	
}
