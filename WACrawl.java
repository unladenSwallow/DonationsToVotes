/**
 * @author Mark Peters
 * it crawls and gets your donors and your keyword-bill associations
 * @version nowwithcorruptionindex!
 * 
 * you need these jar dependencies:
 *	commons-codec-1.10
 *	commons-io-2.5
 *	commons-lang3-3.4
 *	commons-logging-1.2
 *	commons-validator-1.5.1
 *	cssparser-0.9.20
 *	htmlunit-2.23
 *	htmlunit-core-js-2.23
 *	httpclient-4.5.2
 *	httpcore-4.4.4
 *	httpmime-4.5.2
 *	jetty-io-9.2.18
 *	jetty-util-9.2.18
 *	jsoup-1.9.2
 *	neko-htmlunit-2.23
 *	sac-1.3
 *	serializer-2.7.2
 *	websocket-api-9.2.18
 *	websocket-client-9.2.18
 *	websocket-common-9.2.18
 *	xalan-2.7.2
 *	xercexlmpl-2.11.0
 *	xml-apis-1.4.01
 * 
 */

package data;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;



class WACrawl {
	
	
	
	///////////////////////////////////
	///////////////// WACRAWL VARIABLES
	///////////////////////////////////
	
	final static boolean OUTPUT_PROGRESS = true;
	
	final static boolean DOWNLOAD_KEYWORDS = true;
	final static boolean DOWNLOAD_POLITICIANS = true;
	final static boolean DOWNLOAD_DONATIONS = true;
	final static boolean DOWNLOAD_ZIPCODES = true;
	
	final static boolean CRAWL_DONATIONS = true;
	
	final static boolean SAVE_KEYWORDS = true;
	final static boolean SAVE_POLITICIANS = true;
	final static boolean SAVE_DONATIONS = true;
	final static boolean SAVE_ZIPCODES = true;
	
	final static boolean ATTEMPT_SAVE_ON_FAILED_DATA = false;
	final static boolean ATTEMPT_NO_POLITICIAN_CSV_PARSE = true;
	
	final static String BASE_DIRECTORY = "./";
	final static StringBuilder LOG = new StringBuilder("LOG start: " + new Timestamp(System.currentTimeMillis()));
	final static String DONATIONS_DIRECTORY = BASE_DIRECTORY + "donations/";
	final static String LOG_FILE_PRE = BASE_DIRECTORY + "WACrawl_log_";
	final static String LOG_FILE_POST = ".txt";
	final static String DONATIONS_FILE = BASE_DIRECTORY + "donations.csv";
	final static String ZIPCODES_FILE = BASE_DIRECTORY + "zipcodes.csv";
	final static String POLITICIANS_FILE = BASE_DIRECTORY + "politcians.csv";
	final static String KEYWORDS_FILE = BASE_DIRECTORY + "keywords.txt";
	final static String KEYSANDBILLS_FILE = BASE_DIRECTORY + "keysandbills.csv";
	
	
	
	///////////////////////////////////
	///////////////// SYSTEM FUNCTIONS
	///////////////////////////////////
	
	final static String COMMA_REPLACE = ";";
	final static String NUMBER_REGEX = "[^0-9.-]";
	final static int SIZE = 1000;
	final static int EXT_SIZE = 4;
	final static String a = " -> ";
	final static String d = ",";
	final static String t = "\t";
	final static String n = "\n";
	final static String P_WRITE_ATTEMPT = " information acquired. Writing to file...";
	final static String P_WRITE_SUCCESS = " file write succeeded!";
	final static String FAILED_DATA_SAVE = "Attempting to write with failed data...";
	final static boolean QUICK_RUN = false;
	final static int QUICK_COUNT = 15;
	final static boolean KEEP_LOG = true;
	
	static int errors = 0;
	static WebClient client;
	static int index = 0;
	static boolean successKeywords = false;
	static boolean successPoliticians = false;
	static boolean successDonations = false;
	static boolean successZipcodes = false;
	
	public static void main(String[] args) {
		new File(DONATIONS_DIRECTORY).mkdirs();
		if (KEEP_LOG) LOG.append(n+"Log: Begin WACrawl");
		else System.out.println(n+"Warning!!! No log being kept; there is too much text output so most will be lost.\nPlease set KEEP_LOG to true.");
		System.out.println(n+"Console: Begin WACrawl");
		makeClient();
		downloadKeywords();
		saveKeywords();
		downloadPoliticians();
		downloadDonations();
		crawlDonations();
		saveDonations();
		saveZipcodes();
		savePoliticians();
		System.out.println(n+"Console: End WACrawl");
		log(0);
	}
	
