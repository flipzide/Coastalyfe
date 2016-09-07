package com.ondemandbay.taxianytime.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.callback.ImageOptions;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.component.MyFontTextView;
import com.ondemandbay.taxianytime.parse.AsyncTaskCompleteListener;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

public class EventAdapter extends BaseAdapter implements
		AsyncTaskCompleteListener, OnClickListener {
	private LayoutInflater inflater;
	private ArrayList<Event> list;
	private ImageOptions imageOptions;

	SimpleDateFormat data = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private Activity context;
	private PreferenceHelper pHelper;
	private Dialog deleteCardDialog;
	private MyFontTextView tvDeleteCardOk;
	private MyFontTextView tvDeleteCardCancel;
	private int selectedPosition = -1;
	private TextView tvDialogMsg;

	public EventAdapter(Activity activity, ArrayList<Event> eventList) {
		this.list = eventList;
		context = activity;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		imageOptions = new ImageOptions();
		imageOptions.fileCache = true;
		imageOptions.memCache = true;
		imageOptions.fallback = R.drawable.no_items;
		pHelper = new PreferenceHelper(context);
	}

	@Override
	public int getCount() {
		AppLog.Log("List size", String.valueOf(list.size()));
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Date newDate = new Date();
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.event_item, parent, false);
			holder = new ViewHolder();

			holder.tvEventName = (TextView) convertView
					.findViewById(R.id.tvEventName);
			holder.tvEventTime = (TextView) convertView
					.findViewById(R.id.tvEventTime);
			holder.tvEventAddr = (TextView) convertView
					.findViewById(R.id.tvEventAddr);
			holder.ivEventDelete = (ImageView) convertView
					.findViewById(R.id.ivEventDelete);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Event event = list.get(position);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		try {
			newDate = sdf.parse(event.getEventDateTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		sdf = new SimpleDateFormat("EEE, MMM d hh:mm a");
		String startTime = sdf.format(newDate);
		holder.tvEventName.setText(event.getEventName());
		holder.tvEventTime.setText("" + startTime);
		holder.tvEventAddr.setText(event.getEventAddrs());
		holder.ivEventDelete.setTag(position);
		holder.ivEventDelete.setOnClickListener(this);
		return convertView;
	}

	private class ViewHolder {
		TextView tvEventName, tvEventTime, tvEventAddr;
		ImageView ivEventDelete;
	}

	private void deleteEvent() {
		AndyUtils.showCustomProgressDialog(context,
				context.getString(R.string.text_deleting_event), true, null);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Const.URL, Const.ServiceType.DELETE_EVENT);
		map.put(Const.Params.ID, String.valueOf(pHelper.getUserId()));
		map.put(Const.Params.TOKEN, String.valueOf(pHelper.getSessionToken()));
		map.put(Const.Params.EVENT_ID,
				String.valueOf(list.get(selectedPosition).getEventId()));
		new HttpRequester(context, map, Const.ServiceCode.DELETE_EVENT, this);

		// requestQueue.add(new VolleyHttpRequest(Method.POST, map,
		// Const.ServiceCode.DELETE_EVENT, this, this));
	}

	public void showDeleteEventDialog() {
		deleteCardDialog = new Dialog(context);
		deleteCardDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		deleteCardDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		deleteCardDialog.setContentView(R.layout.dialog_delete_card);
		tvDialogMsg = (TextView) deleteCardDialog
				.findViewById(R.id.tvDialogMsg);
		tvDialogMsg.setText(context.getResources().getString(
				R.string.dialog_delete_event));
		tvDeleteCardOk = (MyFontTextView) deleteCardDialog
				.findViewById(R.id.tvDeleteCardOk);
		tvDeleteCardCancel = (MyFontTextView) deleteCardDialog
				.findViewById(R.id.tvDeleteCardCancel);

		tvDeleteCardOk.setOnClickListener(this);
		tvDeleteCardCancel.setOnClickListener(this);
		deleteCardDialog.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivEventDelete:
			selectedPosition = Integer.parseInt(v.getTag().toString());
			showDeleteEventDialog();
			break;

		case R.id.tvDeleteCardOk:
			deleteCardDialog.dismiss();
			deleteEvent();
			break;

		case R.id.tvDeleteCardCancel:
			deleteCardDialog.dismiss();
			break;
		default:
			break;
		}
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		switch (serviceCode) {
		case Const.ServiceCode.DELETE_EVENT:
			AppLog.Log("", "Delete event Response : " + response);
			AndyUtils.removeCustomProgressDialog();
			if (new ParseContent(context).isSuccess(response)) {
				list.remove(list.get(selectedPosition));
				notifyDataSetChanged();
				AndyUtils.showToast(
						context.getString(R.string.text_success_delete_event),
						context);
			}
			break;
		}
	}
}
