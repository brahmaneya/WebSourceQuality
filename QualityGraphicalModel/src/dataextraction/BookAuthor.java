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
public class BookAuthor {
	private final static String DATAFILE = "TestDatasets/BookAuthor/book.txt";
	private final static String LABELLEDDATAFILE = "TestDatasets/BookAuthor/book_truth.txt";
	private final static String ISBNLABELLEDDATAFILE = "TestDatasets/BookAuthor/isbn_book_truth.txt";
	

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
	static void parseData(Map<String, Set<String>> sourceOutputStrings, Set<String> trueTuples) throws IOException{
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
				//continue;
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

		BufferedReader labelledDataFile = new BufferedReader(new FileReader(ISBNLABELLEDDATAFILE));		
		while ((s = labelledDataFile.readLine()) != null) {
			String[] fields = s.split("\t");
			if (fields.length < 2) {
				continue;
			}
			final String bookId = fields[0];
			final String authorString = fields[1];
			String[] authors = authorString.split(";  ");
			for (int i = 0; i < authors.length; i++) {
				authors[i] = canonicalize(authors[i].trim());
				trueTuples.add(bookId + "\t" + authors[i]);
			}
		}
		
		labelledDataFile.close();
	}
	
	/**
	 * Searches books referred to by sources in the site isbnsearch.org, and extracts authors from there. 
	 */
	static void createAuthorsFile () throws IOException {
		BufferedReader dataFile = new BufferedReader(new FileReader(DATAFILE));
		String inputLine;
		
		PrintWriter isbnLabeledDataFile = new PrintWriter(new FileWriter(ISBNLABELLEDDATAFILE));
		
		Set<String> books = new HashSet<String>();
		while ((inputLine = dataFile.readLine()) != null) {
			if (inputLine.indexOf('\t') == -1) {
				continue;
			}
			String[] fields = inputLine.split("\t");
			if (fields.length != 4) {
				continue;
			}
			final String bookId = fields[1];
			if (books.contains(bookId)) {
				continue;
			} else {
				books.add(bookId);
				String authorString = getAuthorString(bookId);
				isbnLabeledDataFile.println(bookId + "\t" + authorString);
				out.println(bookId + "\t" + authorString);
			}
		}
		dataFile.close();
		isbnLabeledDataFile.close();
	}
	
	static String getAuthorString (String bookId) throws IOException {
		final String siteString = "http://www.isbnsearch.org/isbn/";
		final String urlString = siteString + bookId;
		URL url = new URL(urlString);
	    URLConnection uc = url.openConnection();

	    InputStreamReader input = new InputStreamReader(uc.getInputStream());
	    BufferedReader in = new BufferedReader(input);
		String authorString = "";
	    String inputLine;
	    while ((inputLine = in.readLine()) != null) {
	    	if (inputLine.indexOf("<p><strong>Author") != -1) {
	    		authorString = inputLine.substring(8 + inputLine.indexOf("strong> "), inputLine.indexOf("</p>"));
	    	} 
	    }
	    authorString = authorString.replaceAll(";", "; ");
	    authorString = authorString.replaceAll("Ph.D.", "").replaceAll("B.Sc.", "").replaceAll("D.Sc.", "")
				.replaceAll("Dip.Ed", "").replaceAll("MS  BS.", "");
		// In addition, there may be long suffixes, such as "employee of..." or "Technical Head..." that need to be removed.
	    in.close();
		return authorString;
	}
	
	static void createSourceFeaturesFile (List<String> sources, String outputFile) throws IOException {
		PrintWriter outputFileStream = new PrintWriter(new FileWriter(outputFile));
		for (String source : sources) {
			String sourceURL = source;
			out.println(source);
			sourceURL = sourceURL.replaceAll(" ", "");
			if (sourceURL.indexOf(".") == -1) {
				sourceURL = sourceURL + ".com";
			}
			try{
				Map<String, String> features = getSourceFeatures(sourceURL);
				outputFileStream.println(source + "\t" + features.toString());
			} catch (Exception e) {
				out.println(e.toString());
			}
		}
		outputFileStream.close();
	}
	
	public static Map<String, Map<String, String>> createSourceFeaturesMap (List<String> sources) {
		Map<String, Map<String, String>> sourceFeatures = new HashMap<String, Map<String, String>>();
		for (String source : sources) {
			String sourceURL = source;
			out.println(source);
			sourceURL = sourceURL.replaceAll(" ", "");
			if (sourceURL.indexOf(".") == -1) {
				sourceURL = sourceURL + ".com";
			}
			try{
				Map<String, String> features = getSourceFeatures(sourceURL);
				sourceFeatures.put(source, features);
			} catch (Exception e) {
				out.println(e.toString());
			}
		}
		return sourceFeatures;
	}
	
