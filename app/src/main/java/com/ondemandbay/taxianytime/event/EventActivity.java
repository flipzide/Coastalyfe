/**
 * 
 */
package com.ondemandbay.taxianytime.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ondemandbay.taxianytime.ActionBarBaseActivitiy;
import com.ondemandbay.taxianytime.R;
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
public class EventActivity extends ActionBarBaseActivitiy implements
		OnItemClickListener {
	private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
	private EventAdapter eventAdapter;
	private ArrayList<Event> eventList;
	// private ArrayList<History> historyListOrg;
	private PreferenceHelper preferenceHelper;
	private ParseContent parseContent;
	private ImageView tvNoHistory;
	private ArrayList<Date> dateList = new ArrayList<Date>();
	private TextView fromDateBtn;
	private TextView toDateBtn;
	Calendar cal = Calendar.getInstance();
	int day;
	int month;
	int year;
	DatePickerDialog fromPiker;
	private OnDateSetListener dateset;
	DatePickerDialog toPiker;
	Date fromDate, toDate;
	private String userDate;
	private ParseContent pContent;
	private ListView lvEvents;

	// for parsing
	private final String ID = "id";
	private final String KEY_SUCCESS = "success";
	private final String OWNER_ID = "owner_id";
	private final String EVENT_NAME = "event_name";
	private final String EVENT_ADDRS = "event_place_address";
	private final String EVENT_LAT = "event_latitude";
	private final String EVENT_LNG = "event_longitude";
	private final String EVENT_MEMBER = "event_total_members";
	private final String EVENT_PAY_PER_PERSON = "event_pre_pay_for_each_member";
	private final String START_TIME = "start_time";
	private final String ALL_SCHEDULED_REQUEST = "all_scheduled_requests";
	private final String PROMO_CODE = "promo_code";

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v7.app.ActionBarActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);
		setIconMenu(R.drawable.back);
		setTitle(getString(R.string.text_event));
		btnNotification.setVisibility(View.GONE);
		pContent = new ParseContent(this);
		lvEvents = (ListView) findViewById(R.id.lvEvents);
		lvEvents.setOnItemClickListener(this);
		// fromDateBtn = (TextView) findViewById(R.id.fromDateBtn);
		// toDateBtn = (TextView) findViewById(R.id.toDateBtn);
		// fromDateBtn.setOnClickListener(this);
		// toDateBtn.setOnClickListener(this);
		eventList = new ArrayList<Event>();
		//
		tvNoHistory = (ImageView) findViewById(R.id.ivEmptyView);

		preferenceHelper = new PreferenceHelper(this);
		findViewById(R.id.btnAddEvent).setOnClickListener(this);
		// parseContent = new ParseContent(this);
		// dateList = new ArrayList<Date>();
		// historyListOrg = new ArrayList<History>();
		//
		// // historyAdapter = new HistoryAdapter(this, historyListOrg,
		// // mSeparatorsSet);
		// // lvHistory.setAdapter(historyAdapter);
		//
		// day = cal.get(Calendar.DAY_OF_MONTH);
		// month = cal.get(Calendar.MONTH);
		// year = cal.get(Calendar.YEAR);
		// fromDate = new Date();
		// toDate = new Date();
		//

		//
		// dateset = new OnDateSetListener() {
		//
		// @Override
		// public void onDateSet(DatePicker view, int year, int monthOfYear,
		// int dayOfMonth) {
		// userDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
		//
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
		//
		// try {
		// if (view == fromPiker.getDatePicker()) {
		// fromDateBtn.setText(userDate);
		// fromDate = sdf.parse(userDate);
		// } else {
		// toDateBtn.setText(userDate);
		// toDate = sdf.parse(userDate);
		// }
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
		//
		// }
		// };
		// fromPiker = new DatePickerDialog(this, dateset, year, month, day);
		// fromPiker.getDatePicker().setMaxDate(System.currentTimeMillis());
		// toPiker = new DatePickerDialog(this, dateset, year, month, day);
		// toPiker.getDatePicker().setMaxDate(System.currentTimeMillis());

	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		switch (serviceCode) {
		case Const.ServiceCode.GET_EVENT:
			AndyUtils.removeCustomProgressDialog();
			AppLog.Log("", "Get Events Response : " + response);
			if (!pContent.isSuccess(response)) {
				return;
			}
			eventList.clear();
			// dateList.clear();
			try {
				parseEvent(response, eventList);

				if (eventList.size() > 0) {
					lvEvents.setVisibility(View.VISIBLE);
					tvNoHistory.setVisibility(View.GONE);
				} else {
					lvEvents.setVisibility(View.GONE);
					tvNoHistory.setVisibility(View.VISIBLE);
				}
				eventAdapter = new EventAdapter(this, eventList);
				lvEvents.setAdapter(eventAdapter);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
	}

	private void getEvents() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Const.URL, Const.ServiceType.GET_EVENT);
		map.put(Const.Params.ID, preferenceHelper.getUserId());
		map.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());

		AndyUtils.showCustomProgressDialog(this,
				getString(R.string.text_getting_event), true, null);

		new HttpRequester(this, map, Const.ServiceCode.GET_EVENT, this);
		// requestQueue.add(new VolleyHttpRequest(Method.POST, map,
		// Const.ServiceCode.GET_EVENT, this, this));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnActionMenu:
			onBackPressed();
			break;

		case R.id.btnAddEvent:
			startActivity(new Intent(this, AddEventActivity.class));
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Event selectedEvent = eventList.get(arg2);
		Intent intent = new Intent(this, EventDetailsActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("event", selectedEvent);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	protected boolean isValidate() {
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		getEvents();
	}

	public ArrayList<Event> parseEvent(String response, ArrayList<Event> list) {
		list.clear();
		if (TextUtils.isEmpty(response)) {
			return list;
		}
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.getBoolean(KEY_SUCCESS)) {
				JSONArray jsonArray = jsonObject
						.getJSONArray(ALL_SCHEDULED_REQUEST);
				if (jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject object = jsonArray.getJSONObject(i);
						Event event = new Event();
						event.setEventId(object.getInt(ID));
						event.setOwnerId(object.getInt(OWNER_ID));
						event.setEventName(object.getString(EVENT_NAME));
						event.setEventAddrs(object.getString(EVENT_ADDRS));
						event.setEventMember(object.getInt(EVENT_MEMBER));
						event.setEventDateTime(object.getString(START_TIME));
						event.setEventLat(object.getDouble(EVENT_LAT));
						event.setEventLong(object.getDouble(EVENT_LNG));
						event.setEventPayPerPerson(object
								.getDouble(EVENT_PAY_PER_PERSON));
						event.setEventPromo(object.getString(PROMO_CODE));
						list.add(event);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}
}
