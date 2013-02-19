package com.liorginsberg.lgtasks;

import static com.liorginsberg.lgtasks.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.liorginsberg.lgtasks.CommonUtilities.EXTRA_MESSAGE;
import static com.liorginsberg.lgtasks.CommonUtilities.SENDER_ID;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;

public class TaskListActivity extends Activity {

	public static final String PREFS_NAME = "com.liorginsberg.lgtasks_preferences";

	static final private int VOICE_RECOGNITION_REQUEST_CODE = 1121;
	static final private int ADD_TASK_REQUEST_CODE = 100;
	static final public int EDIT_TASK_REQUEST_CODE = 101;

	static public boolean isAppVisible = false;

	private ImageButton ibAddTask;
	private ImageButton ibMaps;
	private ImageButton ibSet;
	private ImageButton ibTalk;
	private ListView lvTasksList;

	private TaskAdapter taskAdapter;

	private View viewPH;

	public static SharedPreferences settings;

	ConnectionDetector cd;

	public static String user_name;
	public static String user_email;

	private AsyncTask<Void, Void, Void> mRegisterTask;

	@Override
	protected void onPause() {
		super.onPause();
		try {
			unregisterReceiver(mHandleMessageReceiver);
		} catch (IllegalArgumentException e) {
			Log.i("unRegisterReciver",
					"mHandleMessageReceiver is not registered");
		}

	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_task_list);

		// Get the intent that started this Activity.
		Intent intent = this.getIntent();
		Uri uri = intent.getData();

		// Call setContext() here so that we can access EasyTracker
		// to update campaign information before activityStart() is called.
		EasyTracker.getInstance().setContext(this);

		if (uri != null) {
			if (uri.getQueryParameter("utm_source") != null) { // Use campaign
																// parameters if
																// avaialble.
				EasyTracker.getTracker().setCampaign(uri.getPath());
			} else if (uri.getQueryParameter("referrer") != null) { 
																	
				EasyTracker.getTracker().setReferrer(
						uri.getQueryParameter("referrer"));
			}
		}

		isAppVisible = true;

		// start Facebook Login
		Session.openActiveSession(this, true, new Session.StatusCallback() {

			// callback when session changes state
			@Override
			public void call(Session session, SessionState state,
					Exception exception) {
				if (session.isOpened()) {
					// make request to the /me API
					Request.executeMeRequestAsync(session,
							new Request.GraphUserCallback() {

								// callback after Graph API response with user
								// object
								@Override
								public void onCompleted(GraphUser user,
										Response response) {
									if (user != null) {
										Toast.makeText(TaskListActivity.this,
												"Welocme " + user.getName(),
												Toast.LENGTH_SHORT).show();
									}
								}
							});
				}
			}
		});

		user_email = AccountsHelper.getEmail(this);
		if (user_email == null) {
			user_email = "test";
			user_name = "test";
		}
		String[] parts = user_email.split("@");
		user_name = parts[0];
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
		settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("userID", user_email);
		editor.putString("userName", user_name);
		editor.commit();

