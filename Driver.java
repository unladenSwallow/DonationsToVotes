package Robert;

public class Driver {
	
	//user name and password connect to database
	private static String USER_NAME = "root";
	private static String PASSWORD = "tcss445@dmin";
	
	//CVS file name
	private static String DATA_FILE = "/Users/robertkariuki/Desktop/bills_Barkis.csv";
	
	/**
	 * main method
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		dbInsert.insertData(USER_NAME, PASSWORD, CSVUtils.parse(DATA_FILE));
	}
	
}
