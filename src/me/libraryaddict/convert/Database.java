package me.libraryaddict.convert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class Database {
    private static Connection connection;
    private static String mysqlHost, mysqlUsername, mysqlPassword, mysqlDatabase;
    private static ArrayList<TableInfo> tableInfos = new ArrayList<TableInfo>();

    public static void addTable(String databaseName, String tableName, String userField) {
        tableInfos.add(new TableInfo(databaseName, tableName, userField));
    }

    private static Connection connectMysql() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String conn = "jdbc:mysql://" + mysqlHost;
            return DriverManager.getConnection(conn, mysqlUsername, mysqlPassword);
        } catch (Exception ex) {
            System.err.println("[LimitCreative] Unknown error while fetching MySQL connection. Is the mysql details correct? "
                    + ex.getMessage());
        }
        return null;
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Connection getConnection() {
        try {
            if (connection == null) {
                connection = connectMysql();
                Statement stmt = connection.createStatement();
                stmt.execute("CREATE DATABASE IF NOT EXISTS TestDB");
                stmt.execute("CREATE TABLE IF NOT EXISTS `" + mysqlDatabase + "`.`NameStorage` ( `uuid` varchar(50) NOT NULL, "
                        + "`name` varchar(16) NOT NULL,UNIQUE KEY `uuid` (`uuid`))");
                return connection;
            }
            try {
                connection.createStatement().execute("DO 1");
            } catch (Exception ex) {
                connection = connectMysql();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return connection;
    }

    public static void onLogin(String uuid, String playername) {
        try {
            Statement stmt = getConnection().createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT name FROM `" + mysqlDatabase + "`.`NameStorage` WHERE uuid = '" + uuid + "'");
            rs.beforeFirst();
            if (rs.next()) {
                rs.close();
                String oldName = rs.getString("name");
                if (!oldName.equals(playername)) {
                    System.out.print(playername + " changed their name from " + oldName + ". Now converting the database");
                    for (TableInfo tableInfo : tableInfos) {
                        stmt.execute("UPDATE `" + tableInfo.getDatabase() + "`.`" + tableInfo.getTable() + "` SET `"
                                + tableInfo.getUserField() + "` = '" + playername + "' WHERE `" + tableInfo.getDatabase() + "`.`"
                                + tableInfo.getTable() + "`.`" + tableInfo.getUserField() + "` = '" + oldName + "';");
                    }
                    stmt.execute("UPDATE `" + mysqlDatabase + "`.`NameStorage` SET `name` = '" + playername + "' WHERE `"
                            + mysqlDatabase + "`.`NameStorage`.`name` = '" + oldName + "';");
                }
            } else {
                rs.close();
                stmt.execute("INSERT INTO `" + mysqlDatabase + "`.`NameStorage` (`uuid`, `name`) VALUES ('" + uuid + "', '"
                        + playername + "');");
            }
            stmt.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setMysql(String host, String username, String password, String database) {
        mysqlHost = host;
        mysqlUsername = username;
        mysqlPassword = password;
        mysqlDatabase = database;
    }

}
