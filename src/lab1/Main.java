package lab1;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
	public enum tileState{
		EMPTY, BLACK, WHITE
	}
	
	
	
	public static final int size = 10;
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
        for (int i = 0; i < size;i++){
        	for (int j = 0; j < size;j++){
        		boardState[i][j] = tileState.EMPTY;
        		
        		Button button = new Button("		");
        		button.setPrefSize(100, 40);
        		int newI = i;
				int newJ = j;
        		button.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						changeBoardState(newI, newJ, tileState.WHITE);	
					}
        		});
        		buttons[i][j] = button;
        		pane.add(button, i, j);
        	}
        }

        
        

		stage.setScene(scene);
        stage.sizeToScene();

		stage.show();

	}

	protected void changeBoardState(int i, int j, tileState newState) {
		switch (boardState[i][j]){
			case EMPTY:
				boardState[i][j] = newState;
				buttons[i][j].setText(newState.toString());
				break;
		default:
			break;
		}
		
			
		
	}

}
