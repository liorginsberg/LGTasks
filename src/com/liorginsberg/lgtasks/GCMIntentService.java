package com.liorginsberg.lgtasks;

import static com.liorginsberg.lgtasks.CommonUtilities.SENDER_ID;
import static com.liorginsberg.lgtasks.CommonUtilities.displayMessage;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.liorginsberg.lgtasks.R;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
        Log.i(TAG, "construct with "+ SENDER_ID);
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        ServerUtilities.register(context, TaskListActivity.user_name, TaskListActivity.user_email, registrationId);
    }

    /**
     * Method called on device un registred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        ServerUtilities.unregister(context, registrationId);
    }

    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String message = intent.getExtras().getString("message");
        
        JSONObject taskJSON = null;
		try {
			taskJSON = new JSONObject(message);
			String action = taskJSON.getString("action");
			Log.i("PUSH", action);
			if(action.equals("addTask")) {
				TaskList.getInstance(getApplicationContext()).addTask(taskJSON.getLong("task_id"), taskJSON.getString("title"), taskJSON.getString("desc"), taskJSON.getString("from"), taskJSON.getString("to"), taskJSON.getString("location"), 0, false, false);
				TaskAdapter.getInstance(context, R.layout.tasklist_item).notifyDataSetChanged();
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
        
        // notifies user
        generateNotification(context, message);
    }

    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
       
        // notifies user
        generateNotification(context, message);
    }

    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
   
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
      
    	Intent myIntent = new Intent(context, TaskListActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context)
		.setContentTitle(message)
		.setContentText(message)
		.setSmallIcon(R.drawable.no1)
		.setContentIntent(pendingIntent)
		.setTicker("New message").build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notificationManager.notify(1234, notification);

    }

}
