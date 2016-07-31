/**
 * AlphabetizeTopic.java
 * Sally Budack
 * 2016
 */
package topic;

/**
 * @author Sally Budack
 * 2016
 */


import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 
 * @author Sally Budack
 * 2016
 */
public class AlphabetizeTopic {
	/**starting letter of topic */
	char c;
	
	/**
		The default file name as shown in the assignment description.
	 */
	private static final String DEFAULT_INPUT_FILENAME = "topic_test2.txt";
	/**string length */
	private int length;
	/**string array */
	private String[] abc;
	
	/**
	 * 
	 * constructor
	 * AlphabetizeTopic
	 * @param c
	 * @param abc
	 * @param length
	 */
	public AlphabetizeTopic(char c, String[] abc, int length){
		this.c = c;
		this.abc = abc;
		this.length = length;
		new AlphabetizeTopic();
	}
	
	/**
	 * 
	 * constructor
	 * AlphabetizeTopic
	 */
	public AlphabetizeTopic(){	
		start();
	}
	
	/**
	 * 
		Runs the various functions of the program.
	 */ 
	private void start() {
		try {
			Scanner input = new Scanner(new File(DEFAULT_INPUT_FILENAME));
			// Read the input file and populate the List
			setAbc(input); 
		} catch (FileNotFoundException e) {
			System.out.println("File not found. Please try again.");
		}	
	}//end start()
	
	
	/**
		Read the input file and populate the List
		@param input a Scanner over the input file
		@throws NullPointerException if (input == null)
	 */
	private void setAbc(Scanner input) {
		// data storage
		List<String> temp = new ArrayList<String>();
		String line = "";	
		while (input.hasNextLine()) {
			//read a line from the file  
			line = input.nextLine().trim();
			if(line.length() > 2){
				// match the first character
				char t = line.charAt(0);
				// check for number
				if(Character.isDigit(t) && c == '#'){
					temp.add(line);
				}
				if(Character.isLetter(t)&& c == t){
					temp.add(line);
				}	
			}			
		}// end while loop
		abc = temp.toArray(new String[temp.size()]);
		setLength(temp.size());
		setAbc(abc);
		// clear list for reuse
		temp.clear();
	}// end readFile
	
	/**
	 * setLength
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}
	
	/**
	 * getLength
	 * @return the length
	 */
	public int getLength() {
		return this.length;
	}
	
	/**
	 * getAbc
	 * @return the abc
	 */
	public String[] getAbc() {
		start();
		return this.abc;
	}
	
	/**
	 * setAbc
	 * @param abc the abc to set
	 */
	public void setAbc(String[] abc) {
		this.abc = abc;
	}	
	
} // end class 