package file;
/**
 * TopicCrawler
 * This class is used to crawl the wa.gov website to collect the 
 * bills based on a topic list. The results produce a summary, and the bills 
 * associated with that topic.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import file.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.commons.lang.ArrayUtils;

import topic.AlphabetizeTopic;

/**
 * 
 * @author Sally Budack 2016
 */
public class TopicCrawler {
	/** topic to be searched for  */
	static String topic = getTopic();
	/**core web page */
	static String page = "http://app.leg.wa.gov/DLR";
	/** concatenated string to search */
	static String search = "http://apps.leg.wa.gov/billsbytopic/"
			+ "Results.aspx?subject=" + topic + "&year=2015";
	/** Schemes for the UrlValidator.*/
	static String[] schemes = { "http", "https" };
	/** For validating the URL.*/
	static UrlValidator urlValidator = new UrlValidator(schemes);
	/** map to store each with result with all the bills */
	private static Map<String, String[]> summary = new HashMap<String, String[]>();
	/** Set of words that are not to be gathered while gathering summary.*/
	private static Set<String> invalid_words = new HashSet<String>();
	
	/**
	 * constructor
	 * URLtoFileCrawler
	 */
	public TopicCrawler() {	
		// populate list
		construct_invalid_set();				
	}
	
	
	/**
	 * main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// populate list
		construct_invalid_set();
		// let's do it
		processPage(search);
	}
	
	/**
	 * Constructs the set of invalid words.
	 */
	private static void construct_invalid_set() {
		invalid_words.add(topic);
		invalid_words.add("RSS Feed");
		invalid_words.add("Bills by Topic Results");
		invalid_words.add(topic + " RSS Feed");
		invalid_words.add(":");
		invalid_words.add("billinfo");
		invalid_words.add("summary");
		invalid_words.add("Search");
		invalid_words.add("Test 1");
		invalid_words.add("Test 2");
		invalid_words.add("Test 3");
		invalid_words.add("Legislature Home");
		invalid_words.add("Senate");
		invalid_words.add("House of Representatives");
		invalid_words.add("Contact Us");
		invalid_words.add("Search");
		invalid_words.add("Help");
		invalid_words.add("Mobile");
		invalid_words.add("Outside the Legislature");
		invalid_words.add("Access Washington");
		invalid_words.add("Bill Information > Bills By Topic > Results");
		invalid_words.add("Bills that have passed the Legislature are shown in "
				+ "bold text with an * preceding them.");
		invalid_words.add("Bills by Topic Results");
	}
	
	
	/**
	 * getTopic	
	 * @return
	 */
	public static String getTopic() {	
		// array to populate pull down menu
		String[] choices = {"#","A","B","C","D","E","F","G","H","I","J","K","L",
				"M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		// collect first letter of topic
		JFrame frame = new JFrame("String Choices");
		topic = (String) JOptionPane.showInputDialog(frame , "",
				"Please select the first letter of your topic:", JOptionPane.QUESTION_MESSAGE, null, 	                                                                       
				choices, // Array of choices
				choices[0]); // Initial choice	
		// send letter to shorter list (only that letter will be on next menu)
		topic = chooseSubject(topic);
		return topic;
	}
	
	/**
	 * chooseSubject	
	 * @param topic 
	 * @return
	 */
	private static String chooseSubject(String topic) {
		// choose topic
		String[] choices = {" "};		
		switch(topic){
			case "#": 
				choices = setChoices(topic);
				break;
			case "A":
				choices = setChoices(topic);
				break;
			case "B":
				choices = setChoices(topic);
				break;
			case "C":
				choices = setChoices(topic);
				break;
			case "D":
				choices = setChoices(topic);
				break;
			case "E":
				choices = setChoices(topic);
				break;
			case "F":
				choices = setChoices(topic);
				break;
			case "G":
				choices = setChoices(topic);
				break;
			case "H":
				choices = setChoices(topic);
				break;
			case "I":
				choices = setChoices(topic);
				break;
			case "J":
				choices = setChoices(topic);
				break;
			case "K":
				choices = setChoices(topic);
				break;
			case "L":
				choices = setChoices(topic);
				break;
			case "M":
				choices = setChoices(topic);
				break;
			case "N":
				choices = setChoices(topic);
				break;
			case "O":
				choices = setChoices(topic);
				break;
			case "P":
				choices = setChoices(topic);
				break;
			case "Q":
				choices = setChoices(topic);
				break;
			case "R":
				choices = setChoices(topic);
				break;
			case "S":
				choices = setChoices(topic);
				break;
			case "T":
				choices = setChoices(topic);
				break;
			case "U":
				choices = setChoices(topic);
				break;
			case "V":
				choices = setChoices(topic);
				break;
			case "W":
				choices = setChoices(topic);
				break;
			case "X":
				choices = setChoices(topic);
				break;
			case "Y":
				choices = setChoices(topic);
				break;
			case "Z":
				choices = setChoices(topic);
				break;
			default:
				choices[0] = "Sorry, there are no results available."
						+ "You may search under another letter";
				break;				
		}
		
		JFrame frame = new JFrame("Topic Choices");
		if(choices[0].length() == 0){
			// if no results, redirect user, avoid null pointer
			choices[0] = "Sorry, there are no results available.";
		}
		String subject = (String) JOptionPane.showInputDialog(frame , "",
				"Please select a topic: ", JOptionPane.QUESTION_MESSAGE, null, 	                                                                       
				choices, // Array of choices
				choices[0]); // Initial choice			
		return subject;
	}
	
	/**
	 * setChoices	
	 * @param topic
	 * @return
	 */
	private static String[] setChoices(String topic) {
		char c = topic.charAt(0);
		int length = 0;
		String[] choices = new String[length];
		// topics grouped and separated
		choices = new AlphabetizeTopic(c, choices, length).getAbc();
		return choices;		
	}
	
	/**
	 * 
	 * processPage	
	 * @param URL
	 * @throws IOException
	 */
	public static void processPage(String URL) throws IOException {		
		File dir = new File(".");
		// file to save data
		String loc = dir.getCanonicalPath() + File.separator + "saveMyTopic.txt";	
		// insert to file, overwrite existing data
		FileWriter fstream = new FileWriter(loc, false);
		BufferedWriter wrOut = new BufferedWriter(fstream);
		Document doc = null;
		try {
			doc = Jsoup.connect(URL).get();
		} catch (IOException e1) {
			//handle exception and keep going
			System.err.println(URL + "\n is an exception, NO GO");
		}		
		// save elements
		Elements e = null;
		// get elements
		e = doc.getElementsContainingText(topic);
		// put elements in string
		String text = e.text();
		// remove unwanted parts
		text = removeText(text);
		text = convertToCSV(text);
		// keep strings that are useful
		if(text.trim().length() > 2){
			//			System.out.println(text.trim());
			wrOut.write(text.trim());
		}
		wrOut.close();		
	}
	
	/**
	 * convertToCSV	
	 * @param text
	 * @return
	 */
	private static String convertToCSV(String text) {
		String[] title = new String[1];
		String[] seeAlso = new String[1];
		int top = 0;
		int r3 = 0;
		int sum = 0;
		boolean one = false;
		// save topic
		if(text.contains(topic)){
			title[0] = topic;
		}else{
			title[0] = topic;
		}
		// column 0
		//mark the index at the end of the string
		top = text.indexOf(title[0].length());
		// save see also, column 1
		if(text.contains("(")&& text.contains(")")){
			String temp = text.substring(text.indexOf('(')+1, text.indexOf(')'));
			// check if it's not a year in parentheses
			if(temp.length() > 6){
				seeAlso[0] = temp;
				
				System.err.println("^^^^  " + seeAlso[0] + "\n " );
				//mark the index at the end of the string
				r3 = top + text.indexOf(seeAlso[0].length());
				one = true;
			}
			char[] also = seeAlso[0].toCharArray();
			int cnt = 0;
			for(int i = 0; i < also.length; i++){
				if(also[i] == '('){
					also[i] = also[i +1];
					if(i > 6){
						cnt++;
					}
				}
				if(also[i] == ')'){
					char c = '\n';
					also[i] = also[i +1];
					also[i] = c;
					seeAlso[cnt] = seeAlso[0].substring(i);
					seeAlso[0] = seeAlso[0].substring(0, seeAlso[0].charAt((i - 1)));
					System.err.println("&&&&  " + seeAlso[0] + "\n " + seeAlso[1]);
				}				
			}
		}
		
		if(one){
			// set next marker to end of last string marked
			sum = r3;
		}else{
			sum = top;
		}
		String[] summ = new String[2];
		String[] bills = null;
		if(text.contains(":")){
			
			// save summary
			for(int i = 0; i < text.length(); i++){
				summ = text.split(":");	
				int ind = summ[0].indexOf(')') + 1;
				summ[0] = (String) summ[0].subSequence(ind, summ[0].length());
				System.err.println("####  " + summ[0] + "\n ");
				bills = summ[1].split(",");
				for(int j = 0; j < bills.length; j++){
					
					
				}													
			}			
		}
		StringBuilder sb = new StringBuilder();
		sb.append(title[0]);
		sb.append(",");
		if(one){
			for(int i = 0; i < seeAlso.length; i++){
				sb.append(seeAlso[0]);
			}
		}else{
			sb.append("");
		}
		sb.append(",");
		sb.append(summ[0]);
		for(int i = 0; i < bills.length; i++){
			sb.append(",");
			sb.append(bills[i]);
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
	
	
	/**
	 * removeText	
	 * @param text
	 * @return
	 */
	private static String removeText(String text) {
		String passed = "Bills that have passed the Legislature are shown"
				+ " in bold text with an * preceding them.";
		// remove all of string before useful part
		if(text.contains(passed)){
			text = text.trim().replace(text.subSequence(0, 
					text.lastIndexOf(passed)), "\n");
			text = text.trim().replace(passed, "\n");
		}
		// mark topic 
		if(text.contains(topic)){
			text = text.trim().replace(topic, "\n " + topic );	
		}
		// remove commas
		if(text.contains(",")){
			text = text.trim().replaceAll(",", ";");
		}
		
		return text.trim();
	}
	
	/**
	 * getSummary
	 * @return the summary
	 */
	public static Map<String, String[]> getSummary() {
		return summary;
	}
	
	/**
	 * setSummary
	 * @param summary the summary to set
	 */
	public static void setSummary(Map<String, String[]> summary) {
		TopicCrawler.summary = summary;
	}
}
