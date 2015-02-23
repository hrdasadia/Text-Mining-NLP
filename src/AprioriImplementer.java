import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class AprioriImplementer {

	static Map<Integer, String> transactionMap;					//Stores all extracted data from database in this map
	static SortedSet<String> itemSet = new TreeSet<String>();	//Contains list of all sorted items
	static Set<Set<Set<String>>> frequentDataSet;				//Contains all frequent data sets that contain sets of n-order data sets that contain set of transactional item
	static double support;										//Contains minimum support by user
	static int commonElements;									//Number of expected common elements between n and n+1 order sets
	static double confidence;									//Contains minimum confidence by user
	static List<Set<String>> leftRules = new ArrayList<Set<String>>();	//List of set of LHS of Association rules
	static List<Set<String>> rightRules = new ArrayList<Set<String>>();	//List of set of RHS of Associated Rules

	public static void start() throws IOException {

		// Get Minimum Support and Confidence desired by the User
		Scanner sc = new Scanner(System.in);

		System.out.print("Enter minimum desired support(in percentage):");
		support = sc.nextDouble();
		support = support / 100;
		System.out.print("\nEnter minimum desired confidence(in percentage):");
		confidence = sc.nextDouble();
		confidence = confidence / 100;
		sc.close();

		// Get all transactions from a file an store in transactionMap
		transactionMap = getTransactions("input.txt");
		trimData();
		generateFrequentDataSets(support);
		
		// Generate Frequent data sets
		Iterator<Set<Set<String>>> iter = frequentDataSet.iterator();
		int i = 1;
		while (iter.hasNext()) {
			Set<Set<String>> temp = iter.next();
			System.out.println(i + " order frequent dataset = " + temp);

			if (i >= 2) {
				// Generate association rules
				Iterator<Set<String>> iter2 = temp.iterator();
				while (iter2.hasNext()) {
					generateAssociations(iter2.next());
				}
			}
			i++;
		}

		// Printing Association Rules
		System.out.println("\nAssociated Rules:");
		for (int i1 = 0; i1 < leftRules.size(); i1++) {
			System.out.println(leftRules.get(i1)
					+ "\t-->\t"
					+ rightRules.get(i1)
					+ " "
					+ calculateSupportConfidence(leftRules.get(i1),
							rightRules.get(i1)));
		}

	}

	/*
	 * This method generates Association rules from frequent data sets
	 * 
	 */
	private static void generateAssociations(Set<String> next) {
		int size = next.size();
		int totalIterations = size / 2;

		Set<String> key = new LinkedHashSet<String>();
		Set<String> values = new LinkedHashSet<String>(next);

		int elementsToEat = 1;

		while (elementsToEat <= totalIterations) {

			for (int i = 1;; i++) {

				if (size % 2 == 0 && totalIterations == elementsToEat
						|| size == 2) {
					if (i > (values.size() - (elementsToEat))) {
						break;
					}
				} else {
					if (i > (values.size() + 1 - (elementsToEat))) {
						break;
					}
				}

				for (int j = i; j <= (i + elementsToEat - 1); j++) {
					String target = getSingleElementSet(next, j);
					key.add(target);
					values.remove(target);
				}
				if (confidenceExists(key, values)) {
					leftRules.add(key);
					rightRules.add(values);
				}
				if (confidenceExists(values, key)) {
					leftRules.add(values);
					rightRules.add(key);
				}
				key = new LinkedHashSet<String>();
				values = new LinkedHashSet<String>(next);
			}

			elementsToEat++;

		}

	}
	
	/*
	 * This method scans extracted data from database and generates frequent data sets that satisfy minimum support
	 * 
	 */

	private static void generateFrequentDataSets(double support) {

		Set<String> dataSet;
		Set<Set<String>> ndataSet = new LinkedHashSet<Set<String>>();
		frequentDataSet = new LinkedHashSet<Set<Set<String>>>();

		double totalTransactions = transactionMap.keySet().size();
		double itemOccurence;

		Iterator<String> currentItem = itemSet.iterator();
		while (currentItem.hasNext()) {
			dataSet = new LinkedHashSet<String>();
			itemOccurence = 0;
			String item = currentItem.next();
			for (int i = 1; i <= transactionMap.keySet().size(); i++) {
				String allItems = transactionMap.get(i);
				if (allItems.contains(item)) {
					itemOccurence++;
				}
			}
			if ((double) (itemOccurence / totalTransactions) >= support) {
				dataSet.add(item);
				ndataSet.add(dataSet);
			}
		}

		frequentDataSet.add(ndataSet);

		commonElements = 0;
		generateOtherDataSets(ndataSet);
		System.out.println();
	}
	
	/*
	 * This method generates From 1 order data sets, other n-order data sets
	 * 
	 */

	private static void generateOtherDataSets(Set<Set<String>> ndataSet) {

		Set<Set<String>> qualifiedDataSet = new LinkedHashSet<Set<String>>();

		if (ndataSet.size() == 0) {
			return;
		} else {
			int size = ndataSet.size();
			for (int i = 1; i <= size; i++) {
				for (int j = i + 1; j <= size; j++) {
					Set<String> tempSet = new LinkedHashSet<String>();
					Set<String> setA = getElement(ndataSet, i);
					Set<String> setB = getElement(ndataSet, j);

					if (getIntersectionSize(setA, setB) == commonElements) {
						tempSet.addAll(setA);
						tempSet.addAll(setB);
						// Check support
						if (supportExists(tempSet)) {
							qualifiedDataSet.add(tempSet);
						}
					}
				}
			}
			if (qualifiedDataSet.size() > 0) {
				frequentDataSet.add(qualifiedDataSet);
				commonElements++;
				generateOtherDataSets(qualifiedDataSet);
			}

		}

	}
	
	/*
	 * This method Checks whether a derived associated rule satisfies minimum support
	 */

	private static boolean supportExists(Set<String> tempSet) {

		double totalTransactions = transactionMap.keySet().size();
		double itemOccurence = 0;

		for (int i = 1; i <= transactionMap.keySet().size(); i++) {
			String[] allItems = transactionMap.get(i).split(" ");
			for (int x = 0; x < allItems.length; x++) {
				allItems[x] = allItems[x].trim();
			}
			Set<String> mySet = new LinkedHashSet<String>(
					Arrays.asList(allItems));
			if (mySet.containsAll(tempSet)) {
				itemOccurence++;
			}
		}

		if ((double) (itemOccurence / totalTransactions) >= support) {
			return true;
		}
		return false;
	}

	/*
	 * This method Checks whether a derived associated rule satisfies minimum Confidence
	 */

	private static boolean confidenceExists(Set<String> key, Set<String> value) {

		double totalTransactions = transactionMap.keySet().size();
		double keyItemOccurence = 0;
		double valueItemOccurence = 0;

		if (key.size() == 0 || value.size() == 0) {
			return false;
		}

		for (int i = 1; i <= transactionMap.keySet().size(); i++) {
			String[] allItems = transactionMap.get(i).split(" ");
			for (int x = 0; x < allItems.length; x++) {
				allItems[x] = allItems[x].trim();
			}
			Set<String> mySet = new LinkedHashSet<String>(
					Arrays.asList(allItems));
			if (mySet.containsAll(key)) {
				keyItemOccurence++;
				if (mySet.containsAll(value)) {
					valueItemOccurence++;
				}
			}

		}

		double keySupport = (double) (keyItemOccurence / totalTransactions);
		double valueSupport = (double) (valueItemOccurence / totalTransactions);
		double calculatedConfidence = (double) valueSupport / keySupport;

		if (calculatedConfidence >= confidence) {
			return true;
		}
		return false;
	}

	/*
	 * This method Calculates confidence and support for a given associative rule.
	 */

	
	private static String calculateSupportConfidence(Set<String> key,
			Set<String> value) {

		double totalTransactions = transactionMap.keySet().size();
		double keyItemOccurence = 0;
		double valueItemOccurence = 0;

		if (key.size() == 0 || value.size() == 0) {
			return "";
		}

		for (int i = 1; i <= transactionMap.keySet().size(); i++) {
			String[] allItems = transactionMap.get(i).split(" ");
			for (int x = 0; x < allItems.length; x++) {
				allItems[x] = allItems[x].trim();
			}
			Set<String> mySet = new LinkedHashSet<String>(
					Arrays.asList(allItems));
			if (mySet.containsAll(key)) {
				keyItemOccurence++;
				if (mySet.containsAll(value)) {
					valueItemOccurence++;
				}
			}

		}

		double keySupport = (double) (keyItemOccurence / totalTransactions);
		double valueSupport = (double) (valueItemOccurence / totalTransactions);
		double calculatedConfidence = (double) valueSupport / keySupport;

		return "(Support = " + (keySupport * 100) + "%, Confidence="
				+ (calculatedConfidence * 100) + "%)";
	}

	/*
	 * This method gets the nth set from a LinkedHashSet
	 */
	
	private static Set<String> getElement(Set<Set<String>> ndataSet, int i) {

		Set<String> resultant = null;

		Iterator<Set<String>> iter = ndataSet.iterator();
		while (i > 0) {
			resultant = iter.next();
			i--;
		}

		return resultant;
	}

	/*
	 * This method gets the nth element from a LinkedHashSet
	 */
	private static String getSingleElementSet(Set<String> ndataSet, int i) {

		String resultant = null;

		Iterator<String> iter = ndataSet.iterator();
		while (i > 0) {
			resultant = iter.next();
			i--;
		}

		return resultant;
	}

	/*
	 * This method trims extra spaces from transactional data
	 */
	private static void trimData() {
		for (int i = 1; i <= transactionMap.keySet().size(); i++) {
			String[] allItems = transactionMap.get(i).split(" ");
			for (int j = 0; j < allItems.length; j++) {
				itemSet.add(allItems[j].trim());
			}
		}
	}

	/*
	 * This method connects to database and extracts data from any one of the 5 database
	 */
	public static Map<Integer, String> getTransactions(String fileName) throws IOException {

		Map<Integer, String> transactionMap = new HashMap<Integer, String>();
		
		int transactionId = 1;
		String transactions;
		
		BufferedReader br = new BufferedReader(new FileReader("C:/Users/Hardik/workspace/Text Mining/Apriori Input/"+fileName));
	    try {
	        String line = br.readLine();
	        while (line != null) {
	        	transactions = line;
	        	transactionMap.put(transactionId, transactions);
	            line = br.readLine();
	            transactionId++;
	        }
	    } finally {
	        br.close();
	    }
			
		return transactionMap;
	}

	/*
	 * This method calculates total number of elements in intersection of 2 data sets
	 */
	public static int getIntersectionSize(Set<String> set1, Set<String> set2) {
		boolean set1IsLarger = set1.size() > set2.size();
		Set<String> cloneSet = new LinkedHashSet<String>(set1IsLarger ? set2
				: set1);
		cloneSet.retainAll(set1IsLarger ? set1 : set2);
		return cloneSet.size();
	}
}
