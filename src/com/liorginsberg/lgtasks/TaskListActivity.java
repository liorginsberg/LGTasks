package com.liorginsberg.lgtasks;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ListView;

public class TaskListActivity extends Activity {

	public static final String PREFS_NAME = "com.liorginsberg.lgtasks_preferences";

	static final private int VOICE_RECOGNITION_REQUEST_CODE = 1121;
	static final private int ADD_TASK_REQUEST_CODE = 100;
	static final public int EDIT_TASK_REQUEST_CODE = 101;

	private ImageButton ibAddTask;
	private ImageButton ibMaps;
	private ImageButton ibSet;
	private ImageButton ibTalk;
	private ListView lvTasksList;

	private TaskAdapter taskAdapter;

	private View viewPH;

	public static SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_task_list);
		
		String user_email = AccountsHelper.getEmail(this);
		if(user_email == null) {
			user_email = "test";
		}
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
		settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("userID", user_email).commit();

		// get tasklist view
		lvTasksList = (ListView) findViewById(R.id.lvMainTaskList);

		// get all tasks from database
		TaskList.getInstance(this).getAllTasksFromDB();

		// add footer place holder to tasks list
		viewPH = getLayoutInflater().inflate(R.layout.list_footer, null);
		viewPH.setClickable(false);
		viewPH.setOnTouchListener(null);
		lvTasksList.addFooterView(viewPH, null, false);

		// set tasklist adapter
		taskAdapter = TaskAdapter.getInstance(this, R.layout.tasklist_item);
		lvTasksList.setAdapter(taskAdapter);

		// get image buttons view and set their onclick action
		initButtons();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ADD_TASK_REQUEST_CODE:
				scrollMyListViewToBottom();
				break;
			case EDIT_TASK_REQUEST_CODE:
				break;
			case VOICE_RECOGNITION_REQUEST_CODE:
				ArrayList<String> results = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				SharedPreferences prefs = getApplicationContext()
						.getSharedPreferences(TaskListActivity.PREFS_NAME, 0);
				boolean updateRemoteDB = prefs.getBoolean("backupTasks", false);
				boolean autoShare = prefs.getBoolean("autoShare", false);
				boolean addShare = prefs.getBoolean("addTaskShare", false);
				boolean share = autoShare && addShare;
				long task_id = TaskList.getInstance(getApplicationContext())
						.addTask(results.get(0), "", "", "", "", 0,
								updateRemoteDB, share);
				
				scrollMyListViewToBottom();
			default:
				break;
			}
			taskAdapter.notifyDataSetChanged();
		}
		// Session.getActiveSession().onActivityResult(this, requestCode,
		// resultCode, data);
	}

	private void initButtons() {
		ibTalk = (ImageButton) findViewById(R.id.btnTalkToMe);
		ibTalk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
				intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
						"Say your task...");
				startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
			}
		});

		ibSet = (ImageButton) findViewById(R.id.btnSet);
		ibSet.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent p = new Intent(TaskListActivity.this, Prefrences.class);
				startActivity(p);
			}
		});
		ibMaps = (ImageButton) findViewById(R.id.btnMaps);
		ibMaps.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent calIntent = new Intent(TaskListActivity.this,
						MapActivity.class);
				startActivity(calIntent);
			}
		});
		ibAddTask = (ImageButton) findViewById(R.id.btnAddTaskSimple);
		ibAddTask.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent addTaskIntent = new Intent();
				addTaskIntent.setClass(TaskListActivity.this,
						AddTaskActivity.class);
				startActivityForResult(addTaskIntent, ADD_TASK_REQUEST_CODE);
			}
		});
	}

	private void scrollMyListViewToBottom() {
		lvTasksList.post(new Runnable() {
			@Override
			public void run() {
				// Select the last row so it will scroll into view...
				lvTasksList.setSelection(taskAdapter.getCount() - 1);
			}
		});
	}

}
