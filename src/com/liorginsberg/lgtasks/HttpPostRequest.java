package com.liorginsberg.lgtasks;

import java.util.ArrayList;

import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

import android.content.Context;
import android.widget.Toast;

public class HttpPostRequest implements Subject {

	private ArrayList<Observer> observers;
	private String response;
	private AndroidHttpClient httpClient;
	private ParameterMap params;
	private Context context;

	public HttpPostRequest(Context context) {

		this.context = context;
		observers = new ArrayList<Observer>();
		httpClient = new AndroidHttpClient(
				"http://liorginsberg.com/lgtasks/php/task_backup_service.php");
		httpClient.setMaxRetries(5);

	}

	public void getTaskFromRemoteDB(String user_id) {
		params = httpClient.newParams();
		params.add("method", "getAllTasks").add("user_id", user_id);
		httpClient.post("", params, new AsyncCallback() {

			@Override
			public void onComplete(HttpResponse httpResponse) {
				response = httpResponse.getBodyAsString();
				notifyObserver();
				
			}});
	}

	public void add(String user_id, long task_id, String title, String desc,
			String from, String to, String location, int checked) {
		params = httpClient.newParams().add("method", "addTask")
				.add("user_id", user_id)
				.add("task_id", String.valueOf(task_id))
				.add("task_title", title).add("task_desc", desc)
				.add("task_from", from).add("task_to", to)
				.add("task_location", location)
				.add("task_checked", String.valueOf(checked));
		httpClient.post("", params, new AsyncCallback() {

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}

			@Override
			public void onComplete(final HttpResponse httpResponse) {
				response = httpResponse.getBodyAsString();
			}

		});
	}
	
	public void update(String user_id, long task_id, String title, String desc,
			String from, String to, String location, int checked) {
		params = httpClient.newParams().add("method", "updateTask")
				.add("user_id", user_id)
				.add("task_id", String.valueOf(task_id))
				.add("task_title", title)
				.add("task_desc", desc)
				.add("task_from", from)
				.add("task_to", to)
				.add("task_location", location)
				.add("task_checked", String.valueOf(checked));
		httpClient.post("", params, new AsyncCallback() {

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onComplete(final HttpResponse httpResponse) {
				response = httpResponse.getBodyAsString();
				//Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
			}

		});
	}

	public void remove(String user_id, long task_id) {
		params = httpClient.newParams().add("method", "removeTask")
				.add("user_id", user_id)
				.add("task_id", String.valueOf(task_id));
		httpClient.post("", params, new AsyncCallback() {

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}

			@Override
			public void onComplete(final HttpResponse httpResponse) {
				response = httpResponse.getBodyAsString();
				
			}
		});
	}

	public void setChecked(String user_id, long task_id, int task_checked) {
		params = httpClient.newParams().add("method", "updateCheck")
				.add("user_id", user_id)
				.add("task_id", String.valueOf(task_id))
				.add("task_check", String.valueOf(task_checked));
		httpClient.post("", params, new AsyncCallback() {

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}

			@Override
			public void onComplete(final HttpResponse httpResponse) {
				response = httpResponse.getBodyAsString();
			}
		});
	}

	public void updateID(long row_id, String user_id, long newTaskID) {
		params = httpClient.newParams().add("method", "updateID")
				.add("row_id", String.valueOf(row_id)).add("user_id", user_id)
				.add("task_id", String.valueOf(newTaskID));
		httpClient.post("", params, new AsyncCallback() {

			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}

			@Override
			public void onComplete(final HttpResponse httpResponse) {
				response = httpResponse.getBodyAsString();
			}
		});

	}

	@Override
	public void notifyObserver() {
		for (Observer observer : observers) {
			observer.update(response);
		}

	}

	@Override
	public void Register(com.liorginsberg.lgtasks.Observer newObserver) {
		observers.add(newObserver);

	}

	@Override
	public void Unregister(com.liorginsberg.lgtasks.Observer removeObserver) {
		observers.remove(removeObserver);

	}

}
