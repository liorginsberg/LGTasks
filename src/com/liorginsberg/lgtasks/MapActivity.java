package com.liorginsberg.lgtasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends Activity implements LocationListener {

	static final LatLng KIEL = new LatLng(53.551, 9.993);
	private GoogleMap map;
	private LatLng myLatLng;
	private LocationManager locationManager;
	private Marker you;
	private ProgressDialog progressDialog;
	private SeekBar sbRadOuter;
	private TextView tvDist;
	private Polyline outerCircle;
	private CheckBox chbShowAll;

	private ArrayList<MarkerOptions> markers;
	private ArrayList<Marker> markersOnMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_map);

		SharedPreferences prefs = getApplicationContext().getSharedPreferences(TaskListActivity.PREFS_NAME, 0);
		boolean isShowAll = prefs.getBoolean("mapShowAll", false);

		chbShowAll = (CheckBox) findViewById(R.id.chbShowAll);
		chbShowAll.setChecked(isShowAll);
		chbShowAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = TaskListActivity.settings.edit();
				editor.putBoolean("mapShowAll", isChecked).commit();
				sbRadOuter.setEnabled(!isChecked);
				outerCircle.setVisible(!isChecked);
				showAll(isChecked);
			}
		});

		tvDist = (TextView) findViewById(R.id.tvDist);
		sbRadOuter = (SeekBar) findViewById(R.id.sbRad);
		sbRadOuter.setOnSeekBarChangeListener(new mapSeeksListener());
		sbRadOuter.setProgress(1);
		sbRadOuter.setEnabled(!isShowAll);

		ProgressDialog loadProgress = ProgressDialog.show(this, "Finding your location", "Please wait...", true);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setInfoWindowAdapter(new myTaskInfoAdapter());
		map.getUiSettings().setZoomControlsEnabled(false);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

		Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (loc == null) {
			progressDialog = ProgressDialog.show(this, "Finding your location", "Please wait...", true);
		} else {
			updateMarker(loc);
		}
		setMarkersOptions(loc);

		loadProgress.dismiss();

		outerCircle = drawCircle(myLatLng, ((double) sbRadOuter.getProgress()) / 10d, Color.BLUE, 5);
		outerCircle.setVisible(!isShowAll);

		map.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition camera) {
				Log.i("ZOOM", "zoom" + camera.zoom);
				if (camera.zoom >= 15.0) {
					sbRadOuter.setMax(10);
				} else if (camera.zoom < 15.0 && camera.zoom >= 14.0) {
					sbRadOuter.setMax(20);
				} else if (camera.zoom < 14.0 && camera.zoom >= 13.5) {
					sbRadOuter.setMax(30);
				} else if (camera.zoom < 13.5 && camera.zoom >= 12.5) {
					sbRadOuter.setMax(50);
				} else if (camera.zoom < 12.5 && camera.zoom >= 12.0) {
					sbRadOuter.setMax(100);
				} else if (camera.zoom < 12.0 && camera.zoom >= 11.0) {
					sbRadOuter.setMax(200);
				} else if (camera.zoom < 11.0 && camera.zoom >= 10.0) {
					sbRadOuter.setMax(400);
				} else if (camera.zoom < 10.0 && camera.zoom >= 9.0) {
					sbRadOuter.setMax(800);
				} else if (camera.zoom < 9.0 && camera.zoom >= 8.0) {
					sbRadOuter.setMax(1600);
				} else if (camera.zoom < 8.0 && camera.zoom >= 7.0) {
					sbRadOuter.setMax(3200);
				} else if (camera.zoom < 7.0 && camera.zoom >= 6.0) {
					sbRadOuter.setMax(6400);
				} else if (camera.zoom < 6.0 && camera.zoom >= 5.0) {
					sbRadOuter.setMax(12800);
				} else if (camera.zoom < 5.0 && camera.zoom >= 4.0) {
					sbRadOuter.setMax(25600);
				} else if (camera.zoom < 4.0 && camera.zoom >= 3.0) {
					sbRadOuter.setMax(51200);
				}
			}
		});

	}

	private void showAll(boolean showAll) {

		for (Marker marker : markersOnMap) {
			if (showAll) {
				marker.setVisible(true);
			} else {
				Location myLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				Location markerLoc = new Location("marker");
				markerLoc.setLatitude(marker.getPosition().latitude);
				markerLoc.setLongitude(marker.getPosition().longitude);
				if (myLoc.distanceTo(markerLoc) > sbRadOuter.getProgress() * 100) {
					marker.setVisible(false);
				} else {
					marker.setVisible(true);
				}
			}
		}
	}

	private void setMarkersOptions(Location loc) {

		boolean visible = true;
		if (markersOnMap == null) {
			markersOnMap = new ArrayList<Marker>();
		}

		for (Task task : TaskList.getInstance(getApplicationContext()).getTasks()) {

			LatLng temp = getLatLngIfPossible(task.getLocation());
			if (temp != null) {
				Location tloc = new Location(task.getLocation());
				tloc.setLatitude(temp.latitude);
				tloc.setLongitude(temp.longitude);
				float dist = tloc.distanceTo(loc);

				Marker tempMarker = map.addMarker(new MarkerOptions().position(temp).visible(visible).title(String.valueOf(task.getTask_id()))
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.marke_65px)));

				if (dist > sbRadOuter.getProgress() * 100) {
					tempMarker.setVisible(false);
				}

				markersOnMap.add(tempMarker);
			}

		}
	}

	private void updateMarker(Location loc) {
		myLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());

		if (you == null) {
			you = map.addMarker(new MarkerOptions().position(myLatLng).title("Your here").snippet("What do you want to do")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_user)));
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));

		} else {
			you.setPosition(myLatLng);
		}

	}

	@Override
	protected void onResume() {

		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onLocationChanged(Location location) {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		Log.i("LOCATION", "onLocationChanged");
		updateMarker(location);
	}

	public LatLng getLatLngIfPossible(String location) {
		List<Address> address = null;
		try {
			address = new Geocoder(getApplicationContext()).getFromLocationName(location, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (address != null) {
			if (address.size() == 1) {
				return new LatLng(address.get(0).getLatitude(), address.get(0).getLongitude());
			}
		}
		return null;

	}

	class myTaskInfoAdapter implements InfoWindowAdapter {

		@Override
		public View getInfoContents(Marker arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public View getInfoWindow(Marker marker) {
			long task_id;
			try {
				task_id = Long.parseLong(marker.getTitle());
			} catch (Exception e) {
				task_id = -1;
			}
			if (task_id != -1) {
				LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View windowview = inflater.inflate(R.layout.task_window, null);

				Task task = TaskList.getInstance(getApplicationContext()).getTaskById(Long.parseLong(marker.getTitle()));

				((TextView) windowview.findViewById(R.id.tvTitleWin)).setText(task.getTitle());
				((TextView) windowview.findViewById(R.id.tvDescriptionWin)).setText(task.getDesc());
				((TextView) windowview.findViewById(R.id.tvLocationWin)).setText(task.getLocation());
				((TextView) windowview.findViewById(R.id.tvFromToWin)).setText(task.getFromTo());
				String status = (task.isChecked() == 1) ? "Done" : "Not Done";
				((TextView) windowview.findViewById(R.id.tvStatusWin)).setText(status);
				return windowview;
			} else {
				return null;
			}
		}

	}

	class mapSeeksListener implements OnSeekBarChangeListener {

		private LatLng markerLatLng;
		private Location markerLocation = new Location("");

		@Override
		public void onProgressChanged(SeekBar seekBar, int value, boolean byUser) {
			Location you = new Location("you");
			tvDist.setText("RADIUS: " + ((double) value) / 10 + "km");
			if (seekBar == sbRadOuter && byUser) {
				you.setLatitude(myLatLng.latitude);
				you.setLongitude(myLatLng.longitude);
				if (value != 0) {
					Polyline temp = drawCircle(myLatLng, ((double) value) / 10, Color.BLUE, 5);
					outerCircle.remove();
					outerCircle = temp;

					for (Marker marker : markersOnMap) {
						markerLatLng = marker.getPosition();
						markerLocation.setLatitude(markerLatLng.latitude);
						markerLocation.setLongitude(markerLatLng.longitude);
						if (you.distanceTo(markerLocation) > value * 100) {
							marker.setVisible(false);
						} else {
							marker.setVisible(true);
						}
					}

				}

			}

		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

	}

	private Polyline drawCircle(LatLng latLng, double radius, int color, float width) {
		double R = 6371d; // earth's mean radius in km
		double d = radius / R; // radius given in km
		double lat1 = Math.toRadians(latLng.latitude);
		double lon1 = Math.toRadians(latLng.longitude);
		PolylineOptions options = new PolylineOptions();
		for (int x = 0; x <= 360; x++) {
			double brng = Math.toRadians(x);
			double latitudeRad = Math.asin(Math.sin(lat1) * Math.cos(d) + Math.cos(lat1) * Math.sin(d) * Math.cos(brng));
			double longitudeRad = (lon1 + Math.atan2(Math.sin(brng) * Math.sin(d) * Math.cos(lat1),
					Math.cos(d) - Math.sin(lat1) * Math.sin(latitudeRad)));
			options.add(new LatLng(Math.toDegrees(latitudeRad), Math.toDegrees(longitudeRad)));
		}
		return map.addPolyline(options.color(color).width(width));
	}
}
