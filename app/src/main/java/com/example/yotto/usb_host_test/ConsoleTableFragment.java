package com.example.yotto.usb_host_test;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.nio.ByteBuffer;

import androidx.fragment.app.Fragment;

public class ConsoleTableFragment extends Fragment {

    //    private int send_data = 10;
    private final int NORMAL = 10;
    private final int RESET = 11;
    private final int RED_ZONE = 12;
    private final int BLUE_ZONE = 13;
    private final int FAST_MANUAL = 14;
    private final int NORMAL_MANUAL = 15;
    private final int ONE_BUTTON_AUTO = 16;
    private final int ROUTE_SELECT_AUTO = 17;
    private final int WRITE_TIME_OUT = 1000;

    private int[] send_data = {10, 0, 0, 0, 0, 0, 0, 0};
    private TextView serial_state_text;
    private TextView selected_zone_text;
    private TextView x_coordinate_text;
    private TextView y_coordinate_text;
    private TextView theta_coordinate_text;
    private TextView selected_mode_text;
    private TextView sequence_number_text;
    private MainActivity mainActivity;
    private final Handler handler = new Handler();

    static ConsoleTableFragment newInstance() {
        ConsoleTableFragment consoleTableFragment = new ConsoleTableFragment();
        return consoleTableFragment;
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
        serial_state_text = (TextView) view.findViewById(R.id.serial_state);
        selected_zone_text = (TextView) view.findViewById(R.id.selected_zone);
        x_coordinate_text = (TextView) view.findViewById(R.id.x_coordinate);
        y_coordinate_text = (TextView) view.findViewById(R.id.y_coordinate);
        theta_coordinate_text = (TextView) view.findViewById(R.id.theta);
        selected_mode_text = (TextView) view.findViewById(R.id.mode_text);
        sequence_number_text = (TextView) view.findViewById(R.id.sequence_number);

//        view.findViewById(R.id.reconnect).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mainActivity.isSerialPortActive()) {
//                    mainActivity.refreshDevice();
//                    mainActivity.onDeviceStateChange();
//                }
//            }
//        });

        view.findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data[0] = RESET;
                mainActivity.writeThread(intArrayToByteArray(send_data), WRITE_TIME_OUT);
                send_data[0] = NORMAL;
            }
        });

        view.findViewById(R.id.red_zone_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data[0] = RED_ZONE;
                mainActivity.writeThread(intArrayToByteArray(send_data), WRITE_TIME_OUT);
                send_data[0] = NORMAL;
            }
        });

        view.findViewById(R.id.blue_zone_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data[0] = BLUE_ZONE;
                mainActivity.writeThread(intArrayToByteArray(send_data), WRITE_TIME_OUT);
                send_data[0] = NORMAL;
            }
        });

        view.findViewById(R.id.fast_manual).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data[0] = FAST_MANUAL;
                mainActivity.writeThread(intArrayToByteArray(send_data), WRITE_TIME_OUT);
                send_data[0] = NORMAL;
            }
        });

        view.findViewById(R.id.normal_manual).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data[0] = NORMAL_MANUAL;
                mainActivity.writeThread(intArrayToByteArray(send_data), WRITE_TIME_OUT);
                send_data[0] = NORMAL;
            }
        });

        view.findViewById(R.id.one_button_auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data[0] = ONE_BUTTON_AUTO;
                mainActivity.writeThread(intArrayToByteArray(send_data), WRITE_TIME_OUT);
                send_data[0] = NORMAL;
            }
        });

        view.findViewById(R.id.select_route_auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_data[0] = ROUTE_SELECT_AUTO;
                mainActivity.writeThread(intArrayToByteArray(send_data), WRITE_TIME_OUT);
                send_data[0] = NORMAL;
            }
        });
    }

    public void setSerialStateText(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                serial_state_text.setText(text);
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
                setSequenceNumberText("Start To PickUp1");
                break;
            case "1":
                setSequenceNumberText("PickUp1 To WaitPoint");
                break;
            case "2":
                setSequenceNumberText("WaitPoint To PickUp1");
                break;
            case "3":
                setSequenceNumberText("WaitPoint To PickUp2");
                break;
            case "4":
                setSequenceNumberText("PickUp2 To WaitPoint");
                break;
            case "5":
                setSequenceNumberText("WaitPoint To PickUp3");
                break;
            case "6":
                setSequenceNumberText("PickUp3 To WaitPoint");
                break;
            case "7":
                setSequenceNumberText("WaitPoint To PickUp4");
                break;
            case "8":
                setSequenceNumberText("PickUp4 To WaitPoint");
                break;
            case "9":
                setSequenceNumberText("WaitPoint To PickUp5");
                break;
            case "10":
                setSequenceNumberText("PickUp5 To WaitPoint");
                break;
            case "11":
                setSequenceNumberText("WaitPoint To Pass1");
                break;
            case "12":
                setSequenceNumberText("Pass1 To WaitPoint");
                break;
            case "13":
                setSequenceNumberText("Pass1 To KickSet");
                break;
            case "14":
                setSequenceNumberText("KickSet To Kick");
                break;
            case "15":
                setSequenceNumberText("Kick To WaitPoint");
                break;
            case "16":
                setSequenceNumberText("WaitPoint To Pass2");
                break;
            case "17":
                setSequenceNumberText("Pass2 To WaitPoint");
                break;
            case "18":
                setSequenceNumberText("Pass2 To KickSet");
                break;
            case "19":
                setSequenceNumberText("WaitPoint To KickSet");
                break;
            case "20":
                setSequenceNumberText("WaitPoint To Start");
                break;
            case "21":
                setSequenceNumberText("WaitPoint");
                break;
            case "22":
                setSequenceNumberText("Waiting");
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

    private byte[] intArrayToByteArray(final int[] value_array) {
        int len = 0;
        byte[] result = new byte[((Integer.SIZE / Byte.SIZE) * (value_array.length + 2))];
        byte[] save;
        for (int i = 0; i < value_array.length; ++i) {
            save = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(value_array[i]).array();
            System.arraycopy(save, 0, result, (i * save.length), save.length);
        }
        return result;
    }
}
