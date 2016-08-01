package Robert;

import java.sql.*;
import java.util.List;
/*
 * Patrick Sanchez
 * TCSS 445A
 * A collection of functions and sample commands ot be included in 
 * the Donations2Votes project
 */
public class dbInsert {

    /*
     * Connects to the local database and surrounds actions in a try
     * catch block
     * --------------------------------------------------------------
     */
    public static void insertData(String username, String password, List<List<String>> data)
            throws SQLException {
        // create this outside of our individual batches
        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/dotes2votes", username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // temporarily disables foreign key checks when building the database
        Statement toggle = con.createStatement();
        toggle.execute("SET FOREIGN_KEY_CHECKS = 0;");

        Statement stmt = con.createStatement();     
        
        //delete database before inserting
        stmt.addBatch("delete from roles");
        stmt.addBatch("delete from entities");
        stmt.addBatch("delete from bills");
        stmt.addBatch("delete from keywords");
        stmt.addBatch("delete from votes");
        stmt.addBatch("delete from donations");
        
        //Example function calls, each one makes a single insert into the table
        //--------------------------------------------------------------

//          updateBills(stmt,"HB456","TESTBILL456","Another test of the data entry method");
//          updateDonations(stmt,"2","2","19771008","10");
//          updateEntities(stmt,"3","1","SimpleNPO","Independent");
//          updateKeywords(stmt,"SB4321","testkey");
//          updateRoles(stmt,"1","elected official","Senator","Senate","1","THISGUY");
//          updateVotes(stmt,"1","TESTBILL","yea","1");
        
        for (List<String> line: data){
        	
          updateBills(stmt,line.get(0),"-",line.get(2));
          //updateDonations(stmt,"2","2","19771008","10");
          //updateEntities(stmt,"3","1","SimpleNPO","Independent");
          //updateKeywords(stmt,"SB4321","testkey");
          //updateRoles(stmt,"1","elected official","Senator","Senate","1","THISGUY");
          //updateVotes(stmt,"1","TESTBILL","yea","1");
        }
        
        //run our batch of collected inserts
        stmt.executeBatch();
        
        //re-enable foreign key checks
        toggle.execute("SET FOREIGN_KEY_CHECKS = 1;");
        //close resources
        
        stmt.close();
        con.close();

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
    private static void updateBills(Statement st, String bill,
            String name, String summary)
                    throws SQLException {
        // set size equal to number of expected characters to minimize
        // reallocation
        StringBuilder sb = new StringBuilder(200);
        sb.append("insert into bills (BILL_NAME,FullName,Topic) values(");
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

        st.addBatch(sb.toString());

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
    private static void updateDonations(Statement st, String from,
            String to, String date, String decimal)
                    throws SQLException {
        // set size equal to number of expected characters to minimize
        // reallocation
        StringBuilder sb = new StringBuilder(100);
        sb.append("insert into donations (FROM_ID,TO_ID,DonationDate,Amount) values(");
        sb.append(from);
        sb.append(",");
        sb.append(to);
        sb.append(",");
        sb.append(date);
        sb.append(",");
        sb.append(decimal);
        sb.append(")");
        String result = sb.toString();
        //included for test confirmation

        System.out.println(result);

        st.addBatch(sb.toString());

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
    private static void updateEntities(Statement st, String entid,
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

        st.addBatch(sb.toString());

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
    private static void updateKeywords(Statement st, String bill,
            String key)
                    throws SQLException {
        // set size equal to number of expected characters to minimize
        // reallocation
        StringBuilder sb = new StringBuilder(200);
        sb.append("insert into keywords (BILL_NAME,Keyword) values(");
        sb.append("'");
        sb.append(bill);
        sb.append("','");
        sb.append(key);
        sb.append("')");
        //included for test confirmation

        String result = sb.toString();
        System.out.println(result);

        st.addBatch(sb.toString());

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
    private static void updateRoles(Statement st, String rolid,
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

        st.addBatch(sb.toString());

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
    private static void updateVotes(Statement st, String polid,
            String billname, String vote, String sponsor)
                    throws SQLException {
        // set size equal to number of expected characters to minimize
        // reallocation
        StringBuilder sb = new StringBuilder(100);
//        sb.append(
//                "insert into votes (POL_ID,BILL_NAME,Vote,Sponsor) "
//                + "values("+polid+",'"+billname+"','"+vote+"','"+sponsor+"')");
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

        st.addBatch(sb.toString());

    }

}
