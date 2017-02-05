package com.lasagna.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.Collection;
import javax.json.*;
import java.io.*;
import java.net.URLDecoder;

public class DataHandler extends HttpServlet {
    DBUtil db;
    String dbName = "trainingdbtest";
    String TableName = "pages_table_test2";
    String LockColumn = "ad_NR";
    int RequestLimitNum = 15;

	public DataHandler() {
		super();
	}
	
	public void destroy() {
		super.destroy(); 
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        try {
            this.db.connectDB(this.dbName);
            String query = ("SELECT `page_id`, `page_url` FROM $tablename$  WHERE `tag` IS NULL AND $lockcolumn$ <> 1 LIMIT ?")
                .replace("$tablename$", this.TableName)
                .replace("$lockcolumn$", this.LockColumn);
            String breakLockQuery = ("SELECT `page_id`, `page_url` from $tablename$ WHERE `tag` IS NULL LIMIT ?")
                .replace("$tablename$", this.TableName);
            String LockQuery = ("UPDATE $tablename$ SET $lockcolumn$ = 1 WHERE `page_id` = ?")
                .replace("$tablename$", this.TableName)
                .replace("$lockcolumn$", this.LockColumn);
            ResultSet rs = db.query(query, this.RequestLimitNum);
            rs.beforeFirst();
            if(!rs.next()){
                rs = this.db.query(breakLockQuery, this.RequestLimitNum);
            }
            rs.beforeFirst();

            JsonObjectBuilder modelBuilder = Json.createObjectBuilder();
            while(rs.next()) {
                String pid = rs.getString("page_id");
                String purl = rs.getString("page_url");
                modelBuilder.add(pid, purl);
                // lock 
                db.update(LockQuery, Integer.valueOf(pid));
            }
            JsonObject model = modelBuilder.build();

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            // ===== Test
            //System.out.println("Now come the output...");
            //System.out.println(model.toString());
            //System.out.println("End output...");
            // ===== END Test
            out.write(model.toString());
            out.close();
            this.db.closeDBConn();
        }catch(Exception e){
            e.printStackTrace();
            return;
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        try {
            this.db.connectDB(this.dbName);
            String jsonstr = request.getParameter("result");
            String updateSql = ("UPDATE $tablename$ SET `tag` = ? where `page_id` = ?")
                .replace("$tablename$", this.TableName);
            JsonObject model = Json.createReader(new StringReader(jsonstr)).readObject();
            for(String id : model.keySet()) {
                System.out.print("key: " + id + ", ");
                String tag = model.getString(id);
                System.out.println("value: " + tag);
                db.insert(updateSql, tag, Integer.valueOf(id));
            }
            this.db.closeDBConn();
        }catch(Exception e){
            e.printStackTrace();
            return;
        }
    }

    public void init() throws ServletException {
        db = new DBUtil();
    }
}

class DBUtil {
    private Connection connection;
    private String driver;
    private String url;
    private String urlBase;
    private String username;
    private String password;
    private String databaseName;

    public DBUtil() {
        this.username = "root";
        this.password = "root";
        this.urlBase = "jdbc:mysql://localhost:3306/";
        this.driver = "com.mysql.jdbc.Driver";
    }

    public DBUtil(String DBuser, String DBpasswd){
        this.username = DBuser;
        this.password = DBpasswd;
        this.urlBase = "jdbc:mysql://localhost:3306/";
        this.driver = "com.mysql.jdbc.Driver";
    }

    public DBUtil(String DBuser, String DBpasswd, String DBhost) {
        this.username = DBuser;
        this.password = DBpasswd;
        this.urlBase = DBhost;
        this.driver = "com.mysql.jdbc.Driver";
    }

    public void connectDB(String dataBaseName) throws Exception {
        this.databaseName = dataBaseName;
        this.url = this.urlBase + this.databaseName+"?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&zeroDateTimeBehavior=round";
        getConnection();
    }

    public Connection getConnection() throws Exception {
        if (connection == null) {
            Class.forName(this.driver);
            connection = DriverManager.getConnection(this.url, this.username, this.password);
        }
        return connection;
    }

    public boolean insert(String sql, Object... args) throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql);
        int i = 0;
        for (Object arg : args)
            statement.setObject(++i, arg);
        int exeNum = statement.executeUpdate();
        statement.close();
        return (exeNum == 1);
    }

    public boolean insert(String sql, Collection<Object> args) throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql);
        int i = 0;
        for (Object arg : args)
            statement.setObject(++i, arg);
        int exeNum = statement.executeUpdate();
        statement.close();
        return (exeNum == 1);
    }

    public ResultSet query(String sql, Object... args) throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql);
        int i = 0;
        for (Object arg : args)
            statement.setObject(++i, arg);
        return statement.executeQuery();
    }

    public ResultSet query(String sql, Collection<Object> args) throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql);
        int i = 0;
        for (Object arg : args)
            statement.setObject(++i, arg);
        return statement.executeQuery();
    }

    public ResultSet queryUpdatable(String sql, Object... args) throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        int i = 0;
        for (Object arg : args)
            statement.setObject(++i, arg);
        return statement.executeQuery();
    }

    public synchronized boolean update(String sql, Object... args) throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql);
        int i = 0;
        for (Object arg : args)
            statement.setObject(++i, arg);
        boolean updateResult= statement.executeUpdate() > 0 ? true : false;
        statement.close();
        return updateResult;
    }

    public void closeDBConn() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }
}