	/**
	 * Gets features for source website, from alexa. <Those are given next to the feature value itself.
	 */
	static Map<String, String> getSourceFeatures (String sourceURL) throws IOException {
		final Map<String, String> features = new HashMap<String, String>();
		final String siteString = "http://www.alexa.com/siteinfo/";
		final String urlString = siteString + sourceURL;
		URL url  = new URL(urlString);
		URLConnection uc = url.openConnection();
		InputStreamReader input = new InputStreamReader(uc.getInputStream());
	    BufferedReader in = new BufferedReader(input);
		String inputLine;
	    while ((inputLine = in.readLine()) != null) {
	    	
	    	// Rank
	    	if (inputLine.indexOf("Global rank icon") != -1) {
	    		for (int i = 0; i < 2; i++) {
	    			inputLine = in.readLine();
	    		}
	    		String rank = inputLine.substring(0, inputLine.indexOf('<'));
	    		rank = rank.replaceAll(",", "");
	    		features.put("Rank", rank);
	    	}

	    	// Rank in Country
	    	if (inputLine.indexOf("The rank by country is calculated using a combination") != -1) {
	    		for (int i = 0; i < 9; i++) {
	    			inputLine = in.readLine();
	    		}
	    		String cRank = inputLine.substring(0, inputLine.indexOf('<'));
	    		cRank = cRank.replaceAll(",", "");
	    		features.put("Country Rank", cRank);
	    	}

	    	// Country
	    	if (inputLine.indexOf("<strong>Estimated Pageviews</strong>") != -1) {
	    		for (int i = 0; i < 8; i++) {
	    			inputLine = in.readLine();
	    		}
	    		String country = inputLine.substring(inputLine.indexOf("<span class='text-inline'>") + "<span class='text-inline'>".length(), inputLine.indexOf("</span></span></td>"));
	    		features.put("Country", country);
	    	}

	    	// Bounce Rate
	    	if (inputLine.indexOf("Bounce Rate</h4>") != -1) {
	    		for (int i = 0; i < 3; i++) {
	    			inputLine = in.readLine();
	    		}
	    		String bounceRate = inputLine.substring(0, inputLine.indexOf('%'));
	    		bounceRate = bounceRate.replaceAll(",", "");
	    		features.put("Bounce Rate", bounceRate);
	    	}

	    	// Daily Pageviews per visitor
	    	if (inputLine.indexOf("Daily Pageviews per Visitor</h4>") != -1) {
	    		for (int i = 0; i < 3; i++) {
	    			inputLine = in.readLine();
	    		}
	    		String pageViews = inputLine.substring(0, inputLine.indexOf('<'));
	    		pageViews = pageViews.replaceAll(",", "");
	    		features.put("Daily Page Views Per Visitor", pageViews);
	    	}

	    	// Daily Time on Site per visitor
	    	if (inputLine.indexOf("Daily Time on Site</h4>") != -1) {
	    		for (int i = 0; i < 3; i++) {
	    			inputLine = in.readLine();
	    		}
	    		String time = inputLine.substring(0, inputLine.indexOf('<'));
	    		time = time.replaceAll(",", "");
	    		features.put("Daily Time On Site", time);
	    	}

	    	// Search Visits
	    	if (inputLine.indexOf("Search Visits</h4>") != -1) {
	    		for (int i = 0; i < 3; i++) {
	    			inputLine = in.readLine();
	    		}
	    		String visits = inputLine.substring(0, inputLine.indexOf("%"));
	    		visits = visits.replaceAll(",", "");
	    		features.put("Search Visits", visits);
	    	}

	    	// Total Sites Linking In
	    	if (inputLine.indexOf("Total Sites Linking In</h5>") != -1) {
	    		for (int i = 0; i < 1; i++) {
	    			inputLine = in.readLine();
	    		}
	    		String links = inputLine.substring(inputLine.indexOf(">") + 1, inputLine.indexOf("</span>"));
	    		links = links.replaceAll(",", "");
	    		features.put("Total Sites Linking In", links);
	    	}	    	
	    }
		in.close();
		
	    return features;
	}
	
