package eda132_lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Node extends TreeElement{
	public List<TreeElement> subNodes;
	public Node(Map<String, Integer> counts, Attribute option){
		super(counts, option);
		subNodes = new ArrayList<TreeElement>();
	}
	public void addElement(TreeElement t){
		subNodes.add(t);
	}
	
	public void print(String pre, boolean rec){
		System.out.print(pre + " ---NODE--- " + option.name + ":" + option.type.toString() + " ");
		for (String s : counts.keySet()){
			System.out.print(s + ": " + counts.get(s) + " ");
		}
		System.out.println();

		if (rec){
			for (TreeElement t : subNodes){
				t.print(pre + "\t", rec);
			}
		}
		
	}
	
	public void search(String[] recieved, String pre){
		for (TreeElement t : subNodes){
			if (t.fits(recieved[Main.attributeMap.get(t.option.name)])){
				t.print(pre + "\t", false);
				t.search(recieved, pre + "\t");
			}
			
		}
//		System.out.println(getCountsString(pre));
		
		
	}
//	protected String recSearch(String[] recieved) {
//		for (TreeElement t : subNodes){
//			if (t.fits(recieved[Main.attributeMap.get(t.option.name)])){
//				return t.recSearch(recieved);
//			}
//		}
//		return null;
//		
//	}
}