	static void makeClient() {
		client = new WebClient(BrowserVersion.CHROME);
		client.getOptions().setJavaScriptEnabled(true);
		client.getOptions().setRedirectEnabled(true);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.getOptions().setTimeout(0);
		Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		p(n+"WebClient make successful.",0,true);
	}
	
	static void p(final String progress, int tabs, boolean newline) {
		if (!OUTPUT_PROGRESS) return;
		String message = progress + (newline?n:"");
		for (int r = 0; r < tabs; r++) message = t+message;
		LOG.append(message);
		System.out.print(message);
	}
	
	static void e(Exception e, boolean shutdown) {
		errors++;
		p(n+"*****OPERATION UNSUCCESSFUL*****"+n+"***** \\/ ERROR MESSAGES \\/ *****",0,true);
		e.printStackTrace();
		if (shutdown) {
			p(n+"FATAL ERROR; SYSTEM EXIT",1,true);
			log(1);
			System.exit(1);
		}
	}
	
	static void log(int error) {
		if (!KEEP_LOG) return;
		if (error == 0) LOG.append("no fatal errors detected during run\n");
		else	  		LOG.append("at least one fatal error with code: " + error + " encountered\n");
		String errorText = errors + " error(s) detected during runtime\n";
		System.out.print(errorText);
		LOG.append(errorText);
		LOG.append("Log: End WACrawl");
		LOG.append(n+"LOG terminate: " + new Timestamp(System.currentTimeMillis()));
		try {
			FileWriter log = new FileWriter(LOG_FILE_PRE + System.currentTimeMillis() + LOG_FILE_POST);
			log.append(LOG.toString());
			log.flush();
			log.close();
		} catch (Exception e) { System.out.println("Oh man you really fucked it up this time"); }
	}
	
	static void pfile(final String file, final String code) {
		boolean success = (code == P_WRITE_SUCCESS);
		p((success?"":n)+file.substring(BASE_DIRECTORY.length(), file.length()-EXT_SIZE) + code,(success?1:0),true);
	}
	
	
	
	///////////////////////////////////
	///////////////// KEYWORDS SECTION
	///////////////////////////////////
	
	final static String TOPIC_PAGE = "http://app.leg.wa.gov/dlr/topicalindex/Results.aspx?year=2016";
	final static String KEYWORDS_HEADER = "KEYWORD"+n;
	final static String KEYSANDBILLS_HEADER = "KEYWORD"+d+"BILL"+n;
	final static String NONTOPIC = "(/*";
	final static String NO_BILL = "NOBILL";
	final static String TOPIC_AUGUR = "<b>";
	final static int 	TOPIC_SPACE = 4;
	final static String BILL_AUGUR = "bill=";
	final static int 	BILL_SPACE = 12;
	final static String NEXT_AUGUR = "<";
	final static int	TOPIC_LIST_INDEX = 1;
	final static String keywords[] = new String[SIZE];
	final static String bills[][] = new String[SIZE][SIZE*10];

		
	static void downloadKeywords() {
		
		if (!DOWNLOAD_KEYWORDS) return;
		
		index = 0;
		
		try {
			
			int i=0,j=0,k=0;
			HtmlPage html = client.getPage(TOPIC_PAGE);
			String webpage = Jsoup.parse(html.asXml()).getAllElements().get(TOPIC_LIST_INDEX).toString();
			p(n+"Keyword website acquired. Beginning crawl...",0,true);
			
			while (true) {
				do i = webpage.indexOf(TOPIC_AUGUR, i) + TOPIC_SPACE;
				while (NONTOPIC.contains(""+webpage.charAt(i)));
				for (k = 0; j < i && j > 0; k++) {
					bills[index][k] = webpage.substring(j, webpage.indexOf(NEXT_AUGUR, j)-1).replace(d, COMMA_REPLACE);
					j = webpage.indexOf(BILL_AUGUR, j) + BILL_SPACE;
					p("BILL: " + bills[index][k],2,true);
				} bills[index++][k] = NO_BILL;
				j = webpage.indexOf(BILL_AUGUR, i) + BILL_SPACE;
				if (i >= TOPIC_SPACE) keywords[index] = webpage.substring(i, webpage.indexOf(NEXT_AUGUR, i)-1).replace(d, COMMA_REPLACE);
				else break; p("KEYWORD: " + keywords[index],1,true);
			}
			
		} catch (Exception e) { e(e,false); return; }
		
		successKeywords = true;

	}
	
