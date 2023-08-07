package mealplanner;

import java.sql.*;
import java.util.*;

public class Functions {

    public static void add(List<List> items, HashMap<String, String[]> ingList, int mealId, int ingId) throws SQLException {
        String DB_URL = "jdbc:postgresql:meals_db";
        String USER = "postgres";
        String PASS = "1111";
        Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
        connection.setAutoCommit(true);
        Statement statement = connection.createStatement();
        Scanner scanner = new Scanner(System.in);
        Set<String> types = Set.of("breakfast", "lunch", "dinner");
        String mealType;
        String mealName;
        String mealIngredients;
        while (true) {
            System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
            try {
                mealType = scanner.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Wrong format. Use letters only.");
                continue;
            }
            if (!types.contains(mealType.toLowerCase())) {
                System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
                continue;
            }

            System.out.println("Input the meal's name:");
            while (true) {
                mealName = scanner.nextLine();
                if (!mealName.matches("^[ A-Za-z]+$") || (mealName.length() == 0)) {
                    System.out.println("Wrong format. Use letters only!");
                } else {
                    break;
                }
            }

            System.out.println("Input the ingredients: ");
            while (true) {
                mealIngredients = scanner.nextLine();
                boolean valid = true;
                String[] ingredientsArray = mealIngredients.split(",");
                for (String ingredient : ingredientsArray) {
                    String trimmedIngredient = ingredient.trim();
                    if (!trimmedIngredient.matches("[a-zA-Z]+[a-zA-Z\\s*]*")) {
                        System.out.println("Wrong format. Use letters only!");
                        valid = false;
                    }
                }
                if (valid) {
                    break;
                }
            }
            List<String> toAdd = Arrays.asList(mealType, mealName);
            String[] ingredientsArray = mealIngredients.split(",");
            ingList.put(mealName, ingredientsArray);
            items.add(toAdd);
            statement.executeUpdate("insert into meals (meal_id, meal, category) values (" +
                    mealId + ", " +
                    "'" + mealName + "', " +
                    "'" + mealType + "')");
            for (String ingredient : ingredientsArray) {
                statement.executeUpdate("INSERT INTO ingredients (ingredient, ingredient_id, meal_id) VALUES (" + "'" + ingredient + "', " + ingId + ", " + mealId + ")");
                ingId++;
            }
            System.out.println("The meal has been added!");
            break;
        }
    }
    public static void show() throws SQLException {
        String DB_URL = "jdbc:postgresql:meals_db";
        String USER = "postgres";
        String PASS = "1111";
        Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
        connection.setAutoCommit(true);
        int meal_id = 1;
        PreparedStatement statementMeal = connection.prepareStatement("SELECT * FROM meals");
        ResultSet rs = statementMeal.executeQuery();
        while (rs.next()) {
            PreparedStatement statementIngredient = connection.prepareStatement("SELECT * FROM ingredients WHERE meal_id = " + meal_id);
            ResultSet rs2 = statementIngredient.executeQuery();
            System.out.println("Category: " + rs.getString("category"));
            System.out.println("Name: " + rs.getString("meal"));
            System.out.println("Ingredients:");
            while (rs2.next()) {
                System.out.println(rs2.getString("ingredient").strip());
            }
            System.out.println("");
            meal_id++;
        }
        statementMeal.close();
        connection.close();
    }
}
