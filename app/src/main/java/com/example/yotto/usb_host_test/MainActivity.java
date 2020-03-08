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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private static final int MESSAGE_REFRESH = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    private Fragment_1 fragment_1;
    private Fragment_two fragment_two;
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
//                    refreshDeviceList();
                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    };

    private void updateReceivedData(byte[] data) {
        final String message = HexDump.dumpHexString(data);
        String[] list = message.split(",", 0);
        if (list.length > 5) {
            fragment_1.setXCoordinateText(list[0]);
            fragment_1.setYCoordinateText(list[1]);
            fragment_1.setThetaCoordinateText(list[2]);
            fragment_1.selectSequenceNumber(list[3]);
            fragment_1.selectZone(list[4]);
            fragment_1.selectMode(list[5]);
//            int px = Integer.parseInt(list[0]);
//            int py = Integer.parseInt(list[1]);
//            float th = Float.parseFloat(list[2]);
//            fragment_two.movePicture((((int)Float.parseFloat(list[0])) / 10), (((int)Float.parseFloat(list[1])) / 10), Float.parseFloat(list[2]));
            fragment_two.movePicture(415, 905, -90);
        }
//        fragment_1.setSequenceNumberText(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            fragment_1 = Fragment_1.newInstance();
            fragment_two = Fragment_two.newInstance();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.add(R.id.fragment4, fragment_1);
            transaction.add(R.id.fragment3, fragment_two);
            transaction.commit();

            refrashDevice();

            onDeviceStateChange();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startIoManager();
        mHandler.sendEmptyMessage(MESSAGE_REFRESH);
        registerReceiver(usb_receiver, new IntentFilter(INTENT_ACTION_GRANT_USB));
    }

    @Override
    protected void onPause() {
        super.onPause();
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

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if(fragment_1 != null){
//            getSupportFragmentManager().putFragment(outState, "fragment_1", fragment_1);
//        }
//        if (fragment_two != null){
//            getSupportFragmentManager().putFragment(outState, "fragment_two", fragment_two);
//        }
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        if (fragment_1 == null){
//            fragment_1 = (Fragment_1) getSupportFragmentManager().getFragment(savedInstanceState, "fragment_1");
//        }
//        if(fragment_two == null) {
//            fragment_two = (Fragment_two) getSupportFragmentManager().getFragment(savedInstanceState, "fragment_two");
//        }
//    }

    public void refrashDevice() {
        stopIoManager();
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(this, "No device", Toast.LENGTH_SHORT).show();
            fragment_1.setSerialStateText("No device");
            return;
        }

        usb_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(INTENT_ACTION_GRANT_USB)) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        showSerialActivity(usb_serial_port);
                    } else {
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
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            manager.requestPermission(usbDevice, usbPermissionIntent);
            return;
        }

        port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            fragment_1.setSerialStateText(port.getClass().getSimpleName());
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
            mExecutor.submit(serial_io_manager);
        }
    }

    public void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    public static String bin2hex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString();
    }
}

