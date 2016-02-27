package eda132_lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Node extends TreeElement{
	public List<TreeElement> subNodes;
	public Node(Map<String, Integer> counts, Attribute attribute){
		super(counts, attribute);
		subNodes = new ArrayList<TreeElement>();
	}
	public void addElement(TreeElement t){
		subNodes.add(t);
	}
	
	public void print(String pre){
		System.out.print(pre + " ---NODE--- " + option.name + ":" + option.type + " ");
		for (String s : counts.keySet()){
			System.out.print(s + ": " + counts.get(s) + " ");
		}
		System.out.println();

		for (TreeElement t : subNodes){
			t.print(pre + "\t");
		}
	}
}