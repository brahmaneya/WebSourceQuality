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

		BufferedReader labelledDataFile = new BufferedReader(new FileReader(LABELLEDDATAFILE));		
		while ((s = labelledDataFile.readLine()) != null) {
			if (s.indexOf('\t') == -1) {
				continue;
			}
			String[] fields = s.split("\t");
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
	 * The sources and tuples maps get populated by this method, mapping integer ids of the sources/tuples to their
	 * string descriptions. Samples sampleFraction fraction of the sources, and only considers tuples outputted by those
	 * (to reduce size of the dataset).
	 * @throws IOException 
	 */
	static ModelInstance createModelInstance(Double sampleFraction, List<String> sources, List<String> tuples) throws IOException {
		Map<String, Set<String>> sourceOutputStrings = new HashMap<String, Set<String>>();
		Set<String> trueTuples = new HashSet<String>();
		parseData(sourceOutputStrings, trueTuples);
		Map<String, Integer> sourceIds = new HashMap<String, Integer>(); // inverse of sources.
		Map<String, Integer> tupleIds = new HashMap<String, Integer>(); // inverse of tuples.
		
		final List<List<Integer>> groupSources = new ArrayList<List<Integer>>();
		final List<List<Integer>> sourceOutputs = new ArrayList<List<Integer>>();
		final Map<Integer, Boolean> tupleTruth = new HashMap<Integer, Boolean>();
		
		for (String source : sourceOutputStrings.keySet()) {
			if (Math.random() > sampleFraction) {
				continue;
			}
			final Integer sourceId = sourceIds.keySet().size(); 
			sources.add(source);
			sourceIds.put(source, sourceId);
			final List<Integer> currentSourceOutputs = new ArrayList<Integer>();
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
		
		int numTuples = tuples.size();
		int numSources = sources.size();
		int numGroups = 1;
		
		{
			List<Integer> allSources = new ArrayList<Integer>();
			for (int i = 0; i < numSources; i++) {
				allSources.add(i);
			}
			groupSources.add(allSources);
		}
		
		List<Integer> groupTrueTrueInit = new ArrayList<Integer>();
		List<Integer> groupTrueFalseInit = new ArrayList<Integer>();
		List<Integer> groupFalseTrueInit = new ArrayList<Integer>();
		List<Integer> groupFalseFalseInit = new ArrayList<Integer>();
		for (int groupId = 0; groupId < numGroups; groupId++) {
			groupTrueTrueInit.add(2);
			groupTrueFalseInit.add(1);
			groupFalseTrueInit.add(1);
			groupFalseFalseInit.add(3);
		}
		
		// groupTupleBelief-sourceGroupTupleBelief pair value counts 
		List<Integer> sourceTrueTrueInit = new ArrayList<Integer>();
		List<Integer> sourceTrueFalseInit = new ArrayList<Integer>();
		List<Integer> sourceFalseTrueInit = new ArrayList<Integer>();
		List<Integer> sourceFalseFalseInit = new ArrayList<Integer>();
		for (int sourceId = 0; sourceId < numSources; sourceId++) {
			sourceTrueTrueInit.add(2);
			sourceTrueFalseInit.add(1);
			sourceFalseTrueInit.add(1);
			sourceFalseFalseInit.add(3);
		}
		
		return new ModelInstance(numTuples, numGroups, numSources, groupSources, 
				sourceOutputs, groupTrueTrueInit, groupTrueFalseInit, groupFalseTrueInit, groupFalseFalseInit, 
				sourceTrueTrueInit, sourceTrueFalseInit, sourceFalseTrueInit, sourceFalseFalseInit, tupleTruth) ;
		//return null;
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
		//out.println(trueSourceCounts.toString());
		//out.println(falseSourceCounts.toString());
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
				out.println(precision1);				
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
	
	public static void main (String[] args) throws IOException {
		List<String> sources = new ArrayList<String>();
		List<String> tuples = new ArrayList<String>();
		ModelInstance modelInstance = createModelInstance(1.0, sources, tuples);
		DenseSample denseSample = new DenseSample(modelInstance);
		out.println("Current base truth rate:\t" + denseSample.tupleTruthProb());
		final int numSamples = 4;
		final int burnIn = 100000;
		final int thinFactor = 10000;
		List<DenseSample> samples = denseSample.GibbsSampling(numSamples, burnIn, thinFactor);

		for (DenseSample sample : samples.subList(0,1)) {
			out.println(sample.tupleTruthProb());
			for (int k = 0; k < modelInstance.getNumGroups(); k++) {
				out.println(sample.groupBeliefProb(k, true));
				out.println(1 - sample.groupBeliefProb(k, false));				
			}
			for (int j = 0; j < modelInstance.getNumSources(); j++) {
				out.println(sample.sourceBeliefProb(j, true));
				out.println(1 - sample.sourceBeliefProb(j, false));				
			}
			out.println();
		}
		
	}
}
