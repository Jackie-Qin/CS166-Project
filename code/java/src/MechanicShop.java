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
import java.util.Random;
import java.lang.Math;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
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
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
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
	}
	
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
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
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
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
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
		if (rs.next()) return rs.getInt(1);
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
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

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
	

	public static void AddCustomer(MechanicShop esql){//1
		int cid;
		String address;
		String fName;
		String lName;
		String phone;

		

		do {	 
			System.out.print("Please insert customer's address: ");
			try {
				if(Integer.parseInt(esql.executeQueryAndReturnResult("SELECT MAX(id) FROM Customer;").get(0).get(0)) != 0){
					cid = Integer.parseInt(esql.executeQueryAndReturnResult("SELECT MAX(id) FROM Customer;").get(0).get(0)) + 1;
				} else {
					cid = 1;
				}
				address = in.readLine();
				if( address.length() <= 0 || address.length() > 256){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			}
		}while (true);
		System.out.println("--------------------------------------------------------");

		
		do { System.out.print("-------- Please insert customer's first name: ---------");
			try { fName = in.readLine();
				if( fName.length() <= 0 || fName.length() > 32){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);
		System.out.println("--------------------------------------------------------");


		do { System.out.print("---------- Please insert customer's last name: ----------");
			try { lName = in.readLine();
				if( lName.length() <= 0 || lName.length() > 32){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);
		System.out.println("--------------------------------------------------------");


		do { System.out.print("---------- Please insert customer's phone number: ---------");
			try { phone = in.readLine();
				for(int i = 1; i < 13; ++i){
					if(i == 4 || i == 8){
						continue;
					}
					if( Character.isDigit(phone.charAt(i)) ){
						continue;
					} else {
						throw new RuntimeException("Invalid input. Please try again!");
					}
				}
				if( phone.length() != 13 || Character.isDigit(phone.charAt(0)) || Character.isDigit(phone.charAt(4)) || Character.isDigit(phone.charAt(8))){
					throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);

		String res = "INSERT INTO Customer (id, fname, lname, phone, address) VALUES ('" + cid + "', '" + fName + "', '" + lName + "', '" + phone + "', '" + address + "');";
		try{ esql.executeUpdate(res); 

			System.out.println("------- Here are the new customer's information: -------");
		int output = esql.executeQueryAndPrintResult("SELECT * FROM Customer WHERE id = " + cid);
		System.out.println("--------------------------------------------------------");

		}
		catch (Exception e) { System.err.println(e.getMessage()); }

		
	}
	
	public static void AddMechanic(MechanicShop esql){//2
		int empid;
		String fName;
		String lName;
		int exp_year;

		
		do { System.out.print("------ Please insert mechanic's first name: -------");
			try {
				if(Integer.parseInt(esql.executeQueryAndReturnResult("SELECT MAX(id) FROM Mechanic;").get(0).get(0)) != 0){
					empid = Integer.parseInt(esql.executeQueryAndReturnResult("SELECT MAX(id) FROM Mechanic;").get(0).get(0)) + 1;
				} else {
					empid = 1;
				}
		 
				fName = in.readLine();
				if( fName.length() <= 0 || fName.length() > 32){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);
		System.out.println("--------------------------------------------------------");


		do { System.out.println("Please insert mechanic's last name: ");
			try { lName = in.readLine();
				if( lName.length() <= 0 || lName.length() > 32){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);
		System.out.println("--------------------------------------------------------");

		do { System.out.print("Please insert mechanic's experience in a number of years: ");
			try { exp_year = java.lang.Integer.parseInt(in.readLine());
				if ( exp_year <= 0 || exp_year > 99){
					throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);

		String res = "INSERT INTO Mechanic (id, fname, lname, experience) VALUES ('" + empid + "', '" + fName + "', '" + lName + "', '" + exp_year + "');";
		try{ esql.executeUpdate(res); }
		catch (Exception e) { System.err.println(e.getMessage()); }

		try{System.out.println("------- Here are the new mechanic's information: -------");
		int output = esql.executeQueryAndPrintResult("SELECT * FROM Mechanic WHERE id = " + empid);
		System.out.println("--------------------------------------------------------");
	}catch (Exception e) { System.err.println(e.getMessage()); }}
	
	public static void AddCar(MechanicShop esql){//3
		String VIN;
		String Make;
		String Model;
		int Year;

		do { System.out.print("--------- Please insert car's VIN: ----------");
			try { VIN = in.readLine();
				if( VIN.length() != 16){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				for (int i = 0; i < 6; ++i){
					if (Character.isDigit(VIN.charAt(i))){
						throw new RuntimeException("Invalid input. Please try again!");
					}
				}
				for (int i = 6; i < 16; ++i){
					if (!Character.isDigit(VIN.charAt(i))){
						throw new RuntimeException("Invalid input. Please try again!");
					}
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);
		System.out.println("--------------------------------------------------------");

		
		do { System.out.print("---------- Please insert car's make: -------------");
			try { Make = in.readLine();
				if( Make.length() <= 0 || Make.length() > 32){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);
		System.out.println("--------------------------------------------------------");

		do { System.out.print("-------- Please insert car's model: ---------");
			try { Model = in.readLine();
				if( Model.length() <= 0 || Model.length() > 32){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);


		do { System.out.print("--------- Please insert car's production year: ---------");
			try { Year = java.lang.Integer.parseInt(in.readLine());
				if ( Year <= 0 || Year > 2021){
					throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);

		String res = "INSERT INTO Car (vin, make, model, year) VALUES ('" + VIN + "', '" + Make + "', '" + Model + "', '" + Year + "');";
		try{ esql.executeUpdate(res); 
		System.out.println("--------- Here are the new car's information: ----------");
		int output = esql.executeQueryAndPrintResult("SELECT * FROM Car WHERE vin = '" + VIN +"';");
		System.out.println("--------------------------------------------------------");


		}
		catch (Exception e) { System.err.println(e.getMessage()); }
		
	
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		int rid;
		String VIN;
		String date;
		int odometer;
		String complain;
		String CURRENT_DATE;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        CURRENT_DATE = dtf.format(now);
		String lName;
		int input;
		String carRecord;
		int temp;
		do { System.out.print("-------- Please insert customer's last name: --------");
			try { lName = in.readLine();
				if( lName.length() <= 0 || lName.length() > 32){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);

		do{
			try{if (esql.executeQuery("SELECT * FROM Customer WHERE lname = '" + lName + "';")!= 0){
			System.out.println("-------- Please Choose your id if there exists ---------");
			System.out.println("------------------ Otherwise, type 0 -------------------");
			temp = esql.executeQueryAndPrintResult("SELECT * FROM Customer WHERE lname = '" + lName + "';");
			input = java.lang.Integer.parseInt(in.readLine());
							
			
			if(input == 0){
				System.out.println("-------- No existing record. Creating now... ---------");
				AddCustomer(esql);
				System.out.println("--------------- Please type your id again ------------");
				input = java.lang.Integer.parseInt(in.readLine());

			} 

				carRecord = "SELECT car_vin FROM Owns WHERE customer_id = '" + input + "';";
				if (esql.executeQuery(carRecord)!=0){
					System.out.println("--------------------------------------------------------");
					temp = esql.executeQueryAndPrintResult(carRecord);
					System.out.println("------------- Please choose your car VIN ---------------");
					VIN = in.readLine();
				} else {
					System.out.println("-------- No existing record. Adding a new car... -------");
					AddCar(esql);
					System.out.println("------------ Please type your VIN number again ---------");
					VIN = in.readLine();
				}
			
		
		System.out.println("--------------------------------------------------------");
		break;
	}}
	catch (Exception e) {
				System.out.println(e);
				continue;}
}while(true);


		
		do { System.out.print("------ Creating the service record now ... -------");
			try {
				if(Integer.parseInt(esql.executeQueryAndReturnResult("SELECT MAX(rid) FROM Service_Request;").get(0).get(0)) != 0){
					rid = Integer.parseInt(esql.executeQueryAndReturnResult("SELECT MAX(rid) FROM Service_Request;").get(0).get(0)) + 1;
				} else {
					rid = 1;
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);
		System.out.println("--------------------------------------------------------");
	
		do { System.out.print("---------- Please insert odometer: ----------");
			try { odometer = java.lang.Integer.parseInt(in.readLine());
				if( odometer < 0 || odometer > 9999999){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);
		
		do { System.out.print("---------- Please insert complain: ----------");
			try { 	complain = in.readLine();
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);
		
		

		String res = "INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain) VALUES ('" + rid + "', '" + input + "', '" + VIN + "', '" + CURRENT_DATE + "', '" + odometer + "', '" + complain + "');";
		try{ esql.executeUpdate(res); 
			 temp = esql.executeQueryAndPrintResult("SELECT * FROM Service_Request WHERE rid = '" + rid + "';");}
		catch (Exception e) { System.err.println(e.getMessage()); }
		
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		String serviceNum;
		String temp;
		String eid;
		String CURRENT_DATE;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        CURRENT_DATE = dtf.format(now);
		int run;
		do{try {
			System.out.println("------ Please enter the service request number you want to close: -----");
			temp = "SELECT * FROM Service_Request";
			run = esql.executeQueryAndPrintResult(temp);

			serviceNum = in.readLine();
			temp = "SELECT * FROM Service_Request WHERE rid = '" + serviceNum + "';";
			run = esql.executeQuery(temp);
			while(run == 0){
			    System.out.println("--- Not a valid number. Please type again... ---");
			    serviceNum = in.readLine();
			    temp = "SELECT * FROM Service_Request WHERE rid = '" + serviceNum + "';";
			    run = esql.executeQuery(temp);
			}

			System.out.println("--- Choose an employee that you wish to work on your service ---");
			System.out.println("---------- Enter the id here --------");
			temp = "SELECT * FROM Mechanic;";
			run = esql.executeQueryAndPrintResult(temp);

			eid = in.readLine();
			temp = "SELECT * FROM Mechanic WHERE id = '" + eid + "';";
			run = esql.executeQuery(temp);
			while (run == 0){
			    System.out.println("--- Not a valid number. Please type again... ---");
			    eid = in.readLine();
			    temp = "SELECT * FROM Mechanic WHERE id = '" + eid + "';";
			    run = esql.executeQuery(temp);
			}
			break;
	    }
	    catch(Exception e){
		System.out.println(e);
				continue;
	    }

	    }while(true);
	    
	    String comment;
	    int bill;
	    do { System.out.println("------------- Please insert comment: -------------");
			try { comment = in.readLine();
				if( comment.length() < 1){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			}
		} while (true);

		do { System.out.print("---------- Please insert bill number: ----------");
			try { bill = java.lang.Integer.parseInt(in.readLine());
				if( bill < 0){
				  	throw new RuntimeException("Invalid input. Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);



    	try{System.out.println("---- Deleting the service request... ----");
		String res = "INSERT INTO Closed_Request (wid, rid, mid, date, comment, bill) VALUES ('" + serviceNum + "', '" + serviceNum + "', '" + eid + "', '" + CURRENT_DATE + "', '" + comment + "', '" + bill + "');";
		temp = "DELETE FROM Service_Request WHERE rid = '" + serviceNum+"';";
		esql.executeQuery(temp);}
		catch(Exception e) {
				System.out.println(e);
				}
		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		
		try{
			String query = "SELECT date,comment,bill FROM Closed_Request WHERE bill < 100;";
			int run = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + run);
		}
	 	catch(Exception e){
			System.err.println(e.getMessage());
		}	
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		
		try{
			String query = "SELECT fname,lname FROM Customer, (SELECT customer_id, COUNT(customer_id) FROM Owns GROUP BY customer_id HAVING COUNT(customer_id) > 20) AS NUM WHERE NUM.customer_id=Customer.id;";
			
			int run = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + run);
		}
	 	catch(Exception e){
			System.err.println(e.getMessage());
		}	
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		
		try{
			String query = "SELECT DISTINCT make, model, year FROM Car AS C, Service_Request AS S WHERE year < 1995 and S.car_vin = C.vin and S.odometer < 50000;";
			int run = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + run);
		}
	 	catch(Exception e){
			System.err.println(e.getMessage());
		}	
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		int k;
		do { System.out.print("-------- Please insert the the K numbers of cars: --------");
			try { k = java.lang.Integer.parseInt(in.readLine());
				if( k <= 0 ){
				  	throw new RuntimeException("K needs to be greater than 0! Please try again!");
				}
				break;
			} catch (Exception e) {
				System.out.println(e);
				continue;
			} 
		}while (true);
		
		try{
			String query = "SELECT make, model, NOC.num FROM Car C, (SELECT car_vin, COUNT(rid) AS num FROM Service_Request GROUP BY car_vin) NOC WHERE NOC.car_vin = C.vin ORDER BY NOC.num DESC LIMIT ";
			query += k + ";";
			int run = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + run);
		}
	 	catch(Exception e){
			System.err.println(e.getMessage());
		}	
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//10
		//
		
		try{
			String query = "SELECT Customer.fname, Customer.lname, total FROM Customer,(SELECT S.customer_id, SUM(C.bill) AS total FROM Closed_Request AS C, Service_Request AS S WHERE C.rid = S.rid GROUP BY S.customer_id) AS TEMP WHERE Customer.id=TEMP.customer_id ORDER BY TEMP.total DESC;";
			int run = esql.executeQueryAndPrintResult(query);
			System.out.println("total row(s): " + run);
		}
	 	catch(Exception e){
			System.err.println(e.getMessage());
		}	
		
	}
	
}