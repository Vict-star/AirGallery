package cn.edu.scut.airgallery.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.scut.airgallery.R;
import cn.edu.scut.airgallery.data.Media;

public class nullFragment extends BaseMediaFragment {
    @BindView(R.id.textShow)
    TextView textShow;

    @NonNull
    public static nullFragment newInstance(@NonNull Media media) {
        return BaseMediaFragment.newInstance(new nullFragment(), media);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_null, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textShow.setText("暂时不支持视频");
        setTapListener(view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
