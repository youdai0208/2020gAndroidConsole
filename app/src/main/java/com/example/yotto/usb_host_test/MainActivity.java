package com.example.yotto.usb_host_test;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

import com.hoho.android.usbserial.util.SerialInputOutputManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private static final int MESSAGE_REFRESH = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    private boolean bcr = false;
    private byte[] responce = new byte[100];
    private int responce_counter = 0;
    private ConsoleTableFragment consoleTableFragment;
    private PositionFragment positionFragment;
    private List<UsbSerialDriver> availableDrivers;
    private UsbManager manager;
    private UsbSerialDriver driver;
    private UsbDeviceConnection connection;
    private UsbSerialPort port;
    private SerialInputOutputManager serial_io_manager;
    private BroadcastReceiver usb_receiver;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {
                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    };

    private void updateReceivedData(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            responce[responce_counter] = data[i];
            responce_counter++;

            if (data[i] == '\n') {
                bcr = true;
                break;
            }
        }

        if (bcr) {
            bcr = false;
            responce_counter = 0;

            final String message = new String(responce);
            String[] list = message.split(",", 0);

            if (list.length > 5) {
                consoleTableFragment.setXCoordinateText(list[0]);
                consoleTableFragment.setYCoordinateText(list[1]);
                consoleTableFragment.setThetaCoordinateText(list[2]);
                consoleTableFragment.selectSequenceNumber(list[3]);
                consoleTableFragment.selectZone(list[4]);
                consoleTableFragment.selectMode(list[5]);

                switch (list[4]) {
                    case "0":
                        try {
                            final int px = (((((int) Float.parseFloat(list[0])) / 10) + 5) - (positionFragment.getImageWidth() / 2));
                            final int py = ((1010 - (((int) Float.parseFloat(list[1]) / 10) + 5)) - (positionFragment.getImageHeight() / 2));
                            final float th = Float.parseFloat(list[2]) * (-1);
                            positionFragment.movePicture(px, py, th);
                        } catch (NumberFormatException n) {
                            n.printStackTrace();
                        }
                        break;
                    case "1":
                        try {
                            final int px = ((1340 + (((int) Float.parseFloat(list[0]) / 10) - 5)) - (positionFragment.getImageWidth() / 2));
                            final int py = ((1010 - (((int) Float.parseFloat(list[1]) / 10) + 5)) - (positionFragment.getImageHeight() / 2));
                            final float th = Float.parseFloat(list[2]) * (-1);
                            positionFragment.movePicture(px, py, th);
                        } catch (NumberFormatException n) {
                            n.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (this.consoleTableFragment == null) {
            this.consoleTableFragment = ConsoleTableFragment.newInstance();
        }
        if (this.positionFragment == null) {
            this.positionFragment = PositionFragment.newInstance();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.console, this.consoleTableFragment);
        transaction.add(R.id.position, this.positionFragment);
        transaction.commit();

        refreshDevice();

        onDeviceStateChange();

        mHandler.sendEmptyMessage(MESSAGE_REFRESH);
        registerReceiver(usb_receiver, new IntentFilter(INTENT_ACTION_GRANT_USB));
    }

    @Override
    protected void onPause() {
        super.onPause();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this.consoleTableFragment);
        ft.remove(this.positionFragment);
        ft.commit();
        if (port != null) {
            try {
                mHandler.removeMessages(MESSAGE_REFRESH);
                unregisterReceiver(usb_receiver);
                port.close();
                port = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void refreshDevice() {
        stopIoManager();
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x0483, 0x374b, CdcAcmSerialDriver.class);
        UsbSerialProber prober = new UsbSerialProber(customTable);
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(this, "No device", Toast.LENGTH_SHORT).show();
            consoleTableFragment.setSerialStateText("No device");
            availableDrivers = prober.findAllDrivers(manager);
        }

        if (availableDrivers.isEmpty()) {
            return;
        }

        usb_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(INTENT_ACTION_GRANT_USB)) {
                    if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Toast.makeText(context, "USB permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        // Open a connection to the first available driver.
        driver = availableDrivers.get(0);
        connection = manager.openDevice(driver.getDevice());
        UsbDevice usbDevice = driver.getDevice();
        if (connection == null) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            manager.requestPermission(usbDevice, usbPermissionIntent);
            return;
        }

        port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            consoleTableFragment.setSerialStateText(port.getDriver().getDevice().getDeviceName());
            Toast.makeText(MainActivity.this, "Device connected", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopIoManager() {
        if (serial_io_manager != null) {
            Log.i(TAG, "Stopping io manager ..");
            serial_io_manager.stop();
            serial_io_manager = null;
        }
    }

    private void startIoManager() {
        if (port != null) {
            Log.i(TAG, "Starting io manager ..");
            serial_io_manager = new SerialInputOutputManager(port, mListener);
            serial_io_manager.setReadTimeout(100);
            Executors.newSingleThreadExecutor().submit(serial_io_manager);
        }
    }

    public void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    public void writeThread(final byte[] send_data, final int time_out) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (port != null) {
                        final int write = port.write(send_data, time_out);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean isSerialPortActive() {
        boolean result;
        if (port != null) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }
}
