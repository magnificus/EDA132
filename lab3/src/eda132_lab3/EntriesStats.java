package eda132_lab3;

import java.util.List;

import javax.naming.directory.InvalidAttributesException;

public class EntriesStats{
	public int totY;
	public int totN;
	public int tot;
	
	public EntriesStats(List<List<String>> entries) throws InvalidAttributesException{
		totY = 0;
		totN = 0;
		for (List<String> l : entries){
			if (l.get(l.size()-1).toLowerCase().equals("true")){
				totY++;
			} else if (l.get(l.size()-1).toLowerCase().equals("false")){
				totN++;
			} else{
				throw new InvalidAttributesException();
			}
		}
		tot = totY+totN;
	}
}
