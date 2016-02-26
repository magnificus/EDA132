package eda132_lab3;

public class Leaf extends TreeElement{
	public Leaf(int pos, int neg, Attribute option){
		super(pos,neg, option);
		
	}

	@Override
	public void print(String pre) {
		System.out.println(pre + " -LEAF- " + option.name + ":" + option.type + " T:" + pos + " F:" + neg);
		
	}
}