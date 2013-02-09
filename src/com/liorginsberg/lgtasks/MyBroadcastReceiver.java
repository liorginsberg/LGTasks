package com.liorginsberg.lgtasks;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {
	
	private final int REMINDME_NOTIFICATION_ID = 1234;
	
	@Override
	public void onReceive(Context context, Intent intent) {

		Intent myIntent = new Intent(context, TaskListActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification notification = new Notification.Builder(context)
				.setContentTitle(intent.getStringExtra("title"))
				.setContentText(intent.getStringExtra("desc"))
				.setSmallIcon(R.drawable.no1)
				.setContentIntent(pendingIntent)
				.setTicker("Just Reminding You!").build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notificationManager.notify(REMINDME_NOTIFICATION_ID, notification);
		
	}
}