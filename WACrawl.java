/**
 * @author Mark Peters
 * it crawls and gets your donors and your keyword-bill associations
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
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WACrawl {
	
	static WebClient client;
	final static int SIZE = 1000;
	
	public static void main(String[] args) {
		makeClient();
		crawlDonations();
		crawlKeywords();
	}
	
	public static void makeClient() {
		client = new WebClient(BrowserVersion.CHROME);
		client.getOptions().setJavaScriptEnabled(true);
		client.getOptions().setRedirectEnabled(true);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.getOptions().setTimeout(20000);
		Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
	}
		
	public static void crawlKeywords() {

		final String page = "http://app.leg.wa.gov/dlr/topicalindex/Results.aspx?year=2016";
		final String nontopic = "(/*";
		final String nobill = "NOBILL";
		final String keywords[] = new String[SIZE];
		final String bills[][] = new String[SIZE][SIZE*10];
		int index = 0, i = 0, j = 0, k = 0;
		
		try {

			HtmlPage html = client.getPage(page);
			String webpage = Jsoup.parse(html.asXml()).getAllElements().get(1).toString();
			
			while (true) {
				do i = webpage.indexOf("<b>", i) + 4;
				while (nontopic.contains(""+webpage.charAt(i)));
				for (k = 0; j < i && j > 0; k++) {
					bills[index][k] = webpage.substring(j, webpage.indexOf('<', j)-1).replace(',', ';');
					j = webpage.indexOf("bill=", j) + 12;
				} bills[index++][k] = nobill;
				j = webpage.indexOf("bill=", i) + 12;
				if (i > 3) keywords[index] = webpage.substring(i, webpage.indexOf('<', i)-1).replace(',', ';');
				else break;
			}
			
			FileWriter keys = new FileWriter("keywords.txt");
			FileWriter keysandbills = new FileWriter("keysandbills.csv");
			keysandbills.append("KEYWORD,BILL\n");
			
			for (i = 0; i < index; i++) {
				keys.append(keywords[i] + '\n');
				for (k = 0;;k++)
					if (!bills[i][k].equals(nobill)) keysandbills.append(keywords[i] + ',' + bills[i][k] + '\n');
					else break;
			}
			
			keys.flush();
			keysandbills.flush();
			keys.close();
			keysandbills.close();
			
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public static void crawlDonations() {
		
		final String legislature = "http://web.pdc.wa.gov/MvcQuerySystem/Candidate/leg_candidates?page=";
		final String contributions = "2016 Detailed Contributions.csv";
		final String d = ",";
		final String names[] = new String[1000];
		final String codes[] = new String[1000];
		final StringBuilder donations = new StringBuilder("FROM,TO,Date,Amount\n");
		final boolean fetchcsvs = true;
		int index = 0, p = 0;
		
		if (fetchcsvs) try {

			while (p < 26) {
				
				HtmlPage html = client.getPage(legislature + p);
				boolean flag = false;
				
				for (Element e : Jsoup.parse(html.asXml()).getAllElements())
					if (flag) {
						String a = e.toString();
						names[index++] = a.substring(5, a.length()-6);
						flag = false;
					} else {
						String a = e.attr("href");
						int i = a.indexOf("param");
						if (i!=-1) { flag = true; codes[index] = (a.substring(i+6, i+20)); }
					}
			
				p++;
			}

			for (int i = 0; i < index; i++)
				FileUtils.copyURLToFile(new URL("http://web.pdc.wa.gov/MvcQuerySystem/CandidateData/excel?param=" +
												codes[i] + "====&year=2016&tab=contributions&type=legislative&page=1&orderBy=&groupBy=&filterBy="),
										new File(names[i] + contributions));
			
		} catch (IOException e) { e.printStackTrace(); }

		try {
			
			for (File f : new File(".").listFiles()) {
			
				int nameend = f.getName().indexOf(contributions);
				
				if (nameend!=-1) {
					Scanner scanner = new Scanner(f).useDelimiter(d);
					String politician = f.getName().substring(0, nameend);
					
					for (int i = 0; i < 5; i++) scanner.nextLine();
					while (scanner.hasNext()) {
						for (int i = 0; i < 4; i++)
							donations.append((i==1? politician : scanner.next()) + (i==3?"\n":d));
						scanner.nextLine();
					}
					
					scanner.close();
				}
			}
			
			FileWriter file = new FileWriter("donations.csv");
			file.append(donations.toString());
			file.flush();
			file.close();
			
		} catch (IOException e) { e.printStackTrace(); }			
				
	}	
}
