/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Advanced Android Development_
    http://commonsware.com/AdvAndroid
 */

package com.liorginsberg.lgtasks;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

	public static final String ACTION_UPDATE = "com.liorginsberg.lgtasks.UPDATE_MY_WIDGET";
	
	@Override
	public void onUpdate(Context ctxt, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		Log.i("WidgetProvider","onUpdate");

		
		for (int i = 0; i < appWidgetIds.length; i++) {
		
			Intent svcIntent = new Intent(ctxt, WidgetService.class);

			svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

			RemoteViews widget = new RemoteViews(ctxt.getPackageName(), R.layout.widget);

			widget.setRemoteAdapter(R.id.tasks, svcIntent);
			
			appWidgetManager.updateAppWidget(appWidgetIds[i], widget);
		}

		super.onUpdate(ctxt, appWidgetManager, appWidgetIds);
	}
	
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		Log.i("WidgetProvider","onRecive");
//
//		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
//		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
//		
//		onUpdate(context, appWidgetManager, appWidgetIds);
//		
//		super.onReceive(context, intent);
//	}
}