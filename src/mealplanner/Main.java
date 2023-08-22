package mealplanner;

import java.util.*;
import java.sql.*;
import static mealplanner.Functions.*;

public class Main {
  public static void main(String[] args) throws SQLException {
    Connection connection = getConnection();
    connection.setAutoCommit(true);
    int mealId = 1;
    int ingId = 1;
    Statement statement = connection.createStatement();
    statement.executeUpdate("CREATE SEQUENCE IF NOT EXISTS sequence_meals");
    statement.executeUpdate("CREATE SEQUENCE IF NOT EXISTS sequence_ings");

    statement.executeUpdate("CREATE TABLE IF NOT EXISTS meals " +
            "(category VARCHAR, " +
            "meal VARCHAR, " +
            "meal_id INT PRIMARY KEY)");

    statement.executeUpdate("CREATE TABLE IF NOT EXISTS ingredients " +
            "(ingredient VARCHAR, " +
            "ingredient_id INT PRIMARY KEY, " +
            "meal_id INT)");

    statement.executeUpdate("CREATE TABLE IF NOT EXISTS plan " +
            "(category VARCHAR, " +
            "meal VARCHAR, " +
            "meal_id INT)");

    Scanner scanner = new Scanner(System.in);
    List<List> items = new ArrayList<>();
    HashMap<String, String[]> ingList = new HashMap<>();
    while (true) {
      System.out.println("What would you like to do (add, show, plan, exit)?");
      String action = scanner.nextLine();
      if (action.toLowerCase().equals("add")) {
        add(items, ingList, mealId, ingId);
        mealId++;

      } else if (action.toLowerCase().equals("show")) {
        show(mealCategory());

      } else if (action.toLowerCase().equals("plan")) {
        plan();

      } else if (action.toLowerCase().equals("exit")) {
        System.out.println("Bye!");
        break;
      }
    }
  }
}