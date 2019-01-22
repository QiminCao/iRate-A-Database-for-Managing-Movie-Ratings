import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class iRateTest {
  // JDBC driver name, database URL, credentials, dbname
  private static final String JDBC_DRIVER = iRate.JDBC_DRIVER;
  private static final String DB_URL = iRate.DB_URL;
  private static final String USER = iRate.USER;
  private static final String PSW = iRate.PSW;
  private static final String dbname = iRate.dbname;

  static Connection conn;
  static Statement stmt;

  /**
   * This method is used to connect iRate database.
   */
  static void connectiRate() {
    try {
      // connect to the database using URL
      Class.forName(JDBC_DRIVER);

      // Open a connection
      System.out.println("Connecting to iRate database...");
      conn = DriverManager.getConnection(DB_URL, USER, PSW);

      // Execute a query
      stmt = conn.createStatement();

      // statement is channel for sending commands through connection
      stmt.executeUpdate("USE " + dbname);
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * This method is used to clear data from iRate tables.
   */
  static void clearData() {
    // tables created by this program
    String dbTables[] = {
            "Attendance", "Endorsement", "Review", "Customer", "Movie"
    };

    // clear data from tables
    for (String tbl : dbTables) {
      try {
        stmt.executeUpdate("DELETE FROM " + tbl);
        System.out.println("Deleted table " + tbl);
      } catch (SQLException ex) {
        System.out.println("Did not delete table " + tbl);
      }
    }
  }

  /**
   * This method is used to insert data into tables.
   */
  static void insertData() {
    try {
      // insert prepared statements
      PreparedStatement insertRow_Customer = conn.prepareStatement(
              "insert into Customer(first_name, last_name, email, join_date) values(?, ?, ?, ?)");
      PreparedStatement insertRow_Movie = conn.prepareStatement(
              "insert into Movie(title) values(?)");
      PreparedStatement insertRow_Attendance = conn.prepareStatement(
              "insert into Attendance(customer_uid, movie_id, watch_date) values(?, ?, ?)");
      PreparedStatement insertRow_Review = conn.prepareStatement(
              "insert into Review(movie_id, customer_uid, rating, review, review_date) values(?, ?, ?, ?, ?)");
      PreparedStatement insertRow_Endorsement = conn.prepareStatement(
              "insert into Endorsement(customer_uid, review_id, endorse_date) values(?, ?, ?)");

      parseData(insertRow_Customer, "customer_data.txt", 4);
      System.out.println("Insert 11 rows into Customer table successfully!");

      parseData(insertRow_Movie, "movie_data.txt", 1);
      System.out.println("Insert 8 rows into Movie table successfully!");

      parseData(insertRow_Attendance, "attendance_data.txt", 3);
      System.out.println("Insert 14 rows into Attendance table successfully!");

      parseData(insertRow_Review, "review_data.txt", 5);
      System.out.println("Insert 10 rows into Review table successfully!");

      parseData(insertRow_Endorsement, "endorsement_data.txt", 3);
      System.out.println("Insert 13 rows into Endorsement table successfully!");


      // tables created by this program
      String dbTables[] = {
              "Customer", "Movie", "Attendance", "Review", "Endorsement"
      };

      // result set for queries
      ResultSet rs = null;
      System.out.println();
      System.out.println("Number of rows in every table:");
      // print number of rows in tables
      for (String tbl : dbTables) {
        rs = stmt.executeQuery("select count(*) from " + tbl);
        if (rs.next()) {
          int count = rs.getInt(1);
          System.out.printf("Table %12s: count: %d\n", tbl, count);
        }
      }
      rs.close();
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * This method is used to parse data from data.txt and insert data into cprresponding table.
   * @param preparedStatement prepared statements
   * @param file the data file
   * @param columns the number of columns
   */
  static void parseData(PreparedStatement preparedStatement, String file, int columns) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(new File(file)));
      String line = null;
      while ((line = br.readLine()) != null) {
        String[] data = line.trim().split("\t");
        if (data.length != columns) {
          continue;
        }

        for (int i = 0; i < columns; i++) {
          preparedStatement.setString(i + 1, data[i].trim());
        }
        preparedStatement.executeUpdate();
      }
    } catch (SQLException | IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method is used to test invalid email format.
   */
  static void testEmail() {
    // Test email missing . operator
    try {
      System.out.println("Test 1: Try to insert email address without \".\", it should fail and print out a warning message.");
      String invalid_email =
              "INSERT INTO Customer(first_name, last_name, email, join_date) VALUES" +
                      "('karen', 'Cao', 'ikaren@gmailcom', '2018-10-1')";
      stmt.executeUpdate(invalid_email);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage()  + "\n");
    }

    // Test email missing @ operator
    try {
      System.out.println("Test 2: Try to insert email address without \"@\", it should fail and print out a warning message.");
      String invalid_email =
              "INSERT INTO Customer(first_name, last_name, email, join_date) VALUES" +
                      "('alice', 'He', 'ialice?gmail.com', '2018-10-2')";
      stmt.executeUpdate(invalid_email);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage()  + "\n");
    }

    // Test email missing . and @ operator
    try {
      System.out.println("Test 3: Try to insert email address without \"@\" and \".\", it should fail and print out a warning message.");
      String invalid_email =
              "INSERT INTO Customer(first_name, last_name, email, join_date) VALUES" +
                      "('monica', 'Zhang', 'monica gmail com', '2018-10-3')";
      stmt.executeUpdate(invalid_email);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage()  + "\n");
    }
  }

  /**
   * This method is used to test review limitation.
   */
  static void testReviewLimit() {
    // Test a customer can only have one review on one movie.
    try {
      System.out.println("Test 1: Try to insert a second review from a customer on one movie, it should fail and print out a warning message.");
      String invalid_review_data =
              "INSERT INTO Review(movie_id, customer_uid, rating, review, review_date) VALUES" +
                      "(1, 1, '3 stars', 'customer 1 already have a review to movie 1','2018-10-13')";
      stmt.executeUpdate(invalid_review_data);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage()  + "\n");
    }

    // Test a customer can not review a movie if he/she did not watch that move.
    try {
      System.out.println("Test 2: Try to insert a review of a movie from a customer who did not attend that movie, it should fail and print out a warning message.");
      String invalid_review_data =
              "INSERT INTO Review(movie_id, customer_uid, rating, review, review_date) VALUES" +
                      "(8, 1, '3 stars', 'customer 1 did not watch movie 8, but he/she wants to review it.', '2018-10-11 08:00:00')";
      stmt.executeUpdate(invalid_review_data);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage()  + "\n");
    }


    // Test insert review after 7 days review created.
    try {
      System.out.println("Test 3: Try to insert a review from a customer after 7 days he/she watched the movie, it should fail and print out a warning message.");
      String invalid_review_data =
              "INSERT INTO Review(movie_id, customer_uid, rating, review, review_date) VALUES" +
                      "(2, 2, '3 stars', 'customer 2 tries to review movie 2 after 7 days he/she watched.', '2018-10-20 08:00:00')";
      stmt.executeUpdate(invalid_review_data);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage()  + "\n");
    }

    // Test insert review before a customer attend a movie.
    try {
      System.out.println("Test 4: Try to insert a review from a customer before he/she watching the movie, it should fail and print out a warning message.");
      String invalid_review_data =
              "INSERT INTO Review(movie_id, customer_uid, rating, review, review_date) VALUES" +
                      "(2, 2, '3 stars', 'customer 2 tries to review movie 2 before he/she watching it.', '2018-10-10 08:00:00')";
      stmt.executeUpdate(invalid_review_data);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage()  + "\n");
    }

    // Test the date of the review must be within 7 days of the most recent attendance of the movie
    try {
      System.out.println("Test 4: Customer(11) tries to review movie(8) at date 2017-11-01, whereas he watched it at 2017-10-1. It should fail and print out a warning message.");
      String invalid_review_data =
              "INSERT INTO Review(movie_id, customer_uid, rating, review, review_date) VALUES" +
                      "(8, 11, '3 stars', 'can not endorse this review', '2017-11-01 08:00:00')";
      stmt.executeUpdate(invalid_review_data);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage());
    }

    try {
      System.out.println("Inserting a new attendance record: customer(11) attend movie(8) at 2017-10-30...");
      String second_attendance =
              "INSERT INTO Attendance(customer_uid, movie_id, watch_date) VALUES" +
                      "(11, 8, '2017-10-30 08:00:00')";
      stmt.executeUpdate(second_attendance);

      System.out.println("After insertion, insert the same review record to Review should be successful.");
      String invalid_review_data =
              "INSERT INTO Review(movie_id, customer_uid, rating, review, review_date) VALUES" +
                      "(8, 11, '3 stars', 'this review should be inserted successfully', '2017-11-01 08:00:00')";
      stmt.executeUpdate(invalid_review_data);
      System.out.println("Testing result: insert successfully.\n");
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * This method is used to test endorsement limitation.
   */
  static void testEndorsementLimit() {
    // Test a customer cannot endorse his/her own review.
    try {
      System.out.println("Test 1: Customer(1) tries to endorse his/her own review, it should fail and print out a warning message.");
      String invalid_endorsement_data =
              "INSERT INTO Endorsement(customer_uid, review_id, endorse_date) VALUES" +
                      "(1, 1, '2018-11-2')";
      stmt.executeUpdate(invalid_endorsement_data);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage()  + "\n");
    }

    // Test a customer can not endorse the review that is created 3 days ago.
    try {
      System.out.println("Test 2: Customer(1) tries to endorse review(5) that was created three days ago, it should fail and print out a warning message.");
      String invalid_endorsement_data =
              "INSERT INTO Endorsement(customer_uid, review_id, endorse_date) VALUES" +
                      "(1, 5, '2018-11-2')";
      stmt.executeUpdate(invalid_endorsement_data);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage()  + "\n");
    }

    // Test a customer can not endorse a review twice on the same day.
    try {
      System.out.println("Test 2: Customer(3) tries to endorse review(1) twice on the same day, it should fail and print out a warning message.");
      String invalid_endorsement_data =
              "INSERT INTO Endorsement(customer_uid, review_id, endorse_date) VALUES" +
                      "(3, 1, '2018-10-15')";
      stmt.executeUpdate(invalid_endorsement_data);
    } catch (SQLException e) {
      System.out.println("Testing result: " + e.getMessage()  + "\n");
    }
  }

  /**
   * This method is used to test delete specific data from a table and print out the corresponding
   * tables.
   */
  static void testDelete() {
    // tables created by this program
    String dbTables[] = {
            "Attendance", "Endorsement", "Review", "Customer", "Movie"
    };

    try {
      // delete customer 1 -- karen
      System.out.println("Test 1: If a customer is deleted, all of his or her attendances, reviews and endorsements should be deleted.\n");
      System.out.println("Deleting Customer(1) \"Karen (Cao)\" from Customer...");
      stmt.execute("DELETE FROM Customer WHERE first_name = 'Karen' AND last_name = 'Cao'");
      iRateUtil.printCustomer(conn);
      iRateUtil.printAttendance(conn);
      iRateUtil.printReview(conn);
      iRateUtil.printEndorsement(conn);
      System.out.println();

      // delete movie -- The Lion King
      System.out.println("Test 2: If a movie is deleted, all of its attendances, reviews and endorsements should be deleted.\n");
      System.out.println("Deleting Movie(3) 'The Lion King' from Movie...");
      stmt.execute("DELETE FROM Movie WHERE title = 'The Lion King'");
      iRateUtil.printMovie(conn);
      iRateUtil.printAttendance(conn);
      iRateUtil.printReview(conn);
      iRateUtil.printEndorsement(conn);
      System.out.println();

      // delete review
      System.out.println("Test 3: If a review is deleted, all of its endorsements should be deleted.\n");
      System.out.println("Deleting Review(5) 'Loved so many things about it but Disney Princesses slumber party was hilarious!' from Review...");
      stmt.execute("DELETE FROM Review WHERE review = 'Loved so many things about it but Disney Princesses slumber party was hilarious!'");
      iRateUtil.printReview(conn);
      iRateUtil.printEndorsement(conn);

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  /**
   * This method is used to print out the customer whose review is top rated review of a movie
   * written three days earlier.
   */
  static void callFindTopRatedReview() {
    System.out.println("At this time, the writer of the top rated review three days earlier is:");
    CallableStatement cs;
    try {
      cs = conn.prepareCall("{CALL findTopEndorsedReview('2018-10-23')}");
      cs.execute();
      ResultSet rs = cs.getResultSet();

      System.out.println(String.format("%-12s | %-12s | %-12s | %-15s",
              "customer_uid", "first_name", "last_name", "email"));

      if (rs != null) {
        while (rs.next()) {
          int customer_uid = rs.getInt("uid");
          String first_name = rs.getString("first_name");
          String last_name = rs.getString("last_name");
          String email = rs.getString("email");
          System.out.println(String.format("%-12d | %-12s | %-12s | %-15s\n",
                  customer_uid, first_name, last_name, email));
        }
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }

  }

  static void callFindCustomerForFreeConcessionItem() {
    System.out.println("At this time, the candidates who can receive free concession items are: ");
    CallableStatement cs;
    try {
      cs = conn.prepareCall("{CALL findCustomerForFreeConcessionItem('2018-10-22')}");
      cs.execute();
      ResultSet rs = cs.getResultSet();

      System.out.println(String.format("%-12s | %-12s | %-12s | %-15s",
              "customer_uid", "first_name", "last_name", "email"));

      if (rs != null) {
        while (rs.next()) {
          int customer_uid = rs.getInt("uid");
          String first_name = rs.getString("first_name");
          String last_name = rs.getString("last_name");
          String email = rs.getString("email");
          System.out.println(String.format("%-12d | %-12s | %-12s | %-15s",
                  customer_uid, first_name, last_name, email));
        }
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
    System.out.println();
    System.out.println("\n=============================================== Start testing iRate ================================================");
    connectiRate();

    //
    System.out.println("\n------------------------- STEP 1: Deleting and repopulating tables in the iRate database ---------------------------");
    clearData();
    insertData();

    System.out.println("\n------------------------------------- STEP 2: Testing invalid email ------------------------------------------------");
    testEmail();


    System.out.println("\n---------------------------------- STEP 3: Testing review constraints ----------------------------------------------");
    testReviewLimit();


    System.out.println("\n-------------------------------- STEP 4: Testing endorsement constraints -------------------------------------------");
    testEndorsementLimit();


    System.out.println("\n---------------------- STEP 5: Find the writer of the top rated review procedure call-------------------------------");
    callFindTopRatedReview();


    System.out.println("\n---------------------------------- STEP 6: Testing delete constraints ----------------------------------------------");
    testDelete();

    System.out.println("\n------------ STEP 7: Find the writer of the top rated review procedure call after deleting records -----------------");
    callFindTopRatedReview();

    System.out.println("\n-------------------- STEP 8: Find candidates to receive free concession items --------------------------------------");
    callFindCustomerForFreeConcessionItem();
    try {
      conn.close();
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("\n============================================= Finished testing iRate ===============================================");
  }
}

