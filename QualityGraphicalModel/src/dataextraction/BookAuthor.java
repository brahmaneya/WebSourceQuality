package dataextraction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.System.out;

/**
 * This dataset consists of books, author pairs from different sources, from abebooks.com. It has 100 cleaned and labelled
 * examples by Jiawei Han et al. 
 * @author manasrj
 *
 */
public class BookAuthor {
	private final static String DATAFILE = "TestDatasets/BookAuthor/book.txt";
	private final static String LABELLEDDATAFILE = "TestDatasets/BookAuthor/book_truth.txt";
	
	/**
	 * Canonical name for an author is a lowercase version of the author's last name.
	 */
	private static String canonicalize (String authorName) {
		if (authorName.contains(",")) {
			return authorName.substring(0, authorName.indexOf(',')).toLowerCase();
		} else if( authorName.contains(" ")) {
			return authorName.substring(authorName.lastIndexOf(' ') + 1, authorName.length()).toLowerCase();
		} else {
			return authorName.toLowerCase();
		}
	}
	
	private static List<String> parseAuthorString (String authorString) {
		List<String> authorList = new ArrayList<String>();
		if (authorString.contains("Not Available")) {
			return authorList;
		} else if (authorString.contains(";")) {
			String[] authors = authorString.split(";");
			for (String authorName : authors) {
				authorList.add(canonicalize(authorName));
			}
			return authorList;
		} else if (authorString.contains("/ ")) {
			String[] authors = authorString.split("/ ");
			for (String authorName : authors) {
				authorList.add(canonicalize(authorName));
			}
			return authorList;			
		} else if (authorString.contains("|")) {
			String[] authors = authorString.split("//|");
			for (String authorName : authors) {
				authorList.add(canonicalize(authorName));
			}
			return authorList;			
		} else if (authorString.indexOf(' ') != -1 && authorString.indexOf(',') > authorString.indexOf(' ')) {
			String[] authors = authorString.split(",");
			for (String authorName : authors) {
				authorList.add(canonicalize(authorName));
			}
			return authorList;						
		} else if (authorString.contains(",")) {
			String[] authors = authorString.split(",");
			for (int i = 0; i < authors.length - 1; i++) {
				authorList.add(canonicalize(authors[i]));
			}
			return authorList;		
		} else {
			authorList.add(canonicalize(authorString));	
			return authorList;
		}
	}
	
	/**
	 * Parses datafile and populates the sourceOutputStrings and trueTuples arguments, where each book author pair is 
	 * represented as a single string of the form bookId + "\t" + author.
	 */
	static void parseData(Map<String, Set<String>> sourceOutputStrings, Set<String> trueTuples) throws IOException{
		BufferedReader dataFile = new BufferedReader(new FileReader(DATAFILE));
		
		Set<String> bookSet = new HashSet<String>();
		Set<String> authorSet = new HashSet<String>();
		Set<String> tupleSet = new HashSet<String>();
		Set<String> sourceSet = new HashSet<String>();
		
		String s;
		while ((s = dataFile.readLine()) != null) {
			if (s.indexOf('\t') == -1) {
				continue;
			}
			String[] fields = s.split("\t");
			if (fields.length != 4) {
				continue;
			}
			final String sourceId = fields[0];
			final String bookId = fields[1];
			final String authorString = fields[3];
			List<String> authorList = parseAuthorString(authorString);
			
			bookSet.add(bookId);
			authorSet.addAll(authorList);
			sourceSet.add(sourceId);
			if (!sourceOutputStrings.containsKey(sourceId)) {
				sourceOutputStrings.put(sourceId, new HashSet<String>());
			}
			for (String author : authorList) {
				final String tuple = bookId + "\t" + author;
				tupleSet.add(tuple);
				sourceOutputStrings.get(sourceId).add(tuple);
			}
		}
		dataFile.close();

		BufferedReader labelledDataFile = new BufferedReader(new FileReader(LABELLEDDATAFILE));		
		while ((s = labelledDataFile.readLine()) != null) {
			if (s.indexOf('\t') == -1) {
				continue;
			}
			String[] fields = s.split("\t");
			final String bookId = fields[0];
			final String authorString = fields[1];
			String[] authors = authorString.split(" ;  ");
			for (int i = 0; i < authors.length; i++) {
				authors[i] = canonicalize(authors[i]);
				trueTuples.add(bookId + "\t" + authors[i]);
			}
		}
		
		labelledDataFile.close();
	}
	
	public static void main (String[] args) throws IOException {
		Map<String, Set<String>> sourceOutputStrings = new HashMap<String, Set<String>>();
		Set<String> trueTuples = new HashSet<String>();
		parseData(sourceOutputStrings, trueTuples);
		out.println(trueTuples.size());
	}
}
