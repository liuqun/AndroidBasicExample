package com.example.demo_app;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class MainActivity extends Activity {
    private static final String TAG = "liuqun";
    private static TextView mTextView1 = null;
    private Resources mResources = null;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main); // 当心: setContentView()必须位于修改任何View类对象之前

        final boolean forceKeepScreenOn = true; // 是否强制令屏幕常亮, 仅为了方便调试
        final int FLAG_MASK = ~0;
        //noinspection ConstantConditions
        if (forceKeepScreenOn) {
            // 强制保持屏幕常亮
            Window w = getWindow();
            w.setFlags(FLAG_KEEP_SCREEN_ON, FLAG_MASK);
            //noinspection UnusedAssignment
            w = null;
        }

        mResources = getResources();
        mTextView1 = findViewById(R.id.text_view_1);
        Button mButton1 = findViewById(R.id.button_1);
        Button mButton2 = findViewById(R.id.button_2);
        mButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (null == mTextView1) {
                    return;
                }
                final String text = (mResources != null) ?
                        mResources.getString(R.string.message_001) : "";
                mTextView1.setText(text);
            }
        });
        mButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (null == mTextView1) {
                    return;
                }
                final String text = (mResources != null) ?
                        mResources.getString(R.string.message_002) : "";
                mTextView1.setText(text);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mTextView1 = null;
        super.onDestroy();
    }
}

