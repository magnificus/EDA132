package eda132_lab3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Main {

	public static String relation;
	public static List<Attribute> attributes;
	public static List<List<String>> entries;

	public static void main(String[] args) throws IOException {

		attributes = new ArrayList<Attribute>();
		entries = new ArrayList<List<String>>();
		
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
		System.out.println("Relation " + relation);
		
		readLine = reader.readLine();
				
		while (readLine.equals("")){
			readLine = reader.readLine();
		}
		
		while (readLine.split(" ")[0].equals("@ATTRIBUTE")){
			String[] array = readLine.split(" ");
			attributes.add(new Attribute(array[1], array[2]));
			readLine = reader.readLine();
		}
		System.out.println(readLine);
		
		System.out.println("Attributes: ");
		for (Attribute a : attributes){
			System.out.println(a.name + " " + a.type);
		}
		
		while (readLine.equals("")){
			readLine = reader.readLine();
		}
		
		if (!readLine.startsWith("@DATA")){
			System.out.println("nodata");
			System.exit(0);
		}	
		
		readLine = reader.readLine();
		
		while (readLine != null && !readLine.equals("")){
			String array[] = readLine.split(",");
			ArrayList<String> newEntry = new ArrayList<String>();
			for (int i = 0; i < array.length; i++){
				newEntry.add(array[i]);
			}
			entries.add(newEntry);
			readLine = reader.readLine();
			
		}
		
		
		System.out.println("Entries: ");
		for (List<String> l : entries){
			System.out.println();
			for (String s : l){
				System.out.print(s + " ");
			}
		}
		
		 reader.close();
	}
	

}
