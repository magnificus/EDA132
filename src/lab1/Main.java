package lab1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

	public static boolean hCouldMove = true;
	public static boolean bCouldMove = true;

	public enum tileState {
		EMPTY, BLACK, WHITE
	}

	public class Move {
		public Move(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int x;
		public int y;

		@Override
		public boolean equals(Object obj) {
			Move other = (Move) obj;
			return (other.x == x && other.y == y);

		}
	}

	public static boolean playerTurn = true;
	public static Move placedLatest;

	public static long timeLimit;
	public static int searchDepth = 7;
	
	public static final int size = 8;
	public static tileState[][] boardState;
	public static Button[][] buttons;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		
		timeLimit = Long.parseLong(JOptionPane.showInputDialog("Enter time limit per turn (ms)", "1000"));

		boardState = new tileState[size][size];
		buttons = new Button[size][size];

		GridPane pane = new GridPane();
		Scene scene = new Scene(pane);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				boardState[i][j] = tileState.EMPTY;

				Button button = new Button();
				button.setPrefSize(40, 40);
				int newI = i;
				int newJ = j;
				button.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						if (playerTurn) {
							Move myMove = new Move(newI, newJ);
							if (moveOk(newI, newJ, tileState.WHITE, boardState)) {
								updateBoard(myMove, tileState.WHITE);
								playerTurn = false;
								System.out.println("User move was OK");
								performBotTurn();

							} else {
								System.out.println("User move was NOT OK");
							}

						}

					}
				});
				button.setStyle("-fx-base: #b6e7c9;");
				buttons[i][j] = button;
				pane.add(button, i, j);
			}
		}

		// middle starting tiles

		changeBoardState(boardState, size / 2, size / 2, tileState.WHITE, true);
		changeBoardState(boardState, size / 2 - 1, size / 2 - 1, tileState.WHITE, true);
		changeBoardState(boardState, size / 2 - 1, size / 2, tileState.BLACK, true);
		changeBoardState(boardState, size / 2, size / 2 - 1, tileState.BLACK, true);

		stage.setScene(scene);
		stage.sizeToScene();

		stage.show();

	}

	protected List<Move> getAllowedMoves(tileState t, tileState[][] currentState) {
		List<Move> ok = new ArrayList<Move>();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (moveOk(i, j, t, currentState)) {
					ok.add(new Move(i, j));
				}
			}
		}
		return ok;

	}

	private boolean moveOk(int x, int y, tileState t, tileState[][] currentState) {
		tileState mine = t;
		tileState other = null;
		switch (t) {
		case WHITE:
			other = tileState.BLACK;
			break;
		case BLACK:
			other = tileState.WHITE;
			break;
		default:
			break;
		}
		if (currentState[x][y] != tileState.EMPTY) {
			return false;
		}

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (currentState[i][j] == mine) {
					if (pathBetween(x, y, i, j, mine, other, currentState)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean pathBetween(int x, int y, int x2, int y2, tileState mine, tileState other, tileState[][] state) {
		int maxX = Math.max(x, x2);
		int maxY = Math.max(y, y2);
		int minX = Math.min(x, x2);
		int minY = Math.min(y, y2);

		if (y == y2) {
			return testAxis(minX, y, maxX, y + 1, 1, 0, mine, other, state);
		} else if (x == x2) {
			return testAxis(x, minY, x + 1, maxY, 0, 1, mine, other, state);
		} else if (maxX - minX == maxY - minY) {
			return testAxis(minX, minY, maxX, maxY, 1, 1, mine, other, state) || testAxis(minX, maxY, maxY, minY, 1, -1, mine, other, state);
		}

		return false;
	}

	private boolean testAxis(int startX, int startY, int finishX, int finishY, int changeX, int changeY, tileState mine, tileState other, tileState[][] state) {
		boolean foundOther = false;
		for (int i = startX + changeX, j = startY + changeY; i != finishX && j != finishY; i += changeX, j += changeY) {
			if (state[i][j] != other) {
				return false;
			}
			foundOther = true;
		}
		return foundOther;
	}

	protected void changeBoardState(tileState[][] t, int i, int j, tileState newState, boolean b) {
		t[i][j] = newState;
		if (b) {
			updateButton(buttons[i][j], i, j);
		}
	}

	public void updateButton(Button b, int x, int y) {
		tileState current = boardState[x][y];
		String toPlace = null;
		switch (current) {
		case WHITE:
			toPlace = "ffffff";
			break;
		case BLACK:
			toPlace = "000000";
			break;
		case EMPTY:
			toPlace = "b6e7c9";
			break;
		}
		b.setStyle("-fx-base: #" + toPlace + ";");
		// b.re
	}

	protected void performBotTurn() {
		long pre = System.currentTimeMillis();

		List<Move> moves = getAllowedMoves(tileState.BLACK, boardState);
		if (moves.size() == 0) {
			bCouldMove = false;
			if (!hCouldMove) {
				JOptionPane.showMessageDialog(null, "Game over! Final scores: White: " + calculateBoardValue(boardState, tileState.WHITE) + " Black: "
						+ calculateBoardValue(boardState, tileState.BLACK));
				System.exit(0);
			}
			JOptionPane.showMessageDialog(null, "Bot cannot move! You move again.");
			playerTurn = true;
			return;
		} else {
			bCouldMove = true;
		}
		MoveValue bestMove = ABPruning(boardState, searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, pre);
		if (System.currentTimeMillis() - pre > timeLimit / 2){
			System.out.println("Decreasing search depth");
			searchDepth = Math.max(searchDepth-1, 1);
		} else if (System.currentTimeMillis() - pre < timeLimit / 1000){
			System.out.println("Increasing search depth");
			searchDepth++;
		}
		System.out.println("Search depth: " + searchDepth);

		updateBoard(bestMove.m, tileState.BLACK);
		if (getAllowedMoves(tileState.WHITE, boardState).size() == 0) {
			hCouldMove = false;
			if (!bCouldMove) {
				JOptionPane.showMessageDialog(null, "Game over! Final scores: White: " + calculateBoardValue(boardState, tileState.WHITE) + " Black: "
						+ calculateBoardValue(boardState, tileState.BLACK));
				System.exit(0);
			}
			JOptionPane.showMessageDialog(null, "You cannot move! Bot moves again.");
			performBotTurn();
		} else {
			hCouldMove = true;
		}
		playerTurn = true;
	}

	public class MoveValue {
		public MoveValue(Move m, int value) {
			this.m = m;
			this.value = value;
		}

		public Move m;
		public int value;
	}

	private MoveValue ABPruning(tileState[][] currentState, int sd, int alpha, int beta, boolean maximizing, long pre) {

		// return value of bottom node
		
		if (sd == 0) {
				return new MoveValue(null, calculateBoardValue(currentState, tileState.BLACK));
		}

		MoveValue optimal;
		if (maximizing) {
			List<Move> moves = getAllowedMoves(tileState.BLACK, currentState);
			if (moves.size() == 0) {
				return ABPruning(currentState, sd - 1, alpha, beta, false, pre);
			}
			optimal = new MoveValue(null, Integer.MIN_VALUE);
			for (Move m : moves) {
				MoveValue v = ABPruning(calculateNewBoard(currentState, m, tileState.BLACK), sd - 1, alpha, beta, false, pre);
				if (v.value > optimal.value) {
					optimal = new MoveValue(m, v.value);
					alpha = Math.max(alpha, optimal.value);
					if (beta <= alpha) {
						break;
					}
				}

			}
			return optimal;
		} else {
			optimal = new MoveValue(null, Integer.MAX_VALUE);
			List<Move> eMoves = getAllowedMoves(tileState.WHITE, currentState);
			if (eMoves.size() == 0) {
				return ABPruning(currentState, sd - 1, alpha, beta, true, pre);
			}
			for (Move m : eMoves) {
				MoveValue v = ABPruning(calculateNewBoard(currentState, m, tileState.WHITE), sd - 1, alpha, beta, true, pre);
				if (v.value < optimal.value) {
					optimal = new MoveValue(m, v.value);
					beta = Math.min(beta, optimal.value);
					if (beta <= alpha) {
						break;
					}
				}
			}
			return optimal;
		}

	}

	private void updateBoard(Move m, tileState t) {
		boardState = calculateNewBoard(boardState, m, t);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				updateButton(buttons[i][j], i, j);
			}
		}
		if (placedLatest != null) {
			buttons[placedLatest.x][placedLatest.y].setText("");
		}
		placedLatest = m;
		buttons[m.x][m.y].setText("X");

	}

	private void calculateAxis(tileState[][] board, int startX, int startY, int changeX, int changeY, int stopX, int stopY, tileState mine) {
		for (int x = startX + changeX, y = startY + changeY; x != stopX && y != stopY; x += changeX, y += changeY) {
			if (board[x][y] == mine) {
				for (int nX = startX, nY = startY; nX != x || nY != y; nX += changeX, nY += changeY) {
					changeBoardState(board, nX, nY, mine, false);
				}
				return;
			}
		}
	}

	public tileState[][] calculateNewBoard(tileState[][] old, Move m, tileState t) {
		tileState[][] newBoard = copyBoard(old);

		tileState mine = t;

		// x & y - axis
		
		calculateAxis(newBoard, m.x, m.y, 1, 0, size, Integer.MAX_VALUE, mine);
		calculateAxis(newBoard, m.x, m.y, -1, 0, -1, Integer.MAX_VALUE, mine);
		calculateAxis(newBoard, m.x, m.y, 0, 1, Integer.MAX_VALUE, size, mine);
		calculateAxis(newBoard, m.x, m.y, 0, -1, Integer.MAX_VALUE, -1, mine);

		// diagonal

		calculateAxis(newBoard, m.x, m.y, 1, 1, size, size, mine);
		calculateAxis(newBoard, m.x, m.y, 1, -1, size, -1, mine);
		calculateAxis(newBoard, m.x, m.y, -1, 1, -1, size, mine);
		calculateAxis(newBoard, m.x, m.y, -1, -1, -1, -1, mine);

		return newBoard;

	}

	public tileState[][] copyBoard(tileState[][] old) {
		tileState[][] newBoard = new tileState[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				newBoard[i][j] = old[i][j];
			}
		}
		return newBoard;
	}

	public int calculateBoardValue(tileState[][] board, tileState t) {
		int sum = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (t == board[i][j]) {
					sum++;
				}
			}
		}
		return sum;
	}

}
