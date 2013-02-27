package com.liorginsberg.lgtasks;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddTaskActivity extends Activity {

	private EditText etAddTaskTitle;
	private EditText etAddTaskDesc;
	private EditText spDateFrom;
	private EditText spTimeFrom;
	private EditText spDateTo;
	private EditText spTimeTo;

	private CheckBox chbRemindMe;
	private ImageButton btnAccept;
	private ImageButton btnCancel;

	private Calendar fromCalendar;
	private Calendar toCalendar;

	private MyGestureDetector myGestureDetector;
	private MyOnGestureListener myOnGestureListener;
	private ImageButton btnFetch;
	private ProgressBar waitSpinner;

	AutoCompleteTextView autoCompleteTextView;

	private Geocoder geocoder;
	private boolean canExecute = true;

	ArrayList<Address> addresses;
	ArrayList<String> stringAdresses;

	ArrayAdapter<String> adapter;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_task);

		etAddTaskTitle = (EditText) findViewById(R.id.etAddTaskTitle);
		etAddTaskDesc = (EditText) findViewById(R.id.etAddTaskDesc);
		spDateFrom = (EditText) findViewById(R.id.spDateFrom);
		spTimeFrom = (EditText) findViewById(R.id.spTimeFrom);
		spDateTo = (EditText) findViewById(R.id.spDateTo);
		spTimeTo = (EditText) findViewById(R.id.spTimeTo);
		autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.acLocation);
		chbRemindMe = (CheckBox) findViewById(R.id.chbRemindMe);
		

		spDateFrom.setKeyListener(null);
		spTimeFrom.setKeyListener(null);
		spDateTo.setKeyListener(null);
		spTimeTo.setKeyListener(null);
	

		etAddTaskTitle.requestFocus();
		
		
		addresses = new ArrayList<Address>();
		geocoder = new Geocoder(this, new Locale("en", "IL"));
		stringAdresses = new ArrayList<String>();
		autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.acLocation);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, stringAdresses);
		autoCompleteTextView.setAdapter(adapter);
		
	
		
		autoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				EasyTracker.getTracker().sendEvent("ui_action", "clicked EditText", "Location autocomplete", null);
			
				try {
					List<Address> a = geocoder.getFromLocationName(((TextView)arg1).getText().toString(),1);
					if(a != null) {
						Toast.makeText(getApplicationContext(),a.get(0).getLatitude() + ":" + a.get(0).getLongitude(), Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}
		
		});
		
		autoCompleteTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				Log.i("onTextChanged", "text changed");
				if (canExecute) {

					new LocationAsyncTask().execute(s.toString());
					Log.i("onTextChanged", "execute Location task");
				} else {
					Log.i("onTextChange", "task not done, skiped action");
				}
			}
		});

	
		
		//EDIT
		final int position = getIntent().getIntExtra("position", -1);
		if (position != -1) {
			Task temp = TaskList.getInstance(getApplicationContext()).getTaskAt(position);
			etAddTaskTitle.setText(temp.getTitle());
			etAddTaskDesc.setText(temp.getDesc());
			
			fromCalendar = temp.getCalendarFrom();
			toCalendar = temp.getCalendarTo();
			if(fromCalendar == null) {
				fromCalendar = Calendar.getInstance();
			}
			if (toCalendar == null) {
				toCalendar = Calendar.getInstance();
				toCalendar.add(Calendar.HOUR_OF_DAY, 1);
			}
			if(!temp.getLocation().isEmpty()) {
				autoCompleteTextView.setText(temp.getLocation());
			}
			
		}//ADD
		else {
			fromCalendar = Calendar.getInstance();
			toCalendar = Calendar.getInstance();
			toCalendar.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		spDateFrom.setText(String.format(Locale.getDefault(), "%02d/%02d/%02d", fromCalendar.get(Calendar.DAY_OF_MONTH),
				fromCalendar.get(Calendar.MONTH) + 1, fromCalendar.get(Calendar.YEAR)));
		spTimeFrom.setText(String.format(Locale.getDefault(), "%02d:%02d", fromCalendar.get(Calendar.HOUR_OF_DAY),
				fromCalendar.get(Calendar.MINUTE)));

		spDateTo.setText(String.format(Locale.getDefault(), "%02d/%02d/%02d", toCalendar.get(Calendar.DAY_OF_MONTH),
				toCalendar.get(Calendar.MONTH) + 1, toCalendar.get(Calendar.YEAR)));
		spTimeTo.setText(String.format(Locale.getDefault(), "%02d:%02d", toCalendar.get(Calendar.HOUR_OF_DAY), toCalendar.get(Calendar.MINUTE)));


		myOnGestureListener = new MyOnGestureListener();
		myGestureDetector = new MyGestureDetector(this, myOnGestureListener);

		MyOnTouchListener myOnTouchListener = new MyOnTouchListener();
		spDateFrom.setOnTouchListener(myOnTouchListener);
		spDateTo.setOnTouchListener(myOnTouchListener);
		spTimeFrom.setOnTouchListener(myOnTouchListener);
		spTimeTo.setOnTouchListener(myOnTouchListener);

		btnAccept = (ImageButton) findViewById(R.id.btnAccept);
		btnAccept.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String taskTitle = etAddTaskTitle.getText().toString();
				String taskDesc = etAddTaskDesc.getText().toString();
				String fromString = spDateFrom.getText().toString() + " " + spTimeFrom.getText().toString();
				String toString = spDateTo.getText().toString() + " " + spTimeTo.getText().toString();
				String location = autoCompleteTextView.getText().toString();
			
				long task_id;
				if (!taskTitle.isEmpty()) {
					if(position == -1) {
						SharedPreferences prefs = getApplicationContext().getSharedPreferences(TaskListActivity.PREFS_NAME, 0);
						boolean updateRemoteDB = prefs.getBoolean("backupTasks", false);
						boolean autoShare = prefs.getBoolean("autoShare", false);
						boolean addShare = prefs.getBoolean("addTaskShare", false);
						boolean share  = autoShare && addShare;
						task_id = TaskList.getInstance(getApplicationContext()).addTask(-1, taskTitle, taskDesc, fromString, toString, location, 0, updateRemoteDB, share);
					}else {
						task_id = TaskList.getInstance(getApplicationContext()).getTaskAt(position).getTask_id();
						int task_checked = TaskList.getInstance(getApplicationContext()).getTaskAt(position).isChecked();
						TaskList.getInstance(getApplicationContext()).updateTask(position, task_id, taskTitle, taskDesc, fromString, toString, location, task_checked);
					}
					if (chbRemindMe.isChecked()) {
						Intent intent = new Intent(AddTaskActivity.this, MyBroadcastReceiver.class);
						intent.putExtra("title", taskTitle);
						intent.putExtra("desc", taskDesc);
						PendingIntent pendingIntent = PendingIntent.getBroadcast(AddTaskActivity.this.getApplicationContext(), (int)task_id, intent, 0);
						AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
						Toast.makeText(
								getApplicationContext(),
								"Set remider for: "
										+ String.format(Locale.getDefault(), "%02d/%02d/%02d %02d:%02d", fromCalendar.get(Calendar.DAY_OF_MONTH),
												fromCalendar.get(Calendar.MONTH) + 1, fromCalendar.get(Calendar.YEAR),
												fromCalendar.get(Calendar.HOUR_OF_DAY), fromCalendar.get(Calendar.MINUTE)), Toast.LENGTH_SHORT)
								.show();
						alarmManager.set(AlarmManager.RTC_WAKEUP, fromCalendar.getTimeInMillis(), pendingIntent);
					}
					setResult(RESULT_OK);
					finish();
				} else {
					Log.i("addTaskDialog", "The Title canot be empty");
				}
			}
		});

		btnCancel = (ImageButton) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		

	}

	private class MyOnSetDateListener implements OnDateSetListener {

		private View v;

		public MyOnSetDateListener(View v) {
			this.v = v;
		}

		public void onDateSet(DatePicker dp, int year, int monthOfYear, int dayOfMonth) {
			switch (v.getId()) {

			case R.id.spDateFrom:
				fromCalendar.set(year, monthOfYear, dayOfMonth);

				// if "from date" is later than "to date"
				if (fromCalendar.after(toCalendar)) {

					// set to date equal to date
					toCalendar = (Calendar) fromCalendar.clone();

					// add to date 1 hour
					toCalendar.add(Calendar.HOUR_OF_DAY, 1);

					// set to time text
					String toTime = String.format(Locale.getDefault(), "%02d:%02d", toCalendar.get(Calendar.HOUR_OF_DAY),
							toCalendar.get(Calendar.MINUTE));
					spTimeTo.setText(toTime);

					// set to date text
					String toDate = String.format(Locale.getDefault(), "%02d/%02d/%02d", toCalendar.get(Calendar.DAY_OF_MONTH),
							toCalendar.get(Calendar.MONTH) + 1, toCalendar.get(Calendar.YEAR));
					spDateTo.setText(toDate);
				}

				// set from date text
				String fromDate = String.format(Locale.getDefault(), "%02d/%02d/%02d", fromCalendar.get(Calendar.DAY_OF_MONTH),
						fromCalendar.get(Calendar.MONTH) + 1, fromCalendar.get(Calendar.YEAR));
				spDateFrom.setText(fromDate);

				break;
			case R.id.spDateTo:
				Calendar tempCalendar = (Calendar) toCalendar.clone();
				tempCalendar.set(year, monthOfYear, dayOfMonth);

				// if to date is before from date
				if (tempCalendar.before(fromCalendar)) {

					// raise toast and do nothing
					Toast.makeText(AddTaskActivity.this, "Can not set \"To\" earlier than \"From\" ", Toast.LENGTH_SHORT).show();
				} else {

					// set the "to calendar" to be like the "from calendar" with
					// 1 hour later
					toCalendar.set(year, monthOfYear, dayOfMonth);

					if (toCalendar.before(fromCalendar)) {
						toCalendar = (Calendar) fromCalendar.clone();
						toCalendar.add(Calendar.HOUR_OF_DAY, 1);
					}

					// set to date text
					String toDate = String.format(Locale.getDefault(), "%02d/%02d/%02d", toCalendar.get(Calendar.DAY_OF_MONTH),
							toCalendar.get(Calendar.MONTH) + 1, toCalendar.get(Calendar.YEAR));
					spDateTo.setText(toDate);
				}
				break;
			default:
				break;
			}
		}
	}

	private class MyOnSetTimeListener implements OnTimeSetListener {

		private View v;

		public MyOnSetTimeListener(View v) {
			this.v = v;
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			switch (v.getId()) {
			case R.id.spTimeFrom:
				fromCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				fromCalendar.set(Calendar.MINUTE, minute);

				if (fromCalendar.after(toCalendar)) {
					toCalendar.set(Calendar.HOUR_OF_DAY, fromCalendar.get(Calendar.HOUR_OF_DAY));
					toCalendar.set(Calendar.MINUTE, fromCalendar.get(Calendar.MINUTE));
					toCalendar.add(Calendar.HOUR_OF_DAY, 1);

					String toDate = String.format(Locale.getDefault(), "%02d/%02d/%02d", toCalendar.get(Calendar.DAY_OF_MONTH),
							toCalendar.get(Calendar.MONTH) + 1, toCalendar.get(Calendar.YEAR));
					spDateTo.setText(toDate);

					String toTime = String.format(Locale.getDefault(), "%02d:%02d", toCalendar.get(Calendar.HOUR_OF_DAY),
							toCalendar.get(Calendar.MINUTE));
					spTimeTo.setText(toTime);

				}

				// set to date text
				String toDate = String.format(Locale.getDefault(), "%02d/%02d/%02d", toCalendar.get(Calendar.DAY_OF_MONTH),
						toCalendar.get(Calendar.MONTH) + 1, toCalendar.get(Calendar.YEAR));
				spDateTo.setText(toDate);

				spTimeFrom.setText(String.format(Locale.getDefault(), "%02d:%02d", fromCalendar.get(Calendar.HOUR_OF_DAY),
						fromCalendar.get(Calendar.MINUTE)));

				break;
			case R.id.spTimeTo:
				Calendar tempCalendar = (Calendar) toCalendar.clone();
				tempCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				tempCalendar.set(Calendar.MINUTE, minute);

				if (tempCalendar.before(fromCalendar)) {
					Toast.makeText(AddTaskActivity.this, "Canot set \"To\" earlier than \"From\" ", Toast.LENGTH_SHORT).show();
				} else {
					toCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					toCalendar.set(Calendar.MINUTE, minute);

					String toTime = String.format(Locale.getDefault(), "%02d:%02d", toCalendar.get(Calendar.HOUR_OF_DAY),
							toCalendar.get(Calendar.MINUTE));
					spTimeTo.setText(toTime);
				}
				break;
			default:
				break;
			}

		}

	}

	private class MyOnGestureListener implements OnGestureListener {

		View view;

		public void setView(View view) {
			this.view = view;
		}

		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return true;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			// TODO Auto-generated method stub
			return false;
		}

		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			return false;
		}

		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		public boolean onSingleTapUp(MotionEvent e) {

			Log.i("Touched", "onSingleTap()");
			switch (view.getId()) {
			case R.id.spDateFrom:
				DatePickerDialog datePickerDialogFrom = new DatePickerDialog(AddTaskActivity.this, new MyOnSetDateListener(view),
						fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH), fromCalendar.get(Calendar.DAY_OF_MONTH));
				datePickerDialogFrom.show();
				break;
			case R.id.spTimeFrom:
				TimePickerDialog timePickerDialogFrom = new TimePickerDialog(AddTaskActivity.this, new MyOnSetTimeListener(view),
						fromCalendar.get(Calendar.HOUR_OF_DAY), fromCalendar.get(Calendar.MINUTE), true);
				timePickerDialogFrom.show();
				break;
			case R.id.spDateTo:
				DatePickerDialog datePickerDialogTo = new DatePickerDialog(AddTaskActivity.this, new MyOnSetDateListener(view),
						toCalendar.get(Calendar.YEAR), toCalendar.get(Calendar.MONTH), toCalendar.get(Calendar.DAY_OF_MONTH));
				datePickerDialogTo.show();
				break;
			case R.id.spTimeTo:
				TimePickerDialog timePickerDialogTo = new TimePickerDialog(AddTaskActivity.this, new MyOnSetTimeListener(view),
						toCalendar.get(Calendar.HOUR_OF_DAY), fromCalendar.get(Calendar.MINUTE), true);
				timePickerDialogTo.show();
				break;
		
			default:
				break;
			}
			return true;

		}

	}

	private class MyOnTouchListener implements OnTouchListener {

		public boolean onTouch(View v, MotionEvent event) {
			myGestureDetector.setViewForListaner(v);
			return myGestureDetector.onTouchEvent(event);
		}

	}

	private class MyGestureDetector extends GestureDetector {

		private MyOnGestureListener myOnGestureListener;

		public MyGestureDetector(Context context, MyOnGestureListener listener) {
			super(context, listener);
			this.myOnGestureListener = listener;
		}

		public void setViewForListaner(View v) {
			myOnGestureListener.setView(v);
		}

	}

	private class GetFromWebTask extends AsyncTask<URL, Integer, String> {

		@Override
		protected void onPreExecute() {
			waitSpinner.setVisibility(ProgressBar.VISIBLE);
		}

		@Override
		protected String doInBackground(URL... urls) {
			String res = "";
			try {
				res = getFromWeb(urls[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return res;
		}

		private String getFromWeb(URL url) throws Exception {
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			String response;
			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			InputStreamReader inReader = new InputStreamReader(in);
			BufferedReader bufferedReader = new BufferedReader(inReader);
			StringBuilder responseBuilder = new StringBuilder();
			for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
				responseBuilder.append(line);
			}
			response = responseBuilder.toString();

			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			JSONObject taskJASON = null;
			try {
				taskJASON = new JSONObject(result);
				etAddTaskTitle.setText(taskJASON.getString("topic"));
				etAddTaskDesc.setText(taskJASON.getString("description"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			waitSpinner.setVisibility(ProgressBar.INVISIBLE);
		}
	}
	
	public class LocationAsyncTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			canExecute = false;
			Log.i("doInBackground", "try getting addresses");
			if (params[0].equals("")) {
				addresses = new ArrayList<Address>();
			}
			try {
				addresses = (ArrayList<Address>) geocoder.getFromLocationName(params[0], 3);

			} catch (IOException e) {
				e.printStackTrace();
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			Log.i("onPostExecute", "starting runOnUiThread");
			addressesToStrings(addresses);
			adapter = new ArrayAdapter<String>(AddTaskActivity.this, android.R.layout.simple_dropdown_item_1line, stringAdresses);
			autoCompleteTextView.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			canExecute = true;

		}

	}

	public void addressesToStrings(ArrayList<Address> addresses2) {

		if (addresses != null && addresses.size() > 0) {
			stringAdresses = new ArrayList<String>();
			for (Address address : addresses) {
				StringBuilder string = new StringBuilder();
			
				string.append(address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) + " ," : "");
				string.append(address.getLocality() != null ? address.getLocality() + " ,":"");
				string.append(address.getCountryName() != null ? address.getCountryName() : "");
				stringAdresses.add(string.toString());
			}
		}
	}

}
