package com.example.zero.androidskeleton.ui;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.zero.androidskeleton.GlobalObjects;
import com.example.zero.androidskeleton.R;

public class ModeSettingActivity extends AppCompatActivity {

    private LinearLayout manualModeLayout;
    private ImageView manualModeImg;
    private TextView manualModeText;

    private LinearLayout autoModeLayout;
    private ImageView autoModeImg;
    private TextView autoModeText;

    private LinearLayout shakeModeLayout;
    private ImageView shakeModeImg;
    private TextView shakeModeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_setting);

        setupUiComp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMode(GlobalObjects.unlockMode);
    }

    private void setupUiComp() {
        {
            manualModeLayout = (LinearLayout) findViewById(R.id.manual_mode_layout);
            assert manualModeLayout != null;
            manualModeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setMode(GlobalObjects.UNLOCK_MODE_MANUNAL);
                }
            });
            manualModeImg = (ImageView) findViewById(R.id.manual_mode_img);
            assert manualModeImg != null;
            manualModeText = (TextView) findViewById(R.id.manual_mode_text);
            assert manualModeText != null;
        }

        {
            autoModeLayout = (LinearLayout) findViewById(R.id.auto_mode_layout);
            assert autoModeLayout != null;
            autoModeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setMode(GlobalObjects.UNLOCK_MODE_AUTO);
                }
            });
            autoModeImg = (ImageView) findViewById(R.id.auto_mode_img);
            assert manualModeImg != null;
            autoModeText = (TextView) findViewById(R.id.auto_mode_text);
            assert autoModeText != null;
        }

        {
            shakeModeLayout = (LinearLayout) findViewById(R.id.shake_mode_layout);
            assert shakeModeLayout != null;
            shakeModeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setMode(GlobalObjects.UNLOCK_MODE_SHAKE);
                }
            });
            shakeModeImg = (ImageView) findViewById(R.id.shake_mode_img);
            assert shakeModeImg != null;
            shakeModeText = (TextView) findViewById(R.id.shake_mode_text);
            assert shakeModeText != null;
        }

    }

    private void setMode(int mode) {
        GlobalObjects.unlockMode = mode;
        switch (mode) {
            case GlobalObjects.UNLOCK_MODE_MANUNAL:
                manualModeImg.setImageResource(R.drawable.icon_green_manual);
                manualModeText.setTextColor(Color.parseColor("green"));
                autoModeImg.setImageResource(R.drawable.icon_gray_auto);
                autoModeText.setTextColor(Color.parseColor("grey"));
                shakeModeImg.setImageResource(R.drawable.icon_gray_rock);
                shakeModeText.setTextColor(Color.parseColor("grey"));
                break;
            case GlobalObjects.UNLOCK_MODE_AUTO:
                manualModeImg.setImageResource(R.drawable.icon_gray_manual);
                manualModeText.setTextColor(Color.parseColor("grey"));
                autoModeImg.setImageResource(R.drawable.icon_green_auto);
                autoModeText.setTextColor(Color.parseColor("green"));
                shakeModeImg.setImageResource(R.drawable.icon_gray_rock);
                shakeModeText.setTextColor(Color.parseColor("grey"));
                break;
            case GlobalObjects.UNLOCK_MODE_SHAKE:
                manualModeImg.setImageResource(R.drawable.icon_gray_manual);
                manualModeText.setTextColor(Color.parseColor("grey"));
                autoModeImg.setImageResource(R.drawable.icon_gray_auto);
                autoModeText.setTextColor(Color.parseColor("grey"));
                shakeModeImg.setImageResource(R.drawable.icon_green_rock);
                shakeModeText.setTextColor(Color.parseColor("green"));
                break;
            default:
                break;
        }
    }
}
