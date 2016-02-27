package eda132_lab3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.application.Application;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import javax.naming.directory.InvalidAttributesException;

import comparators.DoubleComparator;
import comparators.StringComparator;
import comparators.ValueComparator;

public class Main extends Application {

	public static String fileName = "input.txt";

	public static String relation;
	// possible "splitters", known factors
	public static List<String> attributes;
	// possible answers, i.e. True and False of binary question
	public static List<String> possibilities;
	// possible alternatives for each factor
	public static Map<String, List<ValueComparator>> options;
	// map to get attribute position in each data entry
	public static Map<String, Integer> attributeMap;
	// the data entries
	public static List<List<String>> entries;

	
	
	public static void launch(String[] newName) {

		// potentially pass filename as parameter
		if (newName != null && newName.length > 0) {
			fileName = newName[0];
		}

		try {
			parse();
		} catch (InvalidAttributesException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		EntriesStats e = null;
		try {
			e = new EntriesStats(entries, possibilities);
		} catch (InvalidAttributesException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Decision tree possibilities: " + e.counts.size());
		attributes.remove(attributes.size() - 1);
		Node root = new Node(e.counts, new Attribute("Root", new StringComparator("")));
		try {
			buildTree(root, attributes, entries);
		} catch (InvalidAttributesException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("\nFull decision tree: ");
		root.print("", true);
		
		
		// now search in it
		String[] recieved = new String[attributes.size()];
		for (int i = 0; i < recieved.length; i++) {
			String alts = "";
			for (ValueComparator v : options.get(attributes.get(i))){
				alts += v.toString() + " ";
			}
			TextInputDialog inp = new TextInputDialog();
			inp.setTitle("Input");
			inp.setHeaderText(attributes.get(i));
			inp.setContentText(alts);
			Optional<String> result = inp.showAndWait();
			recieved[i] = result.get();

		}
		
		System.out.println("\nTree traversal for user input: ");
		root.search(recieved, "");

	}

	private static void buildTree(Node previous, List<String> remaining, List<List<String>> remainingEntries) throws InvalidAttributesException {

		double minEnt = Double.MAX_VALUE;
		String minEntAtt = null;
		double currEntropy;
		for (String a : remaining) {
			// calculate entropy for each..
			currEntropy = calculateEntropy(a, remainingEntries);
			// System.out.println(currEntropy);
			if (currEntropy < minEnt) {
				minEnt = currEntropy;
				minEntAtt = a;
			}
		}

		// split on attributes
		for (ValueComparator option : options.get(minEntAtt)) {
			List<String> stillRemainingAttributes = new ArrayList<String>(remaining);
			stillRemainingAttributes.remove(minEntAtt);
			List<List<String>> stillRemaining = new ArrayList<List<String>>();
			for (List<String> l : remainingEntries) {
				if (option.compare(l.get(attributeMap.get(minEntAtt)))) {
					stillRemaining.add(l);
				}
			}
			EntriesStats e = new EntriesStats(stillRemaining, possibilities);
			int tot = e.totalNonEmpty;
			if (tot < 2 || stillRemainingAttributes.size() == 0 || stillRemaining.size() == 0) {
				if (tot == 0) {
					// no leaf for empty set
					continue;
				}
				Leaf newLeaf = new Leaf(e.counts, new Attribute(minEntAtt, option));
				previous.addElement(newLeaf);
			} else {
				Node newNode = new Node(e.counts, new Attribute(minEntAtt, option));
				previous.addElement(newNode);
				buildTree(newNode, stillRemainingAttributes, stillRemaining);
			}

		}

	}

	private static double calculateEntropy(String a, List<List<String>> remainingEntries) {

		List<ValueComparator> currOptions = options.get(a);

		double[] tots = new double[currOptions.size()];
		double[] ents = new double[currOptions.size()];

		for (int i = 0; i < currOptions.size(); i++) {
			ValueComparator v = currOptions.get(i);
			Map<String, Integer> positions = new HashMap<String, Integer>();
			for (String s1 : possibilities) {
				positions.put(s1, 0);
			}
			List<List<String>> remainingAfterFilter = filter(v, attributeMap.get(a), remainingEntries);

			for (List<String> l : remainingAfterFilter) {
				positions.put(l.get(l.size() - 1), positions.get(l.get(l.size() - 1)) + 1);

			}
			// weight
			tots[i] = remainingAfterFilter.size();
			if (tots[i] == 0) {
				ents[i] = 0;
				continue;
			}

			int found = 0;
			for (Integer pV : positions.values()) {
				if (pV > 0) {
					found++;
				}
			}

			if (found <= 1) {
				// zero entropy
				ents[i] = 0;
			} else {
				double currEntr = 0;
				for (String s1 : possibilities) {
					if (positions.get(s1) != 0) {
						currEntr -= (positions.get(s1) / tots[i]) * log2(positions.get(s1) / tots[i]);
					}
				}
				ents[i] = currEntr;
			}

		}

		double totalPop = 0;
		double totalEnt = 0;
		for (int i = 0; i < tots.length; i++) {
			totalPop += tots[i];
			totalEnt += ents[i] * tots[i];
		}

		if (totalPop == 0) {
			return 1;
		}

		return totalEnt / totalPop;
	}

	private static List<List<String>> filter(ValueComparator v, int pos, List<List<String>> remainingEntries) {
		List<List<String>> toReturn = new ArrayList<List<String>>();
		for (List<String> l : remainingEntries) {
			if (v.compare(l.get(pos))) {
				toReturn.add(l);
			}
		}
		return toReturn;
	}

	public static double log2(double n) {
		return (Math.log(n) / Math.log(2));
	}

	private static void parse() throws IOException, InvalidAttributesException {
		attributes = new ArrayList<String>();
		possibilities = new ArrayList<String>();
		entries = new ArrayList<List<String>>();
		options = new HashMap<String, List<ValueComparator>>();
		attributeMap = new HashMap<String, Integer>();

		BufferedReader reader = new BufferedReader(new FileReader(fileName));

		String readLine = null;

		readLine = reader.readLine();

		// comments
		while (readLine.startsWith("%")) {
			readLine = reader.readLine();
		}

		// whitespace
		while (readLine.equals("")) {
			readLine = reader.readLine();
		}

		// relation
		if (readLine.split(" ")[0].toLowerCase().equals("@relation")) {
			relation = readLine.split(" ")[1].toLowerCase();
		}
		System.out.println("Relation " + relation);

		readLine = reader.readLine();

		while (readLine.equals("")) {
			readLine = reader.readLine();
		}

		int counter = 0;
		while (readLine.split(" ")[0].toLowerCase().equals("@attribute")) {
			// remove most of the annoying signs, care only about spaces
			readLine = readLine.replace("{", "").replace("}", "").replace(",", "");
			String[] array = readLine.split(" ");
			attributeMap.put(array[1], counter);
			attributes.add(array[1]);
			readLine = reader.readLine();
			counter++;
		}

		while (readLine.equals("")) {
			readLine = reader.readLine();
		}

		if (!readLine.trim().toLowerCase().equals("@data")) {
			System.out.println("no @data");
			reader.close();
			throw new InvalidAttributesException();

		}

		readLine = reader.readLine();
		while (readLine.startsWith("%")) {
			readLine = reader.readLine();
		}

		// set up list of options for all attributes
		Map<String, List<String>> preliminaryComparators = new HashMap<String, List<String>>();
		for (String s : attributes) {
			preliminaryComparators.put(s, new ArrayList<String>());
		}

		// add all the data
		while (readLine != null && !readLine.equals("")) {
			String array[] = readLine.split(",");
			ArrayList<String> newEntry = new ArrayList<String>();
			for (int i = 0; i < array.length - 1; i++) {
				// the recieved data, could be numbers or strings
				newEntry.add(array[i]);
				List<String> curr = preliminaryComparators.get(attributes.get(i));
				curr.add(array[i]);
			}
			// the result, i.e. most often only true or false, make sure that
			// the current result exists as a possibility
			if (!possibilities.contains(array[array.length - 1])) {
				possibilities.add(array[array.length - 1]);
			}
			newEntry.add(array[array.length - 1]);
			entries.add(newEntry);
			readLine = reader.readLine();
		}

		decideFormat(preliminaryComparators);

		reader.close();

	}

	private static void decideFormat(Map<String, List<String>> preliminaryComparators) {
		// decide after looking at first element in list whether it's a
		// numerical measurement or not
		for (String key : preliminaryComparators.keySet()) {
			List<ValueComparator> comps = new ArrayList<ValueComparator>();
			List<String> l = preliminaryComparators.get(key);
			if (l.size() == 0) {
				continue;
			}
			if (isNumeric(l.get(0))) {
				// since numeric, split in 2 parts
				double total = 0;
				for (String s : l) {
					total += Double.parseDouble(s);
				}
				double average = total / l.size();
				comps.add(new DoubleComparator(average, true));
				comps.add(new DoubleComparator(average, false));
				options.put(key, comps);

			} else {
				// since string, only compare with the exact same string
				Set<String> used = new HashSet<String>();
				for (String s : l) {
					if (used.add(s)) {
						comps.add(new StringComparator(s));
					}
				}
				options.put(key, comps);
			}
		}

	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	@Override
	public void start(Stage arg0) throws Exception {
		launch(null);
		
	}

}
