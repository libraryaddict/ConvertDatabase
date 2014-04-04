package me.libraryaddict.convert;

public class TableInfo {
    private String database;
    private String table;
    private String userField;

    public TableInfo(String database, String table, String userField) {
        this.database = database;
        this.table = table;
        this.userField = userField;
    }

    public String getDatabase() {
        return database;
    }

    public String getTable() {
        return table;
    }

    public String getUserField() {
        return userField;
    }
}
