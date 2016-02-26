package eda132_lab3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.InvalidAttributesException;



public class Main {

	public static String relation;
	public static List<String> attributes;
	public static Map<String, List<String>> options;
	public static Map<String, Integer> attributeMap;
	public static List<List<String>> entries;
	


	public static void main(String[] args) throws IOException, InvalidAttributesException {
		
		parse();
		
		EntriesStats e = new EntriesStats(entries);
		System.out.println("Decision tree contains: " + e.totY + " positives & " + e.totN + " negatives");
		attributes.remove(attributes.size()-1);
		Node root = new Node(e.totY, e.totN, new Attribute("Root", ""));
		buildTree(root, attributes, entries);
		
		root.print("");

		
	}

	private static void buildTree(Node previous, Collection<String> remainingAttributes, List<List<String>> remainingEntries) throws InvalidAttributesException {
		
		double minEnt = Double.MAX_VALUE;
		String minEntAtt = null;
		double currEntropy;
		for (String a : remainingAttributes){
			// calculate entropy for each..
			currEntropy = calculateEntropy(a, remainingEntries);
			System.out.println("Entropy for: " + a + " = " + currEntropy);
			if (currEntropy < minEnt){
				minEnt = currEntropy;
				minEntAtt = a;
			}
		}
		
		
		// split on attribute
//		System.out.println();
		for (String option : options.get(minEntAtt)){
			List<String> stillRemainingAttributes = new ArrayList<String>(remainingAttributes);
			stillRemainingAttributes.remove(minEntAtt);
			List<List<String>> stillRemaining = new ArrayList<List<String>>();
			for (List<String> l : remainingEntries){
				if (l.get(attributeMap.get(minEntAtt)).equals(option)){
					stillRemaining.add(l);
				}
			}
			EntriesStats e = new EntriesStats(stillRemaining);
			if (e.totY == 0 || e.totN == 0 || stillRemainingAttributes.size() == 0 || stillRemaining.size() == 0){
				if (e.tot == 0){
					continue;
				}
				Leaf newLeaf = new Leaf(e.totY, e.totN, new Attribute(minEntAtt, option));
				previous.addElement(newLeaf);
			} else{
				Node newNode = new Node(e.totY, e.totN, new Attribute(minEntAtt, option));
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
			double totY = 0;
			double totN = 0;
			for (List<String> l : remainingEntries){
				if (l.get(attributeMap.get(a)).equals(s)){
					if (l.get(l.size()-1).toLowerCase().equals("true")){
						// entry and true
						totY++;
					} else{
						// entry and false
						totN++;
					}
				} else{
					if (l.get(l.size()-1).toLowerCase().equals("true")){
						// not entry and true
						totN++;
					} else{
						// not entry and false
						totY++;
					}
				}
			}
			// weight
			tots[i] = totY + totN;
			if (totY == 0 || totN == 0){
				// zero entropy
				ents[i] = 0;
			} else{
				double tot = totY + totN;
				double currEntr = - (totY/tot) * log2(totY/tot) - (totN/tot) * log2(totN/tot);
				ents[i] = currEntr;
			}
			
		}
		
		double totalPop = 0;
		double totalEnt = 0;
		for (int i = 0; i < tots.length; i++){
			totalPop += tots[i];
			totalEnt += ents[i] * tots[i];
		}
			
		
		return totalEnt / totalPop;
	}
	
	public static double log2(double n)
	{
	    return (Math.log(n) / Math.log(2));
	}

	private static void parse() throws IOException, InvalidAttributesException {
		attributes = new ArrayList<String>();
		entries = new ArrayList<List<String>>();
		options = new HashMap<String, List<String>>();
		attributeMap = new HashMap<String, Integer>();
		
		BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
		
		String readLine = null;
		
		readLine = reader.readLine();

		// comments
		while (readLine.equals("%")){
			readLine = reader.readLine();
		}
		
		//whitespace
		while (readLine.equals("")){
			readLine = reader.readLine();
		}
		
		//relation
		if (readLine.split(" ")[0].toLowerCase().equals("@relation")){
			relation = readLine.split(" ")[1].toLowerCase();
		}
//		System.out.println("Relation " + relation);
		
		readLine = reader.readLine();
				
		while (readLine.equals("")){
			readLine = reader.readLine();
		}
		
		int counter = 0;
		while (readLine.split(" ")[0].equals("@ATTRIBUTE")){
			String[] array = readLine.split(" ");
			attributeMap.put(array[1], counter);
//			attributes.add(new Attribute(array[1], array[2]));
			attributes.add(array[1]);
			readLine = reader.readLine();
			counter++;
		}
		
		while (readLine.equals("")){
			readLine = reader.readLine();
		}
		
		if (!readLine.startsWith("@DATA")){
			System.out.println("no @data");
			reader.close();
			throw new InvalidAttributesException();

		}	
		
		readLine = reader.readLine();
		
		for (String s : attributes){
			options.put(s, new ArrayList<String>());
		}
		while (readLine != null && !readLine.equals("")){
			String array[] = readLine.split(",");
			ArrayList<String> newEntry = new ArrayList<String>();
			for (int i = 0; i < array.length-1; i++){
				newEntry.add(array[i]);
				List<String> curr = options.get(attributes.get(i));
				if (!curr.contains(array[i])){
					curr.add(array[i]);
				}
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
