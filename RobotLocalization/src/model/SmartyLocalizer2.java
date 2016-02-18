package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import control.EstimatorInterface;
import model.SmartyLocalizer.Direction;

public class SmartyLocalizer2 implements EstimatorInterface {

	public static final int NORTH = 0;
	public static final int SOUTH = 1;
	public static final int WEST = 2;
	public static final int EAST = 3;

	private static final double firstNeighborChance = 0.05;
	private static final double secondNeighborChance = 0.025;
	private static final double trueChance = 0.1;

	private double[][] matrix;
	private double[][] sensorMatrices;
	private Point currentTrueLocation;
	private Point sensorLoc;
	private int rows, cols, head;
	private int currentDirection;

	public SmartyLocalizer2(int rows, int cols, int head) {
		this.cols = cols;
		this.rows = rows;
		this.head = head;

		matrix = new double[rows*cols*4][rows*cols*4];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < head; k++) {
					matrix[i*16 +j*4 +k] = calculatedLikelihood(i, j, k);
				}
			}
		}
		sensorMatrices = new double[rows*cols+1][rows*cols*head];
		for (int i = 0; i < sensorMatrices.length - 1; i++) {
			initSensorMatrix(sensorMatrices[i], i);
		}

		List<PointDir> nulls = new ArrayList<PointDir>();
		double totalOutside = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				int currentOutside = getNbrOutside(i, j);
				totalOutside += currentOutside;
				nulls.add(new PointDir(new Point(i, j), currentOutside));
			}
		}
		for (PointDir p : nulls) {
			for (int k = 0; k < 4; k++) {
				sensorMatrices[16][p.p.x*16 +p.p.y*4+k] = p.d / totalOutside;
			}
		}

		Random rand = new Random();
		currentTrueLocation = new Point(rand.nextInt(rows), rand.nextInt(cols));
		sensorLoc = new Point(rows / 2, cols / 2);
		currentDirection = getRandomDirection();
		
		double[] total = new double[64];
		for (int i = 0; i < 64; i++){
			total[i] = 0;
		}
		for (int i = 0; i < 17; i++){
			printMatrix(sensorMatrices[i]);
			System.out.println("\n");
			addMatrix(sensorMatrices[i],total);
			
		}
		printMatrix(total);
	}

	private void addMatrix(double[] ds, double[] total) {
		for (int i = 0; i < ds.length; i++){
			total[i] += ds[i];
			}
		
	}

	private void printMatrix(double[] ds) {
		for (int i = 0; i < ds.length; i++){
			System.out.print(" " + ds[i]);
		}
		
	}

	private int getNbrOutside(int i, int j) {
		int total = 0;

		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				if (!inScope(i + x, j + y)) {
					total++;
				}
			}
		}

		return total;
	}

	private void initSensorMatrix(double[] sMatrix, int i) {
		int x = i / 4;
		int y = i % 4;

		for (int dir = 0; dir < head; dir++) {
			sMatrix[x*16+y*4+dir] = trueChance;
		}

		List<Point> candidates1 = getNeighBorsAtDist(1, new Point(x, y));
		for (Point p : candidates1) {
			for (int dir = 0; dir < 4; dir++) {
				sMatrix[p.x*16 + p.y*4 +dir] = firstNeighborChance;
			}
		}

		List<Point> candidates2 = getNeighBorsAtDist(2, new Point(x, y));
		for (Point p : candidates2) {
			for (int dir = 0; dir < 4; dir++) {
				sMatrix[p.x*16 +p.y*4 +dir] = secondNeighborChance;
			}
		}
	}

	public class PointDir {
		public Point p;
		public int d;

		public PointDir(Point p, int d) {
			this.p = p;
			this.d = d;

		}
	}

	private double[]calculatedLikelihood(int i, int j, int k) {

		double[] outMatrix = new double[rows*cols*4];

		for (int m = 0; m < rows; m++) {
			for (int n = 0; n < cols; n++) {
				for (int o = 0; o < head; o++) {
					outMatrix[m*16 +n*4 + o]= 0;
				}
			}
		}

		List<PointDir> candidates = new ArrayList<PointDir>();
		if (getColliding(i, j, k)) {
			if (i > 0) {
				candidates.add(new PointDir(new Point(i - 1, j), WEST));
			}
			if (i < cols - 1) {
				candidates.add(new PointDir(new Point(i + 1, j), EAST));
			}
			if (j > 0) {
				candidates.add(new PointDir(new Point(i, j - 1), SOUTH));
			}
			if (j < rows - 1) {
				candidates.add(new PointDir(new Point(i, j + 1), NORTH));
			}

			for (PointDir pointdir : candidates) {
				outMatrix[pointdir.p.x * 16 + pointdir.p.y * 4 + pointdir.d] = 1 / candidates.size();
			}
		} else {
			noCollision(i, j, k, outMatrix);
		}

		return outMatrix;

	}

	private void noCollision(int i, int j, int k, double[] outMatrix) {
		for (int dir = 0; dir < 4; dir++) {
			Point newP = stepInDir(i, j, k);
			if (dir == k) {
				outMatrix[newP.x*16 +newP.y * 4 + dir] = 0.7;
			} else {
				outMatrix[newP.x*16 +newP.y * 4 + dir] = 0.1;
			}
		}

	}

	private Point stepInDir(int i, int j, int k) {
		switch (k) {
		case NORTH:
			return new Point(i, j - 1);
		case SOUTH:
			return new Point(i, j + 1);
		case EAST:
			return new Point(i + 1, j);
		case WEST:
			return new Point(i - 1, j);
		}
		return null;
	}

	private boolean getColliding(int i, int j, int k) {
		switch (k) {
		case NORTH:
			return j == 0;
		case SOUTH:
			return j == rows - 1;
		case WEST:
			return i == 0;
		case EAST:
			return i == cols - 1;
		}
		return false;
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
		double[] currentSens;
		if (p.x == -1){
			currentSens = sensorMatrices[16];
		} else{
			currentSens = sensorMatrices[p.x*4 + p.y];
		}
		
		sensorLoc = p;
		System.out.println();
	}


	private Point getNewPoint() {
		Random rand = new Random();
		double f = rand.nextFloat();
		double acc = 0f;
		List<Point> firstNeighbors = getNeighBorsAtDist(1, currentTrueLocation);
		List<Point> sndNeighbors = getNeighBorsAtDist(2, currentTrueLocation);
		for (Point p : firstNeighbors) {
			acc += firstNeighborChance;
			if (f < acc) {
				return p;
			}
		}
		for (Point p : sndNeighbors) {
			acc += secondNeighborChance;
			if (f < acc) {
				return p;
			}
		}
		acc += trueChance;
		if (f < acc) {
			return currentTrueLocation;
		}

		// This corresponds to sending message "nothing"
		return new Point(-1, -1);
	}

	private List<Point> getNeighBorsAtDist(int dist, Point loc) {
		List<Point> points = new ArrayList<Point>();
		for (int i = loc.x - dist; i <= loc.x + dist; i++) {
			for (int j = loc.y - dist; j <= loc.y + dist; j++) {
				if (Math.max(Math.abs(i), Math.abs(j)) == dist && inScope(i, j)) {
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
		return new int[] { sensorLoc.y, sensorLoc.x };
	}

	@Override
	public double getOrXY(int rX, int rY, int x, int y) {

		return 0;
	}

	@Override
	public double getTProb(int x, int y, int h, int nX, int nY, int nH) {
		return matrix[x*16+y*4+h][nX*16+nY*4+nH];
	}

	double getTotalSumMatrix(double[][] in) {
		double acc = 0;
		for (int i = 0; i < in.length; i++) {
			for (int j = 0; j < in[0].length; j++) {
				acc += in[i][j];
			}
		}
		return acc;
	}

	private int getRandomDirection() {
		return new Random().nextInt(4);
	}

	@Override
	public double getCurrentProb(int x, int y) {
		return 0;
	}

}
