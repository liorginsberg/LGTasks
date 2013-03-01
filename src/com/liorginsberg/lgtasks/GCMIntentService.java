package com.liorginsberg.lgtasks;

import static com.liorginsberg.lgtasks.CommonUtilities.SENDER_ID;
import static com.liorginsberg.lgtasks.CommonUtilities.notifyApp;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;


public class GCMIntentService extends GCMBaseIntentService {

	
    public GCMIntentService() {
        super(SENDER_ID);
       
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        ServerUtilities.register(context, TaskListActivity.user_name, TaskListActivity.user_email, registrationId);
    }

    /**
     * Method called on device unregistered
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
       
        ServerUtilities.unregister(context, registrationId);
    }

    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
       
        String message = intent.getExtras().getString("message");
        if(message.contains("action\": \"addTask")) {
        	
        	generateNotification(context, "New Task added to LGtasks");
        }
        
        notifyApp(context, message);
    }

    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
      
        String message = getString(R.string.gcm_deleted, total);
       
        // notifies user
        generateNotification(context, message);
    }

    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
      
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    public static void generateNotification(Context context, String message) {
      
    	Intent myIntent = new Intent(context, TaskListActivity.class);
    	myIntent.setAction(Intent.ACTION_MAIN);
    	myIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context)
		.setContentTitle(message)
		.setContentText(message)
		.setSmallIcon(R.drawable.no1)
		.setContentIntent(pendingIntent)
		.setTicker("New message from LGTasks").build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notificationManager.notify(1234, notification);

    }

}