/*public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private byte buf[] = new byte[258];
    private int revieve_size = 0;
    private UsbManager usb_manager;
    private UsbSerialDriver usb_serial_driver;
    private static UsbSerialPort usb_serial_port;
    private SerialInputOutputManager serial_io_manager;
    private BroadcastReceiver usb_receiver;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler();
    private String dev_name = "";

    private static final int MESSAGE_REFRESH = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;
    public static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

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
//                    refreshDeviceList();
                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if(savedInstanceState == null){
            setContentView(R.layout.activity_main);
//        }
        final Context context = this;

        usb_manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        usb_receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(INTENT_ACTION_GRANT_USB)) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        showSerialActivity(usb_serial_port);
                    } else {
                        Toast.makeText(context, "USB permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        final List<UsbSerialDriver> driverList = UsbSerialProber.getDefaultProber().findAllDrivers(usb_manager);
        if(driverList == null || driverList.isEmpty()){
            return;
        }else {
            final UsbSerialDriver usbSerialDriver = driverList.get(0);
            final UsbSerialPort port = usbSerialDriver.getPorts().get(0);
            final UsbDevice usbDevice = port.getDriver().getDevice();

            if(!usb_manager.hasPermission(usbDevice)){
                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
                usb_manager.requestPermission(usbDevice, usbPermissionIntent);
                usb_serial_port = port;
//                showSerialActivity(port);
            }else {
//                showSerialActivity(port);
                usb_serial_port = port;
            }
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        mHandler.sendEmptyMessage(MESSAGE_REFRESH);
        registerReceiver(usb_receiver, new IntentFilter(INTENT_ACTION_GRANT_USB));

        usb_manager = (UsbManager)getSystemService(USB_SERVICE);

        if(usb_serial_port == null){
            Toast.makeText(MainActivity.this, "No devices connected", Toast.LENGTH_SHORT).show();
        }else {


            UsbDeviceConnection connection = usb_manager.openDevice(usb_serial_port.getDriver().getDevice());
            if(connection == null){
                Toast.makeText(MainActivity.this, "Opening device failed", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                usb_serial_port.open(connection);
                usb_serial_port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                Toast.makeText(MainActivity.this, "Open the device", Toast.LENGTH_SHORT).show();
            }catch (IOException e) {
                try {
                    usb_serial_port.close();
                }catch (IOException e2){
                    e2.printStackTrace();
                }
                usb_serial_port = null;
                return;
            }
        }
        onDeviceStateChange();

//        serialManagement.serialInit(usb_manager, 9600);

        /*updateList();
        usb_serial_driver = UsbSerialProber.acquire(usb_manager);
        if(usb_serial_driver != null){
            try{
                usb_serial_driver.open();
//                start_read_thread();
                Toast.makeText(MainActivity.this, "Open SerialPort", Toast.LENGTH_SHORT).show();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        onDeviceStateChange();
    }

    @Override
    protected void onPause(){
        super.onPause();

        mHandler.removeMessages(MESSAGE_REFRESH);
        unregisterReceiver(usb_receiver);
        stopIoManager();
        serialPortClose();
//        serialManagement.close();
    }

//    private void showSerialActivity(UsbSerialPort port){
//        SerialActivity.show(this, port);
//    }

//    private void updateList() {
//        HashMap<String, UsbDevice> device_list = usb_manager.getDeviceList();
//
//        if (device_list == null || device_list.isEmpty()) {
////            text_view.setText("No device found");
//        } else {
//            for (String name : device_list.keySet()) {
//                dev_name += name + "\n";
//                usb_devices = device_list.get(name);
//            }
////            text_view.setText(dev_name);
//        }
//    }

//    public void start_read_thread(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    while(true){
//                        revieve_size = usb_serial_port.read(buf, 100);
//                        if(revieve_size > 0){
//                            Log.v("arduino", new String(buf, 0, revieve_size));
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(MainActivity.this, new String(buf, 0, revieve_size), Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }
//                        Thread.sleep(5);
//                    }
//                }catch (IOException e){
//                    e.printStackTrace();
//                }catch (InterruptedException e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//
//    public void writeThread(final byte[] send_data, final int size){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    if(usb_serial_driver != null){
//                        final int write = usb_serial_port.write(send_data, 100);
//                    }
//                }catch (IOException e) {
//                    e.printStackTrace();
////                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            }
//        }).start();
//    }
//
    public void serialPortClose(){
        try {
            if(usb_serial_port != null){
                usb_serial_port.close();
                usb_serial_port = null;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
//
    private void stopIoManager() {
        if (serial_io_manager != null) {
            Log.i(TAG, "Stopping io manager ..");
            serial_io_manager.stop();
            serial_io_manager = null;
        }
    }

    private void startIoManager() {
        if (usb_serial_driver != null) {
            Log.i(TAG, "Starting io manager ..");
            serial_io_manager = new SerialInputOutputManager(usb_serial_port, mListener);
            mExecutor.submit(serial_io_manager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {

        final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";
        if(data.length > 7){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
            buf = data;
            revieve_size = data.length;
        }

//        mDumpTextView.append(message);
//        mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
    }
//
//    public String getDevName(){
//        return dev_name;
//    }
//
    public byte[] getBuf() {
        return buf;
    }

    public int getRevieve_size() {
        return revieve_size;
    }
//
//    static void show(Context context, UsbSerialPort port) {
//        usb_serial_port = port;
//        final Intent intent = new Intent(context, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
//        context.startActivity(intent);
//    }
}*/
