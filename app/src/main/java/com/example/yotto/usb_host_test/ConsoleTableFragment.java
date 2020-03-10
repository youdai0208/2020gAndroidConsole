package com.example.yotto.usb_host_test;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

import java.nio.ByteBuffer;

import androidx.fragment.app.Fragment;

public class ConsoleTableFragment extends Fragment {

    private int send_data = 0;
    private TextView serial_stete_text;
    private TextView selected_zone_text;
    private TextView x_coordinate_text;
    private TextView y_coordinate_text;
    private TextView theta_coordinate_text;
    private TextView selected_mode_text;
    private TextView sequence_number_text;
    private Button serial_reconnect_button;
    private Button red_zone_button;
    private Button blue_zone_button;
    private Button reset_button;
    private Button fast_manual_button;
    private Button normal_manual_button;
    private Button one_button_mode_button;
    private Button route_select_mode_button;
    private UsbSerialDriver usbSerialDriver_;
    private MainActivity mainActivity;
    private final Handler handler = new Handler();

    static ConsoleTableFragment newInstance() {
        ConsoleTableFragment fragment_One = new ConsoleTableFragment();
        return fragment_One;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_one, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) getActivity();
        serial_stete_text = (TextView) view.findViewById(R.id.serial_state);
        selected_zone_text = (TextView) view.findViewById(R.id.selected_zone);
        x_coordinate_text = (TextView) view.findViewById(R.id.x_coordinate);
        y_coordinate_text = (TextView) view.findViewById(R.id.y_coordinate);
        theta_coordinate_text = (TextView) view.findViewById(R.id.theta);
        selected_mode_text = (TextView) view.findViewById(R.id.mode_text);
        sequence_number_text = (TextView) view.findViewById(R.id.sequence_number);

//        readThread();

        view.findViewById(R.id.reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mainActivity.isSerialPortActive()) {
                    mainActivity.refrashDevice();
                    mainActivity.onDeviceStateChange();
                }
            }
        });

        view.findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data = 1;
                mainActivity.writeThread(intToByteArray(send_data), 100);
                send_data = 0;
            }
        });

        view.findViewById(R.id.red_zone_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data = 2;
                mainActivity.writeThread(intToByteArray(send_data), 100);
                send_data = 0;
            }
        });

        view.findViewById(R.id.blue_zone_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data = 3;
                mainActivity.writeThread(intToByteArray(send_data), 100);
                send_data = 0;
            }
        });

        view.findViewById(R.id.fast_manual).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data = 4;
                mainActivity.writeThread(intToByteArray(send_data), 100);
                send_data = 0;
            }
        });

        view.findViewById(R.id.normal_manual).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data = 5;
                mainActivity.writeThread(intToByteArray(send_data), 100);
                send_data = 0;
            }
        });

        view.findViewById(R.id.one_button_auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data = 6;
                mainActivity.writeThread(intToByteArray(send_data), 100);
                send_data = 0;
            }
        });

        view.findViewById(R.id.select_route_auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data = 7;
                mainActivity.writeThread(intToByteArray(send_data), 100);
                send_data = 0;
            }
        });
    }

    public void setSerialStateText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                serial_stete_text.setText(text);
            }
        });
    }

    public void setSelectedZoneText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                selected_zone_text.setText(text);
            }
        });
    }

    public void setSequenceNumberText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                sequence_number_text.setText(text);
            }
        });
    }

    public void setXCoordinateText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                x_coordinate_text.setText(text);
            }
        });
    }

    public void setYCoordinateText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                y_coordinate_text.setText(text);
            }
        });
    }

    public void setThetaCoordinateText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                theta_coordinate_text.setText(text);
            }
        });
    }

    public void setSelectedModeText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                selected_mode_text.setText(text);
            }
        });
    }

    public void selectSequenceNumber(final String text) {
        switch (text) {
            case "0":
                setSequenceNumberText("Waiting");
                break;
            case "1":
                setSequenceNumberText("StartToPickup1");
                break;
            case "2":
                setSequenceNumberText("Pickup1ToWaitpoint");
                break;
            default:
                setSequenceNumberText("Exception: " + text);
                break;
        }
    }

    public void selectZone(final String text) {
        switch (text) {
            case "0":
                setSelectedZoneText("Red Zone");
                break;
            case "1":
                setSelectedZoneText("Blue Zone");
                break;
            default:
                setSelectedZoneText("Exception: " + text);
                break;
        }
        if (text == "0") {

        } else if (text == "1") {

        } else {

        }
    }

    public void selectMode(final String text) {
        switch (text) {
            case "0":
                setSelectedModeText("Fast Manual Mode");
                break;
            case "1":
                setSelectedModeText("Normal Manual Mode");
                break;
            case "2":
                setSelectedModeText("One Button Harf Auto Mode");
                break;
            case "3":
                setSelectedModeText("Route Select Harf Auto Mode");
                break;
            default:
                setSelectedModeText("Exception: " + text);
                break;
        }
    }

    private byte[] intToByteArray(final int value) {
        return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(value).array();
    }

//    public void readThread(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
////                mainActivity.start_read_thread();
//                byte[] data;
//                while (true){
//                    /*data = mainActivity.getBuf();
//                    if(data.length > 7 && data.length < 100){
//                        final String message = "Read " + data.length + " bytes: \n"
//                                + HexDump.dumpHexString(data) + "\n\n";
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                textView.setText(message);
//                            }
//                        });
//                    }*/
//
//                    /*if(mainActivity.getRevieve_size() > 7){
//                        data = mainActivity.getBuf();
//
//                    }*/
//                }
//            }
//        }).start();
//    }
//
//    public void read(final String message){
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                textView.setText(message);
//            }
//        });
//    }
}