		// register for gcm
		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			Toast.makeText(
					this,
					"No internet connection, notification from the server is not available",
					Toast.LENGTH_LONG).show();
		} else {
			GCMRegistrar.checkDevice(this);
			registerReceiver(mHandleMessageReceiver, new IntentFilter(
					DISPLAY_MESSAGE_ACTION));

			// Get GCM registration id
			final String regId = GCMRegistrar.getRegistrationId(this);

			// Check if regid already presents
			if (regId.equals("")) {
				// Registration is not present, register now with GCM
				GCMRegistrar.register(getApplicationContext(), SENDER_ID);
			} else {
				// Device is already registered on GCM
				if (GCMRegistrar.isRegisteredOnServer(this)) {
					// Skips registration.
					Log.i("GCM", "Already registered with GCM");
				} else {
					// Try to register again, but not in the UI thread.
					// It's also necessary to cancel the thread onDestroy(),
					// hence the use of AsyncTask instead of a raw thread.
					final Context context = this;
					mRegisterTask = new AsyncTask<Void, Void, Void>() {

						@Override
						protected Void doInBackground(Void... params) {
							// Register on our server
							// On server creates a new user
							ServerUtilities.register(context, user_name,
									user_email, regId);
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {
							mRegisterTask = null;
						}

					};
					mRegisterTask.execute(null, null, null);
				}
			}
		}

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

				Calendar fromCalendar = Calendar.getInstance();
				Calendar toCalendar = Calendar.getInstance();
				toCalendar.add(Calendar.HOUR_OF_DAY, 1);

				String from = String.format(Locale.getDefault(),
						"%02d/%02d/%02d",
						fromCalendar.get(Calendar.DAY_OF_MONTH),
						fromCalendar.get(Calendar.MONTH) + 1,
						fromCalendar.get(Calendar.YEAR))
						+ " "
						+ String.format(Locale.getDefault(), "%02d:%02d",
								fromCalendar.get(Calendar.HOUR_OF_DAY),
								fromCalendar.get(Calendar.MINUTE));

				String to = String.format(Locale.getDefault(),
						"%02d/%02d/%02d",
						toCalendar.get(Calendar.DAY_OF_MONTH),
						toCalendar.get(Calendar.MONTH) + 1,
						toCalendar.get(Calendar.YEAR))
						+ " "
						+ String.format(Locale.getDefault(), "%02d:%02d",
								toCalendar.get(Calendar.HOUR_OF_DAY),
								toCalendar.get(Calendar.MINUTE));

				TaskList.getInstance(getApplicationContext()).addTask(-1,
						results.get(0), "By Voice", from, to, "", 0,
						updateRemoteDB, share);

				scrollMyListViewToBottom();
			default:
				break;
			}
			taskAdapter.notifyDataSetChanged();
			
			
		}
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	private void initButtons() {
		ibTalk = (ImageButton) findViewById(R.id.btnTalkToMe);
		ibTalk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EasyTracker.getTracker().sendEvent("ui_action",
						"click on ImageButton", "Mic", null);
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
				EasyTracker.getTracker().sendEvent("ui_action",
						"click on ImageButton", "Settings", null);
				Intent p = new Intent(TaskListActivity.this, Prefrences.class);
				startActivity(p);
			}
		});
		ibMaps = (ImageButton) findViewById(R.id.btnMaps);
		ibMaps.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				EasyTracker.getTracker().sendEvent("ui_action",
						"click on ImageButton", "Map", null);
				Intent calIntent = new Intent(TaskListActivity.this,
						MapActivity.class);
				startActivity(calIntent);
			}
		});
		ibAddTask = (ImageButton) findViewById(R.id.btnAddTaskSimple);
		ibAddTask.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				EasyTracker.getTracker().sendEvent("ui_action",
						"click on ImageButton", "Add Task", null);
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

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			if (newMessage == null) {
				Log.i("mHandleMessageReceiver",
						"The intent does not have an extra value 'message'");
			} else {
				Log.i("mHandleMessageReceiver", newMessage);

				JSONObject taskJSON = null;
				try {
					taskJSON = new JSONObject(newMessage);
					String action = taskJSON.getString("action");
					Log.i("PUSH", action);
					if (action.equals("addTask")) {
						EasyTracker.getTracker().sendEvent("GCM", "GCM message", "addTask", null);
						TaskList.getInstance(context)
								.addTask(taskJSON.getLong("task_id"),
										taskJSON.getString("title"),
										taskJSON.getString("desc"),
										taskJSON.getString("from"),
										taskJSON.getString("to"),
										taskJSON.getString("location"), 0,
										false, false);
						TaskAdapter
								.getInstance(context, R.layout.tasklist_item)
								.notifyDataSetChanged();

					} else if (action.equals("removeTask")) {
						EasyTracker.getTracker().sendEvent("GCM", "GCM message", "removeTask", null);
						long taskID = taskJSON.getLong("task_id");
						boolean success = TaskList.getInstance(context)
								.removeTaskByTaskID(taskID);
						if (success) {
							Toast.makeText(context,
									"Task removed by LGTasks web application",
									Toast.LENGTH_SHORT).show();
							TaskAdapter.getInstance(context,
									R.layout.tasklist_item)
									.notifyDataSetChanged();
						} else {
							Toast.makeText(
									context,
									"Failed to remove Task by LGTasks web application",
									Toast.LENGTH_SHORT).show();
						}

					} else if (action.equals("updateTask")) {
						EasyTracker.getTracker().sendEvent("GCM", "GCM message", "updateTask", null);
						boolean success = TaskList.getInstance(context)
								.updateTaskByTaskID(
										taskJSON.getLong("task_id"),
										taskJSON.getString("title"),
										taskJSON.getString("desc"),
										taskJSON.getString("from"),
										taskJSON.getString("to"),
										taskJSON.getString("location"),
										taskJSON.getInt("checked"));
						if (success) {
							TaskAdapter.getInstance(context,
									R.layout.tasklist_item)
									.notifyDataSetChanged();
						} else {
							Toast.makeText(
									context,
									"Failed to update Task by LGTasks web application",
									Toast.LENGTH_SHORT).show();
						}

					} else if (action.equals("updateCheck")) {
						EasyTracker.getTracker().sendEvent("GCM", "GCM message", "updateCheck", null);
						boolean success = TaskList.getInstance(context)
								.setDoneFromWeb(taskJSON.getLong("task_id"),
										taskJSON.getInt("checked"));
						if (success) {
							TaskAdapter.getInstance(context,
									R.layout.tasklist_item)
									.notifyDataSetChanged();
						} else {
							Toast.makeText(
									context,
									"Failed to set Task done by LGTasks web application",
									Toast.LENGTH_SHORT).show();
						}

					} else if (action.equals("GCMRegistration")) {
						EasyTracker.getTracker().sendEvent("GCM", "GCM message", "GCMRegistration", null);
						Toast.makeText(context, taskJSON.getString("message"),
								Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// Waking up mobile if it is sleeping
				WakeLocker.acquire(getApplicationContext());

				// Releasing wake lock
				WakeLocker.release();
			}
		}
	};
}
