package com.example.yotto.usb_host_test;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class PositionFragment extends Fragment /*implements View.OnTouchListener*/ {

    private CustomImageView cImageView;
    private ImageView imageView;
    private Bitmap base_bitmap;
    private Bitmap result_bitmap;
    private int preDx, preDy;
    private final int picture_origin_x = 5, picture_origin_y = (1010 - 50 + 110);
    private TextView textView;

    static PositionFragment newInstance() {
        PositionFragment positionFragment_ = new PositionFragment();
        return positionFragment_;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        super.onCreateView(inflater, container, saveInstanceState);
        return inflater.inflate(R.layout.fragment_two, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        textView = (TextView) view.findViewById(R.id.text_view);
        imageView = (ImageView) view.findViewById(R.id.base_image);
        cImageView = view.findViewById(R.id.image_view);

//        cImageView.setOnTouchListener(this);
    }

    /*@Override
    public boolean onTouch(View v, MotionEvent event) {
        // x,y 位置取得
        int newDx = (int) event.getRawX();
        int newDy = (int) event.getRawY();

        switch (event.getAction()) {
            // タッチダウンでdragされた
            case MotionEvent.ACTION_MOVE:
                // ACTION_MOVEでの位置
                // performCheckを入れろと警告が出るので
                v.performClick();
                int dx = cImageView.getLeft() + (newDx - preDx);
                int dy = cImageView.getTop() + (newDy - preDy);
                int imgW = dx + cImageView.getWidth();
                int imgH = dy + cImageView.getHeight();

                if (dx < 0) {
                    dx = 0;
                }

                if (dy < 0) {
                    dy = 0;
                }
                // 画像の位置を設定する
                cImageView.layout(dx, dy, imgW, imgH);

                String str = "dx=" + dx + "\ndy=" + dy;
                textView.setText(str);
                Log.d("onTouch", "ACTION_MOVE: dx=" + dx + ", dy=" + dy);
                break;
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }

        // タッチした位置を古い位置とする
        preDx = newDx;
        preDy = newDy;

        return true;
    }*/

    public void movePicture(final int pre_dx, final int pre_dy, final float pre_th) {
        int x = cImageView.getWidth() + (pre_dx - preDx);
        int y = cImageView.getHeight() + (pre_dy - preDy);
        int imgW = pre_dx + cImageView.getWidth();
        int imgH = pre_dy + cImageView.getHeight();

        cImageView.setRotation(pre_th);
        cImageView.layout(pre_dx, pre_dy, imgW, imgH);
    }

    public int getImageWidth() {
        return cImageView.getWidth();
    }

    public int getImageHeight() {
        return cImageView.getHeight();
    }
}