	/**
	 * The sources and tuples maps get populated by this method, mapping integer ids of the sources/tuples to their
	 * string descriptions. tupleTruthsAll is populated to include all tuple truth values (while only 1 - holdoutFraction of those are put into the model).
	 * Samples sampleFraction fraction of the sources, and only considers tuples outputted by those
	 * (to reduce size of the dataset). holdoutFraction specifies the amout of labelled data to hold out (and use for testing later). 
	 * @throws IOException 
	 */
	static ModelInstance createModelInstance(Double sampleFraction, Double holdoutFraction, List<String> sources, List<String> tuples, Map<Integer, Boolean> tupleTruthsAll) throws IOException {
		Map<String, Set<String>> sourceOutputStrings = new HashMap<String, Set<String>>();
		Set<String> trueTuples = new HashSet<String>();
		parseData(sourceOutputStrings, trueTuples);
		Map<String, Integer> sourceIds = new HashMap<String, Integer>(); // inverse of sources.
		Map<String, Integer> tupleIds = new HashMap<String, Integer>(); // inverse of tuples.
		
		final List<List<Integer>> groupSources = new ArrayList<List<Integer>>();
		final List<List<Integer>> sourceOutputs = new ArrayList<List<Integer>>();
		final Map<Integer, Boolean> tupleTruth = new HashMap<Integer, Boolean>();
		
		// Randomly samples stuples and sources that have outputted at least one. 
		Set<String> allTuples = new HashSet<String>();
		for (String source : sourceOutputStrings.keySet()) {
			allTuples.addAll(sourceOutputStrings.get(source));
		}
		for (String tuple : allTuples) {
			if (Math.random() > sampleFraction) {
				continue;
			}
			final Integer tupleId = tupleIds.keySet().size();
			tuples.add(tuple);
			tupleIds.put(tuple, tupleId);
		}
		Integer numEmptyTuples = 7500; // An empty tuple is a false tuple that isn't outputted by any source.
		final String emptyTupleBook = "EMPTYTUPLE";
		for (int i = 0; i < numEmptyTuples; i++) {
			final String tuple = emptyTupleBook + "\t" + i;
			final Integer tupleId = tupleIds.keySet().size();
			allTuples.add(tuple);
			tuples.add(tuple);
			tupleIds.put(tuple, tupleId);
		}
		for (String source : sourceOutputStrings.keySet()) {
			boolean keepSource = false;
			final Integer sourceId = sourceIds.keySet().size();
			final List<Integer> currentSourceOutputs = new ArrayList<Integer>();
			for (String output : sourceOutputStrings.get(source)) {
				if (tupleIds.containsKey(output)) {
					keepSource = true;
					final Integer tupleId = tupleIds.get(output);
					currentSourceOutputs.add(tupleId);
				}
			}
			if (keepSource) {
				sourceIds.put(source, sourceId);
				sources.add(source);
				sourceOutputs.add(currentSourceOutputs);
			}
		}


		Map<String, Set<String>> bookAuthors = new HashMap<String, Set<String>>();
		for (String output : trueTuples) {
			String[] splitString = output.split("\t");
			final String book = splitString[0];
			final String author = splitString[1];
			if (!bookAuthors.containsKey(book)) {
				bookAuthors.put(book, new HashSet<String>());
			}
			bookAuthors.get(book).add(author);
		}
		
		for (String tuple : tupleIds.keySet()) {
			final Integer tupleId = tupleIds.get(tuple);
			String[] splitString = tuple.split("\t");
			final String book = splitString[0];
			final String author = splitString[1];
			
			if (book.equals(emptyTupleBook)) {
				tupleTruth.put(tupleId, false);
				tupleTruthsAll.put(tupleId, false);
				continue;
			}

			if (bookAuthors.containsKey(book)) {
				if (bookAuthors.get(book).contains(author)) {
					if (Math.random() > holdoutFraction) {
						tupleTruth.put(tupleId, true);
					}
					tupleTruthsAll.put(tupleId, true);
				} else {
					if (Math.random() > holdoutFraction) {
						tupleTruth.put(tupleId, false);
					}
					tupleTruthsAll.put(tupleId, false);
				}
			}
		}
		
		int numTuples = tuples.size();
		int numSources = sources.size();
		int numGroups = addModelGroups(groupSources, sourceOutputs, tupleTruth, "1All");

		List<Integer> groupTrueTrueInit = new ArrayList<Integer>();
		List<Integer> groupTrueFalseInit = new ArrayList<Integer>();
		List<Integer> groupFalseTrueInit = new ArrayList<Integer>();
		List<Integer> groupFalseFalseInit = new ArrayList<Integer>();
		for (int groupId = 0; groupId < numGroups; groupId++) {
			groupTrueTrueInit.add(20000);
			groupTrueFalseInit.add(1);
			groupFalseTrueInit.add(1);
			groupFalseFalseInit.add(20000);
		}
		
		// groupTupleBelief-sourceGroupTupleBelief pair value counts 
		List<Integer> sourceTrueTrueInit = new ArrayList<Integer>();
		List<Integer> sourceTrueFalseInit = new ArrayList<Integer>();
		List<Integer> sourceFalseTrueInit = new ArrayList<Integer>();
		List<Integer> sourceFalseFalseInit = new ArrayList<Integer>();
		for (int sourceId = 0; sourceId < numSources; sourceId++) {
			sourceTrueTrueInit.add(1);
			sourceTrueFalseInit.add(1);
			sourceFalseTrueInit.add(1);
			sourceFalseFalseInit.add(1);
		}
		
		return new ModelInstance(numTuples, numGroups, numSources, groupSources, 
				sourceOutputs, groupTrueTrueInit, groupTrueFalseInit, groupFalseTrueInit, groupFalseFalseInit, 
				sourceTrueTrueInit, sourceTrueFalseInit, sourceFalseTrueInit, sourceFalseFalseInit, tupleTruth) ;
		//return null;
	}
	
