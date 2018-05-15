import android.util.Log;

import com.example.x6.serial.SerialPort;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.Thread;
import java.util.concurrent.atomic.AtomicInteger;

public class MyDemoProgram implements Runnable {
    private SerialPort mSerialPort;
    final static String COM1_DevicePathName;
    final static String COM2_DevicePathName;
    final static String COM3_DevicePathName;
    final static String COM4_DevicePathName;

    static {
        COM1_DevicePathName = "/dev/ttyS0";
        COM2_DevicePathName = "/dev/ttyS1";
        COM3_DevicePathName = "/dev/ttyS2";
        COM4_DevicePathName = "/dev/ttyS3";
    }

    public MyDemoProgram() {
        mSerialPort = null;
    }

    public SerialPort getNewSerialPortInstance(String devicePathName) {
        final int BAUDRATE = 115200;  // 默认波特率
        final int NONE = 0;  // 奇偶校验方式0代表NONE, 即无校验
        final int ODD = 1;  // 奇偶校验方式1代表奇校验
        final int EVEN = 2;  // 奇偶校验方式2代表偶校验

        try {
            return new SerialPort(new File(devicePathName), BAUDRATE, NONE);
        } catch (IOException ignored) {
        }
        return null;
    }

    @Override
    public void run() {
        mSerialPort = getNewSerialPortInstance(COM1_DevicePathName);
        if (null == mSerialPort) {
            Log.e("MyDemoProgram", "Thread run(): Can not connect to device " + COM1_DevicePathName);
            return;
        }

        InputStream in = new java.io.BufferedInputStream(
            mSerialPort.getInputStream()
        );
        OutputStream out = mSerialPort.getOutputStream();
        while (true) {
            byte[] data = new byte[64];
            try {
                int cnt = in.read(data, 0, data.length);
                Log.v("MyDemoProgram", "Thread run(): read(data): cnt=" + String.valueOf(cnt));
                if (cnt > 0) {
                    out.write(data, 0, cnt);
                    out.flush();
                } else {
                    final int SLEEP_INTERVAL = 1000;  // 单位: 毫秒
                    Log.v("MyDemoProgram", "Thread run(): No input data. Let's sleep " + String.valueOf(SLEEP_INTERVAL) + "ms");
                    Thread.sleep(SLEEP_INTERVAL);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("MyDemoProgram", "Thread run(): Unknown error on device " + COM1_DevicePathName);
                break;
            } catch (InterruptedException ignored) {
            }
        }

        if (in instanceof java.io.BufferedInputStream) try {
            in.close();
        } catch (IOException ignore) {
        }
        cleanup();
    }

    protected void finalize() throws java.lang.Throwable {
        super.finalize();
        cleanup();
    }

    @SuppressWarnings("UnusedAssignment")
    private void cleanup() {
        if (null == mSerialPort) {
            return;
        }
        try {
            OutputStream dataOut = mSerialPort.getOutputStream();
            dataOut.close();
            dataOut = null;
        } catch (IOException ignored) {
        }

        try {
            InputStream dataIn = mSerialPort.getInputStream();
            dataIn.close();
            dataIn = null;
        } catch (IOException ignored) {
        }

        // 关闭上述输入输出流之后再关闭串口设备
        mSerialPort.close();
        mSerialPort = null;
    }
}
