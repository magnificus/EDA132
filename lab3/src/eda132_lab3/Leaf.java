package eda132_lab3;

import java.util.Map;

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
	}
	
	
}