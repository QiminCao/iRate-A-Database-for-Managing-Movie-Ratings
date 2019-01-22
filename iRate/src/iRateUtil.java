import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class contains functions for printing Customer,
 * Movie, Attendance, Review and Endorsement in the iRate database.
 *
 * @author Xiaojin(ALice) He and Qimin(Karen) Cao
 *
 */
public class iRateUtil {

  /**
   * Print Customer table.
   * @param conn the connection
   * @return number of customers
   * @throws SQLException if a database operation fails
   */
  static int printCustomer(Connection conn) throws SQLException {
    int count = 0;

    try {
      Statement stmt = conn.createStatement();
      // list customers with their email and join date
      ResultSet rs = stmt.executeQuery("select uid, first_name, last_name, email, join_date from Customer");

      System.out.println("Customers:");
      while (rs.next()) {
        int uid = rs.getInt(1);
        String first_name = rs.getString(2);
        String last_name = rs.getString(3);
        String email = rs.getString(4);
        String date = rs.getString(5);
        System.out.printf("  %d. %s (%s) %s %s\n", uid, first_name, last_name, email, date);
        count++;
      }


    } catch (SQLException e) {
      e.printStackTrace();
    }
    return count;
  }

  /**
   * Print Movie table.
   * @param conn the connection
   * @return number of movies
   * @throws SQLException if a database operation fails
   */
  static int printMovie(Connection conn) throws SQLException {
    int count = 0;

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("select id, title from Movie");

      System.out.println("Movies:");
      while (rs.next()) {
        int id = rs.getInt(1);
        String movie_title = rs.getString(2);
        System.out.printf("  %d. %s\n", id, movie_title);
        count++;
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return count;
  }

  /**
   * Print Attendance table.
   * @param conn the connection
   * @return number of attendances
   * @throws SQLException if a database operation fails
   */
  static int printAttendance(Connection conn) throws SQLException {
    int count = 0;

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("select customer_uid, movie_id, watch_date from Attendance");

      System.out.println("Attendances:");
      while (rs.next()) {
        int customer_uid = rs.getInt(1);
        int movie_id = rs.getInt(2);
        String watch_date = rs.getString(3);
        System.out.printf("  Customer(%d) Movie(%d) %s\n", customer_uid, movie_id, watch_date);
        count++;
      }


    } catch (SQLException e) {
      e.printStackTrace();
    }
    return count;
  }

  /**
   * Print Review table.
   * @param conn the connection
   * @return number of reviews
   * @throws SQLException if a database operation fails
   */
  static int printReview(Connection conn) throws SQLException {
    int count = 0;

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("select id, movie_id, customer_uid, rating, review, review_date from Review");

      System.out.println("Reviews:");
      while (rs.next()) {
        int id = rs.getInt(1);
        int movie_id = rs.getInt(2);
        int customer_uid = rs.getInt(3);
        String rating = rs.getString(4);
        String review = rs.getString(5);
        String review_date = rs.getString(6);
        System.out.printf("  %d. Movie(%d) Customer(%d) %s %s %s\n", id, movie_id, customer_uid, rating, review, review_date);
        count++;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return count;
  }

  /**
   * Print Endorsement table.
   * @param conn the connection
   * @return number of endorsements
   * @throws SQLException if a database operation fails
   */
  static int printEndorsement(Connection conn) throws SQLException {
    int count = 0;

    try {
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery("select customer_uid, review_id, endorse_date from Endorsement");

      System.out.println("Endorsements:");
      while (rs.next()) {
        int customer_uid = rs.getInt(1);
        int review_id = rs.getInt(2);
        String endorse_date = rs.getString(3);
        System.out.printf("  Customer(%d) Review(%d) %s\n", customer_uid, review_id, endorse_date);
        count++;
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return count;
  }
}
