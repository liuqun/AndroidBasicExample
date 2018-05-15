package com.example.demo_app;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.x6.serial.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static TextView mTextView1 = null;
    private Resources mResources;

    private SerialPort mCOM;
    private InputStream mInput;
    private OutputStream mOutput;

    public MainActivity() {
        mCOM = null;
        mInput = null;
        mOutput = null;
        mResources = null;
    }

    private void closeIOStreams() {
        if (mInput != null) {
            try {
                mInput.close();
            } catch (IOException ignored) {
            } finally {
                mInput = null;
            }
        }
        if (mOutput != null) {
            try {
                mOutput.close();
            } catch (IOException ignored) {
            } finally {
                mOutput = null;
            }
        }

        // 一并关闭串口
        if (mCOM != null) {
            try {
                mCOM.close();
            } catch (Exception ignored) {
            } finally {
                mCOM = null;
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        closeIOStreams();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 为保证安全, 先关闭之前已经打开的TTY串口
        closeIOStreams();

        // 初始化串口
        if (null == (mCOM = getSerialPortInstance("/dev/ttyS0"))) {
            Log.e(TAG, "onStart: " + "无法打开串口/dev/ttyS0");
            return;
        }
        mInput = mCOM.getInputStream();  // 初始化输入流
        mOutput = mCOM.getOutputStream();  // 初始化输出流
    }

    private SerialPort getSerialPortInstance(String devicePathName) {
        try {
            return new SerialPort(new File(devicePathName), 115200, 0);  // TODO: 波特率=115200, 奇偶校验=NONE
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
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
        mTextView1 = (TextView) findViewById(R.id.text_view_1);
        Button mButton1 = (Button) findViewById(R.id.button_1);
        Button mButton2 = (Button) findViewById(R.id.button_2);
        mButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (null == mTextView1) {
                    return;
                }
                final String text = (mResources != null) ?
                        mResources.getString(R.string.message_001) : "";
                mTextView1.setText(text);
                try {
                    byte [] data = text.getBytes();
                    mOutput.write(data);
                    mOutput.flush();
                } catch (IOException ignored) {
                }
            }
        });
        mButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (null == mTextView1) {
                    return;
                }
                try {
                    byte [] data = new byte[38];
                    int cnt = 0;
                    int available = mInput.available();
                    if (available > 0) {
                        cnt = mInput.read(data, 0, Math.min(data.length, available));
                    }
                    if (cnt <= 0) {
                        return;
                    }
                    final String text = hexStringFromRawData(data);
                    mTextView1.setText(text);
                } catch (IOException ignored) {
                }
            }
        });
    }
    private static String hexStringFromRawData(byte[] data){
        StringBuilder result = new StringBuilder();

        for(byte i : data){
            final int value = i & 0xFF;
            result.append(String.format("%02X ", value));
        }
        return result.toString();
    }

    @Override
    protected void onDestroy() {
        mTextView1 = null;
        super.onDestroy();
    }
}