	static void saveKeywords() {
		
		if (!SAVE_KEYWORDS) return;
		if (!successKeywords)
			if (ATTEMPT_SAVE_ON_FAILED_DATA) p(FAILED_DATA_SAVE,0,false);
			else return;
		
		try {
			
			pfile(KEYWORDS_FILE, P_WRITE_ATTEMPT);
			FileWriter keys = new FileWriter(KEYWORDS_FILE);
			keys.append(KEYWORDS_HEADER);
			FileWriter keysandbills = new FileWriter(KEYSANDBILLS_FILE);
			keysandbills.append(KEYSANDBILLS_HEADER);
			
			for (int i = 0; i < index; i++) {
				if (keywords[i] != null && keywords[i].length() > 0)
					keys.append(keywords[i] + n);
				for (int k = 0;;k++)
					if (!bills[i][k].equals(NO_BILL)) keysandbills.append(keywords[i] + d + bills[i][k] + n);
					else break;
			}
			
			keys.flush();
			keys.close();
			keysandbills.flush();
			keysandbills.close();
			pfile(KEYWORDS_FILE, P_WRITE_SUCCESS);
			
		} catch (Exception e) { e(e,false); }
		
	}

	
	
	///////////////////////////////////
	///////////////// DONATIONS SECTION
	///////////////////////////////////

	final static int US_ZIPCODE_MAX = 100000;
	final static int WA_ZIPCODE_MIN = 98000;
	final static int WA_ZIPCODE_MAX = 99404;
	final static int WA_DISTRICTS = 49;
	final static int NO_DISTRICT = US_ZIPCODE_MAX-1;
	final static int OUT_OF_STATE = WA_DISTRICTS+1;
	final static String LEGISLATURE = "http://web.pdc.wa.gov/MvcQuerySystem/Candidate/leg_candidates?page=";
	final static String CONTRIBUTIONS = " 2016 Detailed Contributions.csv";
	final static String CSV_FETCH_PRE = "http://web.pdc.wa.gov/MvcQuerySystem/CandidateData/excel?param=";
	final static String CSV_FETCH_POST = "====&year=2016&tab=contributions&type=legislative&page=1&orderBy=&groupBy=&filterBy=";
	final static String DONATIONS_HEADER = "DONOR"+d+"POLITICIAN"+d+"DATE"+d+"AMOUNT"+d+"ROLE"+d+"DISTRICT"+n;
	final static String ZIPCODES_HEADER = "ZIPCODE"+d+"DISTRICT"+n;
	final static String POLITICIANS_HEADER = "WEBCODES"+d+"POLITICIAN"+d+"DISTRICT"+d+"OFFICE"+d+"POSITION"+d+"PARTY"+d+"DECLARED"+d+"MONEY"+d+"DONATIONS"+d+"CUCKOLD"+n;
	final static String ZIPCODE_NOT_FOUND = "unknown";
	final static String ZIPCODE_NOT_WASH = "not in wa";
	final static String DONOR_UNKNOWN = "money unaccouted for";
	final static String DONOR_MISCALC = "totals miscalculation";
	final static String DONOR_NEGATIVE = "negative funds";
	final static String LINK_AUGUR = "href";
	final static String CODE_AUGUR = "param";
	final static int	CODE_SPACE = 6;
	final static int	CODE_WIDTH = 14 + CODE_SPACE;
	final static int	STAT_SPACE = 5;
	final static String	CASH_AUGUR = "$";
	final static int	CASH_SPACE = 1;
	final static int	CODE_UNIQUE = 7;
	final static String	UNIQUE_FRAME = " (*)";
	final static int	UNIQUE_INDEX = 2;
	final static int STATS_CODES = 0;	final static int STATS_NAMES = 1;	final static int STATS_DISTS = 2;
	final static int STATS_REPRS = 3;	final static int STATS_POSTS = 4;	final static int STATS_PARTY = 5;
	final static int STATS_DECLR = 6;	final static int STATS_MONEY = 7;	final static int STATS_NUMDS = 8;
	final static int STATS_CUCKS = 9;	final static int STATS_LENGTH = 10;	final static int STATS_LATER = 3;
	final static int ZIPCODE_LENGTH = 5;
	final static int CSV_COLS = 5;
	final static int CSV_SKIP_ROWS = 3;
	final static int CSV_PERS_COLS = 6;
	final static int CSV_SKIP_COLS = 4;
	final static double CENT_DROPOFF = 0.01;

