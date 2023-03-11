/**Author: Matthew Currie, Jefferson Adjetey
 * Date: 6/1/2021
 * CS10 Spring 2021
 */


import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

// Handles GUI-Based Drawing Interaction
/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged

	public ArrayList<Point> arr = new ArrayList<>();


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	//Setter for the sketch instance variable which helps us update the client to the current state of the world
	public synchronized void setSketch(Sketch sketch){
		this.sketch = sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
		sketch.draw(g);
		if (curr != null) {
			curr.draw(g);
		}
	}

	// methods to send messages to the server to switch modes

	public String drawShape(Shape s){ // message to switch to draw mode
		return ("DRAW "+ s.toString()+" "+ shapeType);
	}

	public String moveShape(int dx, int dy, int id) { // message to switch to move mode
		return ("MOVE " +dx+" "+ dy +" "+id);
	}

	public String colorShape(int id, Color c) { // message to switch to recolor mode
		return ("RECOLOR "+id+" "+c.getRGB());
	}

	public String deleteShape(int id) { // message to switch to delete mode
		return ("DELETE " + id);
	}

	// Helpers for event handlers
	
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// TODO: YOUR CODE HERE
		// press at point in drawing mode
		if (mode == Mode.DRAW) {
			// draw new ellipse from point
			if (shapeType.equals("ellipse")){
				curr = new Ellipse((int) p.getX(), (int) p.getY(), color);
				drawFrom = p;
			}
			// draw new rectangle from point
			else if(shapeType.equals("rectangle")) {
				curr = new Rectangle((int) p.getX(), (int) p.getY(), color);
				drawFrom = p;
			}
			// draw new segment from point
			else if(shapeType.equals("segment")) {
				curr = new Segment((int) p.getX(), (int) p.getY(), color);
				drawFrom = p;
			}
			// draw new freehand from point
			else if(shapeType.equals("freehand")){
				curr = new Polyline((int) p.getX(),(int) p.getY(), color);
				drawFrom = p;
				System.out.println(curr);
			}

		}

		// request to drag shape at point p
		else if (mode == Mode.MOVE) {
			movingId = sketch.getID(p);  // get id of shape at p
			moveFrom = p; // drag shape at point p

		}

		// request to change clicked shape's color at point p
		else if (mode == Mode.RECOLOR) {
			int id = sketch.getID(p);    			  // get id of shape at p
			if (sketch.getShapes().containsKey(id)){ // if the sketch has a shape with that id, continue
				comm.send(colorShape(id, color));      // tell server to recolor shape and update clients

			}

		}
		// request to delete clicked shape
		else if (mode == Mode.DELETE) {
			int id = sketch.getID(p);                  // get the id of shape at p
			if (sketch.getShapes().containsKey(id)) {  // if the sketch has a shape with that id, continue
				comm.send(deleteShape(id));                 // tell server to delete shape and update clients

			}

		}
		repaint(); // paint canvas
	}

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE
		if (mode == Mode.DRAW) { // only run when we're in drawing mode
			if (drawFrom != null) { // only run if the drawing has been started

				// check shapeType and draw based on it

				if (shapeType.equals("ellipse")) {
					curr = new Ellipse((int) drawFrom.getX(), (int) drawFrom.getY(), (int) p.getX(), (int) p.getY(), color);
				}
				else if (shapeType.equals("rectangle")) {
					curr = new Rectangle((int) drawFrom.getX(), (int) drawFrom.getY(), (int) p.getX(), (int) p.getY(), color);
				}
				else if (shapeType.equals("segment")) {
					curr = new Segment((int) drawFrom.getX(), (int) drawFrom.getY(), (int) p.getX(), (int) p.getY(), color);
				}
				else if (shapeType.equals("freehand")) {
					arr.add(p); // add points to arraylist while dragging
					curr = new Polyline((int) drawFrom.getX(),(int) drawFrom.getY(), arr, color );
				}
			}

		}
		else if(mode == Mode.MOVE) { // check if we're in move mode
			if (moveFrom != null && movingId != -1) {   // if mouse has pressed a shape
				// tell the server to update the shape's position
				comm.send(moveShape((int) (p.getX() - moveFrom.getX()), (int)(p.getY() - moveFrom.getY()), movingId));  //send message to message to server to move the shape accordingly and update other connected clients
				moveFrom = p;

			}
		}
		repaint(); // paint to canvas
	}



	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease() {
		// TODO: YOUR CODE HERE
		if (mode == Mode.DRAW){ // check if in drawing mode
			// if released in drawing mode, message server to update client with the shape
			comm.send(drawShape(curr));
			curr = null;
		}
		else if(mode == Mode.MOVE){ // if in move mode
			// if released in move mode, release shape
			moveFrom = null; // shape is no longer being dragged
			movingId = -1; // no longer need the shape's id
		}
		repaint();
	}


	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() { // first editor for testing
			public void run() {
				new Editor();
			}
		});

	}
}
