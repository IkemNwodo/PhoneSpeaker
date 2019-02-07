package com.ikem.nwodo.phonespeaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int MY_DATA_CHECK_CODE = 100;
    private SharedPreferences mPreferences;
    private String sharedPrefFile = "com.ikem.nwodo.phonespeaker";

    private static final String BUTTON_STATE = "buttonState";
    private static final String BUTTON_TEXT = "buttonText";
    private boolean isButtonEnable;

    public static TextToSpeech mTts;

    private Button enableDisable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeTTS();

        enableDisable = findViewById(R.id.enabel_disable_button);

        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        // restore button state if activity is recreated
        isButtonEnable = mPreferences.getBoolean(BUTTON_STATE, true);
        enableDisable.setText(mPreferences.getString(BUTTON_TEXT, "ENABLE"));

        enableDisable.setOnClickListener(new View.OnClickListener() {

            Intent intent = new Intent(getApplicationContext(), PhoneReceiver.class);
            @Override
            public void onClick(View view) {
                if (isButtonEnable) {
                    startService(intent);
                    isButtonEnable = false;
                    enableDisable.setText("DISABLE");
                } else {
                    stopService(intent);
                    isButtonEnable = true;
                    enableDisable.setText("ENABLE");
                }
            }
        });
    }

    private void initializeTTS() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent,MY_DATA_CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
            // success, create the TTS instance
            mTts = new TextToSpeech(this, this);
        } else {
            // missing data, install it

            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putBoolean(BUTTON_STATE, isButtonEnable);
        preferencesEditor.putString(BUTTON_TEXT, enableDisable.getText().toString());
        preferencesEditor.apply();
    }

    @Override
    protected void onDestroy() {
        if (mTts != null){
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            if (mTts != null){
                int result = mTts.setLanguage(Locale.US);

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED){
                    Toast.makeText(this, "TTS language not supported!", Toast.LENGTH_LONG)
                            .show();
                }
                mTts.setLanguage(Locale.US);
            }
        }
    }
}