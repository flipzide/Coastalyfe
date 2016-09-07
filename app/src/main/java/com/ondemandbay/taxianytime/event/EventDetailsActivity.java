/**
 * 
 */
package com.ondemandbay.taxianytime.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ondemandbay.taxianytime.ActionBarBaseActivitiy;
import com.ondemandbay.taxianytime.R;

/**
 * @author Elluminati elluminati.in
 * 
 */
public class EventDetailsActivity extends ActionBarBaseActivitiy {

	private TextView tvEventName, tvEventAddr, tvEventTime, tvEventMember,
			tvEventMaxAmnt, tvEventPromo;
	private Date newDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_details);
		setIconMenu(R.drawable.back);
		btnNotification.setVisibility(View.GONE);
		setTitle(getString(R.string.text_event_details));

		Event event = (Event) getIntent().getSerializableExtra("event");
		tvEventName = (TextView) findViewById(R.id.tvEventName);
		tvEventAddr = (TextView) findViewById(R.id.tvEventAddr);
		tvEventTime = (TextView) findViewById(R.id.tvEventTime);
		tvEventMember = (TextView) findViewById(R.id.tvEventMember);
		tvEventMaxAmnt = (TextView) findViewById(R.id.tvEventMaxAmnt);
		tvEventPromo = (TextView) findViewById(R.id.tvEventPromo);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		try {
			newDate = sdf.parse(event.getEventDateTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		sdf = new SimpleDateFormat("EEE, MMM d hh:mm a");
		String startTime = sdf.format(newDate);

		tvEventName.setText(event.getEventName());
		tvEventAddr.setText(event.getEventAddrs());
		tvEventTime.setText(startTime);
		tvEventMember.setText(event.getEventMember() + "");
		tvEventMaxAmnt.setText("$" + event.getEventPayPerPerson());
		tvEventPromo.setText(event.getEventPromo());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnActionMenu:
			onBackPressed();
			break;

		default:
			break;
		}
	}

	@Override
	protected boolean isValidate() {
		return false;
	}
}
