package dectree;

import java.util.Map;

import eda132_lab3.Attribute;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Leaf extends TreeElement{
	public Leaf(Map<String, Integer> counts, Attribute option){
		super(counts, option);
		
	}

	@Override
	public void print(String pre, boolean rec) {
		System.out.print(pre + " ---LEAF--- " + option.name + ":" + option.type + " ");
		for (String s : counts.keySet()){
			System.out.print(s + ": " + counts.get(s) + " ");
		}
		System.out.println();
	}

	@Override
	public void search(String[] recieved, String pre) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Result");
		alert.setHeaderText("The result of the search was: ");
		alert.setContentText(getCountsString(""));
		alert.showAndWait();
	}
	
	
}