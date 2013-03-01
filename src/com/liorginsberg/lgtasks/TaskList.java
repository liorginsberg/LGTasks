package com.liorginsberg.lgtasks;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class TaskList implements Observer {
	private static TaskList instance = null;
	private List<Task> tasks;
	private Context context;

	private TaskDB taskDB;

	private TaskList(Context context) {
		this.context = context;
		tasks = new ArrayList<Task>();
		taskDB = new TaskDB(context);
	}

	public static TaskList getInstance(Context context) {
		if (instance == null) {
			instance = new TaskList(context);
		}
		return instance;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public Task getTaskAt(int position) {
		return tasks.get(position);
	}

	public long addTask(long task_id, String title, String desc, String from, String to, String location, int isChecked, boolean updateRemoteDB, boolean share) {
		
		boolean animated = true;
		if(task_id == -1) {
			task_id = System.currentTimeMillis();
			animated = false;
		}
		taskDB.open().insertTask(task_id, title, desc, from, to, location, isChecked);
		Task taskToAdd = new Task(task_id, title, desc, from, to, location, isChecked);
		taskToAdd.animated = animated;
		tasks.add(taskToAdd);
		if (updateRemoteDB) {
			SharedPreferences prefs = context.getSharedPreferences(TaskListActivity.PREFS_NAME, 0);
			new HttpPostRequest(context).add(prefs.getString("userID", "-1"), task_id, title, desc, from, to, location, isChecked);
		}
		
		if (share) {
			new ShareTasks((Activity)context).postOpenGraphActivitiy(ShareTasks.ADD,taskToAdd);
		}
		
		return task_id;
	}

	public void updateTask(int position, long task_id, String title, String desc, String from, String to, String location, int isChecked) {
		boolean updated = taskDB.updateTask(task_id, title, desc, from, to, location, isChecked);
		if (updated) {
			tasks.set(position, new Task(task_id, title, desc, from, to, location, isChecked));
			if (context.getSharedPreferences(TaskListActivity.PREFS_NAME, 0).getBoolean("backupTasks", false)) {
				SharedPreferences prefs = context.getSharedPreferences(TaskListActivity.PREFS_NAME, 0);
				new HttpPostRequest(context).update(prefs.getString("userID", "-1"), task_id, title ,desc, from, to, location, isChecked);
			}
		}
		
	}
	
	public boolean updateTaskByTaskID(long task_id, String title, String desc, String from, String to, String location, int isChecked) {
		Task temp = getTaskById(task_id);
		int position = tasks.indexOf(temp);
		boolean updated = taskDB.updateTask(task_id, title, desc, from, to, location, temp.isChecked());
		if (updated) {
			tasks.set(position, new Task(task_id, title, desc, from, to, location, isChecked));
			return true;
		}
		return false;
		
	}

	public boolean setDone(long task_id, int done) {
		SharedPreferences prefs = context.getSharedPreferences(TaskListActivity.PREFS_NAME, 0);
		if (context.getSharedPreferences(TaskListActivity.PREFS_NAME, 0).getBoolean("backupTasks", false)) {
			new HttpPostRequest(context).setChecked(prefs.getString("userID", "-1"), task_id, done);
		}
		return taskDB.setDone(task_id, done);
	}

	public boolean setDoneFromWeb(long task_id, int done) {
		Task temp = getTaskById(task_id);
		boolean success = taskDB.setDone(task_id, done);
		if(success) {
			temp.setChecked(done);
			return true;
		}
		return false;
	}
	
	public int removeTask(int pos) {
		long taskID = tasks.get(pos).getTask_id();
		int rmRes = taskDB.removeTask(taskID);
		if (rmRes == 1) {
			if (context.getSharedPreferences(TaskListActivity.PREFS_NAME, 0).getBoolean("backupTasks", false)) {
				SharedPreferences prefs = context.getSharedPreferences(TaskListActivity.PREFS_NAME, 0);
				new HttpPostRequest(context).remove(prefs.getString("userID", "-1"), taskID);
			}
			tasks.remove(pos);
			return 1;
		}
		return -1;
	}
	
	public boolean removeTaskByTaskID(long taskID) {
		int rmRes = taskDB.removeTask(taskID);
		if(rmRes == 1) {
			return tasks.remove(getTaskById(taskID));
		} else {
			return false;
		}
	}

	public void getAllTasksFromDB() {
		tasks = taskDB.getAll();
		if (tasks.size() == 0) {
			SharedPreferences prefs = context.getSharedPreferences(TaskListActivity.PREFS_NAME, 0);
			if (prefs.getBoolean("backupTasks", false)) {
				HttpPostRequest httpPostRequest = new HttpPostRequest(context);
				httpPostRequest.Register(this);
				httpPostRequest.getTaskFromRemoteDB(prefs.getString("userID", "-1"));
			}
		}
	}

	public void removeAllTasksFromDB() {
		taskDB.removeAllTasks();
	}

	public void update(String response) {
		JSONObject taskJASON = null;
		try {
			taskJASON = new JSONObject(response);
			JSONArray tasksArray = taskJASON.getJSONArray("tasks");
			

			for (int i = 0; i < tasksArray.length(); i++) {
				JSONObject t = tasksArray.getJSONObject(i);
				long task_id = t.getLong("task_id");
				String task_title = t.getString("task_title");
				String task_desc = t.getString("task_desc");
				String task_from = t.getString("task_from");
				String task_to = t.getString("task_to");
				String task_location = t.getString("task_location");
				int task_checked = t.getInt("task_checked");

				addTask(task_id, task_title, task_desc, task_from, task_to, task_location, task_checked, false, false);
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			TaskAdapter.getInstance(context, R.layout.tasklist_item).notifyDataSetChanged();
		}
	}
	
	public Task getTaskById(long task_id) {
		for(Task task: tasks) {
			if(task.getTask_id() == task_id) {
				return task;
			}
		}
		return null;
	}
}
