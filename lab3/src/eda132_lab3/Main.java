package eda132_lab3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.directory.InvalidAttributesException;




public class Main {
	
	public static String fileName = "lenses.arff";

	public static String relation;	
	// possible "splitters", known factors
	public static List<String> attributes;
	// possible answers, i.e. True and False of binary question
	public static List<String> possibilities;	
	// possible alternatives for each factor
	public static Map<String, List<String>> options;
	// map to get attribute position in each data entry
	public static Map<String, Integer> attributeMap;
	// the data entries
	public static List<List<String>> entries;
	


	public static void main(String[] args) throws IOException, InvalidAttributesException {
		
		parse();
		
		EntriesStats e = new EntriesStats(entries, possibilities);
		System.out.println("Decision tree possibilities: " + e.counts.size()); 
//		attributes.remove(attributes.size()-1);
		Node root = new Node(e.counts, new Attribute("Root", ""));
		buildTree(root, attributes, entries);
		
		root.print("");

		
	}

	private static void buildTree(Node previous, List<String> remaining, List<List<String>> remainingEntries) throws InvalidAttributesException {
		
		double minEnt = Double.MAX_VALUE;
		String minEntAtt = null;
		double currEntropy;
		for (String a : remaining){
			// calculate entropy for each..
			currEntropy = calculateEntropy(a, remainingEntries);
			System.out.println(currEntropy);
			if (currEntropy < minEnt){
				minEnt = currEntropy;
				minEntAtt = a;
			}
		}
		
		
		// split on attribute
//		System.out.println();
		for (String option : options.get(minEntAtt)){
			List<String> stillRemainingAttributes = new ArrayList<String>(remaining);
			stillRemainingAttributes.remove(minEntAtt);
			List<List<String>> stillRemaining = new ArrayList<List<String>>();
			for (List<String> l : remainingEntries){
				if (l.get(attributeMap.get(minEntAtt)).equals(option)){
					stillRemaining.add(l);
				}
			}
			EntriesStats e = new EntriesStats(stillRemaining, possibilities);
			int tot = e.totalNonEmpty;
			if (tot < 2 || stillRemainingAttributes.size() == 0 || stillRemaining.size() == 0){
				if (tot == 0){
					// no leaf for empty set
					continue;
				}
				Leaf newLeaf = new Leaf(e.counts, new Attribute(minEntAtt, option));
				previous.addElement(newLeaf);
			} else{
				Node newNode = new Node(e.counts, new Attribute(minEntAtt, option));
				previous.addElement(newNode);
				buildTree(newNode, stillRemainingAttributes, stillRemaining);
			}
			
		}
		
	}

	private static double calculateEntropy(String a, List<List<String>> remainingEntries) {

		List<String> currOptions = options.get(a);
		
		double[] tots = new double[currOptions.size()];
		double[] ents = new double[currOptions.size()];
		
		for (int i = 0; i < currOptions.size(); i++){
			String s = currOptions.get(i);
			Map<String, Integer> positions = new HashMap<String, Integer>();
			for (String s1 : possibilities){
				positions.put(s1, 0);
			}
			List<List<String>> remainingAfterFilter = filter(s, attributeMap.get(a), remainingEntries);
			
			for (List<String> l : remainingAfterFilter){
				positions.put(l.get(l.size()-1), positions.get(l.get(l.size()-1))+1);
				
			}
			// weight
			tots[i] = remainingAfterFilter.size();
			if (tots[i] == 0){
				ents[i] = 0;
				continue;
			}
			
			int found = 0;
			for (Integer pV : positions.values()){
				if (pV > 0){
					found++;
				}
			}
			
			if (found <= 1){
				// zero entropy
				ents[i] = 0;
			} else{
				double currEntr = 0;
				for (String s1 : possibilities){
					if (positions.get(s1) != 0){
						currEntr -= (positions.get(s1)/tots[i]) * log2(positions.get(s1)/tots[i]);
					}
				}
//				double tot = totY + totN;
//				currEntr = - (totY/tot) * log2(totY/tot) - (totN/tot) * log2(totN/tot);
				ents[i] = currEntr;
			}
			
		}
		
		double totalPop = 0;
		double totalEnt = 0;
		for (int i = 0; i < tots.length; i++){
			totalPop += tots[i];
			totalEnt += ents[i] * tots[i];
		}

		if (totalPop == 0){
			return 1;
		}
		
		return totalEnt / totalPop;
	}
	
	private static List<List<String>> filter(String s, int pos, List<List<String>> remainingEntries) {
		List<List<String>> toReturn = new ArrayList<List<String>>();
		for (List<String> l : remainingEntries){
			if (l.get(pos).equals(s)){
				toReturn.add(l);
			}
		}
		return toReturn;
	}

	public static double log2(double n)
	{
	    return (Math.log(n) / Math.log(2));
	}

	private static void parse() throws IOException, InvalidAttributesException {
		attributes = new ArrayList<String>();
		possibilities = new ArrayList<String>();
		entries = new ArrayList<List<String>>();
		options = new HashMap<String, List<String>>();
		attributeMap = new HashMap<String, Integer>();
		
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		
		String readLine = null;
		
		readLine = reader.readLine();

		// comments
		while (readLine.startsWith("%")){
			readLine = reader.readLine();
		}
		
		//whitespace
		while (readLine.equals("")){
			readLine = reader.readLine();
		}
		
		//relation
		if (readLine.split(" ")[0].toLowerCase().equals("@relation")){
			relation = readLine.split(" ")[1].toLowerCase();		}
		System.out.println("Relation " + relation);
		
		readLine = reader.readLine();
				
		while (readLine.equals("")){
			readLine = reader.readLine();
		}
		
		int counter = 0;
		while (readLine.split(" ")[0].toLowerCase().equals("@attribute")){
			readLine = readLine.replace("{", "").replace("}", "").replace(",","");
			String[] array = readLine.split(" ");
			attributeMap.put(array[1], counter);
			attributes.add(array[1]);
			readLine = reader.readLine();
			counter++;
		}
		
		while (readLine.equals("")){
			readLine = reader.readLine();
		}
		
		if (!readLine.trim().toLowerCase().equals("@data")){
			System.out.println("no @data");
//			System.out.println(readLine);
			reader.close();
			throw new InvalidAttributesException();

		}	
		
		readLine = reader.readLine();
		while (readLine.startsWith("%")){
			readLine = reader.readLine();
		}
		
		
		for (String s : attributes){
			options.put(s, new ArrayList<String>());
		}
		while (readLine != null && !readLine.equals("")){
			String array[] = readLine.split(",");
			ArrayList<String> newEntry = new ArrayList<String>();
			for (int i = 0; i < array.length-1; i++){
				newEntry.add(array[i]);
				List<String> curr = options.get(attributes.get(i));
//				System.out.println(array[i]);
				if (!curr.contains(array[i])){
					curr.add(array[i]);
				}
			}
			if (!possibilities.contains(array[array.length-1])){
				possibilities.add(array[array.length-1]);
			}
			newEntry.add(array[array.length-1]);
			entries.add(newEntry);
			readLine = reader.readLine();
			
		}
		
		
//		System.out.println("Entries: ");
//		for (List<String> l : entries){
//			System.out.println();
//			for (String s : l){
//				System.out.print(s + " ");
//			}
//		}
//		System.out.println();

		
		 reader.close();
		
	}
	

}
