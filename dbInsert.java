import java.sql.*;
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
    
    
//    public static void main(String[] args) throws SQLException {
//
//        dbInsert db = new dbInsert("root", "IdaPo7A70","jdbc:mysql://localhost:3306/dotes2votes", 7);
//        
//        
//        db.updateBills("HB456","TESTBILL456","Another test of the data entry method");
//        db.updateDonations("2","2","19771008","10");
//        db.updateDonations("2","3","19771008","12");
//        db.updateEntities("3","1","SimpleNPO","Independent");
//        db.updateKeywords("SB4321","testkey","redirect");
//        db.updateRoles("1","elected official","Senator","Senate","1","THISGUY");
//        db.updateVotes("1","TESTBILL","yea","1");
//        db.dbClose();
//    }
    
    

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
            //establish connection without using SSL
            con = DriverManager.getConnection(
                    addr + "?autoReconnect=true&useSSL=false", username,
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
     * A small fucntion which closes out internal connections 
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
    
    
    /*
     * A small helper function which increments our count, checks it 
     * against batchsize, and commits the batch if it meets the 
     * threshold.
     * 
     * 
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
    
    //--------------------------------------------------------------
    //FUNCTIONS
    
    
    /* 
     * A Function which populates one entry of the keywords Table
     * @pre A roles table exists within our database 
     * 
     * @param stmt Our prepared statement to hold the batch of inserts
     * 
     * @param bill The string representation of the bill name
     * 
     * @param key Te string repr of an internal keyword
     * 
     * @param redir String representation of our internal redirect
     * 
     * @return No Return
     * 
     * @post The input is inserted into the table
     * --------------------------------------------------------------
     * 
     */
    public void updateBills(String bill,
            String name, String summary)
                    throws SQLException {
        // set size equal to number of expected characters to minimize
        // reallocation
        StringBuilder sb = new StringBuilder(200);
        sb.append("insert into bills (BILL_NAME,FullName,Summary) values(");
        sb.append("'");
        sb.append(bill);
        sb.append("','");
        sb.append(name);
        sb.append("','");
        sb.append(summary);
        sb.append("')");
        //included for test confirmation

        String result = sb.toString();
        System.out.println(result);

        this.stmt.addBatch(sb.toString());
        counter();

    }
    
    
    
    /* 
     * A Function which populates one entry of the donations Table
     * @pre A donations table exists within our database 
     * 
     * @param st Statement passed from our main handling method, this contains
     * all of the batch commands
     * 
     * @param from The string representation of an INT identifier for donator
     * 
     * @param to The string repr of an INT identifier of the donee
     * 
     * @param date String in YYYYMMDD format
     * 
     * @param decimal String decimal value of the donation
     * 
     * @return No Return
     * 
     * @post The input is inserted into the table
     * --------------------------------------------------------------
     * 
     */
    public void updateDonations(String from,
            String to, String date, String decimal)
                    throws SQLException {
        // set size equal to number of expected characters to minimize
        // reallocation
        StringBuilder sb = new StringBuilder(100);
        sb.append(
                "insert into donations (FROM_ID,TO_ID,DonationDate,Amount) values(");
        sb.append("'");
        sb.append(from);
        sb.append("','");
        sb.append(to);
        sb.append("',");
        sb.append(date);
        sb.append(",");
        sb.append(decimal);
        sb.append(")");
        String result = sb.toString();
        //included for test confirmation

        //System.out.println(result.substring(58, result.length()));

        stmt.addBatch(sb.toString());
        //counter();


    }
    
    
    
    /* 
     * A Function which populates one entry of the keywords Table
     * @pre A roles table exists within our database 
     * 
     * @param stmt Our prepared statement to hold the batch of inserts
     * 
     * @param entid The string representation of ID int
     * 
     * @param rolid The string repr of an ID int for roles
     * 
     * @param name String representation of our entitiy name
     * 
     * @param party String representation of party name(see db for list)
     * 
     * @return No Return
     * 
     * @post The input is inserted into the table
     * --------------------------------------------------------------
     * 
     */
    public void updateEntities(String entid,
            String rolid, String name,String party)
                    throws SQLException {
        // set size equal to number of expected characters to minimize
        // reallocation
        StringBuilder sb = new StringBuilder(200);
        sb.append("insert into entities (ENT_ID,ROLE_ID,EntityName,Party) values(");
        sb.append(entid);
        sb.append(",");
        sb.append(rolid);
        sb.append(",'");
        sb.append(name);
        sb.append("','");
        sb.append(party);
        sb.append("')");
        //included for test confirmation

        String result = sb.toString();
        System.out.println(result);

        stmt.addBatch(sb.toString());
        counter();

    }
    
    
    
    
    
    /* 
     * A Function which populates one entry of the keywords Table
     * @pre A roles table exists within our database 
     * 
     * @param stmt Our prepared statement to hold the batch of inserts
     * 
     * @param bill The string representation of the bill name
     * 
     * @param key Te string repr of an internal keyword
     * 
     * @param redir String representation of our internal redirect
     * 
     * @return No Return
     * 
     * @post The input is inserted into the table
     * --------------------------------------------------------------
     * 
     */
    public void updateKeywords(String bill,
            String key, String redir)
                    throws SQLException {
        // set size equal to number of expected characters to minimize
        // reallocation
        StringBuilder sb = new StringBuilder(200);
        sb.append("insert into keywords (BILL_NAME,Keyword,Redirect) values(");
        sb.append("'");
        sb.append(bill);
        sb.append("','");
        sb.append(key);
        sb.append("','");
        sb.append(redir);
        sb.append("')");
        //included for test confirmation

        String result = sb.toString();
        System.out.println(result);
        //////////Duplicate Code
        
        //check duplicate
//        boolean dup = false;
//        for (int j = i + 1; j < bills.size(); j++){
//            List<String> otherLine = bills.get(j);
//            String from2 = otherLine.get(0);
//            if (from2.length() > 32){
//                from2 = from2.substring(0, 32);
//            }
//            
//            if (from.equals(from2) && otherLine.get(2).equals(otherLine.get(2))){
//                dup = true;
//                break;
//            }
//        }
//        if (!dup){
//            updateEntities(stmt, ID, null, from, null);
//            
//            ID++; //next insert
//            
//            updateDonations(stmt, from, to, line.get(1), line.get(2));
//        }
        
        stmt.addBatch(sb.toString());
        counter();

    }
    
    /* 
     * A Function which populates one entry of the roles Table
     * @pre A roles table exists within our database 
     * 
     * @param from The string representation of an INT identifier for donator
     * 
     * @param to Te string repr of an INT identifier of the donee
     * 
     * @param date String in YYYYMMDD format
     * 
     * @param decimal String decimal value of the donation
     * 
     * @return No Return
     * 
     * @post The input is inserted into the table
     * --------------------------------------------------------------
     * 
     */
    public void updateRoles(String rolid,
            String type, String title, String chmbr,String dist,String descr)
                    throws SQLException {
        // set size equal to number of expected characters to minimize
        // reallocation
        StringBuilder sb = new StringBuilder(200);
        sb.append("insert into roles (ROLE_ID,Roletype,RoleTitle,Chamber,District,Description) values(");
        
        sb.append(rolid);
        sb.append(",'");
        sb.append(type);
        sb.append("','");
        sb.append(title);
        sb.append("','");
        sb.append(chmbr);
        sb.append("',");
        sb.append(dist);
        sb.append(",'");
        sb.append(descr);
        sb.append("')");
        //included for test confirmation

        String result = sb.toString();
        System.out.println(result);

        stmt.addBatch(sb.toString());
        counter();


    }
    
    
    
    /* 
     * A Function which populates one entry of the votes Table
     * 
     * @pre A votes table exists within our database 
     * 
     * @param polid The string representation of an INT identifier for polid
     * 
     * @param billname The string repr of the bill name
     * 
     * @param vote String repr of the enum for voting result
     * 
     * @param sponsor String repr of the sponsor boolean
     * 
     * @return No Return
     * 
     * @post The input is inserted into the table
     * --------------------------------------------------------------
     * 
     */
    public void updateVotes(String polid,
            String billname, String vote, String sponsor)
                    throws SQLException {
        // set size equal to number of expected characters to minimize
        // reallocation
        StringBuilder sb = new StringBuilder(100);

        sb.append("insert into votes (POL_ID,BILL_NAME,Vote,SPonsor) values (");
        sb.append(polid);
        sb.append(",'");
        sb.append(billname);
        sb.append("','");
        sb.append(vote);
        sb.append("','");
        sb.append(sponsor);
        sb.append("')");
        String result = sb.toString();
        //included for test confirmation
        System.out.println(result);

        stmt.addBatch(sb.toString());
        counter();


    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    



}