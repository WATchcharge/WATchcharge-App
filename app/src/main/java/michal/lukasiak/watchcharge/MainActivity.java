package michal.lukasiak.watchcharge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.john.waveview.WaveView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import watchcharge.R;

public class MainActivity extends AppCompatActivity {
    private IntentFilter intentFilter;
    private Intent batteryStatusReceiver;

    private VideoView leaf;

    private int infoPosition = 0;

    private Handler handler = new Handler();

    private TextView info1;
    private TextView info2;

    private Switch modeSwitch;

    private View view;

    private String lastMode = "lastAppPreferences";

    private ImageView sun;
    private ImageView moon;

    private WaveView batteryProgressBar;

    private String[] facts;
    private String[] texts;

    private Uri darkURI;
    private Uri lightUri;

    private Spinner languageSwitch;

    private boolean restartAppReminder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        languageSwitch = findViewById(R.id.languageChoice);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, new String[]{"Polski", "English", "Українська"});

        languageSwitch.setAdapter(adapter);

        SharedPreferences settings = getApplicationContext().getSharedPreferences(lastMode, 0);

        int language = settings.getInt("chosenLanguage", 0);
        languageSwitch.setSelection(language);

        texts = this.getString(R.string.polish_texts).split("\\n", -1);
        List<String> factsAsList = Arrays.asList(this.getString(R.string.polish_facts).split("\\n", -1));

        switch (language) {
            case 1:
                texts = this.getString(R.string.english_texts).split("\\n", -1);
                factsAsList = Arrays.asList(this.getString(R.string.english_facts).split("\\n", -1));
                break;
            case 2:
                texts = this.getString(R.string.ukrainian_texts).split("\\n", -1);
                factsAsList = Arrays.asList(this.getString(R.string.ukrainian_facts).split("\\n", -1));
                break;
            default:
                break;
        }

        languageSwitch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                settings.edit().putInt("chosenLanguage", position).apply();

                if(restartAppReminder)
                Toast.makeText(getApplicationContext(), "Restart the app.", Toast.LENGTH_SHORT).show();
                else restartAppReminder = true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        //set uris for videos
        darkURI = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.leaf_animation_dark);
        lightUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.leaf_animation_light);

        //set charging animation
        leaf = findViewById(R.id.leafAnimation);
        leaf.setVideoURI(darkURI);

        batteryProgressBar = findViewById(R.id.progressBar);

        info1 = findViewById(R.id.info1);
        info2 = findViewById(R.id.info2);

        //when user clicks on fact's text change it
        info2.setOnClickListener((e) -> {
            infoPosition++;
            infoPosition %= facts.length;
            info2.setText(facts[infoPosition]);
        });

        //set images
        sun = findViewById(R.id.sun);
        moon = findViewById(R.id.moon);

        //set view to change background color with switch mode button
        view = findViewById(R.id.backgroundView);

        //load previous saved mode
        boolean darkMode = settings.getBoolean("appMode", true);
        changeVisibility(darkMode);

        //assign mode switch button and previous value
        modeSwitch = findViewById(R.id.modeSwitch);
        modeSwitch.setChecked(darkMode);

        //add functionality to mode switch button
        modeSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            changeVisibility(checked);

            settings.edit().putBoolean("appMode", checked).apply();
        });

        //set broadcast receiver for getting information associated with battery
        UpdateBatteryInformation infoUpdater = new UpdateBatteryInformation(info1, batteryProgressBar, leaf, findViewById(R.id.batteryPercentage), texts);
        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatusReceiver = this.registerReceiver(infoUpdater, intentFilter);
        infoUpdater.onReceive(this, batteryStatusReceiver);

        //select element for interesting facts
        //shuffle facts and begin changing cycle
        Collections.shuffle(factsAsList);
        facts = (String[]) factsAsList.toArray();
        info2.setText(facts[facts.length-1]);
        changeInfo();
    }

    //endlessly selecting array's elements from the beginning to the end
    private void changeInfo() {
        handler.postDelayed(() -> {
            info2.setText(facts[infoPosition]);
            infoPosition++;
            infoPosition %= facts.length;
            changeInfo();
        }, 15000);
    }

    //set background and image according to the variable
    private void changeVisibility(boolean darkMode) {
        if(darkMode) {
            view.setBackgroundColor(Color.BLACK);
            leaf.setVideoURI(darkURI);
            moon.setVisibility(View.VISIBLE);
            batteryProgressBar.setBackgroundColor(Color.BLACK);
            sun.setVisibility(View.INVISIBLE);
            info1.setTextColor(Color.WHITE);
            info2.setTextColor(Color.WHITE);
        }
        else {
            batteryProgressBar.setBackgroundColor(Color.WHITE);
            leaf.setVideoURI(lightUri);
            info1.setTextColor(Color.BLACK);
            sun.setVisibility(View.VISIBLE);
            view.setBackgroundColor(Color.WHITE);
            moon.setVisibility(View.INVISIBLE);
            info2.setTextColor(Color.BLACK);
        }
    }
}