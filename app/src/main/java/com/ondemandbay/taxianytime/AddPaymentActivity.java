package com.ondemandbay.taxianytime;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import java.util.HashMap;
import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.util.TextUtils;

/**
 * @author Elluminati elluminati.in
 */
public class AddPaymentActivity extends ActionBarBaseActivitiy
// implements PWTokenObtainedListener, PWTransactionListener
{
	// private static final String TAG = "UberAddPaymentActivity";
	private ImageView btnAddPayment/* , btnPaymentSkip */;
	// private ImageView btnScan;
	private final int MY_SCAN_REQUEST_CODE = 111;
	private EditText etCreditCardNum, etCvc, etYear, etMonth;
	// private String patternVisa = "^4[0-9]{12}(?:[0-9]{3})?$";
	// private String patternMasterCard = "^5[1-5][0-9]{14}$";
	// private String patternAmericanExpress = "^3[47][0-9]{13}$";
	public static final String[] PREFIXES_AMERICAN_EXPRESS = { "34", "37" };
	public static final String[] PREFIXES_DISCOVER = { "60", "62", "64", "65" };
	public static final String[] PREFIXES_JCB = { "35" };
	public static final String[] PREFIXES_DINERS_CLUB = { "300", "301", "302",
			"303", "304", "305", "309", "36", "38", "37", "39" };
	public static final String[] PREFIXES_VISA = { "4" };
	public static final String[] PREFIXES_MASTERCARD = { "50", "51", "52",
			"53", "54", "55" };
	public static final String AMERICAN_EXPRESS = "American Express";
	public static final String DISCOVER = "Discover";
	public static final String JCB = "JCB";
	public static final String DINERS_CLUB = "Diners Club";
	public static final String VISA = "Visa";
	public static final String MASTERCARD = "MasterCard";
	public static final String UNKNOWN = "Unknown";
	public static final int MAX_LENGTH_STANDARD = 16;
	public static final int MAX_LENGTH_AMERICAN_EXPRESS = 15;
	public static final int MAX_LENGTH_DINERS_CLUB = 14;
	private String type;
	static final Pattern CODE_PATTERN = Pattern
			.compile("([0-9]{0,4})|([0-9]{4}-)+|([0-9]{4}-[0-9]{0,4})+");
	// private PWCreditCardType cardType;
	// private boolean currentTokenization;
	// private PWProviderBinder _binder;
	//
	// private ServiceConnection _serviceConnection = new ServiceConnection() {
	// @Override
	// public void onServiceConnected(ComponentName name, IBinder service) {
	// _binder = (PWProviderBinder) service;
	// // we have a connection to the service
	// try {
	// _binder.initializeProvider(PWProviderMode.LIVE,
	// Const.APPLICATIONIDENTIFIER, Const.PROFILETOKEN);
	// _binder.addTokenObtainedListener(UberAddPaymentActivity.this);
	// _binder.addTransactionListener(UberAddPaymentActivity.this);
	// } catch (PWException ee) {
	// setStatusText("Error initializing the provider.");
	// // error initializing the provider
	// ee.printStackTrace();
	// }
	// }
	//
	// @Override
	// public void onServiceDisconnected(ComponentName name) {
	// _binder = null;
	// }
	// };
	// private EditText etHolder;
	private PreferenceHelper preference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_payment);
		// startService(new Intent(this,
		// com.mobile.connect.service.PWConnectService.class));
		// bindService(new Intent(this,
		// com.mobile.connect.service.PWConnectService.class),
		// _serviceConnection, Context.BIND_AUTO_CREATE);
		setTitle(getString(R.string.text_add_payment));
		// setIconMenu(R.drawable.ic_payment);
		// setIcon(R.drawable.back);
		preference = new PreferenceHelper(this);
		btnAddPayment = (ImageView) findViewById(R.id.btnAddPayment);
		// btnPaymentSkip = (Button) findViewById(R.id.btnPaymentSkip);
		// btnPaymentSkip.setVisibility(View.GONE);
		// btnScan = (ImageView) findViewById(R.id.btnScan);
		etCreditCardNum = (EditText) findViewById(R.id.edtRegisterCreditCardNumber);
		etCreditCardNum.addTextChangedListener(new TextWatcher() {
			//
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (TextUtils.isBlank(s.toString())) {
					etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
							null, null, null, null);
				}
				type = getType(s.toString());

				if (type.equals(VISA)) {
					etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.ub__creditcard_visa), null,
							null, null);

				} else if (type.equals(MASTERCARD)) {
					etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.ub__creditcard_mastercard),
							null, null, null);

				} else if (type.equals(AMERICAN_EXPRESS)) {
					etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.ub__creditcard_amex), null,
							null, null);

				} else if (type.equals(DISCOVER)) {
					etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.ub__creditcard_discover), null,
							null, null);

				} else if (type.equals(DINERS_CLUB)) {
					etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
							getResources().getDrawable(
									R.drawable.ub__creditcard_discover), null,
							null, null);

				} else {
					etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
							null, null, null, null);
				}
				if (etCreditCardNum.getText().toString().length() == 19) {
					etMonth.requestFocus();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0 && !CODE_PATTERN.matcher(s).matches()) {
					String input = s.toString();
					String numbersOnly = keepNumbersOnly(input);
					String code = formatNumbersAsCode(numbersOnly);
					etCreditCardNum.removeTextChangedListener(this);
					etCreditCardNum.setText(code);
					etCreditCardNum.setSelection(code.length());
					etCreditCardNum.addTextChangedListener(this);
				}
			}

			private String keepNumbersOnly(CharSequence s) {
				return s.toString().replaceAll("[^0-9]", ""); // Should of
																// course be
																// more robust
			}

			private String formatNumbersAsCode(CharSequence s) {
				int groupDigits = 0;
				String tmp = "";
				for (int i = 0; i < s.length(); ++i) {
					tmp += s.charAt(i);
					++groupDigits;
					if (groupDigits == 4) {
						tmp += "-";
						groupDigits = 0;
					}
				}
				return tmp;
			}
		});
		etCvc = (EditText) findViewById(R.id.edtRegistercvc);
		etYear = (EditText) findViewById(R.id.edtRegisterexpYear);
		etMonth = (EditText) findViewById(R.id.edtRegisterexpMonth);
		etYear.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (etYear.getText().toString().length() == 4) {
					etCvc.requestFocus();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		etMonth.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (etMonth.getText().toString().length() == 2) {
					etYear.requestFocus();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		// etHolder = (EditText) findViewById(R.id.edtRegisterCreditCardHolder);
		// btnScan.setOnClickListener(this);
		btnAddPayment.setOnClickListener(this);
		// findViewById(R.id.btnPaymentSkip).setOnClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btnAddPayment:
			if (isValidate()) {
				saveCreditCard();
			}
			break;
		// case R.id.btnScan:
		// scan();
		// break;
		case R.id.btnActionNotification:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	@Override
	protected boolean isValidate() {
		if (etCreditCardNum.getText().length() == 0
				|| etCvc.getText().length() == 0
				|| etMonth.getText().length() == 0
				|| etYear.getText().length() == 0) {
			AndyUtils.showToast(getString(R.string.text_enter_proper_data),
					this);
			return false;
		}
		return true;
	}

	// private void scan() {
	// Intent scanIntent = new Intent(this, CardIOActivity.class);
	//
	// // required for authentication with card.io
	// // scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN,
	// // Const.MY_CARDIO_APP_TOKEN);
	//
	// // customize these values to suit your needs.
	// scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); //
	// default:
	// // true
	// scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default:
	// // false
	// scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); //
	// default:
	// // false
	//
	// // hides the manual entry button
	// // if set, developers should provide their own manual entry
	// // mechanism in
	// // the app
	// scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false);
	// // default:
	// // false
	//
	// // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this
	// // activity.
	// startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
	// }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case MY_SCAN_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				if (data != null
						&& data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
					CreditCard scanResult = data
							.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

					// Never log a raw card number. Avoid displaying it, but if
					// necessary use getFormattedCardNumber()
					// resultStr = "Card Number: " +
					// scanResult.getRedactedCardNumber()
					// + "\n";
					etCreditCardNum.setText(scanResult.getRedactedCardNumber());

					// Do something with the raw number, e.g.:
					// myService.setCardNumber( scanResult.cardNumber );

					if (scanResult.isExpiryValid()) {
						// resultStr += "Expiration Date: " +
						// scanResult.expiryMonth
						// +
						// "/"
						// + scanResult.expiryYear + "\n";
						etMonth.setText(scanResult.expiryMonth + "");
						etYear.setText(scanResult.expiryYear + "");
					}

					if (scanResult.cvv != null) {
						// Never log or display a CVV
						// resultStr += "CVV has " + scanResult.cvv.length()
						// + " digits.\n";
						etCvc.setText(scanResult.cvv);
					}

					// if (scanResult.postalCode != null) {
					// resultStr += "Postal Code: " + scanResult.postalCode +
					// "\n";
					// }
				} else {
					// resultStr = "Scan was canceled.";
					AndyUtils.showToast(getString(R.string.text_scan_canceled),
							this);
				}
			} else {
				AndyUtils.showToast(getString(R.string.text_scan_unsuceesful),
						this);
			}
			break;

		}

	}

	public void saveCreditCard() {
		Log.d("StripePublishableKey","***************StripePublishableKey***************"+preference.getStripePublishableKey());
		Card card = new Card(etCreditCardNum.getText().toString(),
				Integer.parseInt(etMonth.getText().toString()),
				Integer.parseInt(etYear.getText().toString()), etCvc.getText()
						.toString());

		boolean validation = card.validateCard();

		if (validation) {
			AndyUtils.showCustomProgressDialog(this,
					getString(R.string.adding_payment), false, null);
			new Stripe().createToken(card, preference.getStripePublishableKey(),
					new TokenCallback() {
						public void onSuccess(Token token) {
							// getTokenList().addToList(token);
							// AndyUtils.showToast(token.getId(), activity);
							String lastFour = etCreditCardNum.getText()
									.toString().toString();
							lastFour = lastFour.substring(lastFour.length() - 4);
							addCard(token.getId(), lastFour);
							// finishProgress();
						}

						public void onError(Exception error) {
							AndyUtils.showToast(getString(R.string.text_error),
									AddPaymentActivity.this);
							// finishProgress();
							AndyUtils.removeCustomProgressDialog();
						}
					});
		} else if (!card.validateNumber()) {
			// handleError("The card number that you entered is invalid");
			AndyUtils.showToast(getString(R.string.text_number_invalid), this);
		} else if (!card.validateExpiryDate()) {
			// handleError("");
			AndyUtils.showToast(getString(R.string.text_date_invalid), this);
		} else if (!card.validateCVC()) {
			// handleError("");
			AndyUtils.showToast(getString(R.string.text_cvc_invalid), this);

		} else {
			// handleError("");
			AndyUtils.showToast(getString(R.string.text_card_invalid), this);
		}
	}

	public String getType(String number) {
		if (!TextUtils.isBlank(number)) {
			if (TextUtils.hasAnyPrefix(number, PREFIXES_AMERICAN_EXPRESS)) {
				return AMERICAN_EXPRESS;
			} else if (TextUtils.hasAnyPrefix(number, PREFIXES_DISCOVER)) {
				return DISCOVER;
			} else if (TextUtils.hasAnyPrefix(number, PREFIXES_JCB)) {
				return JCB;
			} else if (TextUtils.hasAnyPrefix(number, PREFIXES_DINERS_CLUB)) {
				return DINERS_CLUB;
			} else if (TextUtils.hasAnyPrefix(number, PREFIXES_VISA)) {
				return VISA;
			} else if (TextUtils.hasAnyPrefix(number, PREFIXES_MASTERCARD)) {
				return MASTERCARD;
			} else {
				return UNKNOWN;
			}
		}
		return UNKNOWN;

	}

	private void addCard(String stripeToken, String lastFour) {
		// AppLog.Log(TAG, "Final token : " + peachToken.substring(3));
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Const.URL, Const.ServiceType.ADD_CARD);
		map.put(Const.Params.ID, new PreferenceHelper(this).getUserId());
		map.put(Const.Params.TOKEN,
				new PreferenceHelper(this).getSessionToken());
		map.put(Const.Params.STRIPE_TOKEN, stripeToken);
		map.put(Const.Params.LAST_FOUR, lastFour);
		// map.put(Const.Params.CARD_TYPE, type);
		new HttpRequester(this, map, Const.ServiceCode.ADD_CARD, this);
		// requestQueue.add(new VolleyHttpRequest(Method.POST, map,
		// Const.ServiceCode.ADD_CARD, this, this));
	}

	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AppLog.Log(Const.TAG, response);
		AndyUtils.removeCustomProgressDialog();
		super.onTaskCompleted(response, serviceCode);
		switch (serviceCode) {
		case Const.ServiceCode.ADD_CARD:

			if (new ParseContent(this).isSuccess(response)) {
				AndyUtils.showToast(getString(R.string.text_add_card_scucess),
						this);
				setResult(RESULT_OK);
				preference.putPaymentMode(Const.CREDIT);
			} else {
				AndyUtils.showToast(
						getString(R.string.text_not_add_card_unscucess), this);
				setResult(RESULT_CANCELED);
			}
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	// private void setStatusText(final String string) {
	// AppLog.Log(TAG, string);
	// }

	// @Override
	// public void obtainedToken(String token, PWTransaction transaction) {
	// setStatusText("Obtained a token!");
	// setStatusText(token);
	// String lastFour = etCreditCardNum.getText().toString();
	// if (lastFour.length() > 4) {
	// AppLog.Log(TAG, "lastFour : " + lastFour);
	// lastFour = lastFour.substring(lastFour.length() - 4);
	// addCard(token, lastFour);
	// Log.i(Const.APPLICATIONIDENTIFIER, token);
	// }
	// }
	//
	// @Override
	// public void creationAndRegistrationFailed(PWTransaction transaction,
	// PWError error) {
	// setStatusText("Error contacting the gateway.");
	// Log.e("com.payworks.customtokenization.TokenizationActivity",
	// error.getErrorMessage());
	// }
	//
	// @Override
	// public void creationAndRegistrationSucceeded(PWTransaction transaction) {
	// // check if it is our registration transaction
	// // setStatusText("Processing...");
	// // if (currentTokenization) {
	// // // execute it
	// // try {
	// // _binder.obtainToken(transaction);
	// // } catch (PWException e) {
	// // setStatusText("Invalid Transaction.");
	// // e.printStackTrace();
	// // }
	// // } else {
	// // // execute it
	// // try {
	// // _binder.debitTransaction(transaction);
	// // } catch (PWException e) {
	// // setStatusText("Invalid Transaction.");
	// // e.printStackTrace();
	// // }
	// // }
	// }
	//
	// @Override
	// public void transactionFailed(PWTransaction arg0, PWError error) {
	// setStatusText("Error contacting the gateway.");
	// Log.e("com.payworks.customtokenization.TokenizationActivity",
	// error.getErrorMessage());
	// }
	//
	// @Override
	// public void transactionSucceeded(PWTransaction transaction) {
	// // if (!currentTokenization) { // our debit succeeded
	// // setStatusText("Charged token "
	// // + transaction.getPaymentParams().getAmount() + " EURO!");
	// // }
	// }
}
