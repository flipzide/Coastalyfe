package com.ondemandbay.taxianytime;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

import com.ondemandbay.taxianytime.component.MyFontButton;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;

public class MainActivity extends Activity implements OnClickListener {

	/** Called when the activity is first created. */
	private MyFontButton btnSignIn, btnRegister;
	private PreferenceHelper pHelper;
	private boolean isReceiverRegister;
//	private int oldOptions;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		// Window window = getWindow();
		// window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		// window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		// window.setStatusBarColor(getResources().getColor(
		// R.color.color_action_bar_main));
		//
		// }
		if (!TextUtils.isEmpty(new PreferenceHelper(this).getUserId())) {
			startActivity(new Intent(this, MainDrawerActivity.class));
			this.finish();
			return;
		}
		isReceiverRegister = false;
		pHelper = new PreferenceHelper(this);
		setContentView(R.layout.activity_main);

		btnSignIn = (MyFontButton) findViewById(R.id.btnSignInMain);
		btnRegister = (MyFontButton) findViewById(R.id.btnRegister);
		// rlLoginRegisterLayout = (RelativeLayout) view
		// .findViewById(R.id.rlLoginRegisterLayout);
		// tvMainBottomView = (MyFontTextView) view
		// .findViewById(R.id.tvMainBottomView);
		btnSignIn.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
		if (TextUtils.isEmpty(pHelper.getDeviceToken())) {
			Log.d("isReceiverRegister", "************true**************" + pHelper.getDeviceToken());
			AndyUtils.showCustomProgressDialog(this, getString(R.string.progress_loading), false, null);
			isReceiverRegister = true;
			registerReceiver(mHandleMessageReceiver, new IntentFilter(
					Const.DISPLAY_MESSAGE_REGISTER));
		}
	}
//
//	public void registerGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
//		if (mHandleMessageReceiver != null) {
//			AndyUtils.showCustomProgressDialog(this,
//					getString(R.string.progress_loading), false, null);
//			new GCMRegisterHendler(this, mHandleMessageReceiver);
//
//		}
//	}
//
//	public void unregisterGcmReceiver(BroadcastReceiver mHandleMessageReceiver) {
//		if (mHandleMessageReceiver != null) {
//			if (mHandleMessageReceiver != null) {
//				unregisterReceiver(mHandleMessageReceiver);
//			}
//		}
//	}

	private BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("onReceive", "************onReceive**************" + pHelper.getDeviceToken());
			AndyUtils.removeCustomProgressDialog();
			if (intent.getAction().equals(Const.DISPLAY_MESSAGE_REGISTER)) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					int resultCode = bundle.getInt(Const.RESULT);
					AppLog.Log("", "Result code-----> " + resultCode);
					if (resultCode == Activity.RESULT_OK) {
						AppLog.Log("", "Activity BroadCast FCMId :"
								+ pHelper.getDeviceToken());
					} else {
						AndyUtils.showToast(MainActivity.this.getResources().getString(
								R.string.register_gcm_failed), MainActivity.this);
						setResultCode(Activity.RESULT_CANCELED);
						finish();
					}
				}
			}
		}
	};

	@Override
	public void onClick(View v) {
		Intent startRegisterActivity = new Intent(MainActivity.this,
				RegisterActivity.class);
		switch (v.getId()) {
		case R.id.btnSignInMain:
			startRegisterActivity.putExtra("isSignin", true);
			break;
		case R.id.btnRegister:
			startRegisterActivity.putExtra("isSignin", false);
			break;
		}
		startActivity(startRegisterActivity);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
	}

	@Override
	public void onDestroy() {
		if (isReceiverRegister) {
			unregisterReceiver(mHandleMessageReceiver);
			isReceiverRegister = false;
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		openExitDialog();
	}

	public void openExitDialog() {
		final Dialog mDialog = new Dialog(this);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mDialog.setContentView(R.layout.exit_layout);
		mDialog.setCancelable(false);
		mDialog.findViewById(R.id.tvExitOk).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDialog.dismiss();
						finish();
						overridePendingTransition(R.anim.slide_in_left,
								R.anim.slide_out_right);

					}
				});
		mDialog.findViewById(R.id.tvExitCancel).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDialog.dismiss();
					}
				});
		mDialog.show();
	}
}