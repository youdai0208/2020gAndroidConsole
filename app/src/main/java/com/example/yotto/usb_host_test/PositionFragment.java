package com.example.yotto.usb_host_test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class PositionFragment extends Fragment {

    private CustomImageView cImageView;

    static PositionFragment newInstance() {
        PositionFragment positionFragment = new PositionFragment();
        return positionFragment;
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

        cImageView = view.findViewById(R.id.image_view);
    }

    public void movePicture(final int x, final int y, final float th) {
        int imgW = x + cImageView.getWidth();
        int imgH = y + cImageView.getHeight();

        cImageView.setRotation(th);
        cImageView.layout(x, y, imgW, imgH);
    }

    public int getImageWidth() {
        return cImageView.getWidth();
    }

    public int getImageHeight() {
        return cImageView.getHeight();
    }
}
