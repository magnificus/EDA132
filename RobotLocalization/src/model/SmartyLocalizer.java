package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import control.EstimatorInterface;

public class SmartyLocalizer implements EstimatorInterface {
	
	public enum Direction{
		NORTH, SOUTH, EAST, WEST;
	}
	
	private static final float firstNeighborChance = 0.05f;
	private static final float secondNeighborChance = 0.025f;
	private static final float trueChance = 0.1f;
	
	private double[][] matrix;
	private ArrayList<Point> previous;
	private Point currentTrueLocation;
	private Point currentGuessedLocation;
	private int rows, cols, head;
	private Direction currentDirection;
	

	public SmartyLocalizer(int rows, int cols, int head) {
		this.cols = cols;
		this.rows = rows;
		this.head = head;
		matrix = new double[rows][cols];
		for (int i = 0 ; i < matrix.length; i++){
			for (int j = 0; j < matrix[0].length; j++){
				matrix[i][j] = 1 / (rows * cols);
			}
		}
		previous = new ArrayList<Point>();
		Random rand = new Random();
		currentTrueLocation = new Point(rand.nextInt(rows), rand.nextInt(cols));
		currentGuessedLocation = currentTrueLocation;
		currentDirection = getRandomDirection();
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
		decideHeading();
		System.out.println("Moving: " + currentDirection);
		move();
		updateLikelyLocation();
		//previous.add(getNewPoint());

	}

	private void updateLikelyLocation() {
		
		// placeholder
		currentGuessedLocation.x = currentTrueLocation.x+1;
		currentGuessedLocation.y = currentTrueLocation.y;
	}

	private void move() {
		switch (currentDirection){
		case NORTH: currentTrueLocation.y = Math.max(currentTrueLocation.y - 1, 0); break;
		case SOUTH: currentTrueLocation.y = Math.min(currentTrueLocation.y + 1, rows-1); break;
		case EAST: currentTrueLocation.x = Math.min(currentTrueLocation.x + 1, cols-1); break;
		case WEST: currentTrueLocation.x = Math.max(currentTrueLocation.x - 1, 0); break;
		}
		
	}

	private void decideHeading() {
		
		List<Direction> candidates = new ArrayList<Direction>();
		if (currentGuessedLocation.x > 0){
			candidates.add(Direction.WEST);
		}
		if (currentGuessedLocation.x < cols-1){
			candidates.add(Direction.EAST);
		}
		if (currentGuessedLocation.y > 0){
			candidates.add(Direction.NORTH);
		}
		if (currentGuessedLocation.y < rows-1){
			candidates.add(Direction.SOUTH);
		}
		
		// return random possible direction
		
		currentDirection = candidates.get(new Random().nextInt(candidates.size()));
		
	}


	private Point getNewPoint() {
		Random rand = new Random();
		Float f = rand.nextFloat();
		Float acc = 0f;
		List<Point> firstNeighbors = getNeighBorsAtDist(1, currentTrueLocation);
		List<Point> sndNeighbors = getNeighBorsAtDist(2, currentTrueLocation);
		for (Point p : firstNeighbors){
			acc+=firstNeighborChance;
			if (f < acc){
				return p;
			}
		}
		for (Point p : sndNeighbors){
			acc+=secondNeighborChance;
			if (f < acc){
				return p;
			}
		}
		acc += trueChance;
		if (f < acc){
			return currentTrueLocation;
		}
		
		// This corresponds to sending message "nothing"
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
		return new int[] { currentTrueLocation.y, currentTrueLocation.x };
	}

	@Override
	public int[] getCurrentReading() {
		return new int[] {currentGuessedLocation.y, currentGuessedLocation.x};
	}

	@Override
	public double getCurrentProb(int x, int y) {
		return matrix[x][y];
	}

	@Override
	public double getOrXY(int rX, int rY, int x, int y) {
		int dist = Math.max(Math.abs(rX -x), Math.abs(rY - y));
		switch (dist){
			case 0:
				return trueChance;
			case 1:
				return firstNeighborChance;
			case 2:
				return secondNeighborChance;	
		}
		return 0;
	}

	@Override
	public double getTProb(int x, int y, int h, int nX, int nY, int nH) {
		// TODO Auto-generated method stub
		return 0.1;
	}
	
	private Direction getRandomDirection(){
		return Direction.values()[new Random().nextInt(Direction.values().length)];
	}

}
