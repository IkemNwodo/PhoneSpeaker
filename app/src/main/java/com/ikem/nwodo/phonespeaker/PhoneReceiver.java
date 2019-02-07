package com.ikem.nwodo.phonespeaker;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class PhoneReceiver extends Service {

    TextToSpeech textToSpeech = MainActivity.mTts;


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.provider.Telephony.SMS_RECEIVED")){
                setUpTextToSpeak(getSms(context));
            }
            else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){

            }
        }

        private void setUpTextToSpeak(ArrayList<String> sms) {
            textToSpeech.speak(sms.get(0), TextToSpeech.QUEUE_ADD, null);
            textToSpeech.speak(sms.get(1), TextToSpeech.QUEUE_ADD, null);
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        filter.addAction(TelephonyManager.EXTRA_INCOMING_NUMBER);

        registerReceiver(receiver, filter);
        Log.d("Receiver", "Receiver registered!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        textToSpeech.stop();
        textToSpeech.shutdown();
        unregisterReceiver(receiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{notificationIntent}, 0);

        Notification notification =
                new NotificationCompat.Builder(this, "SERVICE_ID")
                .setContentTitle("Monitor calls and messages")
                .setContentText("This Notification watches out for incoming calls and messages")
                .setSmallIcon(R.drawable.ic_sms_black_24dp)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(101, notification);
        return START_STICKY;
    }

    public ArrayList<String> getSms(Context context){
        ContentResolver cr = context.getContentResolver();

        ArrayList<String> getLastMessage = new ArrayList<>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
          Cursor  c = cr.query(Telephony.Sms.Inbox.CONTENT_URI, null, null, null, null);

          if (c != null){
              if (c.moveToFirst()){
                      String smsNumber = c.getString(
                              c.getColumnIndexOrThrow(Telephony.Sms.Inbox.ADDRESS));
                      String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.Inbox.BODY));

                      getLastMessage.add(smsNumber);
                      getLastMessage.add(body);
              }
              c.close();
          }
        }

        else {
            Cursor c = cr.query(Uri.parse("content://sms/inbox"), null, null, null, null);

            if(c != null) {
                if (c.moveToFirst()) {
                    //String smsNumber = c.getString(c.);
                }
            }
        return getLastMessage;
        }

}}

