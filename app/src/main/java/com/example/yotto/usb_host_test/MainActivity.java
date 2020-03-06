package com.example.yotto.usb_host_test;

import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private TextView text_view;
    private UsbManager usb_manager;
    private UsbDevice usb_devices;
    private Button button;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume(){
        super.onResume();

        text_view = (TextView)findViewById(R.id.textview);
        button = (Button)findViewById(R.id.button);
        usb_manager = (UsbManager)getSystemService(USB_SERVICE);

        updateList();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usb_devices == null) {
                    return;
                }

                if (!usb_manager.hasPermission(usb_devices)) {
                    usb_manager.requestPermission(usb_devices,
                            PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0));
                    return;
                }
                connectDevice();
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();

        usb_devices = null;
        button.setOnClickListener(null);
    }

    private void updateList(){
        HashMap<String, UsbDevice> device_list = usb_manager.getDeviceList();

        if (device_list == null || device_list.isEmpty()){
            text_view.setText("No device found");
        }else {
            String dev_name = "";
            for (String name : device_list.keySet()) {
                dev_name += name + "\n";
                usb_devices = device_list.get(name);
            }
            text_view.setText(dev_name);
        }
    }

    private void connectDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UsbDeviceConnection connection = usb_manager.openDevice(usb_devices);

                if (!connection.claimInterface(usb_devices.getInterface(1), true)) {
                    connection.close();
                    return;
                }

                UsbEndpoint epIN = null;
                UsbEndpoint epOUT = null;

                UsbInterface usbIf = usb_devices.getInterface(1);
                for (int i = 0; i < usbIf.getEndpointCount(); i++) {
                    if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
                            epIN = usbIf.getEndpoint(i);
                        else
                            epOUT = usbIf.getEndpoint(i);
                    }
                }

                connection.bulkTransfer(epOUT, "1".getBytes(), 1, 0);
                connection.close();
            }
        }).start();
    }
}
