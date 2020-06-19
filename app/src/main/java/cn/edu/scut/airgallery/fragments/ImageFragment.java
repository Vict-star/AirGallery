package cn.edu.scut.airgallery.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.scut.airgallery.R;
import cn.edu.scut.airgallery.data.Media;
import cn.edu.scut.airgallery.util.BitmapUtils;

public class ImageFragment extends BaseMediaFragment {
    @BindView(R.id.subsampling_view) SubsamplingScaleImageView imageView;

    @NonNull
    public static ImageFragment newInstance(@NonNull Media media) {
        return BaseMediaFragment.newInstance(new ImageFragment(), media);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Uri mediaUri = media.getUri();
        imageView.setOrientation(BitmapUtils.getOrientation(mediaUri, getContext()));
        imageView.setImage(ImageSource.uri(mediaUri));
        setTapListener(imageView);
    }

    @Override
    public void onDestroyView() {
        imageView.recycle();
        super.onDestroyView();
    }
}

