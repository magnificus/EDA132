package comparators;


public class StringComparator extends ValueComparator{
	String s;
	public StringComparator(String s){
		this.s = s;
	}
	@Override
	public boolean compare(String s) {
		return this.s.equals(s);
	}
	@Override
	public String toString() {
		return s;
		
	}
}
