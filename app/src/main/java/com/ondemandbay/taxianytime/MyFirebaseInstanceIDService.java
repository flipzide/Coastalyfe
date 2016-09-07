/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ondemandbay.taxianytime;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;
import com.splunk.mint.network.util.DelegationException;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.util.ArrayList;
import java.util.List;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private PreferenceHelper preferenceHelper;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
//        publishResults(refreshedToken, Activity.RESULT_OK);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     * <p/>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        preferenceHelper = new PreferenceHelper(getApplicationContext());
        preferenceHelper.putDeviceToken(token);
        if (TextUtils.isEmpty(preferenceHelper.getUserId())) {
            publishResults(token, Activity.RESULT_OK);
        } else {
//            if (!preferenceHelper.getDeviceToken().equals(token)) {
//                preferenceHelper.putDeviceToken(token);
//                new updateFCMTokenToServer().execute();
//            }
        }
    }

    private void publishResults(String regid, int result) {
        Intent publishIntent = new Intent(
                Const.DISPLAY_MESSAGE_REGISTER);
        publishIntent.putExtra(Const.RESULT, result);
        publishIntent.putExtra(Const.REGID, regid);
        AppLog.Log("MyFirebaseInstanceIDService'", "sending broad cast");
        preferenceHelper.putDeviceToken(regid);
        sendBroadcast(publishIntent);
    }

    private class updateFCMTokenToServer extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String id = preferenceHelper.getUserId();
            String token = preferenceHelper.getSessionToken();
            String deviceToken = preferenceHelper.getDeviceToken();
            try {
                HttpParams myParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(myParams, 100000);
                HttpConnectionParams.setSoTimeout(myParams, 100000);
                HttpClient httpClient = new DefaultHttpClient(myParams);
                ResponseHandler<String> res = new BasicResponseHandler();
                HttpPost postMethod = new HttpPost(
                        Const.ServiceType.UPDATE_DEVICE_TOKEN);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair(
                        Const.Params.ID, id));
                nameValuePairs.add(new BasicNameValuePair(
                        Const.Params.TOKEN, token));
                nameValuePairs.add(new BasicNameValuePair(
                        Const.Params.DEVICE_TOKEN, deviceToken));

                AppLog.Log("update_device_token", Const.ServiceType.UPDATE_DEVICE_TOKEN + " => " +
                        Const.Params.ID + " : " + id + " , " + Const.Params.TOKEN +
                        ":" + token + " , " + Const.Params.DEVICE_TOKEN + " : " + deviceToken);
                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));

//                if (Const.DEBUGAlpha2Go) {
//                    postMethod.addHeader("Authorization", Const.ServiceType.DEBUG_AUTHORIZATION_HEADER);
//                }

                try {
                    String response = httpClient.execute(postMethod, res);
                    AppLog.Log("updatedevicetoken", "update_device_token Response: " + response);
                    return response;
                } catch (DelegationException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}
