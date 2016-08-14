import java.sql.*;
import java.util.List;
/*
 * Patrick Sanchez
 * TCSS 445A
 * A simple tool to insert values into a remote SQL server 
 * connection.  This is for use in the Donations to Votes Project
 * Summer Quarter 2016
 * 
 * Prepared Statements and small private batch sizes are used to 
 * combat SQL injection attacks
 * --------------------------------------------------------------
 * 
 */
public class dbInsert {
    
    //size of entries to be entered in our statement before a batch is 
    //executed
    private int batch = 0;
    
    //internal count for our batch, if count ever >= batch, then execute
    private int count = 0;
    
    //prepared statement to hold our batches for execution
    private PreparedStatement stmt;
    
    private Statement toggle;
    
    private Connection con = null;
    
    /*
     * Constructor for the class
     * @pre A remote database exits for our connection
     * @param username String representing the database username
     * @param password String representing the database password
     * @param addr A String representing the server address
     * @param batchsize An int representing the number of entries
     * example address: jdbc:mysql://localhost:3306/dotes2votes
     * @post - a dbInsert object is created, with a connection 
     * established, or an error is created if no connection
     * --------------------------------------------------------------
     */
    dbInsert(String username, String password, String addr, int batchsize) throws SQLException {

        this.batch = batchsize;
        
        //Connection con = null;
        try {
            //establish connection without using SSL and using a faster batching routine
            con = DriverManager.getConnection(
                    addr + "?autoReconnect=true&useSSL=false&rewriteBatchedStatements=true", username,
                    password);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // temporarily disables foreign key checks when building the database
        toggle = con.createStatement();
        toggle.execute("SET FOREIGN_KEY_CHECKS = 0;");
        
        //set our prepared statement 
        stmt = con.prepareStatement("");
        
    }
   
    /*
     * A small helper function to parse incoming dates into
     * proper sql format
     */
    public String parseDate(String s) {
         String input = s;      
      //output in YYYY-MM-DD format
        //mm-dd-yyyy
        String date;
        if(input == null) {
            date = "1999-12-31";
            //System.out.println("!NULL!");
            return date;
        }
        else{
            //System.out.println(input);
            String[]out = input.split("/",3);
            if(out[0].length() == 1) {
                out[0] = "0" + out[0];
            }
            if(out[1].length() == 1) {
                out[1] = "0" + out[1];
            }
        date = out[2] + out[0] + out[1];
        }
        return date;
    }

    /*
     * A small helper function which increments our count, checks it 
     * against batchsize, and commits the batch if it meets the 
     * threshold.
     *   
     * --------------------------------------------------------------
     */
    private void counter() throws SQLException {
        count++;
        if(count >= batch) {
            count = 0;
            stmt.executeBatch();
        }
    }

    /*
     * A small function which closes out internal connections 
     * and finishes any remaining small batches
     * 
     * @pre A dbInsert objetc has been created
     * @return No return
     * @post Database connections are severed, foreign key checks are
     * restored, and the remaining batch is executed
     * --------------------------------------------------------------
     */
    public void dbClose() throws SQLException {
        //run our batch of collected inserts
        stmt.executeBatch();
        
        //re-enable foreign key checks
        toggle.execute("SET FOREIGN_KEY_CHECKS = 1;");
        //close resources
        
        stmt.close();
        con.close();
        
    }
    
    
    
    


                    //IMPROVED FUNCTIONS//
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    /*
     * A loop is performed over the input string to glean values for 
     * a preparedStatement, each line is added to the batch with the
     * correct format, a check is performed to see if the internal 
     * class batch size has been hit, if so, the batch is executed to
     * the server 
     * 
     *  
     * @pre our database exists with empty zipcode data
     * @param csv output list of Lists of strings from our input
     * @post The zipcodes table is updated with the proper values
     * time statistics are displayed
     * @return No return
     * --------------------------------------------------------------
     */
    public void updateZipcodes(List<List<String>> csv) throws SQLException {
        
        System.out.println("Number of Zipcode Entries " + csv.size());
        long startTime = System.currentTimeMillis();
        
        String insert = "insert ignore into zipcodes (ZIPCODE, DISTRICT) values(?,?)";
        
        stmt = con.prepareStatement(insert);
        
        for( int i = 0; i < csv.size(); i++) {
            String zip = csv.get(i).get(0);
            String dist = csv.get(i).get(1);
            
            if(dist.compareTo("not in wa") == 0){
                dist = "50";
            }
            if(dist.compareTo("unknown") == 0) {
                dist = "0";
            }
            
            stmt.setString(1, zip);
            stmt.setString(2, dist);
            stmt.addBatch();
            
            counter();
        }
        stmt.executeBatch();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time Elapsed: " + totalTime + "ms\n");
    }
    
    /*
     * A loop is performed over the input string to glean values for 
     * a preparedStatement, each line is added to the batch with the
     * correct format, a check is performed to see if the internal 
     * class batch size has been hit, if so, the batch is executed to
     * the server 
     * 
     *  
     * @pre our database exists with empty table keywords
     * @param csv output list of Lists of strings from our input
     * @post The keywords table is updated with the proper values
     * time statistics are displayed
     * @return No return
     * --------------------------------------------------------------
     */
    public void updateKeywords(List<List<String>> csv) throws SQLException {
        
        System.out.println("\nNumber of  Keyword Entries " + csv.size());
        long startTime = System.currentTimeMillis();
        
        String insert = "insert ignore into keywords (BILL_NAME, KEYWORD) values(?,?)";
        
        stmt = con.prepareStatement(insert);
        
        for( int i = 0; i < csv.size(); i++) {
            
            String bill = csv.get(i).get(0);
            String key = csv.get(i).get(1);
            
            stmt.setString(1, bill);
            stmt.setString(2, key);
            stmt.addBatch();
            
            counter();
            
        }
        stmt.executeBatch();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time Elapsed: " + totalTime + "ms\n");
    }

    /*
     * A function which updates the bills table in our db using
     * prepared statements and provides runtime statistics
     * A loop is performed over the input string to glean values for 
     * a preparedStatement, each line is added to the batch with the
     * correct format, a check is performed to see if the internal 
     * class batch size has been hit, if so, the batch is executed to
     * the server 
     * 
     * 
     * 
     * @pre The bills table exists within our dotes2votes db
     * @param csv A list of Sting lists of parsed data from csv files
     * @return No return
     * @post The bills table is updated with our text entries
     * 
     * CURRENTLY TAKES 1.5 MINUTES TO UPDATE 2300 ENTRIES
     * 
     * --------------------------------------------------------------
     */
    public void updateBills(List<List<String>> csv) throws SQLException {
        
        System.out.println("Number of  Bill Entries " + csv.size());
        long startTime = System.currentTimeMillis();
        
        String insert = "insert ignore into bills (BILL_NAME, FullName, Summary) values(?,?,?)";
        
        stmt = con.prepareStatement(insert);
        
        for( int i = 0; i < csv.size(); i++) {
            
            String bill = csv.get(i).get(0);
            String name = csv.get(i).get(2);
            String sum = csv.get(i).get(2);
            
            stmt.setString(1, bill);
            stmt.setString(2, name);
            stmt.setString(3, sum);

            stmt.addBatch();
            counter();
            
        }
        stmt.executeBatch();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time Elapsed: " + totalTime + "ms\n");
        
        
    }
    
    /*
     * A function which updates the bills table in our db using
     * prepared statements and provides runtime statistics
     * 
     * A loop is performed over the input string to glean values for 
     * a preparedStatement, each line is added to the batch with the
     * correct format, a check is performed to see if the internal 
     * class batch size has been hit, if so, the batch is executed to
     * the server 
     * 
     *\\TAKES ROUGHLY 5 MMINUTES TO COMPLETE
     * 
     * @pre The donors table exists within our dotes2votes db
     * @param csv A list of Sting lists of parsed data from csv files
     * @return No return
     * @post The donors table is updated with our text entries
     * 
     * --------------------------------------------------------------
     */
    public void updateDonors(List<List<String>> csv) throws SQLException {
        
        System.out.println("Number of Donor Entries " + csv.size());
        long startTime = System.currentTimeMillis();
        
        String insert = "insert ignore into donors (DISTRICT,DonorName, Role) values(?,?,?)";
        
        stmt = con.prepareStatement(insert);
        
        for( int i = 0; i < csv.size(); i++) {
            
            String dist = csv.get(i).get(5);
            String name = csv.get(i).get(0);
            String role = csv.get(i).get(4);
            
            stmt.setString(1, dist);
            stmt.setString(2, name);
            stmt.setString(3, role);

            stmt.addBatch();
            counter();
            
        }
        
        stmt.executeBatch();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time Elapsed: " + totalTime + "ms\n");
        
        
    }
    
    /*
     * A function which updates the donations table in our db using
     * prepared statements and provides runtime statistics
     * 
     * A loop is performed over the input string to glean values for 
     * a preparedStatement, each line is added to the batch with the
     * correct format, a check is performed to see if the internal 
     * class batch size has been hit, if so, the batch is executed to
     * the server 
     * 
     * @pre The donations table exists within our dotes2votes db
     * @param csv A list of Sting lists of parsed data from csv files
     * @return No return
     * @post The donations table is updated with our text entries
     * 
     * --------------------------------------------------------------
     */
    public void updateDonations(List<List<String>> csv) throws SQLException {
        System.out.println("Number of Donation Entries " + csv.size());
        long startTime = System.currentTimeMillis();
        String insert = "insert into donations (DONOR_ID,POL_ID,DonationDate,Amount,SelfFunding) SELECT DONOR_ID,POL_ID,?,?,? FROM donors,politicians WHERE DonorName LIKE ? AND PoliticianName LIKE ?";
                
        stmt = con.prepareStatement(insert);
        
        for( int i = 0; i < csv.size(); i++) {
            
            String date = parseDate(csv.get(i).get(2));
            
            String donor = csv.get(i).get(0);
            String pol = csv.get(i).get(1);
            String decimal = csv.get(i).get(3);

            stmt.setString(1, date);
            stmt.setString(2, decimal);
            if(csv.get(i).get(4).compareTo("self") == 0){
                stmt.setString(3, "1");
            }
            else {
                stmt.setString(3, "0");
            }
            
            stmt.setString(4, donor);
            stmt.setString(5, pol);
            stmt.addBatch();
            //counter();
            
        }
        stmt.executeBatch();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time Elapsed: " + totalTime + "ms\n");
    }
    
    /*
     * A function which updates the votes table in our db using
     * prepared statements and provides runtime statistics
     * 
     * A loop is performed over the input string to glean values for 
     * a preparedStatement, each line is added to the batch with the
     * correct format, a check is performed to see if the internal 
     * class batch size has been hit, if so, the batch is executed to
     * the server 
     *  
     * @pre The votes table exists within our dotes2votes db
     * @param csv A list of Sting lists of parsed data from csv files
     * @return No return
     * @post The votes table is updated with our text entries
     * 
     * --------------------------------------------------------------
     */
    public void updateVotes(List<List<String>> csv) throws SQLException {
        System.out.println("Number of Vote Entries " + csv.size());
        
        long startTime = System.currentTimeMillis();
        String insert = "insert into votes (POL_ID,BILL_NAME,Vote,Sponsor) SELECT IFNULL(9999,POL_ID),BILL_NAME,?,? FROM politicians,bills WHERE PoliticianName LIKE ? AND BILL_NAME LIKE ?";
        
        stmt = con.prepareStatement(insert);
        
        for( int i = 0; i < insert.length() -1; i++) {
            
            String bill = csv.get(i).get(0);
            String pol = csv.get(i).get(1);
            String vote = csv.get(i).get(2);
            //System.out.println(bill);
            //System.out.println(pol);
            stmt.setString(1,  vote);
            stmt.setString(2, "0");
            stmt.setString(3, pol + "%");
            stmt.setString(4, "%" + bill);

            stmt.addBatch();
            
            counter();
        }
        
        stmt.executeBatch();        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time Elapsed: " + totalTime + "ms\n");
        
    }
    
 
    /*
     * A function which updates the politicians table in our db using
     * prepared statements and provides runtime statistics
     * 
     * A loop is performed over the input string to glean values for 
     * a preparedStatement, each line is added to the batch with the
     * correct format, a check is performed to see if the internal 
     * class batch size has been hit, if so, the batch is executed to
     * the server 
     *  
     * @pre The politicians table exists within our dotes2votes db
     * @param csv A list of Sting lists of parsed data from csv files
     * @return No return
     * @post The politicians table is updated with our text entries
     * 
     * --------------------------------------------------------------
     */
    public void updatePoliticians(List<List<String>> csv) throws SQLException {
        System.out.println("Number of Politician Entries " + csv.size());
        long startTime = System.currentTimeMillis();

        String insert = "insert ignore into politicians (DISTRICT, PoliticianName, Chamber, Party, Link, Cuckold) values(?, ?, ?, ?, ?,?)";
        
        stmt = con.prepareStatement(insert);
        
        for( int i = 0; i < csv.size(); i++) {

            String dist = csv.get(i).get(2);
            String name = csv.get(i).get(1);
            String cham = csv.get(i).get(3);
            String party = csv.get(i).get(5);
            String link = csv.get(i).get(0);
            String cuck = csv.get(i).get(9);
            
            
            stmt.setString(1, dist);
            stmt.setString(2, name);
            stmt.setString(3, cham);
            stmt.setString(4, party);
            stmt.setString(5, link);
            stmt.setString(6, cuck);
                        
            stmt.addBatch();
            
            counter();
        }
        stmt.executeBatch();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time Elapsed: " + totalTime + "ms\n");
    }
    
    
    /*
     * A function which updates the locations table in our db using
     * prepared statements and provides runtime statistics
     * 
     * 
     * A loop is performed over the input string to glean values for 
     * a preparedStatement, each line is added to the batch with the
     * correct format, a check is performed to see if the internal 
     * class batch size has been hit, if so, the batch is executed to
     * the server 
     *  
     * @pre The locations table exists within our dotes2votes db
     * @param csv A list of Sting lists of parsed data from csv files
     * @return No return
     * @post The locations table is updated with our text entries
     * 
     * --------------------------------------------------------------
     */
    public void updateLocations(List<List<String>> csv) throws SQLException {
        System.out.println("Number of Location Entries " + csv.size());
        long startTime = System.currentTimeMillis();
        String insert = "insert ignore into locations (DISTRICT, Link, Map) values(?, ?, ?)";
        
        stmt = con.prepareStatement(insert);
        
        for( int i = 0; i < csv.size(); i++) {

            String dist = csv.get(i).get(0);
            String link = csv.get(i).get(2);
            String map = csv.get(i).get(3);

            
            stmt.setString(1, dist);
            stmt.setString(2, link);
            stmt.setString(3, map);
            
            stmt.addBatch();
            
            counter();
        }
        stmt.executeBatch();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time Elapsed: " + totalTime + "ms\n");
        
    }
    
    
    
    
}//EOF- dbInsert