package eda132_lab3;

import java.util.ArrayList;
import java.util.List;


public class Node extends TreeElement{
	public List<TreeElement> subNodes;
	public Node(int pos, int neg, Attribute attribute){
		super(pos,neg, attribute);
		subNodes = new ArrayList<TreeElement>();
	}
	public void addElement(TreeElement t){
		subNodes.add(t);
	}
	
	public void print(String pre){
		System.out.println(pre + " -NODE- " + option.name + ":" + option.type + " T:" + pos + " F:" + neg);
		for (TreeElement t : subNodes){
			t.print(pre + "\t");
		}
	}
}