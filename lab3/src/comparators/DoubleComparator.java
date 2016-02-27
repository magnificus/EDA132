package comparators;


public class DoubleComparator extends ValueComparator{
	double number;
	boolean lte;
	
	public DoubleComparator(double number, boolean lte){
		this.number = number;
		this.lte = lte;
	}

	@Override
	public boolean compare(String s) {
		if (lte){
			return number <= Double.parseDouble(s);
		} else{
			return number > Double.parseDouble(s);
		}
	}

	@Override
	public String toString() {
		String out = "";
		if (lte){
			out += " <= ";
		} else{
			out += " > ";
		}
		out += number;
		return out;
	}
	
}
