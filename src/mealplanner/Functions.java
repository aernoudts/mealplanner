package mealplanner;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.io.FileWriter;

public class Functions {


    public static Connection getConnection() throws SQLException {
        String DB_URL = "jdbc:postgresql:meals_db";
        String USER = "postgres";
        String PASS = "1111";
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void createTables() throws SQLException {
        Connection connection = getConnection();
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
    }

    public static void add(List<List> items, HashMap<String, String[]> ingList, int mealId, int ingId) throws SQLException {
        Connection connection = getConnection();
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
            statement.executeUpdate("INSERT INTO meals (meal_id, meal, category) VALUES (nextval('sequence_meals'), '" +
                    mealName + "', " +
                    "'" + mealType + "')");
            PreparedStatement ps = connection.prepareStatement("SELECT MAX(meal_id) AS max_id FROM meals");
            ResultSet rs = ps.executeQuery();
            int maxID = 0;
            if (rs.next()) {
                maxID = rs.getInt("max_id");
            }
            for (String ingredient : ingredientsArray) {
                statement.executeUpdate("INSERT INTO ingredients (ingredient, ingredient_id, meal_id) VALUES (" + "'" + ingredient + "', nextval('sequence_ings'), " + maxID + ")");
            }
            System.out.println("The meal has been added!");
            break;
        }
    }

    public static void show(String category) throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(true);
        PreparedStatement statementCategory = connection.prepareStatement("SELECT * FROM meals WHERE category = '" + category + "'");
        ResultSet rs = statementCategory.executeQuery();
        PreparedStatement statementCount = connection.prepareStatement("SELECT COUNT (*) AS total FROM meals WHERE category = '" + category + "'");
        ResultSet rsCount = statementCount.executeQuery();
        rsCount.next();
        int rowCount = rsCount.getInt("total");
        if (rowCount == 0) {
            System.out.println("No meals found.");
            return;
        }
        System.out.println("Category: " + category + "\n");
        while (rs.next()) {
            PreparedStatement statementIngredient = connection.prepareStatement("SELECT * FROM ingredients WHERE meal_id = " + rs.getString("meal_id"));
            ResultSet rs2 = statementIngredient.executeQuery();
            System.out.println("Name: " + rs.getString("meal"));
            System.out.println("Ingredients:");
            while (rs2.next()) {
                System.out.println(rs2.getString("ingredient").strip());
            }
            System.out.println("");
        }