	/*
	 * Adds the groups for model, by populating groupSources. The "type" string specifies the scheme to use for grouping.
	*/
	static int addModelGroups (List<List<Integer>> groupSources, List<List<Integer>> sourceOutputs, Map<Integer, Boolean> tupleTruth, String type) {
		if (type.equals("1All")) {
			List<Integer> allSources = new ArrayList<Integer>();
			for (int i = 0; i < sourceOutputs.size(); i++) {
				allSources.add(i);
			}
			groupSources.add(allSources);
			return 1;
		}
		if (type.equals("1All2Most")) {
			List<Integer> allSources = new ArrayList<Integer>();
			for (int i = 0; i < sourceOutputs.size(); i++) {
				allSources.add(i);
			}
			groupSources.add(allSources);
			Map<Integer, List<Integer>> outputSources = new HashMap<Integer, List<Integer>>();
			for (Integer sourceId = 0; sourceId < sourceOutputs.size(); sourceId++) {
				for (Integer tupleId : sourceOutputs.get(sourceId)) {
					if (!outputSources.containsKey(tupleId)) {
						outputSources.put(tupleId, new ArrayList<Integer>());
					}
					outputSources.get(tupleId).add(sourceId);
				}
			}
			Set<Integer> commonSourceSet = new HashSet<Integer>();
			List<Integer> commonSources = new ArrayList<Integer>();
			int threshold = 49;
			for (Integer tupleId : outputSources.keySet()) {
				if (outputSources.get(tupleId).size() > threshold) {
					commonSourceSet.addAll(outputSources.get(tupleId));
				}
			}
			commonSources.addAll(commonSourceSet);
			groupSources.add(commonSources);
			//allSources.removeAll(commonSources);
			out.println(commonSources.size());
			return groupSources.size();
		}
		if (type.equals("ManyTupleBased")) {
			List<Integer> allSources = new ArrayList<Integer>();
			for (int i = 0; i < sourceOutputs.size(); i++) {
				allSources.add(i);
			}
			groupSources.add(allSources);
			Map<Integer, List<Integer>> outputSources = new HashMap<Integer, List<Integer>>();
			for (Integer sourceId = 0; sourceId < sourceOutputs.size(); sourceId++) {
				for (Integer tupleId : sourceOutputs.get(sourceId)) {
					if (!outputSources.containsKey(tupleId)) {
						outputSources.put(tupleId, new ArrayList<Integer>());
					}
					outputSources.get(tupleId).add(sourceId);
				}
			}
			Set<Integer> commonSourceSet = new HashSet<Integer>();
			List<Integer> commonSources = new ArrayList<Integer>();
			int threshold = 49;
			for (Integer tupleId : outputSources.keySet()) {
				if (outputSources.get(tupleId).size() > threshold) {
					groupSources.add(outputSources.get(tupleId));
				}
			}
			//allSources.removeAll(commonSources);
			out.println(groupSources.size());
			return groupSources.size();
		}
		if (type.equals("ManyFull")) {
			int numGroups = 4;
			List<Integer> allSources = new ArrayList<Integer>();
			for (int i = 0; i < sourceOutputs.size(); i++) {
				allSources.add(i);
			}
			for (int k = 0; k < numGroups; k++) {
				groupSources.add(allSources);
			}
			return numGroups;	
		}
		if (type.equals("ManyRandom")) {
			int numGroups = 5;
			double density = 0.5;
			for (int k = 0; k < numGroups; k++) {
				List<Integer> sources = new ArrayList<Integer>();
				for (int i = 0; i < sourceOutputs.size(); i++) {
					if (Math.random() < density) {
						sources.add(i);
					}
				}
				groupSources.add(sources);
			}
			return numGroups;	
		}
		return -1;	
	}

