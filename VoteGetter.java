/**
 * 
 */
package data;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import file.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * VoteGetter gets the votes from the WALeg website 
 * for each bill in the database. Can be rerun upon system restart 
 * or to update at pre-determined intervals, but WA State Legislature
 * html code should be verified periodically for changes to ensure
 * compatibility.
 * @author Leslie Pedro
 *
 */
public class VoteGetter {
	
	/** Schemes for the UrlValidator.*/
	static String[] schemes = { "http", "https" };
	
	/** For validating the URL.*/
	static UrlValidator urlValidator = new UrlValidator(schemes);
	
	/** the URL for the roll-call page (sans bill # <-- must be appended)*/
	static String page = "http://app.leg.wa.gov/dlr/rollcall/rollcall.aspx?bienid=23&legnum=";
	
	/** For holding a list of bills to be checked.*/
	private static List<String> bills;
	
	/** Map of Bills to Votes --- votes are held as an array of Strings w/ indices:
	 * 		0: yeas
	 * 		1: nays
	 * 		2: others.
	 * */
	private static TreeMap<String, String[]> votes;

	/** Set of words that are not to be gathered while gathering votes.*/
	private static Set<String> invalid_words;
	
	/**
	 * Constructor for VoteGetter.
	 */
	public VoteGetter() {
		invalid_words = new HashSet<String>();
		construct_invalid_set();
		votes = new TreeMap<String, String[]>();
	}
	
	/**
	 * Constructs the set of invalid words. Invalid words are words
	 * that the crawler should avoid adding to any lists.
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
	 * @return the position in the string at which the votes (elected official names).
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
	 * Cuts invalid strings from the list of voting officials and reformats names with first
	 * initials.
	 * String is returned containing only officials voting separated by commas.
	 * @param text the parsed and cleaned string - as a string of names separated
	 * by commas.
	 * @param chamber the chamber of the elected official-- used to prevent legislators with the same names in different.
	 * chambers from mistakenly being marked as duplicates.
	 * @param insert_set the set (Y/N/O) to insert the data into.
	 * @param other1 one of the other sets (Y/N/O) to check for duplicate/changed votes.
	 * @param other2 the second of the remaining sets to check for dupelicate/changed votes.
	 * @return the new string of votes
	 */
	public static Set<String> get_votes(String text, String chamber, Set<String> insert_set, Set<String> other1, Set<String> other2){
		Scanner scan = new Scanner(text);
		String old_str = ""; // for appending first initial, when needed
		while(scan.hasNext()){
			String str = scan.next();
			if(!invalid_words.contains(str)) {
				if(str.endsWith(",")) {
					str = str.substring(0, str.indexOf(","));
					if(str.endsWith(".")) { // if ends with "." it is an intial, append it to the previous name
						insert_set.remove(old_str);
						str = old_str.substring(0, old_str.indexOf("_")) + " " + str;
					}
				}
				str += ("_" + chamber);
				if(!insert_set.contains(str)){
					if(other1.contains(str)) {
						other1.remove(str);
					}
					if(other2.contains(str)) {
						other2.remove(str);
					}
					insert_set.add(str);
				}
			}
			old_str = str;
		}
		scan.close();
		return insert_set;
	}
	
