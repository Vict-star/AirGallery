package cn.edu.scut.airgallery.interfaces;

import java.util.ArrayList;
import cn.edu.scut.airgallery.data.Album;
import cn.edu.scut.airgallery.data.Media;

public interface MediaClickListener {

    void onMediaClick(Album album, ArrayList<Media> media, int position);
}
