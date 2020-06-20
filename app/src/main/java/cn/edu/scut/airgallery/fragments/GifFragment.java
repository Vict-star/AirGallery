package cn.edu.scut.airgallery.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import cn.edu.scut.airgallery.data.Media;
import pl.droidsonroids.gif.GifImageView;

/**
 * Media Fragment for showing an Image (static)
 */
public class GifFragment extends BaseMediaFragment {

    @NonNull
    public static GifFragment newInstance(@NonNull Media media) {
        return BaseMediaFragment.newInstance(new GifFragment(), media);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        GifImageView photoView = new GifImageView(getContext());
        photoView.setImageURI(media.getUri());
        setTapListener(photoView);
        return photoView;
    }
}
