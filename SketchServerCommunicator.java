import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**Author: Matthew Currie, Jefferson Adjetey
 * Date: 6/1/2021
 * CS10 Spring 2021
 */

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for
	ObjectOutputStream serverOutputStream;  // sends object from server to client

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
			serverOutputStream = new ObjectOutputStream(sock.getOutputStream());
		}
		catch(IOException e){
			System.out.println(e.getMessage());
		}
	}

	// helper method that updates server sketch when messaged by client, similar to editor communicator
	public void updateSketch(String line){
		String[] input = line.split("\\s+");

		if (input[0].equals("DRAW")){ // if in draw mode, parse string for the contents of the shape
			if (input[1].equals("polyline")){
				int x1 = Integer.parseInt(input[2]);
				int y1 = Integer.parseInt(input[3]);
				// unsure of how to parse arraylist of points
				int RGB = Integer.parseInt(input[5]);
				Color color = new Color(RGB);
			}

			// parse string in order to create shape
			int x1 = Integer.parseInt(input[2]);
			int y1 = Integer.parseInt(input[3]);
			int x2 = Integer.parseInt(input[4]);
			int y2 = Integer.parseInt(input[5]);
			int RGB = Integer.parseInt(input[6]);

			Color color = new Color(RGB);
			Shape shape = EditorCommunicator.drawShape(input[7], x1, y1, x2, y2, null, color); // create a new shape with the information
			server.getSketch().putShape(shape); // update the sketch
		}
		else if(input[0].equals("MOVE")){ // if in move mode, parse string for information on how we will update
			// the shape's position
			int dx = Integer.parseInt(input[1]);
			int dy = Integer.parseInt(input[2]);
			int id = Integer.parseInt(input[3]);
			Shape shape = server.getSketch().getShapes().get(id); // get shape being moved
			shape.moveBy(dx, dy); // update shape's position
		}
		else if(input[0].equals("DELETE")){ // if in delete mode, get the id of the shape
			int id = Integer.parseInt(input[1]);
			server.getSketch().removeShape(id); // delete shape
		}
		else if(input[0].equals("RECOLOR")){ // if in recolor mode, get the id of the shape
			int RGB = Integer.parseInt(input[2]);
			int id =  Integer.parseInt(input[1]);
			Color c = new Color(RGB);

			if (server.getSketch().getShapes().containsKey(id)){ // if there is a shape with the id, recolor it
				server.getSketch().getShapes().get(id).setColor(c);
			}
		}
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			serverOutputStream.writeObject(server.getSketch());
			System.out.println(server.getSketch().getShapes());
			String line;
			while((line = in.readLine())!= null) {

				updateSketch(line);     // update server sketch for each request from client

				server.broadcast(line); // broadcast request to every client

			}

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


}