	static void analyzeData () throws IOException {
		Map<String, Set<String>> sourceOutputStrings = new HashMap<String, Set<String>>();
		Set<String> trueTuples = new HashSet<String>();
		parseData(sourceOutputStrings, trueTuples);
		Map<String, Integer> sourceIds = new HashMap<String, Integer>(); // inverse of sources.
		Map<String, Integer> tupleIds = new HashMap<String, Integer>(); // inverse of tuples.
		List<String> sources = new ArrayList<String>(); 
		List<String> tuples = new ArrayList<String>();
		final List<Set<Integer>> sourceOutputs = new ArrayList<Set<Integer>>();
		final Map<Integer, Boolean> tupleTruth = new HashMap<Integer, Boolean>();
		
		for (String source : sourceOutputStrings.keySet()) {
			final Integer sourceId = sourceIds.keySet().size(); 
			sources.add(source);
			sourceIds.put(source, sourceId);
			final Set<Integer> currentSourceOutputs = new HashSet<Integer>();
			sourceOutputs.add(currentSourceOutputs);
			
			final Set<String> outputs = sourceOutputStrings.get(source);
			for (String output : outputs) {
				Integer id;
				if (!tupleIds.containsKey(output)) {
					id = tupleIds.keySet().size();
					tupleIds.put(output, id);
					tuples.add(output);
				} else {
					id = tupleIds.get(output);
				}
				currentSourceOutputs.add(id);
			}
 		}
		
		Set<String> books = new HashSet<String>();
		for (String output : tuples) {
			String[] splitString = output.split("\t");
			final String book = splitString[0];
			books.add(book);
		}
		
		Map<String, Set<String>> bookAuthors = new HashMap<String, Set<String>>();
		for (String output : trueTuples) {
			String[] splitString = output.split("\t");
			final String book = splitString[0];
			final String author = splitString[1];
			if (!bookAuthors.containsKey(book)) {
				bookAuthors.put(book, new HashSet<String>());
			}
			bookAuthors.get(book).add(author);
		}
		
		for (String tuple : tupleIds.keySet()) {
			final Integer tupleId = tupleIds.get(tuple);
			String[] splitString = tuple.split("\t");
			final String book = splitString[0];
			final String author = splitString[1];
			
			if (bookAuthors.containsKey(book)) {
				if (bookAuthors.get(book).contains(author)) {
					tupleTruth.put(tupleId, true);
				} else {
					tupleTruth.put(tupleId, false);
				}
			}
		}
		
		List<String> tupleBooks = new ArrayList<String>();
		for (int tupleId = 0; tupleId < tuples.size(); tupleId++) {
			String tuple = tuples.get(tupleId);
			String[] splitString = tuple.split("\t");
			final String book = splitString[0];
			tupleBooks.add(book);
		}
		List<Set<String>> sourceBooks = new ArrayList<Set<String>>();
		for (int sourceId = 0; sourceId < sources.size(); sourceId++) {
			Set<String> currSourceBooks = new HashSet<String>();
			sourceBooks.add(currSourceBooks);
			for (int tupleId : sourceOutputs.get(sourceId)) {
				currSourceBooks.add(tupleBooks.get(tupleId));
			}
		}
		Map<String, Integer> bookAuthorCount = new HashMap<String, Integer>();
		for (int tupleId : tupleTruth.keySet()) {
			if (tupleTruth.get(tupleId)) {
				final String book = tupleBooks.get(tupleId);
				if (!bookAuthorCount.containsKey(book)) {
					bookAuthorCount.put(book, 1);
				} else {
					bookAuthorCount.put(book, 1 + bookAuthorCount.get(book));
				}
			}
		}
		
		for (String book : books) {
			if (!bookAuthors.containsKey(book)) {
				//out.println(book);
			}
		}
		
		int trueCount = 0;
		int falseCount = 0;
		Map<Integer, Integer> trueSourceCounts = new HashMap<Integer, Integer>();
		Map<Integer, Integer> falseSourceCounts = new HashMap<Integer, Integer>();
		for (int tupleId : tupleTruth.keySet()) {
			final Boolean currentTupleTruth = tupleTruth.get(tupleId);
			if (currentTupleTruth) {
				trueCount++;
			} else {
				falseCount++;
			}
			int numTrueSources = 0;
			int numFalseSources = 0;
			String outTrueString = tuples.get(tupleId) + "----";
			String outFalseString = tuples.get(tupleId) + "----";
			for (Integer sourceId = 0; sourceId < sourceOutputs.size(); sourceId++) {
				final Boolean sourceTruth = sourceOutputs.get(sourceId).contains(tupleId);
				if (sourceTruth && !currentTupleTruth) {
					numFalseSources++;
					outFalseString = outFalseString + " " + sources.get(sourceId);
				} 
				if (sourceTruth && currentTupleTruth) {
					numTrueSources++;
					outTrueString = outTrueString + " " + sources.get(sourceId);
				}
			}
			if (numFalseSources >= 4) {
				//out.println(outFalseString);
			}
			if (numTrueSources >= 4) {
				//out.println(outTrueString);
			}
			if (currentTupleTruth) {
				if (!trueSourceCounts.containsKey(numTrueSources)) {
					trueSourceCounts.put(numTrueSources, 1);
				} else {
					trueSourceCounts.put(numTrueSources, 1 + trueSourceCounts.get(numTrueSources));
				}				
			} else {
				if (!falseSourceCounts.containsKey(numFalseSources)) {
					falseSourceCounts.put(numFalseSources, 1);
				} else {
					falseSourceCounts.put(numFalseSources, 1 + falseSourceCounts.get(numFalseSources));
				}				
			}
		}
		out.println(trueCount + "\t" + falseCount + "\t" + sourceOutputs.size());
		out.println(trueSourceCounts.toString());
		out.println(falseSourceCounts.toString());

		for (int threshold = 0; threshold < 10; threshold++) {
			int tt=0, tf=0, ft=0, ff=0;
			for (int key : trueSourceCounts.keySet()) {
				if (key >= threshold) {
					tt += trueSourceCounts.get(key);
				} else {
			 		ft += trueSourceCounts.get(key);
				}
			}
			for (int key : falseSourceCounts.keySet()) {
				if (key >= threshold) {
					tf += falseSourceCounts.get(key);
				} else {
					ff += falseSourceCounts.get(key);
				}
			}
			double recall = ((double) tt) / (tt + ft);
			double precision = ((double) tt) / (tt + tf);
			double f1 = 2 * precision * recall / (precision + recall);
			double accuracy = ((double)(tt + ff)) / (tt + ff + tf + ft);

			out.println("Threshold\t" + threshold);
			
			out.println(precision);
			out.println(recall);
			out.println(f1);
			out.println(accuracy);
			out.println();
		}

		int numBigSources = 0;
		for (Integer sourceId  = 0; sourceId < sourceOutputs.size(); sourceId++) {
			if (sourceOutputs.get(sourceId).size() > 1000) {
				numBigSources++;
				//out.println(sourceOutputs.get(sourceId).size());				
			}
		}
		//out.println("numBigSources\t" + numBigSources);
		
		int sf = 0;
		List<Set<Integer>> falseTupleSets = new ArrayList<Set<Integer>>();
		List<Set<Integer>> trueTupleSets = new ArrayList<Set<Integer>>();
		for (Integer sourceId = 0; sourceId < sourceOutputs.size(); sourceId++) {
			Set<Integer> falseTupleSet = new HashSet<Integer>();
			falseTupleSets.add(falseTupleSet);
			Set<Integer> trueTupleSet = new HashSet<Integer>();
			trueTupleSets.add(trueTupleSet);
			int f = 0;
			int t = 0;
			String outString = sources.get(sourceId) + "---";
			for (Integer tupleId : sourceOutputs.get(sourceId)) {
				if (tupleTruth.containsKey(tupleId) && !tupleTruth.get(tupleId)) {
					falseTupleSet.add(tupleId);
					f++;
					outString = outString + " " + tupleId;
				}
				if (tupleTruth.containsKey(tupleId) && tupleTruth.get(tupleId)) {
					trueTupleSet.add(tupleId);
					t++;
				}
			}
			if (f > 5) {
				sf++;
				//out.println(outString);
			}
			if (trueTupleSet.size() + falseTupleSet.size() > 30) {
				//out.println((trueTupleSet.size() + falseTupleSet.size()) + "\t" + sourceOutputs.get(sourceId).size());				
			}
		}
		
		for (Integer sourceId1 = 0; sourceId1 < sourceOutputs.size(); sourceId1++) {
			int bookAuthorCount1 = 0;
			for (String book : sourceBooks.get(sourceId1)) {
				if (bookAuthorCount.containsKey(book)) {
					bookAuthorCount1 += bookAuthorCount.get(book);
				}
			}			
			Double precision1 = ((double)trueTupleSets.get(sourceId1).size()) / ((falseTupleSets.get(sourceId1).size()) + (trueTupleSets.get(sourceId1).size())); 
			if (!precision1.equals(Double.NaN)) {
				//out.println(precision1);				
			}
			Double error1 = ((double)falseTupleSets.get(sourceId1).size()) / sourceOutputs.get(sourceId1).size();
			Double recall1 = ((double)trueTupleSets.get(sourceId1).size()) / trueCount;
			Double gecall1 = ((double)trueTupleSets.get(sourceId1).size()) / bookAuthorCount1;
			Set<Integer> corrSources = new HashSet<Integer>();
			Set<Integer> gorrSources = new HashSet<Integer>();
			if (recall1 * trueCount < 7.5) {				
				continue;
			}
			for (Integer sourceId2 = sourceId1 + 1; sourceId2 < sourceOutputs.size(); sourceId2++) {
				int commonBookAuthorCount = 0;
				int bookAuthorCount2 = 0;
				for (String book : sourceBooks.get(sourceId2)) {
					if (bookAuthorCount.containsKey(book)) {
						bookAuthorCount2 += bookAuthorCount.get(book);
						if (sourceBooks.get(sourceId1).contains(book)) {
							commonBookAuthorCount += bookAuthorCount.get(book);;
						}
					}
				}
				
				Double error2 = ((double)falseTupleSets.get(sourceId2).size()) / sourceOutputs.get(sourceId2).size();
				Double recall2 = ((double)trueTupleSets.get(sourceId2).size()) / trueCount;
				Double gecall2 = ((double)trueTupleSets.get(sourceId2).size()) / bookAuthorCount2;
				if (recall2 * trueCount < 7.5) {
					continue;
				}
				int commonCount = 0;
				if (recall1 < recall2) {
					for (int tupleId : trueTupleSets.get(sourceId1)) {
						if (trueTupleSets.get(sourceId2).contains(tupleId)) {
							commonCount++;
						}
					}
				} else {
					for (int tupleId : trueTupleSets.get(sourceId2)) {
						if (trueTupleSets.get(sourceId1).contains(tupleId)) {
							commonCount++;
						}
					}
				}
				Double recallCommon = ((double) commonCount) / trueCount;
				Double gecallCommon = ((double) commonCount) / commonBookAuthorCount;
				Double correlation = (recallCommon - recall1 * recall2) /
						(Math.sqrt((recall1 * (1 - recall1)) * (recall2 * (1 - recall2))));
				Double gorrelation = (gecallCommon - gecall1 * gecall2) /
						(Math.sqrt((gecall1 * (1 - gecall1)) * (gecall2 * (1 - gecall2))));
				if (Math.abs(correlation) > 0.4) {
					corrSources.add(sourceId2);
					//out.println(correlation + "\t" + recall1 * trueCount + "\t" + recall2 * trueCount);
				}
				if (Math.abs(gorrelation) > 0.7) {
					gorrSources.add(sourceId2);
				}
			}
			if (!corrSources.isEmpty()) {
				//out.println(sourceId1 + "\t" + recall1 * trueCount);
				//out.println(corrSources.toString());
			}
			if (!gorrSources.isEmpty()) {
				//out.println(sourceId1 + "\t" + recall1);
				//out.println(gorrSources.toString());
			}
		}
		//out.println(sf);
	}

