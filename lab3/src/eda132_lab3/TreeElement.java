package eda132_lab3;

public abstract class TreeElement{
	public int pos;
	public int neg;
	
	public Attribute option;
	
	public TreeElement(int pos, int neg, Attribute attribute){
		this.pos = pos;
		this.neg = neg;
		this.option = attribute;
	}

	public abstract void print(String pre);
}