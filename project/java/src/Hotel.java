/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Hotel {

   static private String hotel_userID;

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Hotel 
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Hotel(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Hotel

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement ();

      ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   public int getNewUserID(String sql) throws SQLException {
      Statement stmt = this._connection.createStatement ();
      ResultSet rs = stmt.executeQuery (sql);
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }
   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Hotel.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Hotel esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Hotel object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Hotel (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Hotels within 30 units");
                System.out.println("2. View Rooms");
                System.out.println("3. Book a Room");
                System.out.println("4. View recent booking history");

                //the following functionalities basically used by managers
                System.out.println("5. Update Room Information");
                System.out.println("6. View 5 recent Room Updates Info");
                System.out.println("7. View booking history of the hotel");
                System.out.println("8. View 5 regular Customers");
                System.out.println("9. Place room repair Request to a company");
                System.out.println("10. View room repair Requests history");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewHotels(esql); break;
                   case 2: viewRooms(esql); break;
                   case 3: bookRooms(esql); break;
                   case 4: viewRecentBookingsfromCustomer(esql); break;
                   case 5: updateRoomInfo(esql); break;
                   case 6: viewRecentUpdates(esql); break;
                   case 7: viewBookingHistoryofHotel(esql); break;
                   case 8: viewRegularCustomers(esql); break;
                   case 9: placeRoomRepairRequests(esql); break;
                   case 10: viewRoomRepairHistory(esql); break;
                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Hotel esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine(); 
         String type="Customer";
	 String query = String.format("INSERT INTO USERS (name, password, userType) VALUES ('%s','%s', '%s')", name, password, type);
         esql.executeUpdate(query);
         System.out.println ("User successfully created with userID = " + esql.getNewUserID("SELECT last_value FROM users_userID_seq"));
        
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Hotel esql){
      try{
         System.out.print("\tEnter userID: ");
         String userID = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE userID = '%s' AND password = '%s'", userID, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0) {
            hotel_userID = userID;
            return userID;
	 }
         System.out.println("Invalid username or password.");
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void viewHotels(Hotel esql) {
     try{
         String latitude, longitude;

         System.out.println("Enter latitude: ");
         latitude = in.readLine();

         System.out.println("Enter longitude: ");
         longitude = in.readLine();

         String query = "SELECT h.hotelname " +
                        "FROM Hotel h " +
                        "WHERE calculate_distance(h.latitude, h.longitude, " + latitude + ", " + longitude + ") <= 30;";

         System.out.println("Hotels within 30 units of (" + latitude + ", " + longitude + "):");
         int rowCount = esql.executeQueryAndPrintResult(query);
         System.out.println("total row(s): " + rowCount);

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
   public static void viewRooms(Hotel esql) {
     try{
      System.out.println("Enter Hotel ID: ");
      String input = in.readLine();
      int HotelID_input = Integer.parseInt(input);
      System.out.println("Enter date for booking: MM/DD/YYYY");
      input = in.readLine();
      String date_input = input;
      String query = "SELECT  r.roomNumber AS Room, r.price FROM Rooms r WHERE r.hotelID = ";
      query += HotelID_input + " AND r.roomNumber NOT IN (";
      query += "SELECT rb.roomNumber FROM RoomBookings rb WHERE rb.hotelID = ";
      query += HotelID_input + " AND rb.bookingDate = '";
      query += date_input  + "')";
      System.out.println("Available rooms in hotel #" + HotelID_input + " for " + date_input + ":");
      int rowCount = esql.executeQuery(query);
      if(rowCount <= 0) {
       System.out.println("\tNo available rooms for given date.");
      }
      esql.executeQueryAndPrintResult(query);
      query = "SELECT rb.roomNumber AS Room, r.price FROM RoomBookings rb, Rooms r  WHERE r.roomNumber = rb.roomNumber AND rb.hotelID = ";
      query += HotelID_input + " AND r.hotelID = rb.hotelID AND rb.bookingDate = '";
      query += date_input  + "'";
      System.out.println("Uavailable rooms in hotel #" + HotelID_input + " for " + date_input + ":");
      rowCount = esql.executeQuery(query);
      if(rowCount <= 0) {
       System.out.println("\tAll rooms are available for the given date.");
      }
      esql.executeQueryAndPrintResult(query);
     }catch(Exception e){
      System.err.println (e.getMessage());
     }
   }
   public static void bookRooms(Hotel esql) {
     try {
        System.out.println("Enter Hotel ID: ");
        String hotelID = in.readLine();

        System.out.println("Enter Room #: ");
        String roomNumber = in.readLine();

        System.out.println("Enter the date (MM/DD/YYYY): ");
        String bookingDate = in.readLine();

        String query = "SELECT * " +
                       "FROM RoomBookings rb " +
                       "WHERE rb.hotelID = " + hotelID + " AND rb.roomNumber = " + roomNumber + " AND rb.bookingDate = '" + bookingDate + "';";

        int rowCount = esql.executeQuery(query);

        if (rowCount > 0) {
           System.out.println("Sorry, that room is booked.");
           return;
        }

        query = String.format("INSERT INTO ROOMBOOKINGS (customerID, hotelID, roomNumber, bookingDate) VALUES ('%s','%s', '%s', '%s')", hotel_userID, hotelID, roomNumber, bookingDate);
        esql.executeUpdate(query);

        /* checks to see if INSERT worked
 *         query = "SELECT * " +
 *                 "FROM Roombookings rb " +
 *                         "WHERE rb.customerID = " + hotel_userID + ";";
 *                                 rowCount = esql.executeQueryAndPrintResult(query);
 *                                         System.out.println("total row(s): " + rowCount);*/

        query = "SELECT r.price " +
                "FROM Rooms r " +
                "JOIN RoomBookings rb ON rb.hotelID = r.hotelID " +
                "WHERE rb.hotelID = " + hotelID + " AND r.roomNumber = " + roomNumber +
                "LIMIT 1;";
        System.out.println("Room #" + roomNumber + " at Hotel #" + hotelID + " has been booked for " + bookingDate + ". The price is listed below. ");
        rowCount = esql.executeQueryAndPrintResult(query);
        System.out.println("total row(s): " + rowCount);

     }catch(Exception e){
        System.err.println (e.getMessage ());
     }
   }
   public static void viewRecentBookingsfromCustomer(Hotel esql) {
     try{
      String query = "SELECT rb.hotelID AS hotel, rb.roomNumber AS room, r.price AS price, rb.bookingDate AS date FROM Rooms r, RoomBookings rb WHERE r.hotelID = rb.HotelID AND r.roomNumber = rb.roomNumber AND rb.customerID = " + hotel_userID + " ORDER BY rb.bookingDate DESC LIMIT 5";
      int rowCount = esql.executeQuery(query);
      if(rowCount <= 0) {
       System.out.println("No recent bookings.");
      } else {
        System.out.println("Latest 5 recent bookings:");
      }
      esql.executeQueryAndPrintResult(query);
     }catch(Exception e){
       System.err.println (e.getMessage());
     }
   }
   public static void updateRoomInfo(Hotel esql) {
     try {
        String query = "SELECT * " +
                       "FROM USERS u " +
                       "WHERE u.userID = " + hotel_userID + " AND u.userType = 'manager';";
        int rowCount = esql.executeQuery(query);

        /*check if user is a manger*/
        if (rowCount != 1) {
           System.out.println("Sorry, you do not have access.");
           return;
        }

        System.out.println("Enter Hotel ID: ");
        String hotelID = in.readLine();

        query = "SELECT * " +
                "FROM Users u " +
                "JOIN Hotel h ON h.managerUserID = u.userID " +
                "WHERE h.manageruserID = " + hotel_userID + " AND h.hotelID = " + hotelID + ";";
        rowCount = esql.executeQuery(query);

        /*check if user is a manager of specified hotel*/
        if (rowCount != 1) {
           System.out.println("Sorry, you do not have access. You are not the manager of this hotel. ");
           return;
        }

        System.out.println("Enter Room #: ");
        String roomNumber = in.readLine();

        query = "SELECT * " +
                "FROM Rooms r " +
                "WHERE r.hotelID = " + hotelID + " AND r.roomNumber = " + roomNumber + ";";
        rowCount = esql.executeQuery(query);

        /*check if user entered valid room*/
        if (rowCount != 1) {
           System.out.println("Sorry, that is not a valid room. ");
           return;
        }

        System.out.println("Enter new room price: ");
        String price = in.readLine();
        double newPrice = Double.parseDouble(price);

        if (newPrice % 1 != 0 || newPrice <= 0) {
           System.out.println("Sorry, that is not a valid price. Unable to update room. ");
           return;
        }

        System.out.println("Enter Image URL: ");
        String imageURL = in.readLine();

        if (imageURL.length() > 30) {
           System.out.println("The Image URL is too long. Unable to update room. ");
           return;
        }
        else if (imageURL.length() == 0) {
           System.out.println("The Image URL is empty. Unable to update room. ");
           return;
        }

        query = "UPDATE Rooms SET price = " + price + ", imageURL = '" + imageURL + "' WHERE hotelID = " + hotelID + " AND roomNumber = " + roomNumber + ";";
        esql.executeUpdate(query);

        query = String.format("INSERT INTO roomUpdatesLog (managerID, hotelID, roomNumber, updatedOn) VALUES ('%s','%s', '%s', CURRENT_TIMESTAMP)", hotel_userID, hotelID, roomNumber);
        esql.executeUpdate(query);
        System.out.println("Room " + roomNumber + " has been updated. ");

     }catch(Exception e){
        System.err.println (e.getMessage ());
     }
   }
   public static void viewRecentUpdates(Hotel esql) {
    try{
       String query = "SELECT * FROM Users u WHERE u.userID = " + hotel_userID + " AND u.userType = 'manager'";
       int rowCount = esql.executeQuery(query);
       if(rowCount <= 0) {
        System.out.println("Only managers have access to this feature.");
        return;
       }
       query  = "SELECT updates.updateNumber AS update, updates.hotelID AS hotel, updates.roomNumber AS room, updates.updatedOn AS update_time FROM RoomUpdatesLog updates, Users u WHERE u.userID = updates.managerID AND u.userType = 'manager' AND updates.managerID = " + hotel_userID + " ORDER BY updates.updatedOn DESC LIMIT 5";
       rowCount = esql.executeQuery(query);
       if(rowCount <= 0) {
        System.out.println("No recent updates.");
       } else {
         System.out.println("Latest 5 recent updates made to your hotel:");
       }
       esql.executeQueryAndPrintResult(query);
     }catch(Exception e){
       System.err.println (e.getMessage());
     }
   }
   public static void viewBookingHistoryofHotel(Hotel esql) {
     try {
         String query = "SELECT * " +
                        "FROM USERS u " +
                        "WHERE u.userID = " + hotel_userID + " AND u.userType = 'manager';";
         int rowCount = esql.executeQuery(query);

         /*check if user is a manager*/
         if (rowCount != 1) {
            System.out.println("Sorry, you do not have access.");
            return;
         }

         System.out.println("Enter start date (MM/DD/YYYY): ");
         String startDate = in.readLine();

         System.out.println("Enter end date (MM/DD/YYYY): ");
         String endDate = in.readLine();

         query = "SELECT b.bookingID, u.name, b.hotelID, b.roomNumber, b.bookingDate " +
                 "FROM RoomBookings b " +
                 "JOIN Hotel h ON h.hotelID = b.hotelID " +
                 "JOIN Users u ON u.userID = b.customerID " +
                 "WHERE h.managerUserID = " + hotel_userID +  " AND b.bookingDate BETWEEN '" + startDate + "' AND '" + endDate + "'" +
                 "ORDER BY b.bookingDate DESC;";
         System.out.println("Booking history between " + startDate + "-" + endDate + ": ");
         rowCount = esql.executeQueryAndPrintResult(query);
         if (rowCount < 1)
            System.out.println("\tNo bookings made. ");
         else
            System.out.println("Total row(s): " + rowCount);

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }
   public static void viewRegularCustomers(Hotel esql) {
    try{
       String query = "SELECT * FROM Users u WHERE u.userID = " + hotel_userID + " AND u.userType = 'manager'";
       int rowCount = esql.executeQuery(query);
       if(rowCount <= 0) {
        System.out.println("Only managers have access to this feature.");
        return;
       }
       System.out.println("Enter Hotel ID: ");
       String input = in.readLine();
       int HotelID_input = Integer.parseInt(input);
       query = "SELECT * FROM Hotel h WHERE h.managerUserID = " + hotel_userID + " AND h.hotelID = " + HotelID_input;
       rowCount = esql.executeQuery(query);
       if(rowCount <= 0) {
        System.out.println("You must manage this hotel to view this information.");
        return;
       }
       query = "SELECT books_per_cust.customer_id AS id, u.name AS name  FROM (SELECT rb.customerID AS customer_id, COUNT(rb.bookingID) AS bookings FROM RoomBookings rb WHERE rb.HotelID = " + HotelID_input + " GROUP BY rb.customerID) AS books_per_cust, Users u WHERE u.userID = books_per_cust.customer_id ORDER BY books_per_cust.bookings DESC LIMIT 5";
       rowCount = esql.executeQuery(query);
       if(rowCount <= 0) {
        System.out.println("No regular customers.");
       } else {
        System.out.println("Top 5 regular customers for hotel #" + HotelID_input + ":" );
      }
       esql.executeQueryAndPrintResult(query);
     }catch(Exception e){
       System.err.println (e.getMessage());
     }
   }
   public static void placeRoomRepairRequests(Hotel esql) {
     try {
        String query = "SELECT * " +
                       "FROM USERS u " +
                       "WHERE u.userID = " + hotel_userID + " AND u.userType = 'manager';";
        int rowCount = esql.executeQuery(query);

        /*check if user is a manager*/
        if (rowCount != 1) {
           System.out.println("Sorry, you do not have access.");
           return;
        }

        System.out.println("Enter Hotel ID: ");
        String hotelID = in.readLine();

        query = "SELECT * " +
                "FROM Users u " +
                "JOIN Hotel h ON h.managerUserID = u.userID " +
                "WHERE h.manageruserID = " + hotel_userID + " AND h.hotelID = " + hotelID + ";";
        rowCount = esql.executeQuery(query);

        /*check if user is a manager of specified hotel*/
        if (rowCount != 1) {
           System.out.println("Sorry, you do not have access. You are not the manager of this hotel. ");
           return;
        }

        System.out.println("Enter Room #: ");
        String roomNumber = in.readLine();

        query = "SELECT * " +
                "FROM Rooms r " +
                "WHERE r.hotelID = " + hotelID + " AND r.roomNumber = " + roomNumber + ";";
        rowCount = esql.executeQuery(query);

        /*check if user entered valid room*/
        if (rowCount != 1) {
           System.out.println("Sorry, that is not a valid room. ");
           return;
        }

        System.out.println("Enter Company ID: ");
        String companyID = in.readLine();

        query = "SELECT * " +
                "FROM MaintenanceCompany c " +
                "WHERE c.companyID = " + companyID + ";";
        rowCount = esql.executeQuery(query);

        if (rowCount != 1) {
           System.out.println("Sorry, that is not a valid company. ");
           return;
        }

        query = String.format("INSERT INTO roomRepairs (companyID, hotelID, roomNumber, repairDate) VALUES ('%s','%s', '%s', CURRENT_DATE)", companyID, hotelID, roomNumber);
        esql.executeUpdate(query);

        /*check if roomRepairs INSERTED properly
 *         query = "SELECT * " +
 *                         "FROM roomRepairs r " +
 *                                         "ORDER BY repairID DESC " +
 *                                                         "LIMIT 1;";
 *                                                                 rowCount = esql.executeQueryAndPrintResult(query);*/

        query = String.format("INSERT INTO roomRepairRequests (managerID, repairID) VALUES ('%s', " +
                              "(SELECT r.repairID " +
                              "FROM roomRepairs r " +
                              "ORDER BY repairID DESC " +
                              "LIMIT 1))", hotel_userID);
        esql.executeUpdate(query);
        System.out.println("A request has been made for Hotel #" + hotelID + ", Room #" + roomNumber + " with Company #" + companyID + ". ");

        /*check if roomRepairRequests INSERTED properly
 *         query = "SELECT * " +
 *                         "FROM roomRepairRequests r " +
 *                                         "ORDER BY requestNumber DESC " +
 *                                                         "LIMIT 1;";
 *                                                                 rowCount = esql.executeQueryAndPrintResult(query);*/

     }catch(Exception e){
        System.err.println (e.getMessage ());
     }
   }
   public static void viewRoomRepairHistory(Hotel esql) {
     try{
       String query = "SELECT * FROM Users u WHERE u.userID = " + hotel_userID + " AND u.userType = 'manager'";
       int rowCount = esql.executeQuery(query);
       if(rowCount <= 0) {
        System.out.println("Only managers have access to this feature.");
        return;
       }
       query = "SELECT req.requestNumber AS request, repairs.companyID AS company, repairs.HotelID AS hotel, repairs.roomNumber AS room, repairs.repairDate AS date FROM RoomRepairs repairs, RoomRepairRequests req WHERE repairs.repairID = req.repairID AND req.managerID = " + hotel_userID + " ORDER BY repairs.repairDate DESC";
       rowCount = esql.executeQuery(query);
       if(rowCount <= 0) {
        System.out.println("No repair request history.");
       } else {
        System.out.println("Room repair requests history:" );
       }
       esql.executeQueryAndPrintResult(query);
     }catch(Exception e){
       System.err.println (e.getMessage());
     }
   }

}//end Hotel