	final static String stats[][] = new String[STATS_LENGTH][SIZE];//{codes,names,dists,reprs,posts,party,declr,money,numds,cucks};
	final static int district[] = new int[US_ZIPCODE_MAX];
	final static StringBuilder donations = new StringBuilder(DONATIONS_HEADER);
	final static StringBuilder zipcodes = new StringBuilder(ZIPCODES_HEADER);

	static HtmlPage dist_html = null;	
	
	static void downloadPoliticians() {
		
		index = 0;
		
		if (DOWNLOAD_POLITICIANS) try {
			p(n+"Politician scanning of website beginning...",0,true);
			
			final int slen = STATS_LENGTH - STATS_LATER;
			int p = 1;
			index = 0;
			boolean polsFound = false;
			
			do {
				
				HtmlPage html = client.getPage(LEGISLATURE + p);
				boolean flag = false;
				polsFound = false;
				String s = "";
				int stat = 0;
				p("LEGISLATURE PAGE: " + p,1,true);
				
				for (Element e : Jsoup.parse(html.asXml()).getAllElements())
					if (flag) {
						s = e.toString();
						s = s.substring(STAT_SPACE, s.length()-STAT_SPACE-1).replace(d, "");
						stats[stat][index] = (stat == STATS_DECLR ? s.substring(s.indexOf(CASH_AUGUR)+CASH_SPACE) : s);
						if (++stat == slen) {
							flag = false;
							s = "POLITICIAN: ";
							for (stat = 0; stat < slen; stat++) s += stats[stat][index] + a;
							p(s.substring(0, s.length()-a.length()),2,true);
							index++;
						}
					} else {
						s = e.attr(LINK_AUGUR);
						int i = s.indexOf(CODE_AUGUR);
						if (i!=-1) {
							flag = true;
							polsFound = true;
							stats[STATS_CODES][index] = (s.substring(i+CODE_SPACE, i+CODE_WIDTH));
							stat = STATS_NAMES;
						}
					}

				p++;
			} while (polsFound);
			p("All politician names and codes acquired.",0,true);
		} catch (Exception e) { e(e,false); return; }
		else for (File f : new File(BASE_DIRECTORY).listFiles())
				if (f.getName().equals(POLITICIANS_FILE.substring(BASE_DIRECTORY.length())))
					index = populatePoliticians(f, stats);
		
		if (index == 0) {
			e(new Exception("No politicians file found!"),false);
			return;
		}
		
		successPoliticians = true;
		
	}
	
	static void downloadDonations() {

		if (!DOWNLOAD_DONATIONS) return;
		
		try {
			p(n+"Downloading politician-donor CSVs (may take a while)...",0,true);
			for (int i = 0; i < index; i++) {
				String identifier = UNIQUE_FRAME.substring(0,UNIQUE_INDEX) + stats[STATS_CODES][i].charAt(CODE_UNIQUE) + UNIQUE_FRAME.charAt(UNIQUE_FRAME.length()-1);
				FileUtils.copyURLToFile(new URL(CSV_FETCH_PRE +	stats[STATS_CODES][i] + CSV_FETCH_POST),
										new File(DONATIONS_DIRECTORY + stats[STATS_NAMES][i] + identifier + CONTRIBUTIONS));
				p("CSV DOWNLOADED: " + stats[STATS_NAMES][i] + identifier,1,true);
			}
		} catch (Exception e) { e(e,true); }
		
	}
	
