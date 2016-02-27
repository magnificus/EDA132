package eda132_lab3;

import comparators.ValueComparator;



public class Attribute {
	public String name;
	public ValueComparator type;
	
	public Attribute(String name, ValueComparator type){
		this.name = name;
		this.type = type;
	}

}
