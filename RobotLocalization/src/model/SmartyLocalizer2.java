package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import Jama.Matrix;
import control.EstimatorInterface;
import model.SmartyLocalizer.Direction;

public class SmartyLocalizer2 implements EstimatorInterface {

	public static final int WEST = 0;
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int NORTH = 3;

	private static final double firstNeighborChance = 0.05;
	private static final double secondNeighborChance = 0.025;
	private static final double trueChance = 0.1;

	private double[][] matrix;
	private double[][] sensorMatrices;
	private double[][] stateMatrix;
	private Point currentTrueLocation;
	private int rows, cols, head;
	private int currentDirection;
	
	private Point latestSens;
	private ArrayList<Integer> latestDistances;

	public SmartyLocalizer2(int rows, int cols, int head) {
		this.cols = cols;
		this.rows = rows;
		this.head = head;

		latestDistances = new ArrayList<Integer>();
		matrix = new double[rows * cols * head][rows * cols * head];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < head; k++) {
					matrix[i * 16 + j * 4 + k] = calculatedLikelihood(i, j, k);
				}
			}
		}
		sensorMatrices = new double[rows * cols + 1][rows * cols * head];
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
				sensorMatrices[16][p.p.x * 16 + p.p.y * 4 + k] = (double) p.d / totalOutside;
			}
		}

		Random rand = new Random();
		currentTrueLocation = new Point(rand.nextInt(rows), rand.nextInt(cols));
		currentDirection = getRandomDirection();

		double[] total = new double[64];
		for (int i = 0; i < 64; i++) {
			total[i] = 0;
		}
		
		stateMatrix = new double[rows*cols*head][1];
		for(int i = 0; i < 64; i++){
			stateMatrix[i][0] = (double)1 / 64;
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
			sMatrix[x * 16 + y * 4 + dir] = trueChance;
		}

		List<Point> candidates1 = getNeighBorsAtDist(1, new Point(x, y));
		for (Point p : candidates1) {
			for (int dir = 0; dir < 4; dir++) {
				sMatrix[p.x * 16 + p.y * 4 + dir] = firstNeighborChance;
			}
		}

		List<Point> candidates2 = getNeighBorsAtDist(2, new Point(x, y));
		for (Point p : candidates2) {
			for (int dir = 0; dir < 4; dir++) {
				sMatrix[p.x * 16 + p.y * 4 + dir] = secondNeighborChance;
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

	private double[] calculatedLikelihood(int i, int j, int k) {

		double[] outMatrix = new double[rows * cols * head];

		for (int m = 0; m < rows; m++) {
			for (int n = 0; n < cols; n++) {
				for (int o = 0; o < head; o++) {
					outMatrix[m * 16 + n * 4 + o] = 0;
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
				candidates.add(new PointDir(new Point(i, j - 1), NORTH));
			}
			if (j < rows - 1) {
				candidates.add(new PointDir(new Point(i, j + 1), SOUTH));
			}


			for (PointDir pointdir : candidates) {
				outMatrix[pointdir.p.x * 16 + pointdir.p.y * 4 + pointdir.d] = (double) 1 / (double) candidates.size();
			}
		} else {
			noCollision(i, j, k, outMatrix);
		}

		return outMatrix;

	}

	private void noCollision(int i, int j, int k, double[] outMatrix) {

		
		Point newP = stepInDir(i,j,k);
		outMatrix[newP.x*16 +newP.y * 4 + k] = 0.7;
		
		List<PointDir> candidates = new ArrayList<PointDir>();
		
		for (int dir = 0; dir < 4; dir++){
			PointDir p = new PointDir(stepInDir(i, j,dir), dir);
			if (dir != k && inScope(p.p.x, p.p.y)){
				candidates.add(p);
			}
		}
		
		for (PointDir p : candidates){
			outMatrix[p.p.x * 16 + p.p.y * 4 + p.d] = 0.3 / candidates.size();
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
		throw new NullPointerException();
			
		
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

		moveBot();
		
		Point mostLikely = getMostLikely();
		latestDistances.add(Math.abs(mostLikely.x - currentTrueLocation.x) + Math.abs(mostLikely.y - currentTrueLocation.y));
		
		if (latestDistances.size() > 100){
			latestDistances.remove(0);
		}
		
		double total = 0;
		for (Integer i : latestDistances){
			total += i;
		}
		
		System.out.println("Current averaged distance is: " + total / latestDistances.size());

		latestSens = getNewPoint();
		
		double[] currentSens;
		if (latestSens.x == -1 && latestSens.y == -1){
			currentSens = sensorMatrices[16];
		} else{
			currentSens = sensorMatrices[latestSens.x * 4 + latestSens.y];
		}
		
		stateMatrix = calculatePosition(currentSens, matrix, stateMatrix);
		total = 0;
		for (int i = 0; i < rows*cols*head; i++){
			total += stateMatrix[i][0];
		}
		for (int i = 0; i < rows*cols*head; i++){
			stateMatrix[i][0] /= total;
		}
		

	}

	private Point getMostLikely() {
		Point mostLikely = null;
		double maxChance = Double.MIN_VALUE;
		for (int x = 0; x < rows; x++){
			for (int y = 0; y < cols; y++){
				double currentProb = getCurrentProb(x,y);
				if (currentProb > maxChance){
					mostLikely = new Point(x,y);
					maxChance = currentProb;
				}
			}
		}
		return mostLikely;
	}

	private double[][] calculatePosition(double[] currentSens, double[][] matrix2, double[][] stateVector) {
		double[][] expandedSens = new double[rows*cols*head][rows*cols*head];
		for (int i = 0; i < rows*cols*head; i++){
			for (int j = 0; j < rows*cols*head;j++){
				if (i == j){
					expandedSens[i][j] = currentSens[i];
				} else{
					expandedSens[i][j] = 0;
				}
			}
		}
			
			
		Matrix sensMatrix = new Matrix(expandedSens);
		Matrix transMatrix = new Matrix(matrix2);
		Matrix stateMatrix = new Matrix(stateVector);
		return sensMatrix.times(transMatrix.transpose()).times(stateMatrix).getArray();

		
		

	}

	private void moveBot() {
		float acc = 0;
		float target = new Random().nextFloat();

		double[] targets = matrix[currentTrueLocation.x * 16 + currentTrueLocation.y * 4 + currentDirection];
		for (int i = 0; i < targets.length; i++) {
			acc += targets[i];
			if (acc >= target) {
				int newX = i / 16;
				int newY = (i - (newX*16))/4;
				int newDir = i % 4;
				currentTrueLocation = new Point(newX, newY);
				currentDirection = newDir;
				return;
			}
		}

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
				if (Math.max(Math.abs(i-loc.x), Math.abs(j - loc.y)) == dist && inScope(i, j)) {
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
		return new int[] { currentTrueLocation.x, currentTrueLocation.y };
	}

	@Override
	public int[] getCurrentReading() {
		return new int[] { latestSens.x, latestSens.y };
	}

	@Override
	public double getOrXY(int rX, int rY, int x, int y) {
		
		return sensorMatrices[rX*16+rY*4][x*4+y];
	}

	@Override
	public double getTProb(int x, int y, int h, int nX, int nY, int nH) {
		return matrix[x * 16 + y * 4 + h][nX * 16 + nY * 4 + nH];
	}


	private int getRandomDirection() {
		return new Random().nextInt(4);
	}

	@Override
	public double getCurrentProb(int x, int y) {
		double tot = 0;
		for (int i = 0; i < 4; i++){
			tot += stateMatrix[x*16 + y*4 + i][0];
		}
		return tot;
	}

}
