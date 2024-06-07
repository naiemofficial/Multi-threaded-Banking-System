import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import java.util.List;


class JDBC {
    static final String DB_NAME = "java";
    static final String DB_URL = "jdbc:mysql://localhost/" + DB_NAME;
    static final String DB_USER = "root";
    static final String DB_PASS = "";

    private Boolean isDatabaseExists() {
        try {
            Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            if (con != null) {
                con.close();
                return true;
            } else {
                System.out.println("Connection Failed");
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean createDatabase() {
        if (isDatabaseExists()) {
            return true;
        }
        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/", DB_USER, DB_PASS);
            if (con != null) {
                Statement sql = con.createStatement();
                sql.executeUpdate("CREATE DATABASE " + DB_NAME);
                con.close();
                System.out.println("Database Created Successfully");
                return true;
            } else {
                System.out.println("Connection Failed");
                return false;
            }
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    protected Map<String, String> tables = Map.of(
            "accounts", "accounts",
            "transactions", "transactions");

    private Connection EstablishConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            if (con != null) {
                // System.out.println("Database Connected");
                return con;
            } else {
                System.out.println("Connection Failed");
                return null;
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private boolean isTableExist(Connection con, String tableName) {
        try {
            Statement sql = con.createStatement();
            return sql.executeQuery("SHOW TABLES LIKE '" + tableName + "'").next();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean createTable(Connection con, String tableName, ArrayList<String> columns) {
        try {
            Statement sql = con.createStatement();
            String query = "CREATE TABLE " + tableName + " (";
            for (int i = 0; i < columns.size(); i++) {
                query += columns.get(i);
                if (i != columns.size() - 1) {
                    query += ", ";
                }
            }
            query += ")";
            sql.executeUpdate(query);
            return true;
        } catch (Exception e) {
            System.err.println("Error creating database table");
            System.err.println(e);
            return false;
        }
    }

    public void initializeTables() {
        Connection con = EstablishConnection();
        if (con != null) {
            // Create "accounts" table if doesn't exist
            if (!isTableExist(con, tables.get("accounts"))) {
                ArrayList<String> columns = new ArrayList<String>() {
                    {
                        add("account_id INT PRIMARY KEY AUTO_INCREMENT");
                        add("account_holder_name VARCHAR(255)");
                        add("balance VARCHAR(255)");
                    }
                };
                createTable(con, tables.get("accounts"), columns);
            }

            // Create transactions table if doesn't exist
            if (!isTableExist(con, tables.get("transactions"))) {
                ArrayList<String> columns = new ArrayList<String>() {
                    {
                        add("transaction_id INT PRIMARY KEY AUTO_INCREMENT");
                        add("account_id INT");
                        add("linked_account_id INT DEFAULT NULL");
                        add("transaction_type VARCHAR(255)");
                        add("amount DECIMAL(10, 2)");
                        add("timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    }
                };
                createTable(con, tables.get("transactions"), columns);
            }
        } else {
            System.out.println("Connection Failed");
        }
    }

    protected int[] insertData(String tableName, List<Map<String, Object>> dataList) {
        Connection con = EstablishConnection();
        if (con != null) {
            try {
                // Disable auto-commit mode
                con.setAutoCommit(false);

                try (Statement sql = con.createStatement()) {
                    // Prepare the batch
                    for (Map<String, Object> data : dataList) {
                        // Prepare the INSERT INTO query
                        StringBuilder keys = new StringBuilder("INSERT INTO " + tableName + " (");
                        StringBuilder values = new StringBuilder("VALUES (");
                        int counter = 0; // To add comma after each column except the last one
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            keys.append(entry.getKey());

                            Object value = entry.getValue();
                            if (value instanceof Integer || value instanceof Boolean || value instanceof Double
                                    || value instanceof Float || value instanceof Long || value instanceof Short
                                    || value instanceof Byte) {
                                values.append(value);
                            } else if (value instanceof java.sql.Timestamp) {
                                values.append("FROM_UNIXTIME(").append(value).append(")");
                            } else if (value instanceof String || value instanceof Character
                                    || value instanceof java.sql.Date || value instanceof java.sql.Time
                                    || value instanceof java.net.URL) {
                                values.append("'").append(value.toString()).append("'");
                            } else {
                                values.append("'unsupported_data_type'");
                            }

                            if (++counter < data.size()) {
                                keys.append(", ");
                                values.append(", ");
                            }
                        }
                        keys.append(")");
                        values.append(")");

                        // Add the query to the batch
                        String query = keys.toString() + " " + values.toString();
                        sql.addBatch(query);
                    }

                    // Execute batch
                    int[] affectedRows = sql.executeBatch();
                    // Commit the transaction
                    con.commit();

                    // Get generated IDs of the inserted rows
                    int[] ids = new int[affectedRows.length];
                    if (affectedRows.length > 0) {
                        try (ResultSet generatedKeys = sql.getGeneratedKeys()) {
                            for (int i = 0; i < affectedRows.length; i++) {
                                if (affectedRows[i] > 0 && generatedKeys.next()) {
                                    ids[i] = generatedKeys.getInt(1);
                                }
                            }
                            return ids;
                        } catch (SQLException e) {
                            System.err.println("Error getting generated keys");
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("No rows affected");
                        return null;
                    }
                } catch (Exception e) {
                    System.err.println("Error inserting data into table");
                    System.err.println(e);
                } finally {
                    try {
                        con.setAutoCommit(true); // Reset auto-commit mode
                        con.close(); // Close the connection
                    } catch (Exception e) {
                        System.err.println("Error closing connection");
                        System.err.println(e);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error setting auto-commit mode");
                e.printStackTrace();
            }
        } else {
            System.out.println("Data insertion failed due to database connection error");
        }
        return null;
    }

    public List<Map<String, Object>> getAccountData(String tableName, List<?> accounts) {
        Connection con = EstablishConnection();
        if (con != null) {
            try (Statement sql = con.createStatement()) {
                // Check if the list contains integers
                if (accounts.get(0) instanceof Integer) {
                    List<Map<String, Object>> data = new ArrayList<>();
                    for (Object account : accounts) {
                        String query = "SELECT * FROM " + tableName + " WHERE account_id = " + account;
                        Map<String, Object> row = new HashMap<>(); // Initialize the row map
                        try (ResultSet result = sql.executeQuery(query)) {
                            while (result.next()) {
                                row.put("account_id", result.getInt("account_id"));
                                row.put("account_holder_name", result.getString("account_holder_name"));
                                row.put("balance", result.getString("balance"));
                            }
                        } catch (SQLException e) {
                            System.err.println("Error fetching data from table");
                            e.printStackTrace();
                        }

                        // Number of transactions

                        query = "SELECT COUNT(*) FROM " + tables.get("transactions") + " WHERE account_id = " + account;
                        
                        try (ResultSet result = sql.executeQuery(query)) {
                            while (result.next()) {
                                row.put("transactions", result.getInt(1));
                            }
                        } catch (SQLException e) {
                            System.err.println("Error fetching data from table");
                            e.printStackTrace();
                        }
                        data.add(row);
                    }
                    return data;
                }

                // Check if the list contains strings
                else if (accounts.get(0) instanceof String) {
                    StringBuilder query = new StringBuilder("SELECT account_id FROM " + tableName + " WHERE ");
                    for (Object account : accounts) {
                        query.append("account_holder_name = '").append(account).append("'");
                        if (accounts.indexOf(account) != accounts.size() - 1) {
                            query.append(" OR ");
                        }
                    }

                    List<Object> account_ids = new ArrayList<>();
                    try (ResultSet result = sql.executeQuery(query.toString())) {
                        while (result.next()) {
                            account_ids.add(result.getInt("account_id"));
                        }
                    } catch (SQLException e) {
                        System.err.println("Error fetching data from table");
                        e.printStackTrace();
                    }
                    return getAccountData(tableName, account_ids);
                }

            } catch (SQLException e) {
                System.err.println("Error creating statement");
                e.printStackTrace();
            } finally {
                try {
                    con.close(); // Close the connection
                } catch (Exception e) {
                    System.err.println("Error closing connection");
                    System.err.println(e);
                }
            }
        } else {
            System.out.println("Data fetching failed due to database connection error");
        }
        return null;
    }

    public Object getColumnByID(String tableName, String getColumn, String primaryColumn, int id) {
        Connection con = EstablishConnection();
        if (con != null) {
            try (Statement sql = con.createStatement()) {
                String query = "SELECT " + getColumn + " FROM " + tableName + " WHERE " + primaryColumn + " = " + id;
                try (ResultSet result = sql.executeQuery(query)) {
                    while (result.next()) {
                        return result.getObject(getColumn);
                    }
                } catch (SQLException e) {
                    System.err.println("Error fetching data from table");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                System.err.println("Error creating statement");
                e.printStackTrace();
            } finally {
                try {
                    con.close(); // Close the connection
                } catch (Exception e) {
                    System.err.println("Error closing connection");
                    System.err.println(e);
                }
            }
        } else {
            System.out.println("Data fetching failed due to database connection error");
        }
        return null;
    }

    // method get rows by where parameter
    public List<Map<String, Object>> getRows(String tableName, String whereclause){
        Connection con = EstablishConnection();
        if (con != null) {
            try (Statement sql = con.createStatement()) {
                String query = "SELECT * FROM " + tableName + " WHERE " + whereclause;
                List<Map<String, Object>> data = new ArrayList<>();
                try (ResultSet result = sql.executeQuery(query)) {
                    while (result.next()) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("account_id", result.getInt("account_id"));
                        row.put("account_holder_name", result.getString("account_holder_name"));
                        row.put("balance", result.getString("balance"));
                        // linked_account_id
                        int linked_account_id = result.getInt("linked_account_id");
                        if (linked_account_id != 0) {
                            row.put("linked_account_id", linked_account_id);
                        }
                        data.add(row);
                    }
                    return data;
                } catch (SQLException e) {
                    System.err.println("Error fetching data from table");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                System.err.println("Error creating statement");
                e.printStackTrace();
            } finally {
                try {
                    con.close(); // Close the connection
                } catch (Exception e) {
                    System.err.println("Error closing connection");
                    System.err.println(e);
                }
            }
        } else {
            System.out.println("Data fetching failed due to database connection error");
        }
        return null;
    }

    public boolean updateColumnByID(String tableName, String columnName, Object value, int id) {
        Connection con = EstablishConnection();
        if (con != null) {
            try (Statement sql = con.createStatement()) {
                String query = "UPDATE " + tableName + " SET " + columnName + " = " + value + " WHERE account_id = "
                        + id;
                sql.executeUpdate(query);
                return true;
            } catch (SQLException e) {
                System.err.println("Error creating statement");
                e.printStackTrace();
            } finally {
                try {
                    con.close(); // Close the connection
                } catch (Exception e) {
                    System.err.println("Error closing connection");
                    System.err.println(e);
                }
            }
        } else {
            System.out.println("Data fetching failed due to database connection error");
        }
        return false;
    }

    public boolean deleteRowByID(String tableName, int id) {
        Connection con = EstablishConnection();
        if (con != null) {
            try (Statement sql = con.createStatement()) {
                String query = "DELETE FROM " + tableName + " WHERE account_id = " + id;
                sql.executeUpdate(query);
                return true;
            } catch (SQLException e) {
                System.err.println("Error creating statement");
                e.printStackTrace();
            } finally {
                try {
                    con.close(); // Close the connection
                } catch (Exception e) {
                    System.err.println("Error closing connection");
                    System.err.println(e);
                }
            }
        } else {
            System.out.println("Data fetching failed due to database connection error");
        }
        return false;
    }

    public boolean isIDExist(String tableName, int id) {
        Connection con = EstablishConnection();
        if (con != null) {
            try (Statement sql = con.createStatement()) {
                String query = "SELECT account_id FROM " + tableName + " WHERE account_id = " + id;
                try (ResultSet result = sql.executeQuery(query)) {
                    return result.next();
                } catch (SQLException e) {
                    System.err.println("Error fetching data from table");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                System.err.println("Error creating statement");
                e.printStackTrace();
            } finally {
                try {
                    con.close(); // Close the connection
                } catch (Exception e) {
                    System.err.println("Error closing connection");
                    System.err.println(e);
                }
            }
        } else {
            System.out.println("Data fetching failed due to database connection error");
        }
        return false;
    }

}