	static void crawlDonations() {
		
		if (!CRAWL_DONATIONS) return;
		
		final HashMap<String, String> donors = new HashMap<String, String>();
		final File duplicates[] = new File[SIZE];
		
		try {
			
			if (!DOWNLOAD_ZIPCODES) {
				for (File f : new File(BASE_DIRECTORY).listFiles())
					if (f.getName().equals(ZIPCODES_FILE.substring(BASE_DIRECTORY.length())))
						populateZipcodes(f, district);
			} else successZipcodes = true;
			
			int nameend = 0, count = 0, zipcode = -1, sinix = 0, dupix = 0;

			p(n+"Donor information preparing for parsing (if zipcodes file absent, this will be very long)...",0,true);
			crawl: for (File f : new File(DONATIONS_DIRECTORY).listFiles()) {
				
				for (int ff = 0; ff < dupix; ff++)
					if (f.getName().equals(duplicates[ff].getName())) continue crawl;
				
				nameend = f.getName().indexOf(CONTRIBUTIONS);
				
				if (nameend!=-1) {
					String politician = f.getName().substring(0, nameend - UNIQUE_FRAME.length());
					String donor = "";
					String next = "";
					int dcount = 0;
					double currCash = 0;
					boolean selfDonation = false;
					double persCash = 0;
					double distCash = 0;
					double cuckCash = 0;
					
					p("POLITICIAN: " + politician,1,true);
					
					int pol = -1;
					int nullspot = -1;
					sinix = dupix;
					for (int p = 0; p < stats[STATS_NAMES].length; p++)
						if (stats[STATS_NAMES][p] != null) {
							if (stats[STATS_NAMES][p].equals(politician))
								if (pol == -1) pol = p;
								else {
									try { 
										duplicates[dupix++] = new File(DONATIONS_DIRECTORY + politician + UNIQUE_FRAME.replace(UNIQUE_FRAME.charAt(UNIQUE_INDEX), stats[STATS_CODES][p].charAt(CODE_UNIQUE)) + CONTRIBUTIONS);
										p("Duplicate found: " + politician + " with declared " + stats[STATS_DECLR][pol] + " and " + stats[STATS_DECLR][p] + " funds; attempting merge",1,true);
										String funds = stats[STATS_DECLR][pol];
										try { stats[STATS_DECLR][pol] = ""+(Double.parseDouble(stats[STATS_DECLR][pol]) + Double.parseDouble(stats[STATS_DECLR][p])); }
										catch (Exception e) { stats[STATS_DECLR][pol] = funds; }
									} catch (Exception e) { e(e,false); dupix--; }
									stats[STATS_NAMES][p] = null;
								}
						} else if (nullspot == -1) nullspot = p;
					if (pol == -1)
						if (ATTEMPT_NO_POLITICIAN_CSV_PARSE)
							if (nullspot > -1) {
								pol = nullspot;
								stats[STATS_NAMES][nullspot] = politician;
							} else {
								e(new Exception("Politician not found: " + politician),false);
								continue;
							}
						else continue;
					
					for (boolean doubles = false; sinix <= dupix; sinix++, doubles = true) {

						if (doubles) f = duplicates[sinix-1];
						
						count++;
						Scanner scanner = new Scanner(f).useDelimiter(DELIMITER_REGEX);	
						String selfDistrict = stats[STATS_DISTS][pol];
	
						try {
							for (int i = 0; i < CSV_SKIP_ROWS; i++) scanner.nextLine();
							scanner.next();
							for (int i = 0; i < CSV_PERS_COLS; i++) {
								try { currCash = Double.parseDouble(scanner.next().trim().substring(1).replaceAll(NUMBER_REGEX, "")); }
								catch (Exception e) { e.printStackTrace(); currCash = 0; }
								if (currCash >= CENT_DROPOFF) {
									persCash += currCash;
									dcount++;							
								}							
							}
							if (persCash >= CENT_DROPOFF) {  
								String selfBribe = politician+d+politician+d+null+d+(int)persCash+d+SELF_FUNDED+d+selfDistrict;
								donations.append(selfBribe+n);
								p("BRIBE: " + selfBribe.replace(d, a),2,true);
							}
		
							scanner.nextLine();
							scanner.nextLine();
						} catch (Exception e) { e(e,false); continue; }
						
						while (scanner.hasNext()) {
							p("BRIBE: ",2,false);
							for (int i = 0; i <= CSV_COLS; i++) {
								try { switch (i) {
									case 1:
										next = politician;
										break;
									case 0:
										donor = scanner.next();
										next = donor;
										break;
									case 2:
										next = scanner.next().trim();
										break;
									case 3:
										try { currCash = Double.parseDouble(scanner.next().trim().replaceAll(NUMBER_REGEX, "")); }
										catch (Exception e) { currCash = 0; }
										if (currCash < 0) currCash = 0;
										next = ""+currCash;
										break;
									case 4:
										if (donors.get(donor) == null)
											donors.put(donor, guessRole(donor, politician, stats[STATS_NAMES]));
										next = donors.get(donor);
										if (next.equals(SELF_FUNDED)) selfDonation = true;
										break;
									case 5:
										for (int n = 0; n < CSV_SKIP_COLS; n++) next = scanner.next();
										p("FROM: " + next + a,3,false);
										try { zipcode = Integer.parseInt(next.trim().substring(0, ZIPCODE_LENGTH)); }
										catch (Exception e) { zipcode = NO_DISTRICT; }
										if (zipcode < 0) zipcode = NO_DISTRICT;
										p(""+zipcode,0,false);
										zip: while (true) switch (district[zipcode]) {
											case 0:				district[zipcode] = guessDistrict(zipcode); continue zip;
											case NO_DISTRICT:	next = ZIPCODE_NOT_FOUND;   				break zip;
											case OUT_OF_STATE:	next = ZIPCODE_NOT_WASH;					break zip;
											default:			next = (district[zipcode] < 10 ? "0" : "") +
																	    district[zipcode];					break zip;
										}
										p(a,0,false);
										break;}
								} catch (Exception e) { next = null; if (i == CSV_COLS) successZipcodes = false; }
								donations.append(next + (i==CSV_COLS?n:d));
								p(next + (i<CSV_COLS-1?a:""),0,i>=CSV_COLS-1);

							}
							if (currCash < CENT_DROPOFF) dcount--;
							else if (selfDonation) { persCash += currCash; selfDonation = false; }
							else if (next.equals(selfDistrict)) distCash += currCash;
							else cuckCash += currCash;
							dcount++;
							try { scanner.nextLine(); }
							catch (Exception e) { break; }
						}
						scanner.close();
					}
						
					double allCash = persCash + distCash + cuckCash;					
					p("MONEY: " + politician + a + "pers: " + persCash + " + dist: " + distCash + " + cuck: " + cuckCash + " = " + allCash,1,true);
					stats[STATS_MONEY][pol] = ""+allCash;
					stats[STATS_NUMDS][pol] = ""+dcount;
					stats[STATS_CUCKS][pol] = (dcount == 0 || allCash == 0 ? null : ""+(cuckCash / (allCash) * 100));
					if (QUICK_RUN && count >= QUICK_COUNT) break; 
				}
								
			}
			
		} catch (Exception e) { e(e,false); return; }
		
		successDonations = true;
		
	}
	
	
	static void saveDonations() {
		
		if (!SAVE_DONATIONS) return;
		if (!successDonations)
			if (ATTEMPT_SAVE_ON_FAILED_DATA) p(FAILED_DATA_SAVE,0,false);
			else return;
		
		try {
			pfile(DONATIONS_FILE, P_WRITE_ATTEMPT);
			FileWriter file = new FileWriter(DONATIONS_FILE);
			file.append(donations.toString());
			file.flush();
			file.close();
			pfile(DONATIONS_FILE, P_WRITE_SUCCESS);
		} catch (Exception e) { e(e,false); }
		
	}
	
