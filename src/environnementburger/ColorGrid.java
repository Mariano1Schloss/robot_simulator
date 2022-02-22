package environnementburger;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.JFrame;

public class ColorGrid extends JPanel {
	
	private int width;
	private int height;
	private int rows;
	private int columns;
	private int xratio;
	private int yratio;
	private String title;
	private JFrame window;
	private Color[][] blocks;

	public ColorGrid(int width, int height, int rows, int columns, String title) {
		super();
		this.width = width;
		this.height = height;
		this.rows = rows;
		this.columns = columns;
		this.title = title;
		xratio = width/rows;
		yratio = height/columns;
		blocks = new Color[rows][columns];
	}

	public void init(){
		window = new JFrame(title);
        window.setSize(width, height+25);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.add(this);
        window.setVisible(true);
	}

	public void refresh() {
		window.validate();
		window.repaint();
	}

	public void setBlockColor(int x, int y, Color c){
		blocks[x][y] = c;
	}

	public Color getBlockColor(int x, int y){
		return blocks[x][y];
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				Color block = blocks[row][col];
				g.setColor(block);
				g.fillRect(row*xratio, col*yratio, xratio, yratio);
			}
		}
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}
}