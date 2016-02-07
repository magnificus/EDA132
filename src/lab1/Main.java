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

	public static final int searchDepth = 8;
	public static final int size = 8;
	public static tileState[][] boardState;
	public static Button[][] buttons;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {

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
							// List<Move> okMoves =
							// getAllowedMoves(tileState.WHITE, boardState);
//							for (Move m : getAllowedMoves(tileState.WHITE, boardState)){
//								System.out.println("x: " + m.x + " y: " + m.y);
//							}
							
							Move myMove = new Move(newI, newJ);
							if (moveOk(newI, newJ, tileState.WHITE, boardState)) {
								updateBoard(myMove, tileState.WHITE);
								playerTurn = false;
								System.out.println("User move was OK");
								performBotTurn();

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
			return testAxis(x, minY, x+1, maxY, 0, 1, mine, other, state);
		} else if (maxX - minX == maxY - minY) {
			return testAxis(minX, minY, maxX, maxY, 1, 1, mine, other, state);
		}

		return false;
	}

	private boolean testAxis(int startX, int startY, int maxX, int maxY, int changeX, int changeY, tileState mine, tileState other, tileState[][] state) {
		boolean foundOther = false;
		for (int i = startX + changeX, j = startY + changeY; i < maxX && j < maxY; i += changeX, j += changeY) {
//			System.out.println("testing X: " + i + " Y: " + j);
			if (state[i][j] != other) {
				return false;
			}
			foundOther = true;
		}
		return foundOther;
	}

	public void testPartOfLine(AtomicBoolean foundMine, AtomicBoolean foundOther, AtomicBoolean moveOK, int x, int y, tileState mine, tileState other,
			tileState[][] currentState) {
		if (!foundMine.get()) {
			if (currentState[x][y] == mine) {
				foundMine.set(true);
			}
		} else if (!foundOther.get()) {
			if (currentState[x][y] == tileState.EMPTY) {
				foundMine.set(false);
			} else if (currentState[x][y] == other) {
				foundOther.set(true);
				moveOK.set(true);
			}
		} else {
			if (currentState[x][y] == mine || currentState[x][y] == tileState.EMPTY) {
				moveOK.set(false);
				foundOther.set(false);
			}
		}

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
		MoveValue bestMove = ABPruning(boardState, searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);

		System.out.println("Bot determined max number of bricks for white in " + searchDepth + " turns is: " + bestMove.value);
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

	private MoveValue ABPruning(tileState[][] currentState, int sd, int alpha, int beta, boolean maximizing) {

		// return value of board for WHITE at bottom node
		if (sd == 0) {
			return new MoveValue(null, calculateBoardValue(currentState, tileState.BLACK));
		}

		MoveValue optimal;
		if (maximizing) {
			List<Move> moves = getAllowedMoves(tileState.BLACK, currentState);
			if (moves.size() == 0) {
				return ABPruning(currentState, sd - 1, alpha, beta, false);
			}
			optimal = new MoveValue(null, Integer.MIN_VALUE);
			for (Move m : moves) {
				MoveValue v = ABPruning(calculateNewBoard(currentState, m, tileState.BLACK), sd - 1, alpha, beta, false);
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
				return ABPruning(currentState, sd - 1, alpha, beta, true);
			}
			for (Move m : eMoves) {
				MoveValue v = ABPruning(calculateNewBoard(currentState, m, tileState.WHITE), sd - 1, alpha, beta, true);
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

	public tileState[][] calculateNewBoard(tileState[][] old, Move m, tileState t) {
		tileState[][] newBoard = copyBoard(old);

		tileState mine = t;

		for (int x = m.x + 2; x < size; x++) {
			if (newBoard[x][m.y] == mine) {
				for (int nX = m.x; nX < x; nX++) {
					changeBoardState(newBoard, nX, m.y, mine, false);
				}
				break;
			}
		}
		for (int x = m.x - 2; x >= 0; x--) {
			if (newBoard[x][m.y] == mine) {
				for (int nX = m.x; nX > x; nX--) {
					changeBoardState(newBoard, nX, m.y, mine, false);
				}
				break;
			}
		}
		for (int y = m.y + 2; y < size; y++) {
			if (newBoard[m.x][y] == mine) {
				for (int nY = m.y; nY < y; nY++) {
					changeBoardState(newBoard, m.x, nY, mine, false);
				}
				break;
			}
		}
		for (int y = m.y - 2; y >= 0; y--) {
			if (newBoard[m.x][y] == mine) {
				for (int nY = m.y; nY > y; nY--) {
					changeBoardState(newBoard, m.x, nY, mine, false);
				}
				break;
			}
		}

		// diagonal

		for (int x = m.x + 2, y = m.y + 2; x < size && y < size; x++, y++) {
			if (newBoard[x][y] == mine) {
				for (int nX = m.x, nY = m.y; nX < x; nX++, nY++) {
					changeBoardState(newBoard, nX, nY, mine, false);
				}
				break;
			}
		}
		for (int x = m.x + 2, y = m.y - 2; x < size && y >= 0; x++, y--) {
			if (newBoard[x][y] == mine) {
				for (int nX = m.x, nY = m.y; nX < x; nX++, nY--) {
					changeBoardState(newBoard, nX, nY, mine, false);
				}
				break;
			}
		}

		for (int x = m.x - 2, y = m.y + 2; x >= 0 && y < size; x--, y++) {
			if (newBoard[x][y] == mine) {
				for (int nX = m.x, nY = m.y; nX > x; nX--, nY++) {
					changeBoardState(newBoard, nX, nY, mine, false);
				}
				break;
			}
		}
		for (int x = m.x - 2, y = m.y - 2; x >= 0 && y >= 0; x--, y--) {
			if (newBoard[x][y] == mine) {
				for (int nX = m.x, nY = m.y; nX > x; nX--, nY--) {
					changeBoardState(newBoard, nX, nY, mine, false);
				}
				break;
			}
		}

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
