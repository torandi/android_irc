package irc.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class DatabaseObject<T extends DatabaseObject<T>> {
	protected Integer id = null;
	protected HashMap<String, Object> values;
	protected ArrayList<Field> columns;
	
	protected abstract Class<T> cls();
	protected abstract String table_name();

	/* Hooks that are optional to implement */
	protected void post_commit_hook() { };
	protected String default_order() { return "`" + id_name() + "`"; };
	public void validate() throws ValidationException { }
	
	protected String id_name() { return "id"; };

	public DatabaseObject(){
		columns = new ArrayList<Field>();
		values = new HashMap<String, Object>();
		DatabaseMetaData meta = DatabaseConnection.get().getMetaData();
		try {
			ResultSet rs = meta.getColumns(null, null, table_name(), null);
			while(rs.next()) {
				Field f = new Field(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE"));
				columns.add(f);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public int id() {
		return id.intValue();
	}

	/**
	 * Save all changes
	 * @return true on success
	 * @throws ValidationException, SQLException
	 */
	public boolean commit() throws SQLException, ValidationException {
		validate();
		String query;
		if(id == null) {
			query = "insert into `"+table_name()+"` SET ";
		} else {
			query = "update `"+table_name()+"` SET ";
		}
		for(Field f : columns) {
			if(!f.name.equals(id_name())) {
				query +="`"+f.name+"` = ?,";
			}
		}
		query = query.substring(0, query.length() - 1);
		if(id != null) {
			query += "where `"+id_name()+"` = ?";
		}
		PreparedStatement stmt = DatabaseConnection.get().prepareStatement(query);
		int index = 1;
		for(Field f : columns) {
			if(!f.name.equals(id_name())) {
				stmt.setObject(index++, get(f.name), f.type);
			}
		}
		if(id != null) {
			stmt.setInt(index, id);
		}
		return stmt.execute();
	}
	
	
	public boolean delete() throws SQLException {
		if(id == null) {
			System.err.println("Can't delete unsaved DatabaseObject");
			return false;
		}
		PreparedStatement stmt = DatabaseConnection.get().prepareStatement("delete from `"+table_name()+"` where `"+id_name()+"`=?");
		stmt.setInt(1, id);
		return stmt.execute();
	}
	
	/**
	 * Return first object having attr == value
	 * @param attr
	 * @param value
	 * @return
	 * @throws SQLException 
	 */
	public T first(String attr, Object value) throws SQLException {
		PreparedStatement stmt = statement("`"+ attr + "` = ? " + order_string() + " limit 1");
		stmt.setObject(1, value);
		ResultSet rs = stmt.executeQuery();
		ResultSetMetaData meta = rs.getMetaData();
		if(rs.first()) {
			T obj = null;
			try {
				obj = cls().newInstance();
				obj.set_from_db(meta, rs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return obj;
		} else {
			return null;
		}
	}
	
	
	/**
	 * Return object having id == id
	 * @param id
	 * @return
	 */
	public T from_id(int id) {
		try {
			return first(id_name(), new Integer(id));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Return all objects of this model
	 * @return
	 * @throws SQLException 
	 */
	public ArrayList<T> all() throws SQLException {
		PreparedStatement stmt = DatabaseConnection.get().prepareStatement("select * from "+table_name() + order_string());
		return where(stmt);
	}
	
	/**
	 * Search for all objects having attr == value
	 * @param attr
	 * @param value
	 * @return
	 * @throws SQLException 
	 */
	public ArrayList<T> find(String attr, Object value) throws SQLException {
		PreparedStatement stmt = DatabaseConnection.get().prepareStatement("select * from "+table_name() + order_string());
		return where(stmt);
	}
	
	public void set(String field, Object value) {
		values.put(field, value);
	}
	
	public Object get(String field) {
		return values.get(field);
	}
	
	public ArrayList<T> where(PreparedStatement stmt) throws SQLException {
		ResultSet rs = stmt.executeQuery();
		ResultSetMetaData meta = rs.getMetaData();
		ArrayList<T> res = new ArrayList<T>();
		while(rs.next()) {
			try {
				T obj = cls().newInstance();
				obj.set_from_db(meta, rs);
				res.add(obj);
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<T>();
			}
		}
		return res;
	}
	
	public PreparedStatement statement(String where) {
		return DatabaseConnection.get().prepareStatement("select * from "+table_name()+" WHERE "+where);
	}
	
	protected void set_from_db(ResultSetMetaData meta, ResultSet rs) throws SQLException {
		for(int i=1; i<=meta.getColumnCount(); ++i) {
			values.put(meta.getColumnName(i), rs.getObject(i));
		}
		id = (Integer)get(id_name());
	}
	
	protected int get_int(String field) {
		return ((Integer)get(field)).intValue();
	}
	
	
	protected static class Field {
		public final String name;
		public final int type;
		
		public Field(String name, int type) {
			this.name = name;
			this.type = type;
		}
		
		public String toString() {
			return name + " ("+type +")";
		}
	}
	
	public String toString() {
		String str = this.getClass().getName() + "{\n";
		for(String k : values.keySet()) {
			str += "\t"+k+"="+values.get(k)+"\n";
		}
		str += "}";
		return str;
	}
	
	protected void validateExistance(String attr) throws ValidationException {
		if(get(attr) == null) throw new ValidationException(attr, "Must be set.");
	}
	
	protected void validateMinLength(String attr, int min_length) throws ValidationException {
		validateExistance(attr);
		if(((String)get(attr)).length() < min_length) throw new ValidationException(attr, "Must be at least " +min_length + " characters long.");
	}
	
	protected void validateUniqueness(String attr) throws ValidationException {
		try {
			if(first(attr, get(attr)) != null) {
				throw new ValidationException(attr, "Must be unique");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ValidationException(attr, "An SQL error occurred while trying to validate uniqueness");
		}
	}
	
	private String order_string() {
		return " ORDER BY " + default_order();
	}
}
