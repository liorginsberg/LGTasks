package com.liorginsberg.lgtasks;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ShareActivity extends Activity {

	private long taskID = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().getAttributes().windowAnimations = R.style.slide_top;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_share);
		
		taskID = getIntent().getLongExtra("taskID", -1);
		
		ArrayList<Integer> images = new ArrayList<Integer>();
		
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Content to share");
		PackageManager pm = getApplicationContext().getPackageManager();
		List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent, 0);
		for (final ResolveInfo app : activityList) {
		    if(app.activityInfo.name.contains("com.facebook.katana")){
		    	images.add(R.drawable.face);
		    }
		    if(app.activityInfo.name.contains("com.google.android.googlequicksearchbox")){
		    	images.add(R.drawable.google);
		    }
		    if(app.activityInfo.name.contains("com.android.mms.ui.ComposeMessageActivity")){
		    	images.add(R.drawable.message);
		    }
		    if(app.activityInfo.name.contains("com.android.mail.compose.ComposeActivityGmail")){
		    	images.add(R.drawable.gmail);
		    }
		    if(app.activityInfo.name.contains("com.twitter.android.PostActivity")){
		    	images.add(R.drawable.tweet);
		    }
		    if(app.activityInfo.name.contains("com.skype.raider.Main")){
		    	images.add(R.drawable.skype);
		    }

		}
		
		GridView gridview = (GridView) findViewById(R.id.gridview);
		
	
	    gridview.setAdapter(new ImageAdapter(this, images));

	    gridview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            Intent resIntent = new Intent();
	            resIntent.putExtra("action", (Integer)v.getTag());
	            resIntent.putExtra("taskID", taskID);
	        	setResult(RESULT_OK, resIntent);
	        	finish();
	        }
	    });
	}


	class ImageAdapter extends BaseAdapter {
		private Context mContext;
		private ArrayList<Integer> mThumbIds;

		public ImageAdapter(Context c, ArrayList<Integer> images) {
			mContext = c;
			mThumbIds = images;
		}

		public int getCount() {
			return mThumbIds.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, initialize some
										// attributes
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageResource(mThumbIds.get(position));
			imageView.setTag(mThumbIds.get(position));
			return imageView;
		}

	}
}
