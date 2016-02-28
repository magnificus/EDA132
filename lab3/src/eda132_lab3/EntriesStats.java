package eda132_lab3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.directory.InvalidAttributesException;

public class EntriesStats{
	Map<String, Integer> counts;
	public int totalNonEmpty;
	
	
	public EntriesStats(List<List<String>> entries, List<String> possibilities) throws InvalidAttributesException{
		counts = new HashMap<String, Integer>();
		for (String s : possibilities){
			counts.put(s, 0);
		}
		for (List<String> l : entries){
			Integer rec = counts.get(l.get(l.size()-1));
			if (rec == null){
				throw new InvalidAttributesException();
			}
			else{
				counts.put(l.get(l.size()-1), rec+1);
			}
		}
		totalNonEmpty = calculateTotalNonEmptyPos();
	}

	private int calculateTotalNonEmptyPos() {
		int total = 0;
		
		for (String s : counts.keySet()){
			if (counts.get(s) > 0){
				total++;
			}
		}
		return total;
	}
}
