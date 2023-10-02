package com.mschwartz.rtl_sdr_flutter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.mschwartz.rtl_sdr_flutter.devices.SdrDevice;
import com.mschwartz.rtl_sdr_flutter.tools.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * A service to communicate with a usb device. The service is configured in AndroidManifest.xml
 * This service will be bound/unbound at user/s request (see [{@link MethodHandlerImpl] startServer/stopServer).
 * When bound the service returns a [SdrBinder] which can be used to communicate between the service and the StreamHandler/MethodHandler.
 */
public class SdrService extends Service {

    private final static int ONGOING_NOTIFICATION_ID = 437543912; // random id

    private List<SdrDevice> devices = new LinkedList<>();

    public SdrService() {
        Log.appendLine("UsbService constructor");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.appendLine("SdrService: onStartCommand");
        startForeground();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.appendLine("SdrService: onBind");
        return new SdrBinder(this);

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.appendLine("SdrService: onUnbind");
        stop();
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.appendLine("SdrService: onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.appendLine("SdrService: onDestroy");
        stop();
        super.onDestroy();
    }

    private void stop() {
        for (SdrDevice device : new LinkedList<>(devices)) {
            Log.appendLine("Stopping " + device);
            stopWithDevice(device);
            Log.appendLine("Stopping " + device + " completed.");
        }
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

    public void startWithDevice(SdrDevice sdrDevice, SdrArguments sdrArguments) {
        Log.appendLine("SdrService: startWithDevice");
        sdrDevice.openAsync(sdrArguments);
        devices.add(sdrDevice);
    }

    public void stopWithDevice(SdrDevice sdrDevice) {
        Log.appendLine("SdrService: stopWithDevice");
        sdrDevice.close();
        devices.remove(sdrDevice);
        Log.appendLine("SdrService: stopWithDevice done");
    }


}
