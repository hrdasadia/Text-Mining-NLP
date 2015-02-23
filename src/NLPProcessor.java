import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class NLPProcessor {

	public static void main(String[] args) throws Exception {

		//STEP 1: Read text between body tags of a single SGM file
		List<String> allDocuments = XMLReader.readFromSource("reut2-000.sgm");
		//STEP 2: Convert text to parsed XML documents  using Stanford NLP
		List<String> convertedDocs = datatoXml(allDocuments);
		//STEP 3: Generate a token list from lemma tags from processed documents
		List<String> tokens = TokenGenerator.generateTokens(convertedDocs);
		//STEP 4: Compute the frequency for each token
		List<Map<String, Integer>> mapList = computeTermFrequency(tokens);
		//STEP 5: Calculate frequencies of each token using TF-IDF
		List<Map<Double, String>> tokensWithFrequencies = calculateFrequencies(mapList);
		//STEP 6: Get top 10 terms with highest frequencies
		List<String> topFrequentTerms = getTopFrequentTerms(tokensWithFrequencies);
		//STEP 7: Write these term to a file which will be given to Apriori as input
		writeToFile("input.txt", topFrequentTerms);
		//STEP 8: Start the Apriori Algorithm and generate association rules
		AprioriImplementer.start();

	}

	/*
	 * STEP 2:
	 * This method accepts the list of strings from step 1 and creates a document for every String in the list
	 */
	
	public static List<String> datatoXml(List<String> allDocuments)
			throws Exception {
		List<String> convertedDocs = new ArrayList<String>();
		Properties props = new Properties();
		props.put("annotators", "tokenize, cleanxml, ssplit, pos, lemma");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		for (int i = 0; i < allDocuments.size(); i++) {
			ByteArrayOutputStream xmlout = new ByteArrayOutputStream();
			Annotation annotation = new Annotation(allDocuments.get(i));
			pipeline.annotate(annotation);
			if (xmlout != null)
				pipeline.xmlPrint(annotation, xmlout);
			convertedDocs.add(new String(xmlout.toByteArray(), "UTF-8"));
		}
		int j = 0;
		for (String docs : convertedDocs) {
			PrintStream ps = new PrintStream(
					"C:/Users/Hardik/workspace/Text Mining/Processed Dataset/processed_document_"+ j + ".xml");
			ps.print(docs);
			j++;
			ps.close();
		}
		return convertedDocs;
	}

	/*
	 * STEP 4: 
	 * This method accepts the list of generated token by step 3 and calculates the frequency for each token and stores it in a given map
	 * The key is the token itself and the value is the frequency
	 */
	public static List<Map<String, Integer>> computeTermFrequency(List<String> tokenList) throws Exception {
		Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
		ArrayList<Map<String, Integer>> mapList = new ArrayList<Map<String, Integer>>();
		for (String document : tokenList) {
			frequencyMap = new HashMap<String, Integer>();
			StringTokenizer st = new StringTokenizer(document);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (frequencyMap.containsKey(token))
					frequencyMap.put(token, frequencyMap.get(token) + 1);
				else
					frequencyMap.put(token, 1);
			}
			mapList.add(frequencyMap);
		}
		return mapList;
	}

	/*
	 * Step 5: This method applies the TF-IDF formulae by iterating through the map of tokens and frequencies.
	 * It returns a new map with calculated TF-IDF as key and tokens as values. We do this because We want the top 10 tokens with highest TF-IDF value 
	 */
	public static List<Map<Double, String>> calculateFrequencies(List<Map<String, Integer>> termFrequency) throws Exception {
		List<Map<String, Integer>> temp = new ArrayList<Map<String, Integer>>(termFrequency);
		List<Map<Double, String>> idf = new ArrayList<Map<Double, String>>();
		for (int i = 0; i < temp.size(); i++) {
			Map<Double, String> map = new HashMap<Double, String>();
			Map<String, Integer> other = new HashMap<String, Integer>(temp.get(i));
			for (Map.Entry<String, Integer> entry : other.entrySet()) {
				String token = entry.getKey();
				double x1 = 1.0;
				for (int j = 0; j < temp.size(); j++) {
					if (i != j) {
						if (temp.get(j).containsKey(token))
							x1++;
					}
				}
				map.put(new Double(new BigDecimal(entry.getValue()* Math.log10(temp.size() / x1)).setScale(6,BigDecimal.ROUND_HALF_UP).doubleValue()), token);
			}
			idf.add(map);
		}
		return idf;
	}

	/*
	 * Step 6: This method selects top 10 terms with highest TF-IDF value from map received from step 5 and adds them to a new list
	 * and returns this list
	 */
	public static List<String> getTopFrequentTerms(
			List<Map<Double, String>> tfifList) throws Exception {
		List<Double> sorted = new ArrayList<Double>();
		List<String> sortedList = new ArrayList<String>();
		for (int k = 0; k < tfifList.size(); k++) {
			sorted = new ArrayList<Double>();
			Map<Double, String> p1 = new HashMap<Double, String>(
					tfifList.get(k));
			for (Map.Entry<Double, String> entry : p1.entrySet()) {
				Double t1 = entry.getKey();
				sorted.add(t1);
			}
			Collections.sort(sorted, Collections.reverseOrder());
			StringBuilder sb = new StringBuilder("");
			for (int i = 0; i < 10; i++) {
				if (i < sorted.size()) {
					sb.append(p1.get(sorted.get(i)));
					sb.append(" ");
				}
			}
			sortedList.add(sb.toString());
		}
		return sortedList;
	}

	private static void writeToFile(String fileName, List<String> topFrequentTerms) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter("C:/Users/Hardik/workspace/Text Mining/Apriori Input/"	+ fileName);
		for (String n : topFrequentTerms) {
			pw.println(n);
		}
		pw.close();
	}

}