	static void saveZipcodes() {
		
		if (!SAVE_ZIPCODES) return;
		if (!successZipcodes)
			if (ATTEMPT_SAVE_ON_FAILED_DATA) p(FAILED_DATA_SAVE,0,false);
			else return;;
		
		try {
			
			pfile(ZIPCODES_FILE, P_WRITE_ATTEMPT);
			FileWriter zips = new FileWriter(ZIPCODES_FILE);
			String dist = "";
			
			zip: for (int z = 0; z < US_ZIPCODE_MAX; z++) {
				switch (district[z]) {
					case 0:				continue zip;
					case NO_DISTRICT:	dist = ZIPCODE_NOT_FOUND;   break;
					case OUT_OF_STATE:	dist = ZIPCODE_NOT_WASH;	break;
					default:			dist = ""+district[z];		break;
				}
				zipcodes.append(z+d+dist+n);
			}
			zips.append(zipcodes.toString());
			zips.flush();
			zips.close();
			pfile(ZIPCODES_FILE, P_WRITE_SUCCESS);
			
		} catch (Exception e) { e(e,false); }
		
	}
	
	static void savePoliticians() {
		
		if (!SAVE_POLITICIANS) return;
		if (!successPoliticians)
			if (ATTEMPT_SAVE_ON_FAILED_DATA) p(FAILED_DATA_SAVE,0,false);
			else return;
		
		try {
			pfile(POLITICIANS_FILE, P_WRITE_ATTEMPT);
			FileWriter pols = new FileWriter(POLITICIANS_FILE);
			pols.append(POLITICIANS_HEADER);
			String pol = "";
			String att = "";
			for (int p = 0; p < index; p++) {
				if (stats[STATS_NAMES][p] == null || stats[STATS_NAMES][p].equals("null")) continue;
				att = ""; pol = "";
				for (int s = 0; s < STATS_LENGTH; s++) {
					try { att = stats[s][p] + d; }
					catch (Exception e) { att = null; }
					pol += att;
				}
				pols.append(pol.substring(0, pol.length()-1)+n);
			}
			pols.flush();
			pols.close();
			pfile(POLITICIANS_FILE, P_WRITE_SUCCESS);
		} catch (Exception e) { e(e,false); }
		
	}
	
	
	
