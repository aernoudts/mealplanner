package mealplanner;

import java.io.IOException;
import java.util.*;
import java.sql.*;
import static mealplanner.Functions.*;

public class Main {
  public static void main(String[] args) throws SQLException, IOException {
    int mealId = 1;
    int ingId = 1;

    createTables();

    Scanner scanner = new Scanner(System.in);
    List<List> items = new ArrayList<>();
    HashMap<String, String[]> ingList = new HashMap<>();
    while (true) {
      System.out.println("What would you like to do (add, show, plan, save, exit)?");
      String action = scanner.nextLine();
      if (action.equals("add")) {
        add(items, ingList, mealId, ingId);
        mealId++;

      } else if (action.equals("show")) {
        show(mealCategory());

      } else if (action.equals("plan")) {
        plan();

      } else if (action.equals("save")) {
        save();

      } else if (action.equals("exit")) {
        System.out.println("Bye!");
        break;
      }
    }
  }
}
