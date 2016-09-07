package com.ondemandbay.taxianytime.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ondemandbay.taxianytime.MainDrawerActivity;
import com.ondemandbay.taxianytime.R;
import com.ondemandbay.taxianytime.adapter.PaymentListAdapter;
import com.ondemandbay.taxianytime.parse.HttpRequester;
import com.ondemandbay.taxianytime.parse.ParseContent;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.util.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @author Elluminati elluminati.in
 */
public class AddPaymentFragmentRegister extends BaseFragmentRegister
// implements PWTokenObtainedListener, PWTransactionListener
{

	private ListView listViewPayment;
	private PaymentListAdapter adapter;
	private ArrayList<com.ondemandbay.taxianytime.models.Card> listCards;
	private ImageView tvNoHistory;
	private TextView tvHeaderText, tvSkipCard;
	private LinearLayout btnAddNewPayment;
	// private final int MY_SCAN_REQUEST_CODE = 111;
	// private LinearLayout llPaymentList;
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
	static final Pattern CODE_PATTERN = Pattern
			.compile("([0-9]{0,4})|([0-9]{4}-)+|([0-9]{4}-[0-9]{0,4})+");

	Dialog addCardDialog;
	EditText etCreditCardNum, etCvc, etYear, etMonth;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.activity_view_payment, container,
				false);
		// setContentView(R.layout.activity_view_payment);

		activity.setTitle(getString(R.string.text_cards));
		activity.setIconMenu(R.drawable.car);
		activity.btnNotification.setVisibility(View.GONE);
		listViewPayment = (ListView) view.findViewById(R.id.listViewPayment);
		// llPaymentList = (LinearLayout) findViewById(R.id.llPaymentList);
		tvNoHistory = (ImageView) view.findViewById(R.id.ivEmptyPayment);
		tvHeaderText = (TextView) view.findViewById(R.id.tvHeaderText);
		btnAddNewPayment = (LinearLayout) view
				.findViewById(R.id.tvAddNewPayment);
		// btnScan = (LinearLayout) view.findViewById(R.id.btnScan);
		btnAddNewPayment.setOnClickListener(this);
		listCards = new ArrayList<com.ondemandbay.taxianytime.models.Card>();
		adapter = new PaymentListAdapter(activity, listCards,
				new PreferenceHelper(activity).getDefaultCard());
		listViewPayment.setAdapter(adapter);
		// btnScan.setOnClickListener(this);
		tvHeaderText.setVisibility(View.INVISIBLE);
		tvNoHistory.setVisibility(View.INVISIBLE);
		// getCards();
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// token = getArguments().getString(Const.Params.TOKEN);
		// id = getArguments().getString(Const.Params.ID);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvAddNewPayment:
			showAddCardDialog();
			// startActivityForResult(new Intent(this,
			// UberAddPaymentActivity.class), REQUEST_ADD_CARD);
			break;
		// case R.id.btnScan:
		// scan();
		// break;
		case R.id.btnAddPayment:
			if (isValidate()) {
				saveCreditCard();
			}
			break;

		case R.id.tvSkipCard:
			tvSkipCard.setVisibility(View.GONE);
			addCardDialog.dismiss();
			startActivity(new Intent(activity, MainDrawerActivity.class));
			activity.finish();

			break;
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uberorg.ActionBarBaseActivitiy#isValidate()
	 */
	@Override
	protected boolean isValidate() {
		if (etCreditCardNum.getText().length() == 0
				|| etCvc.getText().length() == 0
				|| etMonth.getText().length() == 0
				|| etYear.getText().length() == 0) {
			AndyUtils.showToast(
					activity.getString(R.string.text_enter_proper_data),
					activity);
			return false;
		}
		return true;
	}

	// private void getCards() {
	// AndyUtils.showCustomProgressDialog(activity,
	// getString(R.string.progress_loading), false, null);
	// HashMap<String, String> map = new HashMap<String, String>();
	// map.put(Const.URL,
	// Const.ServiceType.GET_CARDS + Const.Params.ID + "="
	// + new PreferenceHelper(activity).getUserId() + "&"
	// + Const.Params.TOKEN + "="
	// + new PreferenceHelper(activity).getSessionToken());
	// // new HttpRequester(this, map, Const.ServiceCode.GET_CARDS, true,
	// // this);
	// requestQueue.add(new VolleyHttpRequest(Method.GET, map,
	// Const.ServiceCode.GET_CARDS, this, this));
	// }

	// private void scan() {
	// Intent scanIntent = new Intent(activity, CardIOActivity.class);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uberorg.ActionBarBaseActivitiy#onTaskCompleted(java.lang.String,
	 * int)
	 */
	@Override
	public void onTaskCompleted(String response, int serviceCode) {
		AndyUtils.removeCustomProgressDialog();
		switch (serviceCode) {
		// case Const.ServiceCode.GET_CARDS:
		// if (new ParseContent(activity).isSuccess(response)) {
		// listCards.clear();
		// new ParseContent(activity).parseCards(response, listCards);
		// AppLog.Log("UberViewPayment", "listCards : " + listCards.size());
		// if (listCards.size() > 0) {
		// listViewPayment.setVisibility(View.VISIBLE);
		// tvNoHistory.setVisibility(View.GONE);
		// paymentMode = 1;
		// tvHeaderText.setVisibility(View.VISIBLE);
		// } else {
		// listViewPayment.setVisibility(View.GONE);
		// tvNoHistory.setVisibility(View.VISIBLE);
		// tvHeaderText.setVisibility(View.GONE);
		// paymentMode = 0;
		// }
		// adapter.notifyDataSetChanged();
		// }
		// break;

		case Const.ServiceCode.ADD_CARD:

			if (new ParseContent(activity).isSuccess(response)) {
				AndyUtils.showToast(getString(R.string.text_add_card_scucess),
						activity);
				activity.setResult(Activity.RESULT_OK);
			} else {
				AndyUtils.showToast(
						getString(R.string.text_not_add_card_unscucess),
						activity);
				activity.setResult(Activity.RESULT_CANCELED);
			}
			addCardDialog.dismiss();

			startActivity(new Intent(activity, MainDrawerActivity.class));
			activity.finish();

			// getCards();
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.uberorg.ActionBarBaseActivitiy#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case Activity.RESULT_OK:
			// getCards();
			break;

		// case MY_SCAN_REQUEST_CODE:
		// if (resultCode == Activity.RESULT_OK) {
		// if (data != null
		// && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
		// final CreditCard scanResult = data
		// .getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
		//
		// Card card = new Card(scanResult.getRedactedCardNumber(),
		// scanResult.expiryMonth, scanResult.expiryYear,
		// scanResult.cvv);
		//
		// boolean validation = card.validateCard();
		// if (validation) {
		// AndyUtils
		// .showCustomProgressDialog(activity,
		// getString(R.string.adding_payment),
		// false, null);
		// new Stripe().createToken(card, Const.PUBLISHABLE_KEY,
		// new TokenCallback() {
		// public void onSuccess(Token token) {
		// // getTokenList().addToList(token);
		// // AndyUtils.showToast(token.getId(),
		// // activity);
		// String lastFour = scanResult
		// .getRedactedCardNumber();
		// lastFour = lastFour.substring(lastFour
		// .length() - 4);
		// addCard(token.getId(), lastFour);
		// // finishProgress();
		// }
		//
		// public void onError(Exception error) {
		// AndyUtils.showToast("Error", activity);
		// // finishProgress();
		// AndyUtils.removeCustomProgressDialog();
		// }
		// });
		// } else if (!card.validateNumber()) {
		// // handleError("The card number that you entered is invalid");
		// AndyUtils.showToast(
		// "The card number that you entered is invalid",
		// activity);
		// } else if (!card.validateExpiryDate()) {
		// // handleError("");
		// AndyUtils
		// .showToast(
		// "The expiration date that you entered is invalid",
		// activity);
		// } else if (!card.validateCVC()) {
		// // handleError("");
		// AndyUtils.showToast(
		// "The CVC code that you entered is invalid",
		// activity);
		//
		// } else {
		// // handleError("");
		// AndyUtils
		// .showToast(
		// "The card details that you entered are invalid",
		// activity);
		// }
		//
		// } else {
		// // resultStr = "Scan was canceled.";
		// AndyUtils.showToast("Scan was canceled.", activity);
		// }
		// } else {
		// AndyUtils.showToast("Scan was unsuccessful.", activity);
		// }
		// break;
		}
	}

	private void addCard(String stripeToken, String lastFour) {
		// AppLog.Log(TAG, "Final token : " + peachToken.substring(3));
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(Const.URL, Const.ServiceType.ADD_CARD);
		map.put(Const.Params.ID, new PreferenceHelper(activity).getUserId());
		map.put(Const.Params.TOKEN,
				new PreferenceHelper(activity).getSessionToken());
		map.put(Const.Params.STRIPE_TOKEN, stripeToken);
		map.put(Const.Params.LAST_FOUR, lastFour);
		// map.put(Const.Params.CARD_TYPE, type);
		new HttpRequester(activity, map, Const.ServiceCode.ADD_CARD, this);
		// requestQueue.add(new VolleyHttpRequest(Method.POST, map,
		// Const.ServiceCode.ADD_CARD, this, this));
	}

	public void showAddCardDialog() {
		ImageView btnAddPayment;

		addCardDialog = new Dialog(activity);
		addCardDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		addCardDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		addCardDialog.setContentView(R.layout.fragment_payment);

		btnAddPayment = (ImageView) addCardDialog
				.findViewById(R.id.btnAddPayment);
		etCreditCardNum = (EditText) addCardDialog
				.findViewById(R.id.edtRegisterCreditCardNumber);
		etCvc = (EditText) addCardDialog.findViewById(R.id.edtRegistercvc);
		etYear = (EditText) addCardDialog.findViewById(R.id.edtRegisterexpYear);
		etMonth = (EditText) addCardDialog
				.findViewById(R.id.edtRegisterexpMonth);
		tvSkipCard = (TextView) addCardDialog.findViewById(R.id.tvSkipCard);
		tvSkipCard.setVisibility(View.VISIBLE);
		tvSkipCard.setOnClickListener(this);

		etCreditCardNum.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (TextUtils.isBlank(s.toString())) {
					etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
							null, null, null, null);
				}
				String type = getType(s.toString());

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
					etCvc.requestFocus();
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

		etCvc.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (etCvc.getText().toString().length() == 3) {
					etMonth.requestFocus();
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
		btnAddPayment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isValidate()) {
					saveCreditCard();
				}
			}
		});

		addCardDialog.show();

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

	public void saveCreditCard() {

		Card card = new Card(etCreditCardNum.getText().toString(),
				Integer.parseInt(etMonth.getText().toString()),
				Integer.parseInt(etYear.getText().toString()), etCvc.getText()
						.toString());

		boolean validation = card.validateCard();
		if (validation) {
			AndyUtils.showCustomProgressDialog(activity,
					getString(R.string.adding_payment), false, null);
			new Stripe().createToken(card,new PreferenceHelper(activity).getStripePublishableKey(),
					new TokenCallback() {
						public void onSuccess(Token token) {
							// getTokenList().addToList(token);
							// AndyUtils.showToast(token.getId(), activity);
							String lastFour = etCreditCardNum.getText()
									.toString();
							lastFour = lastFour.substring(lastFour.length() - 4);
							addCard(token.getId(), lastFour);
							// finishProgress();
						}

						public void onError(Exception error) {
							AndyUtils.showToast("Error", activity);
							// finishProgress();
							AndyUtils.removeCustomProgressDialog();
						}
					});
		} else if (!card.validateNumber()) {
			// handleError("The card number that you entered is invalid");
			AndyUtils.showToast(
					activity.getString(R.string.text_number_invalid), activity);
		} else if (!card.validateExpiryDate()) {
			// handleError("");
			AndyUtils.showToast(activity.getString(R.string.text_date_invalid),
					activity);
		} else if (!card.validateCVC()) {
			// handleError("");
			AndyUtils.showToast(activity.getString(R.string.text_cvc_invalid),
					activity);

		} else {
			// handleError("");
			AndyUtils.showToast(activity.getString(R.string.text_card_invalid),
					activity);
		}
	}

	// private static final String TAG = "UberAddPaymentFragmentRegister";
	// private Button btnAddPayment;
	// private ImageView btnScan;
	// private final int MY_SCAN_REQUEST_CODE = 111;
	// private EditText etCreditCardNum, etCvc, etYear, etMonth;
	// // private String patternVisa = "^4[0-9]{12}(?:[0-9]{3})?$";
	// // private String patternMasterCard = "^5[1-5][0-9]{14}$";
	// // private String patternAmericanExpress = "^3[47][0-9]{13}$";
	// public static final String[] PREFIXES_AMERICAN_EXPRESS = { "34", "37" };
	// public static final String[] PREFIXES_DISCOVER = { "60", "62", "64", "65"
	// };
	// public static final String[] PREFIXES_JCB = { "35" };
	// public static final String[] PREFIXES_DINERS_CLUB = { "300", "301",
	// "302",
	// "303", "304", "305", "309", "36", "38", "37", "39" };
	// public static final String[] PREFIXES_VISA = { "4" };
	// public static final String[] PREFIXES_MASTERCARD = { "50", "51", "52",
	// "53", "54", "55" };
	// public static final String AMERICAN_EXPRESS = "American Express";
	// public static final String DISCOVER = "Discover";
	// public static final String JCB = "JCB";
	// public static final String DINERS_CLUB = "Diners Club";
	// public static final String VISA = "Visa";
	// public static final String MASTERCARD = "MasterCard";
	// public static final String UNKNOWN = "Unknown";
	// public static final int MAX_LENGTH_STANDARD = 16;
	// public static final int MAX_LENGTH_AMERICAN_EXPRESS = 15;
	// public static final int MAX_LENGTH_DINERS_CLUB = 14;
	// private String type;
	// private String token, id;
	// private EditText etHolder;
	// private RequestQueue requestQueue;
	//
	// // private PWCreditCardType cardType;
	// // private boolean currentTokenization;
	// //
	// // private PWProviderBinder _binder;
	// //
	// // private ServiceConnection _serviceConnection = new ServiceConnection()
	// {
	// // @Override
	// // public void onServiceConnected(ComponentName name, IBinder service) {
	// // _binder = (PWProviderBinder) service;
	// // // we have a connection to the service
	// // try {
	// // _binder.initializeProvider(PWProviderMode.LIVE,
	// // Const.APPLICATIONIDENTIFIER, Const.PROFILETOKEN);
	// // _binder.addTokenObtainedListener(UberAddPaymentFragmentRegister.this);
	// // _binder.addTransactionListener(UberAddPaymentFragmentRegister.this);
	// // } catch (PWException ee) {
	// // setStatusText("Error initializing the provider.");
	// // // error initializing the provider
	// // ee.printStackTrace();
	// // }
	// // }
	// //
	// // @Override
	// // public void onServiceDisconnected(ComponentName name) {
	// // _binder = null;
	// // }
	// // };
	//
	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// token = getArguments().getString(Const.Params.TOKEN);
	// id = getArguments().getString(Const.Params.ID);
	// requestQueue = Volley.newRequestQueue(activity);
	// // getActivity().startService(
	// // new Intent(getActivity(),
	// // com.mobile.connect.service.PWConnectService.class));
	// // getActivity().bindService(
	// // new Intent(getActivity(),
	// // com.mobile.connect.service.PWConnectService.class),
	// // _serviceConnection, Context.BIND_AUTO_CREATE);
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// activity.setTitle(getResources().getString(
	// R.string.text_addpayment_small));
	// activity.setIconMenu(R.drawable.ic_payment);
	// activity.btnNotification.setVisibility(View.INVISIBLE);
	// View view = inflater.inflate(R.layout.fragment_payment, container,
	// false);
	// btnAddPayment = (Button) view.findViewById(R.id.btnAddPayment);
	// // view.findViewById(R.id.btnPaymentSkip).setOnClickListener(this);
	// btnScan = (ImageView) view.findViewById(R.id.btnScan);
	//
	// etCreditCardNum = (EditText) view
	// .findViewById(R.id.edtRegisterCreditCardNumber);
	// etCreditCardNum.addTextChangedListener(new TextWatcher() {
	//
	// @Override
	// public void onTextChanged(CharSequence s, int start, int before,
	// int count) {
	// if (TextUtils.isBlank(s.toString())) {
	// etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
	// null, null, null, null);
	// }
	// type = getType(s.toString());
	//
	// if (type.equals(VISA)) {
	// etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
	// getResources().getDrawable(
	// R.drawable.ub__creditcard_visa), null,
	// null, null);
	//
	// } else if (type.equals(MASTERCARD)) {
	// etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
	// getResources().getDrawable(
	// R.drawable.ub__creditcard_mastercard),
	// null, null, null);
	//
	// } else if (type.equals(AMERICAN_EXPRESS)) {
	// etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
	// getResources().getDrawable(
	// R.drawable.ub__creditcard_amex), null,
	// null, null);
	//
	// } else if (type.equals(DISCOVER)) {
	// etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
	// getResources().getDrawable(
	// R.drawable.ub__creditcard_discover), null,
	// null, null);
	//
	// } else if (type.equals(DINERS_CLUB)) {
	// etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
	// getResources().getDrawable(
	// R.drawable.ub__creditcard_discover), null,
	// null, null);
	//
	// } else {
	// etCreditCardNum.setCompoundDrawablesWithIntrinsicBounds(
	// null, null, null, null);
	// }
	// }
	//
	// @Override
	// public void beforeTextChanged(CharSequence s, int start, int count,
	// int after) {
	// }
	//
	// @Override
	// public void afterTextChanged(Editable s) {
	//
	// }
	// });
	// etCvc = (EditText) view.findViewById(R.id.edtRegistercvc);
	// etYear = (EditText) view.findViewById(R.id.edtRegisterexpYear);
	// etMonth = (EditText) view.findViewById(R.id.edtRegisterexpMonth);
	// etYear.addTextChangedListener(new TextWatcher() {
	//
	// @Override
	// public void onTextChanged(CharSequence s, int start, int before,
	// int count) {
	// if (etYear.getText().toString().length() == 4) {
	// etCvc.requestFocus();
	// }
	// }
	//
	// @Override
	// public void beforeTextChanged(CharSequence s, int start, int count,
	// int after) {
	// }
	//
	// @Override
	// public void afterTextChanged(Editable s) {
	// }
	// });
	// etMonth.addTextChangedListener(new TextWatcher() {
	//
	// @Override
	// public void onTextChanged(CharSequence s, int start, int before,
	// int count) {
	// if (etMonth.getText().toString().length() == 2) {
	// etYear.requestFocus();
	// }
	// }
	//
	// @Override
	// public void beforeTextChanged(CharSequence s, int start, int count,
	// int after) {
	// }
	//
	// @Override
	// public void afterTextChanged(Editable s) {
	// }
	// });
	// etHolder = (EditText) view
	// .findViewById(R.id.edtRegisterCreditCardHolder);
	// btnScan.setOnClickListener(this);
	// btnAddPayment.setOnClickListener(this);
	// return view;
	// }
	//
	// @Override
	// public void onResume() {
	// activity.currentFragment = Const.FRAGMENT_PAYMENT_REGISTER;
	// activity.actionBar.setTitle(getString(R.string.text_addpayment_small));
	// super.onResume();
	// }
	//
	// @Override
	// public void onActivityCreated(Bundle savedInstanceState) {
	// super.onActivityCreated(savedInstanceState);
	// etCreditCardNum.requestFocus();
	// activity.showKeyboard(etCreditCardNum);
	//
	// }
	//
	// @Override
	// public void onClick(View v) {
	// switch (v.getId()) {
	//
	// case R.id.btnAddPayment:
	// if (isValidate()) {
	// saveCreditCard();
	//
	// }
	// break;
	// case R.id.btnScan:
	// scan();
	// break;
	// // case R.id.btnPaymentSkip:
	// // OnBackPressed();
	// // break;
	// default:
	// break;
	// }
	// }
	//
	// @Override
	// protected boolean isValidate() {
	// if (etCreditCardNum.getText().length() == 0
	// || etCvc.getText().length() == 0
	// || etMonth.getText().length() == 0
	// || etYear.getText().length() == 0) {
	// AndyUtils.showToast("Enter Proper data", activity);
	// return false;
	// }
	// return true;
	// }
	//
	// private void scan() {
	// Intent scanIntent = new Intent(activity, CardIOActivity.class);
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
	// scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false);
	// // default:
	// // false
	// activity.startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE,
	// Const.FRAGMENT_PAYMENT_REGISTER);
	// }
	//
	// @Override
	// public void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// super.onActivityResult(requestCode, resultCode, data);
	// switch (requestCode) {
	// case MY_SCAN_REQUEST_CODE:
	// if (resultCode == Activity.RESULT_OK) {
	// if (data != null
	// && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
	// CreditCard scanResult = data
	// .getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
	//
	// etCreditCardNum.setText(scanResult.getRedactedCardNumber());
	// if (scanResult.isExpiryValid()) {
	// etMonth.setText(scanResult.expiryMonth + "");
	//
	// etYear.setText(scanResult.expiryYear + "");
	// }
	// if (scanResult.cvv != null) {
	// etCvc.setText(scanResult.cvv);
	// }
	// } else {
	// AndyUtils.showToast("Scan was canceled.", activity);
	// }
	// } else {
	// AndyUtils.showToast("Scan was uncessfull.", activity);
	// }
	// break;
	// }
	// }
	//
	// public void saveCreditCard() {
	// // AndyUtils.showCustomProgressDialog(activity,
	// // getString(R.string.adding_payment), false, null);
	// // PWPaymentParams paymentParams = null;
	// // try {
	// // if (getType(etCreditCardNum.getText().toString().trim()).equals(
	// // VISA)) {
	// // cardType = PWCreditCardType.VISA;
	// // } else if (getType(etCreditCardNum.getText().toString().trim())
	// // .equals(AMERICAN_EXPRESS)) {
	// // cardType = PWCreditCardType.AMEX;
	// // } else if (getType(etCreditCardNum.getText().toString().trim())
	// // .equals(DINERS_CLUB)) {
	// // cardType = PWCreditCardType.DINERS;
	// // } else if (getType(etCreditCardNum.getText().toString().trim())
	// // .equals(JCB)) {
	// // cardType = PWCreditCardType.JCB;
	// // } else if (getType(etCreditCardNum.getText().toString().trim())
	// // .equals(MASTERCARD)) {
	// // cardType = PWCreditCardType.MASTERCARD;
	// // }
	// // paymentParams = _binder.getPaymentParamsFactory()
	// // .createCreditCardTokenizationParams(
	// // etHolder.getText().toString(), cardType,
	// // etCreditCardNum.getText().toString(),
	// // etYear.getText().toString(),
	// // etMonth.getText().toString(),
	// // etCvc.getText().toString());
	// // } catch (PWProviderNotInitializedException e) {
	// // AndyUtils.removeCustomProgressDialog();
	// // AndyUtils.showToast("Error: Provider not initialized!", activity);
	// // return;
	// // } catch (PWException e) {
	// // AndyUtils.removeCustomProgressDialog();
	// // AndyUtils.showToast("Error: Invalid Parameters!", activity);
	// // return;
	// // }
	// //
	// // setStatusText("Preparing...");
	// // currentTokenization = true;
	// //
	// // try {
	// // _binder.createAndRegisterObtainTokenTransaction(paymentParams);
	// // } catch (PWException e) {
	// // setStatusText("Error: Could not contact Gateway!");
	// // }
	// Card card = new Card(etCreditCardNum.getText().toString(),
	// Integer.parseInt(etMonth.getText().toString()),
	// Integer.parseInt(etYear.getText().toString()), etCvc.getText()
	// .toString());
	//
	// boolean validation = card.validateCard();
	// if (validation) {
	// AndyUtils.showCustomProgressDialog(activity,
	// getString(R.string.adding_payment), false, null);
	//
	// new Stripe().createToken(card, Const.PUBLISHABLE_KEY,
	// new TokenCallback() {
	// public void onSuccess(Token token) {
	// // getTokenList().addToList(token);
	// // AndyUtils.showToast(token.getId(), activity);
	// String lastFour = etCreditCardNum.getText()
	// .toString().toString();
	// lastFour = lastFour.substring(lastFour.length() - 4);
	// addCard(token.getId(), lastFour);
	// // finishProgress();
	// }
	//
	// public void onError(Exception error) {
	// AndyUtils.showToast("Error", activity);
	// // finishProgress();
	// AndyUtils.removeCustomProgressDialog();
	// }
	// });
	// } else if (!card.validateNumber()) {
	// // handleError("The card number that you entered is invalid");
	// AndyUtils.showToast("The card number that you entered is invalid",
	// activity);
	// } else if (!card.validateExpiryDate()) {
	// // handleError("");
	// AndyUtils
	// .showToast(
	// "The expiration date that you entered is invalid",
	// activity);
	// } else if (!card.validateCVC()) {
	// // handleError("");
	// AndyUtils.showToast("The CVC code that you entered is invalid",
	// activity);
	//
	// } else {
	// // handleError("");
	// AndyUtils.showToast(
	// "The card details that you entered are invalid", activity);
	// }
	// }
	//
	// public String getType(String number) {
	// if (!TextUtils.isBlank(number)) {
	// if (TextUtils.hasAnyPrefix(number, PREFIXES_AMERICAN_EXPRESS)) {
	// return AMERICAN_EXPRESS;
	// } else if (TextUtils.hasAnyPrefix(number, PREFIXES_DISCOVER)) {
	// return DISCOVER;
	// } else if (TextUtils.hasAnyPrefix(number, PREFIXES_JCB)) {
	// return JCB;
	// } else if (TextUtils.hasAnyPrefix(number, PREFIXES_DINERS_CLUB)) {
	// return DINERS_CLUB;
	// } else if (TextUtils.hasAnyPrefix(number, PREFIXES_VISA)) {
	// return VISA;
	// } else if (TextUtils.hasAnyPrefix(number, PREFIXES_MASTERCARD)) {
	// return MASTERCARD;
	// } else {
	// return UNKNOWN;
	// }
	// }
	// return UNKNOWN;
	// }
	//
	// private void setStatusText(final String string) {
	// AppLog.Log(TAG, string);
	// }
	//
	// private void addCard(String stripeToken, String lastFour) {
	// // AppLog.Log(TAG, "Final token : " + peachToken.substring(3));
	// HashMap<String, String> map = new HashMap<String, String>();
	// map.put(Const.URL, Const.ServiceType.ADD_CARD);
	// map.put(Const.Params.ID, id);
	// map.put(Const.Params.TOKEN, token);
	// map.put(Const.Params.STRIPE_TOKEN, stripeToken);
	// map.put(Const.Params.LAST_FOUR, lastFour);
	// // map.put(Const.Params.CARD_TYPE, type);
	// // new HttpRequester(activity, map, Const.ServiceCode.ADD_CARD, this);
	// requestQueue.add(new VolleyHttpRequest(Method.POST, map,
	// Const.ServiceCode.ADD_CARD, this, this));
	// }
	//
	// @Override
	// public void onTaskCompleted(String response, int serviceCode) {
	// AndyUtils.removeCustomProgressDialog();
	// super.onTaskCompleted(response, serviceCode);
	// switch (serviceCode) {
	// case Const.ServiceCode.ADD_CARD:
	//
	// if (new ParseContent(activity).isSuccess(response)) {
	// AndyUtils.showToast(getString(R.string.text_add_card_scucess),
	// activity);
	// activity.startActivity(new Intent(activity,
	// MainDrawerActivity.class));
	// } else
	// AndyUtils.showToast(
	// getString(R.string.text_not_add_card_unscucess),
	// activity);
	// activity.finish();
	// break;
	// default:
	// break;
	// }
	// }
	//
	// //
	// // @Override
	// // public void obtainedToken(String token, PWTransaction transaction) {
	// // setStatusText("Obtained a token!");
	// // setStatusText(token);
	// // String lastFour = etCreditCardNum.getText().toString().toString();
	// // lastFour = lastFour.substring(lastFour.length() - 4);
	// // addCard(token, lastFour);
	// // Log.i(Const.APPLICATIONIDENTIFIER, token);
	// // }
	// //
	// // @Override
	// // public void creationAndRegistrationFailed(PWTransaction transaction,
	// // PWError error) {
	// // setStatusText("Error contacting the gateway.");
	// // Log.e("com.payworks.customtokenization.TokenizationActivity",
	// // error.getErrorMessage());
	// // }
	// //
	// // @Override
	// // public void creationAndRegistrationSucceeded(PWTransaction
	// transaction) {
	// // // check if it is our registration transaction
	// // setStatusText("Processing...");
	// // // if (currentTokenization) {
	// // // // execute it
	// // // try {
	// // // _binder.obtainToken(transaction);
	// // // } catch (PWException e) {
	// // // setStatusText("Invalid Transaction.");
	// // // e.printStackTrace();
	// // // }
	// // // } else {
	// // // // execute it
	// // // try {
	// // // _binder.debitTransaction(transaction);
	// // // } catch (PWException e) {
	// // // setStatusText("Invalid Transaction.");
	// // // e.printStackTrace();
	// // // }
	// // // }
	// // }
	// //
	// // @Override
	// // public void transactionFailed(PWTransaction arg0, PWError error) {
	// // setStatusText("Error contacting the gateway.");
	// // Log.e("com.payworks.customtokenization.TokenizationActivity",
	// // error.getErrorMessage());
	// // }
	// //
	// // @Override
	// // public void transactionSucceeded(PWTransaction transaction) {
	// // // if (!currentTokenization) { // our debit succeeded
	// // // setStatusText("Charged token "
	// // // + transaction.getPaymentParams().getAmount() + " EURO!");
	// // // }
	// // }
	//
	// @Override
	// public void onErrorResponse(VolleyError error) {
	// AppLog.Log(Const.TAG, error.getMessage());
	// }
}
