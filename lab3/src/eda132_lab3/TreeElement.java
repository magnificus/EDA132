package eda132_lab3;

import java.util.Map;

public abstract class TreeElement{
	Map<String, Integer> counts;
	
	public Attribute option;
	
	public TreeElement(Map<String, Integer> counts, Attribute attribute){
		this.counts = counts;
		this.option = attribute;
	}

	public abstract void print(String pre);
}