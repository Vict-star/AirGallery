package cn.edu.scut.airgallery.adapters;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

import cn.edu.scut.airgallery.data.Media;
import cn.edu.scut.airgallery.fragments.GifFragment;
import cn.edu.scut.airgallery.fragments.ImageFragment;

public class MediaPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Media> media;
    private SparseArray<Fragment> registeredFragments = new SparseArray<>();

    public MediaPagerAdapter(FragmentManager fm, ArrayList<Media> media) {
        super(fm);
        this.media = media;
    }

    @Override
    public Fragment getItem(int pos) {
        Media media = this.media.get(pos);
//        if(media.isVedio()) {
//            return nullFragment.newInstance(media);
//        }
        if (media.isGif()) return GifFragment.newInstance(media);
        return ImageFragment.newInstance(media);
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    public void swapDataSet(ArrayList<Media> media) {
        this.media = media;
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        return media.size();
    }
}