	/**
	 * Gets all the votes on a specific bill and puts them in the vote map.
	 * @param doc - the website URL (sans the bill#).
	 * @param bill_num - the bill for which votes are to be gathered.
	 */
	private static void get_votes_on_bill(Document doc, String bill_num) {
		String search_page = page + bill_num; //append the bill number to the page
		try {
			doc = Jsoup.connect(search_page).userAgent("Mozilla").get(); // connect to the website
		} catch (IOException e1) {
			System.err.println(search_page + "\nError: Page Could Not Be Loaded");
			e1.printStackTrace();
			return;
		}
		String body = doc.html(); // the body of the website
		Elements elems = doc.getElementsByClass("reportTd"); // get all elements of the noted class for parsing
		Set<String> yeas = new TreeSet<String>(); // initially empty strings for holding votes
		Set<String> nays = new TreeSet<String>(); 
		Set<String> others = new TreeSet<String>(); 
		int batch_end = 0; //used in determining where to end parsing for a batch of votes
		String chamber = ""; // holds the chamber currently being checked
		int date[] = new int[3]; // holds the date of the votes
		// for each element:
		int num = 0;
		for(Element e : elems) {
			String text = e.text(); // get the text for that element
			if(text.startsWith("Chamber:")) { // if it indicates the chamber, grab that information (not done)
				int i = text.indexOf(" "); 
				chamber = text.substring(i + 1, text.lastIndexOf("E") + 1);
			}
			// If the text starts with "Voting yea" grab the yea votes
			if(text.startsWith("Voting yea:")) {
				batch_end = get_stop_point(text);
				String str = text.substring(0, batch_end);
				text = text.substring(batch_end);
				yeas = get_votes(str, chamber, yeas, nays, others);
			}if(text.startsWith("Voting nay:")) { // else if it starts with "Voting nay" grab nay votes
				batch_end = get_stop_point(text);
				String str = text.substring(0, batch_end);
				text = text.substring(batch_end);
				nays = get_votes(str, chamber, nays, yeas, others);
			}if(text.startsWith("Excused:") || text.startsWith("Absent:")) { // else if they are excused or absent, grab those (non)votes
				batch_end = get_stop_point(text);
				others = get_votes(text, chamber, others, yeas, nays);
			}
		}
		String[] all_votes = new String[3];
		all_votes[0] = vote_set_to_string(yeas);
		all_votes[1] = vote_set_to_string(nays);
		all_votes[2] = vote_set_to_string(others);
		votes.put(bill_num, all_votes);
	}
	
	/**
	 * converts the set of votes to a string.
	 * @param votes a set containing the votes.
	 * @return the completed string.
	 */
	private static String vote_set_to_string(Set votes) {
		StringBuilder sb = new StringBuilder();
		Iterator itr = votes.iterator();
		if(itr.hasNext()) {
			String name = (String) itr.next();
			sb.append(name.substring(0, name.indexOf("_")));
		}
		while(itr.hasNext()) {
			String name = (String) itr.next();
			name = name.substring(0, name.indexOf("_"));
			sb.append(",");
			sb.append(name);
		}
		return sb.toString();
	}
	
	/**
	 * writes the csv file with votes.
	 * @param f_out the csv file to write to.
	 */
	private static void write_csv(File f_out) {
		try {
			FileWriter fout = new FileWriter(f_out);
			fout.write("bill_num, name, vote\n");
			for(String bill: votes.keySet()){
				String[] all_votes = votes.get(bill);
				write_votes(fout, all_votes[0], bill, "y");
				write_votes(fout, all_votes[1], bill, "n");
				write_votes(fout, all_votes[2], bill, "o");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes the votes to the csv file.
	 * @param fout the file writer for writing to the csv file.
	 * @param output the string of votes.
	 * @param bill the bill number.
	 * @param vote the vote value (y = yea, n = nay, o = excused or absent).
	 */
	private static void write_votes(FileWriter fout, String output, String bill, String vote){
		int index = 0;
		int stop = -1;
		try {
			if(!output.isEmpty()){
				while((stop = output.indexOf(",", index)) != -1 && index != stop){
					fout.write(bill + ",");
					fout.write(output.substring(index, stop) + ",");
					fout.write(vote + "\n");
					index = stop + 1;
				} 
				if(index != output.length() - 1){
					fout.write(bill + ",");
					fout.write(output.substring(index) + ",");
					fout.write(vote + "\n");
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Main method for the VoteGetter.
	 * @param args - command line args.
	 */
	public static void main(String[] args) {
		VoteGetter v_get= new VoteGetter();
		File f_in = new File("billsbysponsorandstatus.csv");
		File f_out = new File("votes.csv");
		try {
			Scanner fscan = new Scanner(f_in); // input file scanner
			Document doc = null; // the document from the web
			while(fscan.hasNextLine()){
				fscan.nextLine();
				if(fscan.hasNext()) {
					fscan.next();
					String temp = fscan.next();
					int index = temp.indexOf(",");
					temp = temp.substring(0, index - 1);
					get_votes_on_bill(doc, temp);
				}
			}
			write_csv(f_out);
			fscan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}


