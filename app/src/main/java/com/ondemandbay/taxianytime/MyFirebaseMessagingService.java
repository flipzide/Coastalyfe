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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ondemandbay.taxianytime.utils.AndyUtils;
import com.ondemandbay.taxianytime.utils.AppLog;
import com.ondemandbay.taxianytime.utils.Const;
import com.ondemandbay.taxianytime.utils.PreferenceHelper;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private PreferenceHelper preferenceHelper;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        AppLog.Log("FCM", "push Response : " + remoteMessage.getData());
        // notifies user
        String message = remoteMessage.getData().get("message");
        String team = remoteMessage.getData().get("team");
        AppLog.Log("Notificaton", message);
        AppLog.Log("Team", team);
        String title = remoteMessage.getData().get("title");
        Intent pushIntent = new Intent(Const.INTENT_WALKER_STATUS);
        pushIntent.putExtra(Const.EXTRA_WALKER_STATUS, team);
        CommonUtilities.displayMessage(this, message);
        // notifies user
        generateNotificationNew(this, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(pushIntent);

    }

    private void generateNotificationNew(Context context, String message) {
        final Notification.Builder builder = new Notification.Builder(this);
        builder.setDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        builder.setStyle(
                new Notification.BigTextStyle(builder).bigText(message)
                        .setBigContentTitle(
                                context.getString(R.string.app_name)))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message).setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(true);
        Intent notificationIntent = new Intent(context,
                MainDrawerActivity.class);
        notificationIntent.putExtra("fromNotification", "notification");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(intent);
        final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, builder.build());
    }
}
