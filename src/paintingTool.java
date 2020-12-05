import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

@SuppressWarnings("serial")
public class paintingTool extends JFrame {
	JMenuBar menubar;

	JButton brushBut, pencilBut, eraserBut, lineBut, ellipseBut, rectBut, strokeBut, fillBut, clearBut, undoBut;

	// Slider used to change the transparency
	JSlider transSlider;
	JSlider brushSlider;

	JLabel transLabel;
	JLabel brushLabel;

	// Makes sure the float for transparency only shows 2 digits
	DecimalFormat dec = new DecimalFormat("#.##");

	// Contains all of the rules for drawing
	Graphics2D graphSettings;

	// Change the stroke dynamically with a component
	// graphSettings.setStroke(new BasicStroke(5F));

	// used to monitor what shape to draw next
	int currentAction = 1;
	boolean imgLoaded = false;
	// Transparency of the shape
	// 1.0f transparency off
	float transparentVal = 1.0f;
	int brushVal = 10;
	// Default stroke and fill colors
	Color strokeColor = Color.BLACK, fillColor = Color.BLACK;
	Color bgColor = Color.white;
	private boolean dragging;
	private Graphics graphicsForDrawing;
	BufferedImage srcImg;
	private int strokeSize;

	private ArrayList<drawPath> brushPaths = new ArrayList<>();
	// ArrayLists that contain each shape drawn along with
	// that shapes stroke and fill
	ArrayList<Shape> shapes = new ArrayList<Shape>();
	ArrayList<Color> shapeFill = new ArrayList<Color>();
	ArrayList<Color> shapeStroke = new ArrayList<Color>();
	ArrayList<Float> transPercent = new ArrayList<Float>();
	ArrayList<Integer> strokeSizes = new ArrayList<Integer>();
	ArrayList<Float> brushPercent = new ArrayList<Float>();

	ArrayList<Shape> backup = new ArrayList<Shape>();

	int i, j, k, l, m, n, o;

	public static void main(String[] args) {
		new paintingTool();
	}

	// to keep track of shapes to be removed from backward
	public void set() {
		i = shapes.size();
		j = shapeFill.size();

//		System.out.println("shapeStroke size: " + shapeStroke.size());
//		System.out.println("shapeFill size: " + shapeFill.size());

	}

