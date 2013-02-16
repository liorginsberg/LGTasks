package com.liorginsberg.lgtasks;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

public class TaskAdapter extends ArrayAdapter<Task> {

	private static TaskAdapter instance = null;


	private TaskList taskList;

	private enum TaskPopupMenu {
		REMOVE, EDIT, OPEN, SHARE, WAZE
	};

	Context context;

	protected GestureDetector gt;

	private TaskAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId, TaskList.getInstance(context).getTasks());
		this.context = context;
		this.taskList = TaskList.getInstance(context);
	}

	public static TaskAdapter getInstance(Context context, int textViewResourceId) {
		if (instance == null) {
			instance = new TaskAdapter(context, textViewResourceId);
		}
		return instance;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final Task t = TaskList.getInstance(context).getTasks().get(position);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.tasklist_item, null);

			holder = new ViewHolder();
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTaskTitle);
			holder.chbDone = (CheckBox) convertView.findViewById(R.id.chbDone);
			holder.tvDate = (TextView) convertView.findViewById(R.id.tvTaskDate);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.chbDone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				boolean isChecked = ((CheckBox)v).isChecked();
				boolean setOnDB = false;
				if (isChecked) {
					setOnDB = TaskList.getInstance(context).setDone(t.getTask_id(), 1);
					if (setOnDB) {
						t.setChecked(1);
					}
				} else {
					TaskList.getInstance(context).setDone(t.getTask_id(), 0);
					t.setChecked(0);
				}
				notifyDataSetChanged();
			}
		}); 

		holder.tvTitle.setText(t.getTitle());
		holder.tvDate.setText(t.getFromTo());
		
		convertView.setOnLongClickListener(new OnItemLongClickListener(position));


		int isChecked = t.isChecked();
		boolean ch = isChecked == 0 ? false : true;
		holder.chbDone.setChecked(ch);

		return convertView;
	}

	static class ViewHolder {
		TextView tvTitle;
		CheckBox chbDone;
		TextView tvDate;
	}

	class OnItemLongClickListener implements OnLongClickListener {

		private int position;

		public OnItemLongClickListener(int position) {
			this.position = position;
		}

		public boolean onLongClick(View v) {
			Log.i("LONG", "pressed " + position);
			v.setBackgroundColor(Color.CYAN);
		
			showPopupMenu(v, position);
			
			return false;
		}

		private void showPopupMenu(final View v, final int position) {

			
			PopupMenu popupMenu = new PopupMenu(context, v);
			popupMenu.getMenuInflater().inflate(R.menu.item_popup_menu, popupMenu.getMenu());
			
			popupMenu.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(PopupMenu menu) {
					v.setBackgroundColor(Color.TRANSPARENT);
				}
			});
			
			popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

				public boolean onMenuItemClick(MenuItem item) {
					
					v.setBackgroundColor(Color.TRANSPARENT);
					TaskPopupMenu itemClicked = TaskPopupMenu.valueOf(item.getTitle().toString().toUpperCase());
					switch (itemClicked) {
					case REMOVE:
						if (TaskList.getInstance(context).removeTask(position) == 1) {
							notifyDataSetChanged();
						} else {
							Toast.makeText(context, TaskList.getInstance(context).getTaskAt(position).getDesc(), Toast.LENGTH_LONG).show();
						}
						break;
					case EDIT:
						Intent editTaskIntent = new Intent();
						editTaskIntent.setClass(context, AddTaskActivity.class);
						editTaskIntent.putExtra("position", position);
						((Activity) context).startActivityForResult(editTaskIntent, TaskListActivity.EDIT_TASK_REQUEST_CODE);
						break;
					case OPEN:
						Toast.makeText(context, TaskList.getInstance(context).getTaskAt(position).getTitle(), Toast.LENGTH_LONG).show();
						break;
					case SHARE:
//						 final Intent shareIntent = new Intent(Intent.ACTION_DIAL);
//						 shareIntent.setType("text/plain");
//						 shareIntent.putExtra(Intent.EXTRA_TEXT, TaskList.getInstance(context).getTaskAt(position).getTitle());
//						 shareIntent.putExtra(Intent.EXTRA_SUBJECT, TaskList.getInstance(context).getTaskAt(position).getDesc());
//
//					        try {
//					          context.startActivity(Intent.createChooser(shareIntent, "Select an action"));
//					        } catch (android.content.ActivityNotFoundException ex) {
//					          // (handle error)
//					        }
						
//						Intent postOnFacebookWallIntent = new Intent(context, ShareOnFacebook.class);
//						postOnFacebookWallIntent.putExtra("position", position);
//						context.startActivity(postOnFacebookWallIntent);
						break;
					case WAZE:
						String location = TaskList.getInstance(context).getTaskAt(position).getLocation();
						if (location.equals("Not specified")) {
							Toast.makeText(context, location, Toast.LENGTH_SHORT).show();
						} else {
							try {
								List<Address> address;
								try {
									address = new Geocoder(context).getFromLocationName(location, 1);
								} catch (IOException e) {
									address = null;
									e.printStackTrace();
								}
								if (address == null) {
									Toast.makeText(context, "Could not aquire your location", Toast.LENGTH_SHORT).show();
									return false;
								}

								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("waze://?ll=" + address.get(0).getLatitude() + ","
										+ address.get(0).getLongitude() + "&navigate=yes"));
								context.startActivity(intent);
							} catch (ActivityNotFoundException ex) {
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
								context.startActivity(intent);
							}
						}

						break;
					default:
						break;
					}

					return true;
				}
			});

			popupMenu.show();
		}
	}
	
}
