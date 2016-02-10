package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import control.EstimatorInterface;

public class SmartyLocalizer implements EstimatorInterface {
	private ArrayList<Point> previous;
	private Point currentTrueLocation;
	private int rows, cols, head;
	
	private static float firstNeighborChange = 0.05f;
	private static float sndNeighborChance = 0.025f;
	private static float trueChance = 0.1f;

	public SmartyLocalizer(int rows, int cols, int head) {
		this.cols = cols;
		this.rows = rows;
		this.head = head;
		previous = new ArrayList<Point>();
		Random rand = new Random();
		currentTrueLocation = new Point(rand.nextInt(rows), rand.nextInt(cols));
	}

	@Override
	public int getNumRows() {
		return rows;
	}

	@Override
	public int getNumCols() {
		return cols;
	}

	@Override
	public int getNumHead() {
		return head;
	}

	@Override
	public void update() {
		previous.add(getNewPoint());

	}

	private Point getNewPoint() {
		Random rand = new Random();
		Float f = rand.nextFloat();
		Float acc = 0f;
		List<Point> firstNeighbors = getNeighBorsAtDist(1, currentTrueLocation);
		List<Point> sndNeighbors = getNeighBorsAtDist(2, currentTrueLocation);
		for (Point p : firstNeighbors){
			acc+=firstNeighborChange;
			if (f < acc){
				return p;
			}
		}
		for (Point p : sndNeighbors){
			acc+=sndNeighborChance;
			if (f < acc){
				return p;
			}
		}
		acc += trueChance;
		if (f < acc){
			return currentTrueLocation;
		}
		return new Point(-1,-1);
	}

	private List<Point> getNeighBorsAtDist(int dist, Point loc) {
		List<Point> points = new ArrayList<Point>();
		for (int i = loc.x - dist; i <= loc.x + dist; i++) {
			for (int j = loc.y - dist; j <= loc.y + dist; j++) {
				if (i - loc.x + j - loc.y == dist && inScope(loc.x, loc.y)) {
					points.add(new Point(i, j));
				}
			}
		}
		return points;
	}

	private boolean inScope(int x, int y) {
		return x >= 0 && x < rows && y >= 0 &&  y < cols;
	}

	@Override
	public int[] getCurrentTruePosition() {
		return new int[] { currentTrueLocation.x, currentTrueLocation.y };
	}

	@Override
	public int[] getCurrentReading() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getCurrentProb(int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getOrXY(int rX, int rY, int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTProb(int x, int y, int h, int nX, int nY, int nH) {
		// TODO Auto-generated method stub
		return 0;
	}

}