	///////////////////////////////////
	///////////////// DONATIONS HELPERS
	///////////////////////////////////
	
	final static String DELIMITER_REGEX = d+'|'+n;
	
	static void populateZipcodes(File f, int district[]) {
		try {
			p(n+"Saved zipcodes found! Extracting zipcodes and districts...",0,true);
			Scanner scanner = new Scanner(f).useDelimiter(DELIMITER_REGEX);
			scanner.nextLine();
			int zip = 0;
			while (scanner.hasNext()) {
				try { zip = Integer.parseInt(scanner.next().trim()); }
				catch (Exception e) { scanner.next(); continue; }
				p("ZIPCODE/DISTRICT: " + zip + a,1,false);
				String dist = scanner.next().trim();
				p(dist + a,0,false);
				switch (dist) {
					case "0": break;
					case ZIPCODE_NOT_FOUND:	district[zip] = NO_DISTRICT;	break;
					case ZIPCODE_NOT_WASH:	district[zip] = OUT_OF_STATE;	break;
					default: try {			district[zip] = Integer.parseInt(dist); }
					   catch (Exception e) {district[zip] = 0;				break;  }
				}
				p(""+district[zip],0,true);
			}
			scanner.close();
			p(n+"Finished loading saved zipcodes.",0,true);
			
		} catch (Exception e) {	e(e,false); return; }
		
		successZipcodes = true;
		
	}
	
	
	static int populatePoliticians(File f, String stats[][]) {
		int pol = 0;
		try {
			p(n+"Saved politicians found! Extracting shits and giggles...",0,true);
			Scanner scanner = new Scanner(f).useDelimiter(DELIMITER_REGEX);
			scanner.nextLine();
			int stat = 0;
			while (scanner.hasNext()) {
				String s = "POLITICIAN: ";
				for (stat = 0; stat < STATS_LENGTH; stat++) {
					try { stats[stat][pol] = scanner.next().trim(); }
					catch (Exception e) { stats[stat][pol] = null; }
					s += stats[stat][pol] + a;
				}
				pol++;
				p(s + "[from csv]",1,true);
			}
			scanner.close();
			p(n+"Finished loading saved politicians.",0,true);
		} catch (Exception e) {	e(e,false); }		
		return pol;
	}

	
	final static String DISTRICT_FINDER = "http://app.leg.wa.gov/DistrictFinder/";
	final static String DISTRICT_FORM = "//form[@action='/DistrictFinder/']";
	final static int	DISTRICT_FORM_ID = 2;
	final static String DISTRICT_JUNK = ".";
	final static String DISTRICT_BUTTON = "Find My District";
	final static String DISTRICT_ELEMENT = "districtInfoDisplayContainer";
	final static String DISTRICT_AUGUR = "District Number:";
	final static int	DISTRICT_SPACE = 17;
	
