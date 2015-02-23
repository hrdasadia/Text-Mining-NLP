import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLReader {
	/* STEP 1:
	 * This method reads the .sgm file and extracts all the text between the body tags into an Arraylist of String
	 */
	public static List<String> readFromSource(String fileName) {

		List<String> allDocuments = new ArrayList<String>();
		try {

			File fXmlFile = new File("C:/Users/Hardik/workspace/Text Mining/Dataset/"+fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("BODY");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				allDocuments.add(nNode.getTextContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allDocuments;
	}
}