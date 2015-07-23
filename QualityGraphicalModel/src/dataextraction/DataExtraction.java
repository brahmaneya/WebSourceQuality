package dataextraction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import GroupSourceModel.DenseSample;
import GroupSourceModel.ModelInstance;
import static java.lang.System.out;

/**
 * This dataset consists of books, author pairs from different sources, from abebooks.com. It has 100 cleaned and labelled
 * examples by Jiawei Han et al. 
 * @author manasrj
 *
 */
public class DataExtraction {
	private final static String DATAFILE = "TestDatasets/BookAuthor/book.txt";
	private final static String ISBNTRUTHFILE = "TestDatasets/BookAuthor/isbn_book_truth.txt";
	

	public static String removeAccents(String text) {
		return text == null ? null : Normalizer.normalize(text, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
	/**
	 * Canonical name for an author is a lowercase version of the author's last name.
	 */
	private static String canonicalize (String authorName) {
		if (authorName.contains(",")) {
			return removeAccents(authorName.substring(0, authorName.indexOf(',')).toLowerCase());
		} else if( authorName.contains(" ")) {
			return removeAccents(authorName.substring(authorName.lastIndexOf(' ') + 1, authorName.length()).toLowerCase());
		} else {
			return removeAccents(authorName.toLowerCase());
		}
	}
	
	private static List<String> parseTrueAuthorString (String authorString) {				
		List<String> authorList = new ArrayList<String>();
		
		String[] authors = authorString.split(";");
		for (String authorName : authors) {
			authorList.add(canonicalize(authorName.trim()));
		}
		return authorList;
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
		} else if (authorString.contains(" / ")) {
			String[] authors = authorString.split(" / ");
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
			String[] authors = authorString.split("\\|");
			for (String authorName : authors) {
				authorList.add(canonicalize(authorName));
			}
			return authorList;			
		} else if (authorString.contains(" , ")) {
			String[] authors = authorString.split(" , ");
			for (int i = 0; i < authors.length - 1; i++) {
				authorList.add(canonicalize(authors[i]));
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
	static void parseData(Map<String, Set<String>> sourceOutputStrings) throws IOException{
		BufferedReader dataFile = new BufferedReader(new FileReader(DATAFILE));
		
		Set<String> bookSet = new HashSet<String>();
		Set<String> authorSet = new HashSet<String>();
		Set<String> tupleSet = new HashSet<String>();
		Set<String> sourceSet = new HashSet<String>();
		
		String s;
		Set<String> sourceBlackList = new HashSet<String>();
		// remove below books? increases isolated errors, although there should still be correlations between errors such as using first name
		sourceBlackList.add("A1Books");
		sourceBlackList.add("Indoo.com");
		sourceBlackList.add("Movies With A Smile");
		sourceBlackList.add("Bobs Books");
		sourceBlackList.add("Gunars Store");
		sourceBlackList.add("Gunter Koppon");
		sourceBlackList.add("Quartermelon");
		sourceBlackList.add("Stratford Books");
		sourceBlackList.add("LAKESIDE BOOKS");
		sourceBlackList.add("Books2Anywhere.com");
		sourceBlackList.add("Paperbackshop-US");
		sourceBlackList.add("tombargainbks");
		sourceBlackList.add("Papamedia.com");
		sourceBlackList.add("");
		sourceBlackList.add("");
		sourceBlackList.add("");
		
		Pattern pattern = Pattern.compile("[^a-z]", Pattern.CASE_INSENSITIVE);
		while ((s = dataFile.readLine()) != null) {
			if (s.indexOf('\t') == -1) {
				continue;
			}
			String[] fields = s.split("\t");
			if (fields.length != 4) {
				continue;
			}
			final String sourceId = fields[0];
			if (sourceBlackList.contains(sourceId)) {
				continue;
			}
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
				Matcher matcher = pattern.matcher(author.trim());
				if (matcher.find()) {
					continue; // Skip author names that have a special character, since they are likely a parsing error.
				}
				if (author.equals("")) { // Sometimes, trailing commas result in empty author strings. 
					continue;
				}
				final String tuple = bookId + "\t" + author.trim();
				tupleSet.add(tuple);
				sourceOutputStrings.get(sourceId).add(tuple);
			}
		}
		dataFile.close();
	}
	
	static void parseTrueData(Map<String, Set<String>> trueBookAuthors) throws IOException{
		BufferedReader dataFile = new BufferedReader(new FileReader(ISBNTRUTHFILE));
									
		String s;		
		
		Pattern pattern = Pattern.compile("[^a-z]", Pattern.CASE_INSENSITIVE);
		while ((s = dataFile.readLine()) != null) {
			if (s.indexOf('\t') == -1) {
				continue;
			}
			String[] fields = s.split("\t");
			if (fields.length != 2) {
				continue;
			}
			final String bookId = fields[0];						
			final String authorString = fields[1];			
			List<String> authorList = parseTrueAuthorString(authorString);					
			Set<String> authorSet = new HashSet<String>();
			authorSet.addAll(authorList);			
			trueBookAuthors.put(bookId,authorSet);			
		}
		dataFile.close();
	}
	
	
	public static void main (String[] args) throws IOException, ClassNotFoundException {
		Map<String, Set<String>> sourceOutputStrings = new HashMap<String, Set<String>>();		
		parseData(sourceOutputStrings);
		Map<String, Set<String>> trueBookAuthors = new HashMap<String, Set<String>>();
		parseTrueData(trueBookAuthors);		
						
		Integer bookId = 0;
		Integer authId = 0;		
		Map<String, Integer> bookIdMap = new HashMap<String,Integer>();
		Map<String, Integer> authIdMap = new HashMap<String,Integer>();
		
		
		//Iterate over sources  and generate book author ids		
		
		for (String s : sourceOutputStrings.keySet()) {			
			Set<String> bookTuples = sourceOutputStrings.get(s);
			for (String bTuple : bookTuples) {
				//Split bookid author token
				String[] bTupleTokens = bTuple.split("\t");
				//Get book key 
				if (!bookIdMap.containsKey(bTupleTokens[0])) {					
					bookIdMap.put(bTupleTokens[0], bookId);
					bookId++;
				}				
								
				//Get author key
				if (!authIdMap.containsKey(bTupleTokens[1])) {					
					authIdMap.put(bTupleTokens[1], authId);
					authId++;
				}											
			}
		}
		
		//Iterate over sources and grab all authors for each book
		Map<String, Set<String>> bookAuthors = new HashMap<String, Set<String>>();		
		
		for (String s : sourceOutputStrings.keySet()) {			
			Set<String> bookTuples = sourceOutputStrings.get(s);
			for (String bTuple : bookTuples) {
				//Split bookid author token
				String[] bTupleTokens = bTuple.split("\t");
				if (!bookAuthors.containsKey(bTupleTokens[0])) {
					Set<String> authors = new HashSet<String>();
					authors.add(bTupleTokens[1]);
					bookAuthors.put(bTupleTokens[0], authors);
				}
				else {
					bookAuthors.get(bTupleTokens[0]).add(bTupleTokens[1]);
				}
			}
		}
		
		// Print all authors per book to output file
		PrintWriter writer = new PrintWriter("bookAuthor.csv");
				
		for (String b : bookAuthors.keySet()) {
			for (String a : bookAuthors.get(b)) {				
				writer.println(bookIdMap.get(b).toString() + "," + authIdMap.get(a).toString());				
			}
		}
		writer.close();
		
		
		//Iterate over sources and print source, bookid, author_id, is_true
		PrintWriter writer2 = new PrintWriter("srcBookAuthor.csv");
		int srcId = 0;
		for (String s : sourceOutputStrings.keySet()) {
			Map<String, Set<String>> srcBookAuthors = new HashMap<String, Set<String>>();
			Set<String> bookTuples = sourceOutputStrings.get(s);
			for (String bTuple : bookTuples) {
				//Split bookid author token
				String[] bTupleTokens = bTuple.split("\t");
				if (!srcBookAuthors.containsKey(bTupleTokens[0])) {
					Set<String> authors = new HashSet<String>();
					authors.add(bTupleTokens[1]);
					srcBookAuthors.put(bTupleTokens[0], authors);
				}
				else {
					srcBookAuthors.get(bTupleTokens[0]).add(bTupleTokens[1]);
				}
			}
			
			//write source info
			for (String b : srcBookAuthors.keySet()) {
				for (String a : srcBookAuthors.get(b)) {					
						writer2.println(srcId+","+bookIdMap.get(b).toString()+","+authIdMap.get(a).toString()+",true");				
				}
			}
			srcId+=1;
		}
		writer2.close();
		
		PrintWriter writer3 = new PrintWriter("bookAuthorTrue.csv");
		//Compare book authors and true authors
		for (String tbookId : trueBookAuthors.keySet()) {
			if (!bookAuthors.containsKey(tbookId)) {
				continue;
			}			
			Set<String> trueAuthors = trueBookAuthors.get(tbookId);
			for (String tauth : trueAuthors) {
				if (!authIdMap.containsKey(tauth)) {					
					authIdMap.put(tauth, authId);
					authId++;
				}	
			}
			Set<String> mentionedAuthors = bookAuthors.get(tbookId);
			
			Set<String> allAuthors = new HashSet<String>();
			allAuthors.addAll(trueAuthors);
			allAuthors.addAll(mentionedAuthors);
			
			for (String author : allAuthors) {
				if (trueAuthors.contains(author)) {
					writer3.println(bookIdMap.get(tbookId).toString()+","+authIdMap.get(author).toString()+",true");
				}
				else {
					writer3.println(bookIdMap.get(tbookId).toString()+","+authIdMap.get(author).toString()+",false");
				}
			}
			
		}
		writer3.close();
				
	}
}