	static int guessDistrict(int zip) {
		
		p(a+zip,0,false);
		if (zip < 1 || zip == NO_DISTRICT) return NO_DISTRICT;
		if (zip < WA_ZIPCODE_MIN || zip > WA_ZIPCODE_MAX) return OUT_OF_STATE;
		
		int district = NO_DISTRICT;
		int f = 0;
			
		try {
			if (dist_html == null) 
				dist_html = client.getPage(DISTRICT_FINDER);
			HtmlPage html = dist_html.cloneNode(true);
			HtmlForm form = html.getFirstByXPath(DISTRICT_FORM);
			for (HtmlInput field : form.getInputsByValue(""))
				field.setValueAttribute(f++==DISTRICT_FORM_ID?(""+zip):DISTRICT_JUNK);
			HtmlPage guess = form.getInputByValue(DISTRICT_BUTTON).click();
			HtmlDivision div = guess.getHtmlElementById(DISTRICT_ELEMENT);
			String result = div.asText();
			int distloc = result.indexOf(DISTRICT_AUGUR) + DISTRICT_SPACE;
			district = Integer.parseInt(result.substring(distloc,distloc+2).trim());
		} catch (Exception e) { district = NO_DISTRICT; }
		
		return district;
	}

	
	final static String UNKNOWN_ROLE = "unknown";
	final static String PERSON = "person";
	final static String POLITICIAN = "politician";
	final static String SELF_FUNDED = "self";
	final static String PERSONAL_FUNDS = "personal";
	final static String TITLES[] = { "mr", "mrs", "ms", "dr", "jr", "sr", "dds", "md", "dmd", "phd", "ii", "iii", "iv" };
	final static String COMMITTEES[] = { "committee", "pac", "district", "county", "league", "council", "association", "assoc", "assn", "political", "republican", "democrat" };
	final static String COMPANIES[] = { "company", "llc", "corporation", "corp", "co", "inc", "ltd", "ps", "llp", "od", "associate", "automotive", "vehicle", "auto", "law", "office", "clinic", "store", "service", "health", "healthcare", "dental", "farm", "care", "insurance", "mutual", "bank", "energy" };
	final static String ORGANIZATIONS[] = { "organization", "local", "tribe", "tribal", "community", "department", "dept", "citizen", "institute", "nw", "northwest", "group", "affair", "wa", "washington", "state", "usa", "america", "fund", "fundraiser", "union" };
	final static String ALL_GROUPS[][] = { COMMITTEES, COMPANIES, ORGANIZATIONS };
	final static String NAME_REGEX = "[a-z'-]+";
	final static int SHORT_NAME = 2;
	final static double SELF_THRESHOLD = 5.0;
	final static double MATCH_EXACT = 1.0;
	final static double MATCH_SHORT = 0.5;
	final static double MATCH_CHAR = 0.2;
	final static double MIN_MATCH = 2.0;
	final static double EASY_MATCH = 0.5;
	final static double HARD_MATCH = 0.67;
	
	static String guessRole(String donor, String self, String politicians[]) {

		String role = UNKNOWN_ROLE;
		boolean titleOrInitial = false;
		int attempt = 0;
		donor = donor.toLowerCase();
		self = self.toLowerCase();
		final String tokens[] = donor.split(" ");
		for (int t = 0; t < tokens.length; t++) {
			tokens[t] = tokens[t].substring(0, tokens[t].length() -
					  ((tokens[t].length() > SHORT_NAME && tokens[t].endsWith("s"))?1:0)).replace(".","").trim();
			if (tokens[t].length() <= SHORT_NAME) titleOrInitial = true;
		}

		guess: while (role.equals(UNKNOWN_ROLE))
			
			switch (attempt++) {
											
				case 0: polguess:
					for (String member : politicians) {
						if (member == null) break;
						String pol = member.toLowerCase();
						double match = 0;
						for (String token : tokens)
							if (token.length() == 0) continue;
							else if (token.length() <= SHORT_NAME &&
								pol.substring(pol.length()-token.length(), pol.length()).contains(token)) match += MATCH_SHORT;
							else if (token.length() > SHORT_NAME && token.length() < pol.length())
								if (token.equals(PERSONAL_FUNDS)) match += SELF_THRESHOLD;
								else if (pol.contains(token)) match += MATCH_EXACT;
								else if (pol.charAt(pol.length()-1) == token.charAt(0)) match += MATCH_CHAR;
						if (match >= MIN_MATCH && (match/(tokens.length - (titleOrInitial?1:0)) > (titleOrInitial ? EASY_MATCH : HARD_MATCH))) {
							role = ((self.equals(pol) || match >= SELF_THRESHOLD) ? SELF_FUNDED : POLITICIAN);
							break polguess;
						}
					}
					break;
					
				case 1: orgguess:
					for (String organizations[] : ALL_GROUPS)
						for (String organization : organizations)
							for (String token : tokens)
								if (token.replace(".", "").equals(organization)) {
									role = organizations[0];
									break orgguess;
								}
					break;
					
				case 2: nameguess:
					switch (tokens.length) {
						case 1:
							role = COMPANIES[0];
							break;
						case 2: case 3:
							for (String token : tokens)
								if (!token.matches(NAME_REGEX)) break nameguess;
							role = PERSON;
							break;
						default:
							for (String token : tokens)
								for (String title : TITLES)
									if (token.equals(title)) {
										role = PERSON;
										break nameguess;
									}
							break;
					}
					break;
					
				default: break guess;
			}
		
		return role;
	}	
}