        statementCategory.close();
        connection.close();
    }

    public static String mealCategory() {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            Set<String> categories = Set.of("breakfast", "lunch", "dinner");
            System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
            String category = scanner.nextLine();
            if (!categories.contains(category.toLowerCase())) {
                System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            } else {
                return category;
            }
        }
    }

    public static void plan() throws SQLException {
        Connection connection = getConnection();
        connection.setAutoCommit(true);
        Scanner scanner = new Scanner(System.in);
        String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String weekday : weekdays) {
            System.out.println(weekday);
            listBreakfastMeals();
            System.out.println("Choose the breakfast for " + weekday + " from the list above:");
            String breakfast = scanner.nextLine();
            while (getMealID(breakfast) == -1) {
                System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
                breakfast = scanner.nextLine();
            }
            insertMealPlan(getMealID(breakfast), breakfast, "breakfast");

            listLunchMeals();
            System.out.println("Choose the lunch for " + weekday + " from the list above:");
            String lunch = scanner.nextLine();
            while (getMealID(lunch) == -1) {
                System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
                lunch = scanner.nextLine();
            }
            insertMealPlan(getMealID(lunch), lunch, "lunch");

            listDinnerMeals();
            System.out.println("Choose the dinner for " + weekday + " from the list above:");
            String dinner = scanner.nextLine();
            while (getMealID(dinner) == -1) {
                System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
                dinner = scanner.nextLine();
            }
            insertMealPlan(getMealID(dinner), dinner, "dinner");

            System.out.println("Yeah! We planned the meals for " + weekday + ".\n");
        }
        printMealsForTheWeek();
    }

    public static void listBreakfastMeals() throws SQLException {
        Connection conn = getConnection();
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM MEALS ORDER BY MEAL");
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            String category = rs.getString("category");
            if (category.equals("breakfast")) {
                String meal = rs.getString("meal");
                System.out.println(meal);
            }
        }
    }

    public static void listLunchMeals() throws SQLException {
        Connection conn = getConnection();
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM MEALS ORDER BY MEAL");
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            String category = rs.getString("category");
            if (category.equals("lunch")) {
                String meal = rs.getString("meal");
                System.out.println(meal);
            }
        }
    }

    public static void listDinnerMeals() throws SQLException {
        Connection conn = getConnection();
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM MEALS ORDER BY MEAL");
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            String category = rs.getString("category");
            if (category.equals("dinner")) {
                String meal = rs.getString("meal");
                System.out.println(meal);
            }
        }
    }

    public static int getMealID(String userMealInput) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement statement = conn.prepareStatement("SELECT * FROM MEALS");
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            int meal_id = rs.getInt("meal_id");
            String mealName = rs.getString("meal");

            if (mealName.equals(userMealInput)) {
                return meal_id;
            }
        }
        return -1;
    }

    public static void insertMealPlan(int meal_id, String mealName, String type) throws SQLException {
        Connection conn = getConnection();

        String sqlBreakfast = "INSERT INTO PLAN (meal_id, category, meal) VALUES (?, ?, ?)";
        PreparedStatement statement = conn.prepareStatement(sqlBreakfast);

        statement.setInt(1, meal_id);
        statement.setString(2, type);
        statement.setString(3, mealName);

        statement.executeUpdate();
        conn.close();
    }

    public static void printMealsForTheWeek() throws SQLException {
        String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        Connection conn = getConnection();
        PreparedStatement breakfastStatement = conn.prepareStatement("SELECT * FROM PLAN WHERE CATEGORY = 'breakfast'");
        PreparedStatement lunchStatement = conn.prepareStatement("SELECT * FROM PLAN WHERE CATEGORY = 'lunch'");
        PreparedStatement dinnerStatement = conn.prepareStatement("SELECT * FROM PLAN WHERE CATEGORY = 'dinner'");

        ResultSet rsBreakfast = breakfastStatement.executeQuery();
        ResultSet rsLunch = lunchStatement.executeQuery();
        ResultSet rsDinner = dinnerStatement.executeQuery();

        for (String day : weekdays) {
            System.out.println(day);
            rsBreakfast.next();
            rsLunch.next();
            rsDinner.next();
            String breakfast = rsBreakfast.getString("meal");
            String lunch = rsLunch.getString("meal");
            String dinner = rsDinner.getString("meal");
            System.out.println("Breakfast: " + breakfast);
            System.out.println("Lunch: " + lunch);
            System.out.println("Dinner: " + dinner + "\n");
        }
    }
    public static void save() throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in);
        Connection conn = getConnection();
        PreparedStatement checkPlan = conn.prepareStatement("SELECT * FROM plan");
        PreparedStatement checkId = conn.prepareStatement("SELECT meal_id FROM plan");
        ResultSet rsPlan = checkPlan.executeQuery();
        ResultSet rsId = checkId.executeQuery();
        if (rsPlan.next() == false) {
            System.out.println("Unable to save. Plan your meals first.");
            return;
        }
        ArrayList<Integer> idList = new ArrayList<>();
        HashMap<String, Integer> ingList = new HashMap<>();
        System.out.println("Input a filename:");
        File file = new File(scanner.nextLine());
        FileWriter writer = new FileWriter(file, true);
        while (rsId.next()) {
            int mealId = rsId.getInt("meal_id");
            idList.add(mealId);
        }
        for (int id : idList) {
            PreparedStatement checkIngredients = conn.prepareStatement("SELECT ingredient FROM ingredients WHERE meal_id = " + id);
            ResultSet rsIngs = checkIngredients.executeQuery();
            while (rsIngs.next()) {
                String hashMapIngredient = rsIngs.getString("ingredient");
                if (ingList.containsKey(hashMapIngredient)) {
                    ingList.put((hashMapIngredient), ingList.get(hashMapIngredient) + 1);
                } else {
                    ingList.put((hashMapIngredient), 1);
                }
            }
        }

        for (String ing : ingList.keySet()) {
            if (ingList.get(ing) > 1) {
                writer.write(ing + " x" +  ingList.get(ing) + "\n");
            } else {
                writer.write(ing + "\n");
            }
        }
        writer.close();
        System.out.println("Saved!");
    }
}