	public paintingTool() {

		// Define the defaults for the JFrame
		this.setSize(1000, 1000);
		this.setTitle("Java Paint");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setJMenuBar(menubar);

		JMenuBar menuBar = new JMenuBar();

		// Add the menubar to the frame
		setJMenuBar(menuBar);

		// Define and add two drop down menu to the menubar
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		// Create and add simple menu item to one of the drop down menu
		JMenuItem newAction = new JMenuItem("New Project");
		JMenuItem openAction = new JMenuItem("Open File");
		JMenuItem saveAction = new JMenuItem("Save");

		fileMenu.add(newAction);
		fileMenu.add(openAction);
		fileMenu.add(saveAction);



		// Creating New Project
		newAction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				new paintingTool().setVisible(true);

			}
		});

		JPanel buttonPanel = new JPanel();

		// Swing box that will hold all the buttons horizontally
		Box boxPanel = Box.createHorizontalBox();

		// Make all the buttons in toolButton by passing the
		// button icon.
		brushBut = toolButton("./src/brush.png", 1);
		pencilBut = toolButton("./src/pencil.png", 2);
		eraserBut = toolButton("./src/eraser.png", 3);
		lineBut = toolButton("./src/line.png", 4);
		ellipseBut = toolButton("./src/ellipse.png", 5);
		rectBut = toolButton("./src/rectangle.png", 6);
		clearBut = toolButton("./src/clear.png", 9);
		undoBut = toolButton("./src/undo.png", 10);

		// Make all the buttons in colorEditButton by passing the
		// button icon and true for stroke color or false for fill

		strokeBut = colorEditButton("./src/stroke.png", 7, true);
		fillBut = colorEditButton("./src/fill.png", 8, false);

		
		// 
		
		brushBut.setToolTipText("Draw with a brush");
		pencilBut.setToolTipText("Draw with a pencil");
		eraserBut.setToolTipText("Erase what's wrong");
		lineBut.setToolTipText("Create Line");
		ellipseBut.setToolTipText("Create Circle shape ");
		rectBut.setToolTipText("Create Rectangle shape");
		clearBut.setToolTipText("Clear Drawing Board");
		undoBut.setToolTipText("Undo Previous Step");
		fillBut.setToolTipText("Chose a color for the shape");
		strokeBut.setToolTipText("Fill Shape with color");

		// Add the buttons to the box panel
		boxPanel.add(brushBut);
		boxPanel.add(pencilBut);
		boxPanel.add(eraserBut);
		boxPanel.add(lineBut);
		boxPanel.add(ellipseBut);
		boxPanel.add(rectBut);
		boxPanel.add(strokeBut);
		boxPanel.add(fillBut);
		boxPanel.add(clearBut);
		boxPanel.add(undoBut);

		// Add the transparent label and slider
		transLabel = new JLabel("Transparent: 1");

		// Add the brush label and slider
		brushLabel = new JLabel("Brush Size: 10");

		// Min value, Max value and starting value for slider
		transSlider = new JSlider(1, 100, 100);
		brushSlider = new JSlider(1, 10, 10);

		// Create an instance of ListenForEvents to handle events
		ListenForSlider sliderHandler = new ListenForSlider();

		// creating class listenForSlider that will make an alert when an event
		// occurs on the slider
		transSlider.addChangeListener(sliderHandler);
		brushSlider.addChangeListener(sliderHandler);

		boxPanel.add(transLabel);
		boxPanel.add(transSlider);

		boxPanel.add(brushLabel);
		boxPanel.add(brushSlider);
		// Add the box of buttons to the panel
		buttonPanel.add(boxPanel);

		// Position the buttons in the bottom of the frame
		this.add(buttonPanel, BorderLayout.NORTH);

		// Make the drawing area take up the rest of the frame
		// this.add(new DrawingBoard(), BorderLayout.CENTER);
		final DrawingBoard drawPanel = new DrawingBoard();
		this.add(drawPanel, BorderLayout.CENTER);
		this.getContentPane().setBackground(Color.WHITE);

		// saving image as png
		saveAction.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				BufferedImage image = new BufferedImage(drawPanel.getWidth(), drawPanel.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = image.createGraphics();
				drawPanel.paint(g);
				g.dispose();

				JFileChooser fileChooser = new JFileChooser();
				java.io.File directory = new File("C:/Users/Mimi/Desktop");
				fileChooser.setCurrentDirectory(directory);
				FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG file (*.png)", "png");
				fileChooser.addChoosableFileFilter(pngFilter);
				fileChooser.setFileFilter(pngFilter);

				int status = fileChooser.showSaveDialog(paintingTool.this);

				if (status == JFileChooser.APPROVE_OPTION) {
					try {
						ImageIO.write(image, "png", fileChooser.getSelectedFile());
						JOptionPane.showMessageDialog(null,
								"Image saved to " + fileChooser.getSelectedFile().getName());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}
		});

		openAction.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				JFileChooser fileChooser = new JFileChooser();
				java.io.File directory = new File("C:/Users/Mimi/Desktop");
				// java.io.File directory = new File( "." );

				fileChooser.setCurrentDirectory(directory);
				FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG file (*.png)", "png");
				fileChooser.addChoosableFileFilter(pngFilter);
				fileChooser.setFileFilter(pngFilter);

				int status = fileChooser.showOpenDialog(paintingTool.this);

				if (status == JFileChooser.APPROVE_OPTION) {
					try {
						BufferedImage img = ImageIO.read(fileChooser.getSelectedFile());

						srcImg = img;
						imgLoaded = true;

						// ArrayLists that contain each shape drawn along with
						// that shapes stroke and fill
						shapes.clear();
						shapeFill.clear();
						shapeStroke.clear();
						transPercent.clear();
						strokeSizes.clear();
						brushPercent.clear();
						brushPaths.clear();

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}
		});

		// Show the frame
		this.setVisible(true);

	}

	// Spits out buttons based on the image supplied
	// actionNum represents each shape to be drawn

	public JButton toolButton(String iconFile, final int actionNum) {

		JButton button = new JButton();
		Icon butIcon = new ImageIcon(iconFile);
		button.setIcon(butIcon);

		// Make the proper actionPerformed method execute when the
		// specific button is pressed
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				currentAction = actionNum;
				// set stroke size depending on pencil or brush
				if (currentAction != 2) {
					strokeSize = 5;
				} else {
					strokeSize = 1;
				}

				if (currentAction == 9) {
					shapes.clear();
					shapeFill.clear();
					shapeStroke.clear();
					transPercent.clear();
					strokeSizes.clear();
					brushPercent.clear();
					brushPaths.clear();
					repaint();
				}

				if (currentAction == 10) {
					if (shapes.size() > 0) {
						System.out.println("size: " + shapes.size());
						System.out.println("i: " + i);
						shapes.remove(i);
						repaint();
						i--;
					}
				}
			}
		});

		return button;
	}

	// Spits out buttons based on the image supplied and
	// whether a stroke or fill is to be defined
	public JButton colorEditButton(String iconFile, final int actionNum, final boolean stroke) {

		JButton button = new JButton();
		Icon butIcon = new ImageIcon(iconFile);
		button.setIcon(butIcon);

		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (stroke) {

					// JColorChooser is a popup that lets you pick a color
					strokeColor = JColorChooser.showDialog(null, "Pick a Stroke", Color.BLACK);

				} else {

					fillColor = JColorChooser.showDialog(null, "Pick a Fill", Color.BLACK);
				}
			}
		});

		return button;
	}

	private class DrawingBoard extends JComponent {

		Point drawStart, drawEnd;

		// Monitors events on the drawing area of the frame
		public DrawingBoard() {

			this.addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {
					drawStart = new Point(e.getX(), e.getY());

					if (currentAction != 1 && currentAction != 2 && currentAction != 3) {
						// When the mouse is pressed get x & y position
						drawEnd = drawStart;
						repaint();
					}
					if (currentAction == 2) {
						graphicsForDrawing = getGraphics();
						graphicsForDrawing.setColor(Color.BLACK);
						dragging = true;
					}
					if (currentAction == 1) {
						brushPaths.add(new drawPath(new Point(e.getPoint()), strokeColor, brushVal, transparentVal));
					}

					if (currentAction == 3) {
						brushPaths.add(new drawPath(new Point(e.getPoint()), Color.white, brushVal, 1));
					}

				}

				public void mouseReleased(MouseEvent e) {

					if (currentAction != 1 && currentAction != 2 && currentAction != 3) {
						// Create a shape using the starting x & y
						// and finishing x & y positions
						Shape aShape = null;

						if (currentAction == 4) {

							aShape = drawLine(drawStart.x, drawStart.y, e.getX(), e.getY());

						} else if (currentAction == 5) {

							aShape = drawEllipse(drawStart.x, drawStart.y, e.getX(), e.getY());

						} else if (currentAction == 6) {

							// Create a new rectangle using x & y coordinates
							aShape = drawRectangle(drawStart.x, drawStart.y, e.getX(), e.getY());
						}

						// Add shapes, fills, colors, and transparency to the ArrayLists
						shapes.add(aShape);
						shapeFill.add(fillColor);
						shapeStroke.add(strokeColor);
						transPercent.add(transparentVal);
						strokeSizes.add(strokeSize);
						brushPercent.add((float) brushVal);

						drawStart = null;
						drawEnd = null;

						// repaint the drawing area
						repaint();

						// sa
						backup.add(aShape);
						// sa
					}
					if (currentAction == 2) {
						if (dragging == false)
							return; // Nothing to do because the user isn't drawing.
						dragging = false;
						graphicsForDrawing.dispose();
						graphicsForDrawing = null;

					}
				}
			}); // end of addMouseListener

			this.addMouseMotionListener(new MouseMotionAdapter() {

				public void mouseDragged(MouseEvent e) {

					// If this is a brush have shapes go on the screen quickly
					// x and y will get the end position of the shape

					Shape aShape = null;
					int x = e.getX();
					int y = e.getY();

					if (currentAction == 1 || currentAction == 3) {
						brushPaths.get(brushPaths.size() - 1).addPoint(e.getPoint());
						/*
						 * strokeSize = brushVal;
						 * 
						 * // Make stroke and fill equal // brush strokes are really ellipses
						 * strokeColor = fillColor;
						 * 
						 * aShape = drawLine(drawStart.x, drawStart.y, x, y); shapes.add(aShape);
						 * shapeFill.add(fillColor); shapeStroke.add(strokeColor);
						 * transPercent.add(transparentVal); strokeSizes.add(strokeSize);
						 */
					} else if (currentAction == 2) {
						strokeSize = 1;

						aShape = drawLine(drawStart.x, drawStart.y, x, y);
						shapes.add(aShape);
						shapeFill.add(fillColor);
						shapeStroke.add(strokeColor);
						transPercent.add(transparentVal);
						strokeSizes.add(strokeSize);

						// sa
						set();
						// sa
					}

					// Get the final x & y position after the mouse is dragged
					if (currentAction == 1 || currentAction == 2 || currentAction == 3) {
						drawStart = new Point(e.getX(), e.getY());
						drawEnd = new Point(e.getX(), e.getY());
					}
					repaint();
				}
			}); // end of addMouseMotionListener
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			// Class used to define the shapes to be drawn and rendered on screen
			graphSettings = (Graphics2D) g;
			bgColor = graphSettings.getBackground();

			// Antialiasing cleans up the jagged lines and defines rendering rules
			graphSettings.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Defines the line width of the stroke
			// graphSettings.setStroke(new BasicStroke(5));

			// Iterators created to cycle through arraylist of strokes and fills
			Iterator<Color> strokeCounter = shapeStroke.iterator();
			Iterator<Color> fillCounter = shapeFill.iterator();
			Iterator<Integer> sizeSelector = strokeSizes.iterator();

			// Iterator for transparency
			Iterator<Float> transCounter = transPercent.iterator();

			// paint loaded image if it exists
			if (srcImg != null)
				graphSettings.drawImage(srcImg, 0, 0, null);

			// cycling through shapes that were created
			for (Shape s : shapes) {

				// Sets the shapes transparency value
				graphSettings.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transCounter.next()));

				// Grabs the next stroke from the color arraylist
				graphSettings.setPaint(strokeCounter.next());
				// set stroke
				graphSettings.setStroke(new BasicStroke(sizeSelector.next()));
				// draws shape on the screen
				graphSettings.draw(s);

				// Grabs the next fill from the color arraylist
				graphSettings.setPaint(fillCounter.next());
				graphSettings.fill(s);
			}

			for (drawPath dp : brushPaths) {

				graphSettings.setStroke(dp.getStrokeSettings());
				graphSettings.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
				graphSettings.setPaint(dp.getColor());

				for (int i = 0; i < dp.getPath().size() - 1; i++) {
					graphSettings.drawLine(dp.getPath().get(i).x, dp.getPath().get(i).y, dp.getPath().get(i + 1).x,
							dp.getPath().get(i + 1).y);
				}
			}

			// Guide shape used for drawing
			if (drawStart != null && drawEnd != null) {

				// Makes the guide shape transparent
				// 1.0f means transparency off
				graphSettings.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f));

				// Make guide shape gray for professional look
				graphSettings.setPaint(Color.LIGHT_GRAY);

				Shape aShape = null;

				if (currentAction == 4 || currentAction == 2) {

					aShape = drawLine(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y);
					graphSettings.draw(aShape);

					// sa
					set();
					// sa

				} else if (currentAction == 5) {

					aShape = drawEllipse(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y);
					graphSettings.draw(aShape);

					// sa
					set();
					// sa
				} else if (currentAction == 6) {

					// Create a new rectangle using x & y coordinates
					aShape = drawRectangle(drawStart.x, drawStart.y, drawEnd.x, drawEnd.y);
					graphSettings.draw(aShape);

					// sa
					set();
					// sa
				}

			}
		}

		private Rectangle2D.Float drawRectangle(int x1, int y1, int x2, int y2) {

			// Get the top left hand corner for the shape
			// Math.min returns the points closest to (0,0)
			int x = Math.min(x1, x2);
			int y = Math.min(y1, y2);

			// Gets the difference between the coordinates and
			int width = Math.abs(x1 - x2);
			int height = Math.abs(y1 - y2);

			return new Rectangle2D.Float(x, y, width, height);
		}

		// The other shapes will work similarly
		// More on this in the next tutorial
		private Ellipse2D.Float drawEllipse(int x1, int y1, int x2, int y2) {
			int x = Math.min(x1, x2);
			int y = Math.min(y1, y2);
			int width = Math.abs(x1 - x2);
			int height = Math.abs(y1 - y2);

			return new Ellipse2D.Float(x, y, width, height);
		}

		private Line2D.Float drawLine(int x1, int y1, int x2, int y2) {

			return new Line2D.Float(x1, y1, x2, y2);
		}

		private Ellipse2D.Float drawBrush(int x1, int y1, int brushStrokeWidth, int brushStrokeHeight) {

			return new Ellipse2D.Float(x1, y1, brushStrokeWidth, brushStrokeHeight);
		}
	}

	public class drawPath {
		private ArrayList<Point> path;
		private Stroke strokeSettings;
		private Color color;

		public drawPath(Point start, Color color, float width, float alpha) {
			path = new ArrayList<>();
			path.add(start);
			this.color = new Color((float) color.getRed() / 255, (float) color.getGreen() / 255,
					(float) color.getBlue() / 255, alpha);
			strokeSettings = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		}

		public void addPoint(Point p) {
			path.add(p);
		}

		public ArrayList<Point> getPath() {
			return path;
		}

		public Stroke getStrokeSettings() {
			return strokeSettings;
		}

		public Color getColor() {
			return color;
		}
	}

	// Implements ActionListener so it can react to events on components
	private class ListenForSlider implements ChangeListener {

		// Called when the slider is changed
		public void stateChanged(ChangeEvent e) {

			// Check if the source of the event was the slider
			if (e.getSource() == transSlider) {

				// Change the value for the label next to the slider
				// Use decimal format to make sure only 2 decimals are displayed
				transLabel.setText("Transparent: " + dec.format(transSlider.getValue() * .01));

				// Set the value for transparency for every shape drawn after
				transparentVal = (float) (transSlider.getValue() * .01);

			}

			// Check if the source of the event was the slider
			if (e.getSource() == brushSlider) {
				// Change the value for the label next to the slider
				brushLabel.setText("Brush Size: " + brushSlider.getValue());

				// Set the value for brush size for every shape drawn after
				brushVal = (brushSlider.getValue());

			}

		}

	}

//	void saveToStack(BufferedImage img) { // makes a copy of img and puts on stack.
//		BufferedImage imageForStack = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
//		Graphics2D g2d = imageForStack.createGraphics();
//		g2d.drawImage(img, 0, 0, null);
//		undoStack.push(imageForStack);
//	}
}
