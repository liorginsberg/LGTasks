package com.liorginsberg.lgtasks;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class TaskDB {

	private final String TAG = "TASKDB";
	
	public static final String KEY_ROWID = "task_id";
	public static final String KEY_TASK_TITLE = "task_title";
	public static final String KEY_TASK_DESC = "task_desc";
	public static final String KEY_TASK_DATE_FROM = "task_date_from";
	public static final String KEY_TASK_DATE_TO = "task_date_to";
	public static final String KEY_TASK_LOCATION = "task_location";
	public static final String KEY_TASK_CHECKED = "task_checked";
	

	private static final String DATABASE_NAME = "TaskDB";
	private static final String TASKS_TABLE = "TasksTable";
	private static final int DATABASE_VERSION = 1;


	private String[] columns = {KEY_ROWID, KEY_TASK_TITLE, KEY_TASK_DESC, KEY_TASK_DATE_FROM, KEY_TASK_DATE_TO, KEY_TASK_LOCATION, KEY_TASK_CHECKED};
	
	private final Context context;
	private SQLiteDatabase tasksDB;

	private DBHelper tasksDBHelper;
	

	public TaskDB(Context context) {
		this.context = context;
	}

	public TaskDB open() throws SQLException {
		tasksDBHelper = new DBHelper(context);
		tasksDB = tasksDBHelper.getWritableDatabase();
		return this;	
	}
	
	public void close() {
		tasksDBHelper.close();
	}
	
	public long insertTask(long task_id, String title, String desc, String date_from, String date_to, String location, int checked) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_ROWID, task_id);
		cv.put(KEY_TASK_TITLE, title);
		cv.put(KEY_TASK_DESC, desc);
		cv.put(KEY_TASK_DATE_FROM, date_from);
		cv.put(KEY_TASK_DATE_TO, date_to);
		cv.put(KEY_TASK_LOCATION, location);
		cv.put(KEY_TASK_CHECKED,checked);
		open();
		long idFromDb = tasksDB.insert(TASKS_TABLE, null, cv);
		close();
		
		return idFromDb;
	}
	
	public int removeTask(long taskID) {
		int rowEffectedCount = -1;
		try{
			open();
			rowEffectedCount = tasksDB.delete(TASKS_TABLE, KEY_ROWID + "=" + taskID , null);
			Log.i(TAG, "task with id=" + taskID + " was seccessfuly removed from database");
		} catch (SQLException e) {
			Log.i(TAG, "Could not delete task " + taskID);
		} finally {
			close();
		}
		return rowEffectedCount;
	}
	
	public List<Task> getAll() {
		ArrayList<Task> tasksList = new ArrayList<Task>();
		open();
		Cursor c = tasksDB.query(TASKS_TABLE, columns, null, null, null, null, null);
		try{
			int iRow = c.getColumnIndex(KEY_ROWID);
			int iTitle = c.getColumnIndex(KEY_TASK_TITLE);
			int iDesc = c.getColumnIndex(KEY_TASK_DESC);
			int iFrom = c.getColumnIndex(KEY_TASK_DATE_FROM);
			int iTo = c.getColumnIndex(KEY_TASK_DATE_TO);
			int ilocation = c.getColumnIndex(KEY_TASK_LOCATION);
			int iChecked = c.getColumnIndex(KEY_TASK_CHECKED);
			
			
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				tasksList.add(new Task(c.getLong(iRow), c.getString(iTitle), c.getString(iDesc), c.getString(iFrom), c.getString(iTo), c.getString(ilocation), c.getInt(iChecked)));
			}
		} catch (SQLException e) {
			Toast.makeText(context, "Could Not open database error", Toast.LENGTH_LONG).show();
		}finally {
			c.close();
			close();
		}
		return tasksList;
	}
	
	public void removeAllTasks() {
		try{
			open();
			tasksDB.delete(TASKS_TABLE, "1" , null);
		}catch (SQLException e) {
			Toast.makeText(context, "Could Not open database error", Toast.LENGTH_LONG).show();
		}finally{
			close();
		}
	}
	
	public boolean setDone(long task_id, int done) {
		String whereClause = "task_id=" + task_id;
		ContentValues cv = new ContentValues();
		cv.put(KEY_TASK_CHECKED, done);
		open();
		int rowCount = tasksDB.update(TASKS_TABLE, cv, whereClause, null);
		close();
		if(rowCount == 1) {
			Log.i(TAG, "set task with id=" + task_id + " to " + (done == 0 ? "not done" : "done") );
			return true;
		}
		return false;
	}
	
	public boolean updateTask(long task_id ,String title, String desc, String date_from, String date_to, String location,int checked) {
		String whereClause = "task_id=" + task_id;
		ContentValues cv = new ContentValues();
		cv.put(KEY_TASK_TITLE, title);
		cv.put(KEY_TASK_DESC, desc);
		cv.put(KEY_TASK_DATE_FROM, date_from);
		cv.put(KEY_TASK_DATE_TO, date_to);
		cv.put(KEY_TASK_LOCATION, location);
		cv.put(KEY_TASK_CHECKED,checked);
		
		open();
		int rowCount = tasksDB.update(TASKS_TABLE, cv, whereClause, null);
		close();
		if(rowCount == 1) {
			return true;
		}
		return false;
	}
	
	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TASKS_TABLE + " (" +
					KEY_ROWID + " BIGINT PRIMARY KEY ," +
					KEY_TASK_TITLE + " TEXT NOT NULL, " +
					KEY_TASK_DESC + " TEXT, " +
					KEY_TASK_DATE_FROM + " TEXT, " +
					KEY_TASK_DATE_TO + " TEXT, " +
					KEY_TASK_LOCATION + " TEXT, " +
					KEY_TASK_CHECKED + " INTEGER)"); 

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE);
			onCreate(db);
		}
	}

	

	
}
