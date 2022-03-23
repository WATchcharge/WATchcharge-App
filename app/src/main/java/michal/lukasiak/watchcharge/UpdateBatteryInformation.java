package michal.lukasiak.watchcharge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.core.app.NotificationCompat;

import com.john.waveview.WaveView;

import watchcharge.R;

public class UpdateBatteryInformation extends BroadcastReceiver {
    private TextView textComponent;
    private WaveView progressBar;
    private VideoView leaf;
    private TextView textPercentage;

    private String result = "";

    private final String CHANNEL_ID = "WATchcharge";

    private String notificationText;

    private boolean batteryNotification = true;

    private String[] texts;

    public UpdateBatteryInformation(TextView textComponent, WaveView progressBar, VideoView leaf, TextView textPercentage, String[] texts) {
        this.textComponent = textComponent;
        this.progressBar = progressBar;
        this.leaf = leaf;
        this.textPercentage = textPercentage;
        this.texts = texts;

        //make charging animation loop endlessly
        this.leaf.setOnPreparedListener(mediaPlayer -> mediaPlayer.setLooping(true));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //check if battery is loading and enable animation accordingly
        int batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging = BatteryManager.BATTERY_STATUS_CHARGING == batteryStatus;

        if(isCharging && !leaf.isPlaying()) {
            leaf.setVisibility(View.VISIBLE);
            leaf.start();
        }
        else if(!isCharging) {
            leaf.stopPlayback();
            leaf.setVisibility(View.INVISIBLE);
        }

        //set basic info about the battery
        String batteryTechnology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        result = texts[0] + batteryTechnology + "\n";

        int batteryVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
        result += texts[1] + batteryVoltage + " mV\n";

        double batteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        result += texts[2] + (batteryTemperature/10.0d) + " °C\n";

        int batteryHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 1);
        String healthStatus = texts[4];

        switch (batteryHealth) {
            case 2:
                healthStatus = texts[5];
                break;
            case 3:
                healthStatus = texts[6];
                break;
            case 5:
                healthStatus = texts[7];
                break;
            case 7:
                healthStatus = texts[8];
                break;
        }

        result += texts[3] + healthStatus;

        this.textComponent.setText(result);

        //set battery percentage
        int batteryChargeStatus = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        progressBar.setProgress(batteryChargeStatus);
        textPercentage.setText(batteryChargeStatus + "%");

        //send notification if needed
        if(batteryNotification && (batteryChargeStatus <= 20 || batteryChargeStatus >= 80 && isCharging)) {
            batteryNotification = false;

            if(batteryChargeStatus >= 50) {
                notificationText = texts[9];
            } else {
                notificationText = texts[10];
            }

            Intent tmp = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, tmp, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,"WATchchannel",NotificationManager.IMPORTANCE_DEFAULT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.lightning)
                    .setContentTitle(texts[11])
                    .setContentText(notificationText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(contentIntent)
                    .setContentInfo("Info")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setChannelId(CHANNEL_ID);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(1, builder.build());
        }
    }
}