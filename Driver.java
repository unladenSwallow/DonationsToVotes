
import java.util.ArrayList;
import java.util.List;


/*
 * @Author Patrick Sanchez
 * 
 * TCSS 445 Databases
 * 
 * A driver file used to enter database information using jdbc.
 * 
 * This makes use of the CSVUtils provided by Robert, and the 
 * dbInsert class which I wrote to connect to the server and 
 * enter the data.  CSV parsing is handled by Roberts code, which
 * produces a List of String Lists.  dbInsert takes that list, 
 * and connection information to insert the values into the 
 * corresponding tables within our database.  Runtime statistics
 * are provided after completion of each operation. 
 * 
 * source CSV file required:
 * 
 * zipcodes
 * keysandbills
 * billsbysponsorandstatus
 * donations
 * politcians
 * newlocations
 *    
 * --------------------------------------------------------------
 */
public class Driver {
    
    //user name and password connect to database
    private static String USER_NAME = "root";
    private static String PASSWORD = "IdaPo7A70";
    
    //CSV file name and location for input files
    private static String DATA_FILE = "";
    
    
    
    /**
     * main method
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
        //Start a timer 
        long startTime = System.currentTimeMillis();
        
        //create a new dbInsert object to perform our operations
        dbInsert db = new dbInsert(USER_NAME, PASSWORD,"jdbc:mysql://localhost:3306/dotes2votes", 1000);
  
        //Each module is separated here for isolation testing
        DATA_FILE = "/Users/never/Desktop/zipcodes.csv";
        ArrayList<List<String>> zips = (ArrayList<List<String>>) CSVUtils.parse(DATA_FILE);
        db.updateZipcodes(zips);
        
        
        DATA_FILE = "/Users/never/Desktop/newlocation.csv";
        ArrayList<List<String>> locations = (ArrayList<List<String>>) CSVUtils.parse(DATA_FILE);
        db.updateLocations(locations);
        
        DATA_FILE = "/Users/never/Desktop/keysandbills.csv";
        ArrayList<List<String>> keys = (ArrayList<List<String>>) CSVUtils.parse(DATA_FILE);
        db.updateKeywords(keys);
        
        DATA_FILE = "/Users/never/Desktop/donations.csv";
        ArrayList<List<String>> donors = (ArrayList<List<String>>) CSVUtils.parse(DATA_FILE);
        db.updateDonors(donors);
      
        DATA_FILE = "/Users/never/Desktop/billsbysponsorandstatus.csv";
        ArrayList<List<String>> bills = (ArrayList<List<String>>) CSVUtils.parse(DATA_FILE);
        db.updateBills(bills);
     
        DATA_FILE = "/Users/never/Desktop/politicians.csv";
        ArrayList<List<String>> politicians = (ArrayList<List<String>>) CSVUtils.parse(DATA_FILE);
        db.updatePoliticians(politicians);
 
        
//        //WARNING//!!DONATIONS TAKES 10 MINUTES TO UPDATE FULLY!!//WARNING        
        DATA_FILE = "/Users/never/Desktop/donations.csv";
        ArrayList<List<String>> donations = (ArrayList<List<String>>) CSVUtils.parse(DATA_FILE);
        db.updateDonations(donations);

        DATA_FILE = "/Users/never/Desktop/votes.csv";
        ArrayList<List<String>> votes = (ArrayList<List<String>>) CSVUtils.parse(DATA_FILE);
        db.updateVotes(votes);
        
        //close our files and finish any remaining batches
        db.dbClose();
        
        //Display time stats
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total Runtime: " + totalTime + "ms\n");
        
        System.out.println("\nProgram Complete");

    }
    
}