	public static List<String> getStockSources () throws IOException {
		Set<String> sources = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader("TestDatasets/clean_stock/stock-2011-07-11.txt"));
		String line;
		while ((line = br.readLine()) != null) {
			String[] words = line.split("\t");
			String source = words[0].replaceAll("-","");
			if (source.endsWith("com")) {
				source = source.substring(0, source.length() - 3) + ".com";
			}
			sources.add(source);
		}
		br.close();
		return new ArrayList<String>(sources);
		
	}
	
	public static void main (String[] args) throws IOException, ClassNotFoundException {
		List<String> sources = new ArrayList<String>();
		List<String> tuples = new ArrayList<String>();
		Map<Integer, Boolean> tupleTruthsAll = new HashMap<Integer, Boolean>();
	
		//sources = getStockSources();
		//createSourceFeaturesFile(sources, "TestDatasets/clean_stock/stock_source_features.txt");	
		
		createModelInstance(1.0, 0.5, sources, tuples, tupleTruthsAll);
		createSourceFeaturesFile(sources, "TestDatasets/BookAuthor/book_source_features.txt");
		
		//analyzeData();
		System.exit(0);
		ModelInstance modelInstance;
 
		final int numSamples = 5;
		final int burnIn = 25;
		final int thinFactor = 5;
		Boolean createNew = !false; // True means create new samples and save them. Otherwise, we just read samples from file.
		String saveFileName = "samples.ser";
		DenseSample denseSample;
		List<DenseSample> samples;

		if (createNew) {
			modelInstance = createModelInstance(1.0, 0.5, sources, tuples, tupleTruthsAll);
			denseSample = new DenseSample(modelInstance);
			samples = denseSample.GibbsSampling(numSamples, burnIn, thinFactor);
			FileOutputStream fileOut = new FileOutputStream(saveFileName);
			ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			objOut.writeObject(denseSample);
			objOut.writeObject(samples);
			objOut.writeObject(modelInstance);
			objOut.writeObject(sources);
			objOut.writeObject(tuples);
			objOut.writeObject(tupleTruthsAll);
			objOut.close();
			fileOut.close();
		} else {
			FileInputStream fileIn = new FileInputStream(saveFileName);
			ObjectInputStream objIn = new ObjectInputStream(fileIn);
			denseSample = (DenseSample) objIn.readObject();
			samples = (List<DenseSample>) objIn.readObject();
			modelInstance = (ModelInstance) objIn.readObject();
			sources = (List<String>) objIn.readObject();
			tuples = (List<String>) objIn.readObject();
			tupleTruthsAll = (Map<Integer, Boolean>) objIn.readObject();
			objIn.close();
			fileIn.close();
		}

		out.println("Labels:\t" + modelInstance.tupleTruth.keySet().size());
		out.println("Tuples:\t" + modelInstance.getNumTuples());
		out.println("Sources:\t" + modelInstance.getNumSources());

		List<Double> tupleProbs = new ArrayList<Double>();
		for (int i = 0; i < modelInstance.getNumTuples(); i++) {
			tupleProbs.add(0.0);
		}
		Map<Integer, List<Integer>> outputSources = new HashMap<Integer, List<Integer>>();
		for (Integer sourceId = 0; sourceId < modelInstance.sourceOutputs.size(); sourceId++) {
			for (Integer tupleId : modelInstance.sourceOutputs.get(sourceId)) {
				if (!outputSources.containsKey(tupleId)) {
					outputSources.put(tupleId, new ArrayList<Integer>());
				}
				outputSources.get(tupleId).add(sourceId);
			}
		}
		for (int tupleId : outputSources.keySet()) {
			if (outputSources.get(tupleId).size() > 2 && outputSources.get(tupleId).size() < 8) {
				if (Math.random() > 0.02) {
					continue;
				}
				DenseSample sample = denseSample;//samples.get(0);
				if (true || !sample.groupTupleBeliefs.get(0).get(tupleId)) {
					out.println(outputSources.get(tupleId).size() + "\t" + tupleTruthsAll.get(tupleId));
					out.println(sample.groupBeliefGlobalProb(0, tupleId));
					for (int sourceId : outputSources.get(tupleId)) {
						out.print(modelInstance.sourceOutputs.get(sourceId).size() + "\t");
						out.print(sample.sourceBeliefProb(sourceId, true)/sample.sourceBeliefProb(sourceId, false) + "\t");
						out.print(sample.sourceOutputProb(sourceId, true) + "," + sample.sourceOutputProb(sourceId, false) + "\t");
						out.println(sample.sourceOutputProbActual(sourceId, true, tupleTruthsAll)/sample.sourceOutputProbActual(sourceId, false, tupleTruthsAll));
					}
					out.println();
				}
			}
		}
		for (DenseSample sample : samples) {
			//out.println(sample.tupleTruthProb());
			for (int k = 0; k < modelInstance.getNumGroups(); k++) {
				//out.println(sample.groupBeliefProb(k, true));
				//out.println(sample.groupBeliefProb(k, false));				
				out.println(sample.groupBeliefProbActual(k, true, tupleTruthsAll));
				out.println(sample.groupBeliefProbActual(k, false, tupleTruthsAll));
			}
			for (int j = 0; j < 0.012 * modelInstance.getNumSources(); j++) {
				//out.println(sample.sourceBeliefProb(j, true) + "\t" + modelInstance.sourceOutputs.get(j).size());
				//out.println(sample.sourceBeliefProb(j, false));				
			}
			//out.println();
			for (int i = 0; i < modelInstance.getNumTuples(); i++) {
				//tupleProbs.set(i, (sample.tupleTruths.get(i) ? 1.0 : 0.0) + tupleProbs.get(i));
				tupleProbs.set(i, (sample.groupTupleBeliefs.get(0).get(i) ? 1.0 : 0.0) + tupleProbs.get(i));
			}
		}
		for (int i = 0; i < modelInstance.getNumTuples(); i++) {
			tupleProbs.set(i, tupleProbs.get(i) / numSamples);
		}

		int tt = 0, tf = 0, ft = 0, ff = 0;
		for (double probThreshold = 0.0; probThreshold < 0.9; probThreshold += 0.2) {
			tt = tf = ft = ff = 0;
			for (int i = 0; i < modelInstance.getNumTuples(); i++) {
				if (modelInstance.tupleTruth.containsKey(i) || !tupleTruthsAll.containsKey(i)) {
					continue;
				}
				if (tupleTruthsAll.get(i)) {
					if (tupleProbs.get(i) >= probThreshold) {
						tt++;
					} else {
						ft++;
					}
				} else {
					if (tupleProbs.get(i) >= probThreshold) {
						tf++;
					} else {
						ff++;
					}
				}
			}
			double recall = ((double) tt) / (tt + ft);
			double precision = ((double) tt) / (tt + tf);
			double f1 = 2 * precision * recall / (precision + recall);
			double accuracy = ((double)(tt + ff)) / (tt + ff + tf + ft);
			
			out.println("\n" + probThreshold + ":");
			out.println(tt + ", " + ft + ", " + tf + ", " + ff);
			out.println(precision);
			out.println(recall);
			out.println(f1);
			out.println(accuracy);
		}
	}
}
