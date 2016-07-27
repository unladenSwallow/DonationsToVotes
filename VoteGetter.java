/**
 * 
 */
package data;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import file.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

/**
 * VoteGetter gets the votes from the WALeg website 
 * for each bill in the database. (Currently for one bill)
 * @author Leslie Pedro
 *
 */
public class VoteGetter {
	
	/** Schemes for the UrlValidator.*/
	static String[] schemes = { "http", "https" };
	/** For validating the URL.*/
	static UrlValidator urlValidator = new UrlValidator(schemes);
	/** the URL for the rollcall page -- not usable until bill # is appended.*/
	static String page = "http://app.leg.wa.gov/dlr/rollcall/rollcall.aspx?bienid=23&legnum=";
	/** For holding a list of bills to be checked.*/
	private static List<String> bills;
	/** Map of Bills to Votes --- votes are held as an array of Strings w/ indices:
	 * 		0: yeas
	 * 		1: nays
	 * 		2: others.
	 * */
	private static Map<String, String[]> votes;
	/** Set of words that are not to be gathered while gathering votes.*/
	private static Set<String> invalid_words;
	
	/**
	 * Constructor for VoteGetter.
	 */
	public VoteGetter() {
		invalid_words = new HashSet<String>();
		construct_invalid_set();
		votes = new HashMap<String, String[]>();
	}
	
	/**
	 * Constructs the set of invalid words.
	 */
	private void construct_invalid_set() {
		invalid_words.add("Absent:");
		invalid_words.add("Voting");
		invalid_words.add("yea:");
		invalid_words.add("nay:");
		invalid_words.add("and");
		invalid_words.add("Excused:");
		invalid_words.add("Senator");
		invalid_words.add("Senators");
		invalid_words.add("Representative");
		invalid_words.add("Representatives");
	}
	
	/**
	 * Checks ahead in the string to see when to stop reading the current votes.
	 * @param text - the string to examine.
	 * @return the position in the string at which the votes (elected official names)
	 */
	public static int get_stop_point(String text) {
		int stop = text.indexOf("Voting", 6);
		if(stop == -1){
			stop = text.indexOf("Excused:");
		}
		if(stop == -1){
			stop = text.indexOf("Absent:");
		}
		if(stop == -1) {
			stop = text.length();
		}
		return stop;
	}

	/**
	 * Cuts invalid string from the list of voting officials.
	 * String is returned containing only officials voting separated by commas.
	 * @param text the parsed and cleaned string - as a string of names separated
	 * by commas.
	 * @return the new string.
	 */
	public static String get_votes(String text){
		Scanner scan = new Scanner(text);
		StringBuilder sb = new StringBuilder();
		while(scan.hasNext()){
			String str = scan.next();
			if(scan.hasNext() && !str.endsWith(",") && !invalid_words.contains(str)){
				str += ",";
			}
			if(!invalid_words.contains(str)){
				sb.append(str);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Gets all the votes on a specific bill and puts them in the vote map.
	 * @param doc - the website URL (sans the bill#).
	 * @param bill_num - the bill for which votes are to be gathered.
	 */
	private static void get_votes_on_bill(Document doc, String bill_num) {
		String search_page = page + bill_num; //append the bill number to the page
		try {
			doc = Jsoup.connect(search_page).get(); // connect to the website
		} catch (IOException e1) {
			System.err.println(search_page + "\nError: Page Could Not Be Loaded");
			e1.printStackTrace();
			return;
		}
		String body = doc.html(); // the body of the website
		Elements elems = doc.getElementsByClass("reportTd"); // get all elements of the noted class for parsing
		String yeas = ""; // initially empty strings for holding votes
		String nays = "";
		String others = "";
		int batch_end = 0; //used in determining where to end parsing for a batch of votes
		String chamber = ""; // holds the chamber currently being checked
		int date[] = new int[3]; // holds the date of the votes
		// for each element:
		for(Element e : elems) {
			String text = e.text(); // get the text for that element
			if(text.startsWith("Chamber:")) { // if it indicates the chamber, grab that information (not done)
				int i = text.indexOf(" "); 
				chamber = text.substring(i + 1, text.lastIndexOf("E") + 1);
			} else if(text.startsWith("Date:")) { // if it indicates the date, grab that info (not done)
				String temp = text.substring(text.indexOf(" "));
				int start = 1;
				int stop = temp.indexOf("-");
				int month = Integer.valueOf(temp.substring(start, stop));
				start = stop + 1;
				stop = temp.indexOf("-", start);
				int day = Integer.valueOf(temp.substring(start, stop));
				start = stop + 1;
				int year = Integer.valueOf(temp.substring(start));
				if(date.length == 0) {
					date[0] = month;
					date[1] = day;
					date[2] = year;
				} else if(year >= date[2]) {
					if(month >= date[1]) {
						if(year == date[2] && month == date[1] && day >= date[0]) {
							// do something here to ensure only the most recent date's
							// data is used.
						}
					}
				}
			}
			// If the text starts with "Voting yea" grab the yea votes
			if(text.startsWith("Voting yea:")) {
				batch_end = get_stop_point(text);
				String str = text.substring(0, batch_end);
				text = text.substring(batch_end);
				str = get_votes(str);
				if(!yeas.isEmpty() && !yeas.endsWith(",")) {
					yeas += ",";
				}
				yeas += str;
//				System.out.println("Yeas: " + yeas);
			}if(text.startsWith("Voting nay:")) { // else if it starts with "Voting nay" grab nay votes
				batch_end = get_stop_point(text);
				String str = text.substring(0, batch_end);
				text = text.substring(batch_end);
				str = get_votes(str);
				if(!nays.isEmpty() && !nays.endsWith(",")) {
					nays += ",";
				}
				nays += str;
//				System.out.println("Nays: " + nays);
			}if(text.startsWith("Excused:") || text.startsWith("Absent:")) { // else if they are excused or absent, grab those (non)votes
				batch_end = get_stop_point(text);
				String str = get_votes(text);
				if(!others.isEmpty() && !others.endsWith(",")) {
					others += ",";
				}
				others += str;
//				System.out.println("Others: " + others);
			}
		}
		String[] all_votes = {yeas, nays, others}; // create array of votes for the vote map
		for(int i = 0; i < all_votes.length; i++){
			if(all_votes[i].endsWith(",")){ // if there is a comma at the end, get rid of it
				all_votes[i] = all_votes[i].substring(0, all_votes[i].length() - 1);
			}
			System.out.println(i + ". " + all_votes[i]);
		}
		votes.put(bill_num, all_votes); // place the votes in the vote map
	}
	
	/**
	 * Main method for the VoteGetter.
	 * @param args - command line args.
	 */
	public static void main(String[] args) {
		VoteGetter v_get= new VoteGetter();
		Document doc = null;
		get_votes_on_bill(doc, "1166");
	}
}
