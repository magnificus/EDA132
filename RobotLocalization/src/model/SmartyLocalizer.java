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
	
	private double[][][][][][][][] matrix;
	private Point currentTrueLocation;
	private Point prevSensorLoc;
	private Point sensorLoc;
	private Point currentGuessed;
	private int rows, cols, head;
	private Direction latestDirection;
	private Direction currentDirection;
	
	private ArrayList<Double> latestDistances;
	

	public SmartyLocalizer(int rows, int cols, int head) {
		this.cols = cols;
		this.rows = rows;
		this.head = head;
		
		// every position is equally likely
		
		latestDistances = new ArrayList<Double>();
		
		matrix = new double[rows][cols][4][rows][cols][4][rows][cols];
		double std = 1 / (rows * cols);
		for (int i = 0 ; i < matrix.length; i++){
			for (int j = 0; j < matrix[0].length; j++){
				for (int k = 0; k < matrix[0][0].length; k++){
					for (int l = 0; l < matrix[0][0][0].length; l++){
						for (int m = 0; m < matrix[0][0][0][0].length; m++){
							for (int n = 0; n < matrix[0][0][0][0][0].length; n++){
								for (int o = 0; o < matrix[0][0][0][0][0][0].length; o++){
									for (int p = 0; p < matrix[0][0][0][0][0][0][0].length; p++){
										matrix[i][j][k][l][m][n][o][p] = std;

									}
								}
							}
							
						}
					}
				}
				
			}
		}
		Random rand = new Random();
		currentTrueLocation = new Point(rand.nextInt(rows), rand.nextInt(cols));
		prevSensorLoc = new Point(rows/2, cols/2);
		sensorLoc = new Point(rows/2, cols/2);
		currentGuessed = new Point(rows/2, cols/2);
		latestDirection = getRandomDirection();
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
		
		Point p = getNewPoint();

		while (p.x == -1){
			p = getNewPoint();
		}
		
		matrix[prevSensorLoc.x][prevSensorLoc.y][getEnumInt(latestDirection)][sensorLoc.x][sensorLoc.y][getEnumInt(currentDirection)][p.x][p.y]++;
		prevSensorLoc = sensorLoc;
		sensorLoc = p;
		
		latestDistances.add(currentGuessed.distance(currentTrueLocation));
		if (latestDistances.size() > 100){
			latestDistances.remove(0);
		}
		double tot = 0;
		for (Double d : latestDistances){
			tot += d;
		}
		System.out.println(tot / latestDistances.size());
		
		decideHeading();
		//System.out.println("Moving: " + currentDirection);
		move();
		updateLikelyLocation();
		//previous.add(getNewPoint());

	}

	private void updateLikelyLocation() {
		double highestFound = -1;
		Point highestPoint = null; 
		for (int i = 0; i < cols; i++){
			for (int j = 0; j < cols; j++){
				double current = matrix[prevSensorLoc.x][prevSensorLoc.y][getEnumInt(latestDirection)][sensorLoc.x][sensorLoc.y][getEnumInt(currentDirection)][i][j];
				if (current > highestFound && inScope(i,j)){
					highestFound = current;
					highestPoint = new Point(i,j);
				}
			}
		}
		
		currentGuessed = highestPoint;
		System.out.println("Highest probability in tile: " + highestPoint);
		
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
		if (sensorLoc.x > 0){
			candidates.add(Direction.WEST);
		}
		if (sensorLoc.x < cols-1){
			candidates.add(Direction.EAST);
		}
		if (sensorLoc.y > 0){
			candidates.add(Direction.NORTH);
		}
		if (sensorLoc.y < rows-1){
			candidates.add(Direction.SOUTH);
		}
		
		// return random possible direction
		latestDirection = currentDirection;
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
				if (Math.max(i, j) == dist && inScope(i, j)) {
					points.add(new Point(i, j));
				}
			}
		}
		return points;
	}

	private boolean inScope(int x, int y) {
		return x >= 0 && x < rows && y >= 0 && y < cols;
	}

	@Override
	public int[] getCurrentTruePosition() {
		return new int[] { currentTrueLocation.y, currentTrueLocation.x };
	}

	@Override
	public int[] getCurrentReading() {
		return new int[] {sensorLoc.y, sensorLoc.x};
	}

	@Override
	public double getCurrentProb(int x, int y) {
		return matrix[prevSensorLoc.x][prevSensorLoc.y][getEnumInt(latestDirection)][sensorLoc.x][sensorLoc.y][getEnumInt(currentDirection)][y][x] / getTotalSumMatrix(matrix[prevSensorLoc.x][prevSensorLoc.y][getEnumInt(latestDirection)][sensorLoc.x][sensorLoc.y][getEnumInt(currentDirection)]);
	}

	@Override
	public double getOrXY(int rX, int rY, int x, int y) {
		
		return 0.1;
//		double northChance = matrix[rX][rY][getEnumInt(Direction.NORTH)][x][y-1] / getTotalSumMatrix(matrix[rX][rY][getEnumInt(Direction.NORTH)]);
//		double southChance = matrix[rX][rY][getEnumInt(Direction.SOUTH)][x][y+1] / getTotalSumMatrix(matrix[rX][rY][getEnumInt(Direction.SOUTH)]);
//		double eastChance = matrix[rX][rY][getEnumInt(Direction.WEST)][x-1][y] / getTotalSumMatrix(matrix[rX][rY][getEnumInt(Direction.WEST)]);
//		double westChance = matrix[rX][rY][getEnumInt(Direction.EAST)][x+1][y] / getTotalSumMatrix(matrix[rX][rY][getEnumInt(Direction.EAST)]);
//		
//		return northChance + southChance + eastChance + westChance / 4;
		
		
		
//		int dist = Math.max(Math.abs(rX -x), Math.abs(rY - y));
//		switch (dist){
//			case 0:
//				return trueChance;
//			case 1:
//				return firstNeighborChance;
//			case 2:
//				return secondNeighborChance;	
//		}
//		return 0;
	}

	@Override
	public double getTProb(int x, int y, int h, int nX, int nY, int nH) {
		// TODO Auto-generated method stub
		return 0.1;
	}
	
	double getTotalSumMatrix(double[][] in){
		double acc = 0;
		for (int i = 0; i < in.length; i++){
			for (int j = 0; j < in[0].length; j++){
				acc += in[i][j];
			}
		}
		return acc;
	}
	
	private Direction getRandomDirection(){
		return Direction.values()[new Random().nextInt(Direction.values().length)];
	}
	
	public int getEnumInt(Direction d){
		switch (d){
		case NORTH: return 0;
		case SOUTH: return 1;
		case EAST: return 2;
		case WEST: return 3;
		}
		return Integer.MIN_VALUE;
		
	}

}
