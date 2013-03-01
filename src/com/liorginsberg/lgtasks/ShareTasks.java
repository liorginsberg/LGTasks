package com.liorginsberg.lgtasks;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;

public class ShareTasks {

	private static final int REAUTH_ACTIVITY_CODE = 100;

	private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");
	
	public static final String ADD = "add";
	public static final String FINISH = "finish";

	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private final String app_name = "LG Tasks";

	private Activity activity;

	public ShareTasks(Activity activity) {

		this.activity = activity;
	}

	/**
	 * this method post a task on facebook wall with the task title and
	 * description
	 * 
	 * @param activity
	 *            - the calling activity
	 * @param taskToPost
	 *            - Task object to publish
	 * @param message
	 *            - personal message with the post (optional)
	 */
	public void postToFacebookWall(Task taskToPost, String message) {
		Session session = Session.getActiveSession();

		if (session != null) {

			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(activity, PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}

			JSONObject privacy = new JSONObject();
			try {
				privacy.put("value", "SELF");
			} catch (JSONException e) {
				
			}

			Bundle postParams = new Bundle();
			postParams.putString("name", app_name);
			postParams.putString("caption", taskToPost.getTitle());
			postParams.putString("description", taskToPost.getDesc());
			postParams.putString("privacy", privacy.toString());
			postParams.putString("picture", "http://liorginsberg.com/android/lgtasks/app_icon.png");

			if (message != null) {
				postParams.putString("message", message);
			}

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
					
					try {
						graphResponse.getString("id");
					} catch (JSONException e) {
						
					}
					FacebookRequestError error = response.getError();
					if (error != null) {
						Toast.makeText(activity.getApplicationContext(), error.getErrorMessage(), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(activity.getApplicationContext(), "Task has been posted on your wall", Toast.LENGTH_LONG).show();
					}
				}
			};

			Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}

	}

	public void postOpenGraphActivitiy(String action, Task task) {
		final String actionPath = "me/lgtasks:" + action;

		Session session = Session.getActiveSession();

		if (session == null) {
			return;
		}

		List<String> permissions = session.getPermissions();
		if (!permissions.containsAll(PERMISSIONS)) {
			requestPublishPermissions(session);
			return;
		}

		// progressDialog = ProgressDialog.show(getApplicationContext(),
		// "progress", "progress", true);

		// Run this in a background thread since we don't want to
		// block the main thread. Create a new AsyncTask that returns
		// a Response object
		AsyncTask<Void, Void, Response> asyncTask = new AsyncTask<Void, Void, Response>() {

			@Override
			protected Response doInBackground(Void... voids) {
				// Create an eat action
				FinishAction finishAction = GraphObject.Factory.create(FinishAction.class);
				// Populate the action with the POST parameters:
				// the meal, friends, and place info
				TaskGraphObject task = GraphObject.Factory.create(TaskGraphObject.class);
				task.setUrl("http://liorginsberg.com/android/lgtasks/task.html");
				finishAction.setTask(task);
				finishAction.setProperty("topic", "a");

				// Set up a request with the active session, set up
				// an HTTP POST to the eat action endpoint
				Request request = new Request(Session.getActiveSession(), actionPath, null, HttpMethod.POST);
				// Add the post parameter, the eat action
				request.setGraphObject(finishAction);
				// Execute the request synchronously in the background
				// and return the response.
				return request.executeAndWait();
			}

			@Override
			protected void onPostExecute(Response response) {
				// When the task completes, process
				// the response on the main thread
				onPostActionResponse(response);
			}
		};

		// Execute the task
		asyncTask.execute();

	}

	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}
	
	private void requestPublishPermissions(Session session) {
		if (session != null) {
			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(activity, PERMISSIONS)
					.setRequestCode(REAUTH_ACTIVITY_CODE);
			session.requestNewPublishPermissions(newPermissionsRequest);
		}
	}
	
	private interface TaskGraphObject extends GraphObject {
		public String getUrl();

		public void setUrl(String url);

		public String getId();

		public void setId(String id);
	}
	
	private interface FinishAction extends OpenGraphAction {

		public TaskGraphObject getTask();

		public void setTask(TaskGraphObject task);
	}
	
	private void onPostActionResponse(Response response) {

		if (activity == null) {
			// if the user removes the app from the website,
			// then a request will have caused the session to
			// close (since the token is no longer valid),
			// which means the splash fragment will be shown
			// rather than this one, causing activity to be null.
			// If the activity is null, then we cannot
			// show any dialogs, so we return.
			return;
		}

		PostResponse postResponse = response.getGraphObjectAs(PostResponse.class);

		if (postResponse != null && postResponse.getId() != null) {
			
		} else {
			handleError(response.getError());
		}
	}
	
	private interface PostResponse extends GraphObject {
		String getId();
	}
	
	private void handleError(FacebookRequestError error) {
		DialogInterface.OnClickListener listener = null;
		String dialogBody = null;

		if (error == null) {
			// There was no response from the server.
			dialogBody = "error";
		} else {
			switch (error.getCategory()) {
			case AUTHENTICATION_RETRY:
				// Tell the user what happened by getting the
				// message id, and retry the operation later.
				
				dialogBody = "retry";
				listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						// Take the user to the mobile site.
						Intent intent = new Intent(Intent.ACTION_VIEW, M_FACEBOOK_URL);
						activity.startActivity(intent);
					}
				};
				break;

			case AUTHENTICATION_REOPEN_SESSION:
				// Close the session and reopen it.
				dialogBody = "reopen";
				listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						Session session = Session.getActiveSession();
						if (session != null && !session.isClosed()) {
							session.closeAndClearTokenInformation();
						}
					}
				};
				break;

			case PERMISSION:
				// A permissions-related error
				dialogBody = "premission error";
				listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						// Request publish permission
						requestPublishPermissions(Session.getActiveSession());
					}
				};
				break;

			case SERVER:
			case THROTTLING:
				// This is usually temporary, don't clear the fields, and
				// ask the user to try again.
				dialogBody = "server";
				break;

			case BAD_REQUEST:
				// This is likely a coding error, ask the user to file a bug.
				dialogBody = "bad request";
				break;

			case OTHER:
			case CLIENT:
			default:
				// An unknown issue occurred, this could be a code error, or
				// a server side issue, log the issue, and either ask the
				// user to retry, or file a bug.
				dialogBody = "unknown";
				break;
			}
		}

		// Show the error and pass in the listener so action
		// can be taken, if necessary.
		new AlertDialog.Builder(activity.getApplicationContext()).setPositiveButton("ok", listener).setTitle("error").setMessage(dialogBody).show();
	}

}
