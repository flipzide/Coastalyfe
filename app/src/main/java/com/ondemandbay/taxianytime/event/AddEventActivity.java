/**
 * 
 */
package com.ondemandbay.taxianytime.event;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ondemandbay.taxianytime.ActionBarBaseActivitiy;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.adapter.PlacesAutoCompleteAdapter;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

/**
 * @author Elluminati elluminati.in
 * 
 */
public class AddEventActivity extends ActionBarBaseActivitiy implements
		OnItemClickListener {
	private PreferenceHelper preferenceHelper;
	Calendar cal = Calendar.getInstance();
	int day;
	int month;
	int year;
	DatePickerDialog fromPiker;
	private OnDateSetListener dateset;
	DatePickerDialog toPiker;
	Date fromDate, toDate;
	private String userDate;
	private EditText etEventName;

	private EditText etEventMember;
	private EditText etEventAmount;
	private AutoCompleteTextView etEventAddrs;
	private String msg;
	private ParseContent pContent;
	private Dialog dateDialog;
	private Button btnSetDate;
	private DatePicker dateEvent;
	private TimePicker timeEvent;
	private String eventDate;
	private TextView tvEventTime;
	private PlacesAutoCompleteAdapter adapter;
	private LatLng eventLatLng;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_event);
		setIconMenu(R.drawable.back);
		btnNotification.setVisibility(View.GONE);
		setTitle(getString(R.string.text_add_event));

		preferenceHelper = new PreferenceHelper(this);
		pContent = new ParseContent(this);

		findViewById(R.id.btnAddEvent).setOnClickListener(this);
		etEventName = (EditText) findViewById(R.id.etEventName);
		tvEventTime = (TextView) findViewById(R.id.tvEventTime);
		etEventMember = (EditText) findViewById(R.id.etEventMember);
		etEventAmount = (EditText) findViewById(R.id.etEventAmount);
		etEventAddrs = (AutoCompleteTextView) findViewById(R.id.etEventAddrs);
		tvEventTime.setOnClickListener(this);

		adapter = new PlacesAutoCompleteAdapter(this,
				R.layout.autocomplete_list_text);
		etEventAddrs.setAdapter(adapter);
		etEventAddrs.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				final String selectedEventPlace = adapter.getItem(arg2);

				new Thread(new Runnable() {
					@Override
					public void run() {
						final LatLng latlng = getLocationFromAddress(selectedEventPlace);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								etEventAddrs.clearFocus();
								tvEventTime.requestFocus();
								eventLatLng = latlng;
								etEventAddrs.setText(selectedEventPlace);
							}
						});
					}
				}).start();
			}
		});
	}

	private LatLng getLocationFromAddress(final String place) {
		LatLng loc = null;
		Geocoder gCoder = new Geocoder(this);
		try {
			final List<Address> list = gCoder.getFromLocationName(place, 1);
			if (list != null && list.size() > 0) {
				loc = new LatLng(list.get(0).getLatitude(), list.get(0)
						.getLongitude());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return loc;
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		switch (serviceCode) {
		case Const.ServiceCode.ADD_EVENT:
			AndyUtils.removeCustomProgressDialog();
			AppLog.Log("", "Add Event Response : " + response);
			if (pContent.isSuccess(response)) {
				Toast.makeText(this,
						getString(R.string.toast_add_event_success),
						Toast.LENGTH_SHORT).show();
				// startActivity(new Intent(this, EventActivity.class));
				finish();
			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnAddEvent:
			if (isValidate()) {
				AddEvent();
			}
			break;
		case R.id.tvEventTime:
			showDateDialog();
			break;
		case R.id.confirm_schedule:
			int day = dateEvent.getDayOfMonth();
			int month = dateEvent.getMonth() + 1;
			int year = dateEvent.getYear();
			timeEvent.clearFocus();
			int hour = timeEvent.getCurrentHour();
			int minute = timeEvent.getCurrentMinute();
			tvEventTime.setText(day + "-" + month + "-" + year + " " + hour
					+ ":" + minute);
			eventDate = year + "-" + month + "-" + day + " " + hour + ":"
					+ minute + ":00";

			if (dateDialog.isShowing())
				dateDialog.dismiss();
			break;
		case R.id.btnActionMenu:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	private void AddEvent() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Const.URL, Const.ServiceType.ADD_EVENT);
		map.put(Const.Params.ID, preferenceHelper.getUserId());
		map.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
		map.put(Const.Params.NAME, etEventName.getText().toString().trim());
		map.put(Const.Params.START_TIME, eventDate);
		map.put(Const.Params.TIME_ZONE, TimeZone.getDefault().getID());
		map.put(Const.Params.MEMBERS, etEventMember.getText().toString().trim());
		map.put(Const.Params.AMOUNT, etEventAmount.getText().toString().trim());
		map.put(Const.Params.ADDRESS, etEventAddrs.getText().toString().trim());
		map.put(Const.Params.LATITUDE, "" + eventLatLng.latitude);
		map.put(Const.Params.LONGITUDE, "" + eventLatLng.longitude);

		AndyUtils.showCustomProgressDialog(this,
				getString(R.string.text_adding_event), true, null);
		new HttpRequester(this, map, Const.ServiceCode.ADD_EVENT, this);

		// requestQueue.add(new VolleyHttpRequest(Method.POST, map,
		// Const.ServiceCode.ADD_EVENT, this, this));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

	}

	@Override
	protected boolean isValidate() {
		if (TextUtils.isEmpty(etEventName.getText().toString())) {
			msg = getString(R.string.text_enter_event_name);
			etEventName.requestFocus();
		} else if (TextUtils.isEmpty(etEventAddrs.getText().toString())) {
			msg = getString(R.string.text_enter_event_address);
			etEventAddrs.requestFocus();
		} else if (TextUtils.isEmpty(eventDate)) {
			msg = getString(R.string.text_enter_event_time);
		} else if (TextUtils.isEmpty(etEventMember.getText().toString())) {
			msg = getString(R.string.text_enter_event_member);
			etEventMember.requestFocus();
		} else if (TextUtils.isEmpty(etEventAmount.getText().toString())) {
			msg = getString(R.string.text_enter_event_amount);
			etEventAmount.requestFocus();
		} else {
			msg = null;
		}
		if (msg != null) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			return false;
		}
		if (msg == null) {
			return true;
		}
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		return false;
	}

	private void showDateDialog() {
		dateDialog = new Dialog(this);
		dateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dateDialog.setContentView(R.layout.picker_dialog);

		dateEvent = (DatePicker) dateDialog.findViewById(R.id.date_picker);
		timeEvent = (TimePicker) dateDialog.findViewById(R.id.time_picker);
		dateEvent.setMinDate(System.currentTimeMillis() - 1000);
		// dateEvent.setMinDate((System.currentTimeMillis())
		// + (1 * 24 * 60 * 60 * 1000));

		btnSetDate = (Button) dateDialog.findViewById(R.id.confirm_schedule);
		btnSetDate.setOnClickListener(this);
		dateDialog.show();
	}
}
