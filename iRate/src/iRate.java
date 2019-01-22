import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This program creates a iRate database, which is a database for Managing Movie Ratings. There are
 * entity tables for Customer, Movie, and relationship tables for the Attendance, Review,
 * Endorsement.
 *
 * This project is a portion of a an application that enables registered movie theater customers to
 * rate a movie that they saw at the theater, and for other registered customers to vote for
 * reviews.
 *
 * @author Xiaojin(Alice) He and Qimin(Karen) Cao
 */
public class iRate {

  // JDBC driver name, database URL, credentials, dbname
  static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
  static final String DB_URL = "jdbc:mysql://localhost?useSSL=false&allowPublicKeyRetrieval=true";
  static final String USER = "root";
  static final String PSW = "";
  static final String dbname = "iRate";

  // the connection object conn which is used to connect database
  static Connection conn;
  // the statement object stmt which is a channel for sending command through connection
  static Statement stmt;


  /**
   * This method is used to create tables in iRate, it includes two entity tables Customer and
   * Movie, and three relational tables Attendance, Review, Endorsement.
   *
   * Customer is a registered customer of the theater. Movie is a record of a movie playing at the
   * theater. Attendance is a record of a movie seen by a customer on a given date. Review is a
   * review of a particular movie attended by a customer within the last week. Endorsement is an
   * endorsement of a movie review by a customer.
   */
  static void createTables() {
    try {
      // tables created by this program
      String dbTables[] = {
              "Attendance", "Endorsement", "Review", "Customer", "Movie"
      };

      // drop the database tables and recreate them below
      for (String tbl : dbTables) {
        try {
          stmt.executeUpdate("DROP TABLE IF EXISTS " + tbl);
          System.out.println("Dropped table " + tbl);
        } catch (SQLException ex) {
          System.out.println("Did not drop table " + tbl);
        }
      }

      // create Customer table
      String createTable_Customer =
              "CREATE TABLE Customer (" +
                      "  uid INT NOT NULL AUTO_INCREMENT," +
                      "  first_name VARCHAR(100) NOT NULL," +
                      "  last_name VARCHAR(100) NOT NULL," +
                      "  email VARCHAR(100) NOT NULL UNIQUE," +
                      "  join_date DATE NOT NULL," +
                      "  PRIMARY KEY (uid));";
      stmt.executeUpdate(createTable_Customer);
      System.out.println("Created entity table Customer");

      // create Movie table
      String createTable_Movie =
              "CREATE TABLE Movie (" +
                      "  id INT NOT NULL AUTO_INCREMENT," +
                      "  title VARCHAR(100) NOT NULL," +
                      "  PRIMARY KEY (id))";
      stmt.executeUpdate(createTable_Movie);
      System.out.println("Created entity table Movie");

      // create Attendance table
      String createTable_Attendance =
              "CREATE TABLE Attendance (" +
                      "  customer_uid INT NOT NULL," +
                      "  movie_id INT NOT NULL," +
                      "  watch_date DATE NOT NULL," +
                      "  PRIMARY KEY (customer_uid, movie_id, watch_date)," +
                      "  FOREIGN KEY (customer_uid) REFERENCES Customer(uid) ON DELETE CASCADE," +
                      "  FOREIGN KEY (movie_id) REFERENCES Movie(id) ON DELETE CASCADE" +
                      ")";
      stmt.executeUpdate(createTable_Attendance);
      System.out.println("Created entity table Attendance");

      // create Review table
      String createTable_Review =
              "CREATE TABLE Review (" +
                      "  id INT NOT NULL AUTO_INCREMENT," +
                      "  movie_id INT NOT NULL," +
                      "  customer_uid INT NOT NULL," +
                      "  rating ENUM('0 stars', '1 stars', '2 stars', '3 stars', '4 stars', '5 stars') NOT NULL," +
                      "  review TEXT NOT NULL," +
                      "  review_date DATE NOT NULL," +
                      "  PRIMARY KEY (id)," +
                      "  UNIQUE(movie_id, customer_uid)," +
                      "  FOREIGN KEY (customer_uid) REFERENCES Customer(uid) ON DELETE CASCADE," +
                      "  FOREIGN KEY (movie_id) REFERENCES Movie(id) ON DELETE CASCADE)";
      stmt.executeUpdate(createTable_Review);
      System.out.println("Created entity table Review");

      // create Endorsement table
      String createTable_Endorsement =
              "CREATE TABLE Endorsement (" +
                      "  customer_uid INT NOT NULL," +
                      "  review_id INT NOT NULL," +
                      "  endorse_date DATE NOT NULL," +
                      "  PRIMARY KEY (customer_uid, review_id, endorse_date)," +
                      "  FOREIGN KEY (customer_uid) REFERENCES Customer(uid) ON DELETE CASCADE," +
                      "  FOREIGN KEY (review_id) REFERENCES Review(id) ON DELETE CASCADE)";
      stmt.executeUpdate(createTable_Endorsement);
      System.out.println("Created entity table Endorsement");
    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }


  /**
   * This method is used to create a procedure findTopEndorsedReview. This procedure allows theater
   * to find a top rated review of a movie written three days.
   */
  static void createProcedure() {
    // procedures created by this program
    String dbProcedures[] = {
            "findTopEndorsedReview", "findCustomerForFreeConcessionItem"
    };

    // drop the database procedures and recreate them below
    for (String procedure : dbProcedures) {
      try {
        stmt.execute("DROP PROCEDURE IF EXISTS " + procedure);
        System.out.println("Dropped procedure " + procedure);
      } catch (SQLException ex) {
        System.out.println("Did not drop procedure " + procedure);
      }
    }

    try {
      // This procedure is used to find the writer of the top rated review of a movie written three days earlier.
      // The chosen customer receives a free movie ticket.
      String createProcedure_findTopEndorsedReview =
              "CREATE PROCEDURE findTopEndorsedReview(IN find_date TEXT) " +
                      "BEGIN " +
                      "SELECT e.review_id, count(*) AS endCnt INTO @rid, @cnt " +
                      "FROM Endorsement e INNER JOIN Review r ON e.review_id = r.id " +
                      "WHERE r.review_date + INTERVAL 3 DAY = STR_TO_DATE(find_date, '%Y-%m-%d') " +
                      "GROUP BY e.review_id " +
                      "ORDER BY endCnt DESC LIMIT 1;" +
                      "SELECT Customer.uid, Customer.first_name, Customer.last_name, Customer.email " +
                      "FROM Customer INNER JOIN Review ON Customer.uid = Review.customer_uid WHERE Review.id = @rid;" +
                      "END;";
      stmt.executeUpdate(createProcedure_findTopEndorsedReview);
      System.out.println("Created procedure findTopEndorsedReview");

      // This procedure is used to find customers who voted one or more movie reviews as "helpful" on a given day.
      // The chosen customer receives a free concession item.
      String createProcedure_findCustomerForFreeConcessionItem =
              "CREATE PROCEDURE findCustomerForFreeConcessionItem(IN find_date TEXT) " +
                      "BEGIN " +
                      "  SELECT Customer.uid, Customer.first_name, Customer.last_name, Customer.email " +
                      "  FROM (Customer " +
                      "  INNER JOIN (SELECT * FROM Endorsement WHERE find_date = Endorsement.endorse_date) AS temp" +
                      "  ON Customer.uid = temp.customer_uid);" +
                      "END;";
      stmt.executeUpdate(createProcedure_findCustomerForFreeConcessionItem);
      System.out.println("Created procedure findCustomerForFreeConcessionItem");
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
      ex.printStackTrace();
    }
  }


  /**
   * This method is used to create triggers, it includes three triggers, which are customer_limit,
   * review_limit and endorse_limit.
   */
  static void createTriggers() {
    // triggers created by this program
    String dbTriggers[] = {
            "customer_limit", "review_limit", "endorse_limit"
    };

    // drop the database triggers and recreate them below
    for (String tgr : dbTriggers) {
      try {
        stmt.executeUpdate("DROP TRIGGER IF EXISTS " + tgr);
        System.out.println("Dropped trigger " + tgr);
      } catch (SQLException ex) {
        System.out.println("Did not drop trigger " + tgr);
      }
    }

    try {

       /*
       Create trigger customer_limit
       Constrain:
       if a email is invalid, can not insert data to customer
       */
      String createTrigger_customer_limit =
              "CREATE TRIGGER customer_limit BEFORE INSERT ON Customer FOR EACH ROW" +
                      "  BEGIN" +
                      "    IF (NEW.email NOT REGEXP '^[[:alnum:]._%-\\+]+@[[:alnum:].-]+[.][[:alnum:]]{2,4}$')" +
                      "    THEN" +
                      "      SIGNAL SQLSTATE VALUE '45000' SET MESSAGE_TEXT = 'Warning: Invalid email.';" +
                      "    END IF;" +
                      "  END;";
      stmt.executeUpdate(createTrigger_customer_limit);
      System.out.println("Created customer_limit trigger for Review");

      /*
       Create trigger review_limit
       Constraints:
       1) if a customer did not attend a movie, he/she cannot review it.
       2) the date of the review must be within 7 days of the most recent attendance of the movie.
       3) the date of the review must be within 7 days of the most recent attendance of the movie
       */
      String createTrigger_review_limit =
              "CREATE TRIGGER review_limit BEFORE INSERT ON Review FOR EACH ROW" +
                      "  BEGIN" +
                      "    IF (NEW.customer_uid NOT IN" +
                      "        (SELECT Attendance.customer_uid" +
                      "         FROM Attendance" +
                      "         WHERE Attendance.movie_id = New.movie_id))" +
                      "       OR" +
                      "       (NEW.review_date <" +
                      "        (SELECT watch_date" +
                      "         FROM Attendance" +
                      "         WHERE Attendance.customer_uid = NEW.customer_uid" +
                      "         AND Attendance.movie_id = New.movie_id" +
                      "         GROUP BY watch_date " +
                      "         ORDER BY watch_date ASC LIMIT 1))" +
                      "       OR" +
                      "       (NEW.review_date >" +
                      "        (SELECT watch_date" +
                      "         FROM Attendance" +
                      "         WHERE Attendance.customer_uid = NEW.customer_uid" +
                      "         AND Attendance.movie_id= New.movie_id" +
                      "         GROUP BY watch_date " +
                      "         ORDER BY watch_date DESC LIMIT 1) + INTERVAL 7 DAY)" +
                      "    THEN" +
                      "      SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = 'Warning: Can only review after watching the movie and within 7 days.';" +
                      "    END IF;" +
                      "  END;";
      stmt.executeUpdate(createTrigger_review_limit);
      System.out.println("Created review_limit trigger for Review");

       /*
      Create trigger endorse_limit
      Constraints:
      1) if the customer is the one who wrote the review, cannot endorse
      2) if the endorse_date is after 3 days when the review was written, cannot endorse
         (endorsing is closed for all reviews of a movie written three days ago)
      3) if the customer has endorsed one review of a particular movie on the endorse_date, cannot endorse second time
         (Customers can endorse one review of a particular movie each day.)
         */
      String createTrigger_endorse_limit =
              "CREATE TRIGGER endorse_limit BEFORE INSERT ON Endorsement FOR EACH ROW" +
                      "  BEGIN" +
                      "    IF (NEW.customer_uid IN" +
                      "          (SELECT customer_uid" +
                      "           FROM Review" +
                      "           WHERE NEW.review_id = Review.id))" +
                      "    OR (NEW.endorse_date > " +
                      "          (SELECT review_date" +
                      "           FROM Review" +
                      "           WHERE NEW.review_id = Review.id) + INTERVAL 3 DAY)" +
                      "    OR (SELECT movie_id" +
                      "        FROM Review" +
                      "        WHERE Review.id = NEW.review_id) IN" +
                      "          (SELECT movie_id" +
                      "           FROM Review" +
                      "           WHERE Review.id IN" +
                      "             (SELECT review_id" +
                      "              FROM Endorsement" +
                      "              WHERE NEW.customer_uid = Endorsement.customer_uid" +
                      "              AND NEW.endorse_date = Endorsement.endorse_date))" +
                      "    THEN" +
                      "      SIGNAL SQLSTATE '02000' SET MESSAGE_TEXT = 'Warning: Endorsement failed.';" +
                      "    END IF;" +
                      "  END;";
      stmt.executeUpdate(createTrigger_endorse_limit);
      System.out.println("Created trigger endorse_limit for Endorsement");

    } catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try {
      System.out.println("============================================ Start building iRate schema ===========================================\n");

      // connect to the database using URL
      Class.forName(JDBC_DRIVER);

      // Open a connection
      System.out.println("------------------------------------------ STEP 1: Connecting to database ------------------------------------------");
      conn = DriverManager.getConnection(DB_URL, USER, PSW);

      // Execute a query
      System.out.println("-------------------------------------------- STEP 2: Creating database ---------------------------------------------");
      stmt = conn.createStatement();

      // Create database iRate
      String sql = "CREATE DATABASE IF NOT EXISTS " + dbname;
      stmt.executeUpdate(sql);
      System.out.println("Database " + dbname + " is created successfully.");
      // statement is channel for sending commands through connection
      stmt.executeUpdate("USE " + dbname);

      // invoke createTables() method to create tables
      System.out.println("\n-------------------------- STEP 3: Dropping and recreating tables in the iRate database ----------------------------");
      createTables();

      // invoke createTriggers() method to create triggers
      System.out.println("\n------------------------- STEP 4: Dropping and recreating triggers in the iRate database ---------------------------");
      createTriggers();

      // invoke createProcedure() method to create procedure
      System.out.println("\n------------------------ STEP 5: Dropping and recreating procedures in the iRate database --------------------------");
      createProcedure();


      System.out.println("\n========================================== Finished building iRate schema ==========================================");
      conn.close();
      stmt.close();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

}

