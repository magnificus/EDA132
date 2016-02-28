package dectree;

import java.util.Map;

import eda132_lab3.Attribute;

public abstract class TreeElement{
	Map<String, Integer> counts;
	
	public Attribute option;
	
	public TreeElement(Map<String, Integer> counts, Attribute option){
		this.counts = counts;
		this.option = option;
	}

	public abstract void print(String pre, boolean rec);

	public boolean fits(String recieved) {
		return option.type.compare(recieved);
	}

	public abstract void search(String[] recieved, String pre);
	
	public String getCountsString(String pre){
		String toReturn = pre;
		for (String s : counts.keySet()){
			toReturn +=  s + ": " + counts.get(s) + " "; 
		}
		return toReturn;
	}

}