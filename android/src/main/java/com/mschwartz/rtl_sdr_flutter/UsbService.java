package com.mschwartz.rtl_sdr_flutter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sdrtouch.tools.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * A service to detect if a usb device is attached or detached. The service is configured in AndroidManifest.xml
 * This service will be bound/unbound at user/s request (see [{@link MethodHandlerImpl] startService/stopService).
 * When bound the service returns a [UsbBinder] which can be used to communicate between the service and the StreamHandler/MethodHandler.
 */
public class UsbService extends Service {

    private final static int ONGOING_NOTIFICATION_ID = 437943911; // random id

    private final Set<UsbBinder> binders = new HashSet<>();

    public UsbService() {
        Log.appendLine("UsbService: constructor");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.appendLine("UsbService: onStartCommand");
        startForeground();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.appendLine("UsbService: onBind");
        UsbBinder usbBinder = new UsbBinder(this);
        binders.add(usbBinder);
        start();
        return usbBinder;

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.appendLine("UsbService: onUnbind");
        stop();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.appendLine("UsbService: onDestroy");
        super.onDestroy();
    }

    private void start() {
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        getApplicationContext().registerReceiver(usbAttachBroadcastReceiver, filter);
        filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        getApplicationContext().registerReceiver(usbDetachBroadcastReceiver, filter);
    }

    private void stop() {
        binders.clear();
        getApplicationContext().unregisterReceiver(usbAttachBroadcastReceiver);
        getApplicationContext().unregisterReceiver(usbDetachBroadcastReceiver);
    }

    public void startForeground() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "rtl_sdr";

        if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, "Device driver notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );

            // Configure the notification channel.
            notificationChannel.setDescription("When rtl-sdr operates");
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle(getText(R.string.app_name));

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder
                    .setPriority(Notification.PRIORITY_MAX);
  //      }

        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    public void stopForeground() {
        stopForeground(true);
    }

    BroadcastReceiver usbAttachBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.appendLine("onReceive USB attached");
                binders.forEach(UsbBinder::onUsbDeviceAttached);
            }
        }
    };

    BroadcastReceiver usbDetachBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.appendLine("onReceive USB detached");
                binders.forEach(UsbBinder::onUsbDeviceDetached);
            }
        }
    };


}
