package cn.edu.scut.airgallery.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.edu.scut.airgallery.R;
import cn.edu.scut.airgallery.data.Media;
import cn.edu.scut.airgallery.items.ActionsListener;


public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private final ArrayList<Media> media;
    private int selectedCount = 0;
    private final ActionsListener actionsListener;
    private boolean isSelecting = false;

    public MediaAdapter(Context context, ActionsListener actionsListener) {
        super();
        media = new ArrayList<>();
        this.actionsListener = actionsListener;
    }

    @Override
    public long getItemId(int position) {
        return media.get(position).getUri().hashCode() ^ 1312;
    }

    public ArrayList<Media> getSelected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new ArrayList<>(media.stream().filter(Media::isSelected).collect(Collectors.toList()));
        } else {
            ArrayList<Media> arrayList = new ArrayList<>(selectedCount);
            for (Media m : media)
                if (m.isSelected())
                    arrayList.add(m);
            return arrayList;
        }
    }

    public Media getFirstSelected() {
        if (selectedCount > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                return media.stream().filter(Media::isSelected).findFirst().orElse(null);
            else
                for (Media m : media)
                    if (m.isSelected())
                        return m;
        }
        return null;
    }

    public ArrayList<Media> getMedia() {
        return media;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public void selectAll() {
        for (int i = 0; i < media.size(); i++)
            if (media.get(i).setSelected(true))
                notifyItemChanged(i);
        selectedCount = media.size();
        startSelection();
    }

    public boolean clearSelected() {
        boolean changed = true;
        for (int i = 0; i < media.size(); i++) {
            boolean b = media.get(i).setSelected(false);
            if (b)
                notifyItemChanged(i);
            changed &= b;
        }
        selectedCount = 0;
        stopSelection();
        return changed;
    }

    @NonNull
    @Override
    public MediaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_photo, parent, false));
    }

    private void notifySelected(boolean increase) {
        selectedCount += increase ? 1 : -1;
        actionsListener.onSelectionCountChanged(selectedCount, getItemCount());

        if (selectedCount == 0 && isSelecting) stopSelection();
        else if (selectedCount > 0 && !isSelecting) startSelection();
    }

    private void startSelection() {
        isSelecting = true;
        actionsListener.onSelectMode(true);
    }

    private void stopSelection() {
        isSelecting = false;
        actionsListener.onSelectMode(false);
    }

    public boolean selecting() {
        return isSelecting;
    }
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Media f = media.get(position);

        RequestOptions options = new RequestOptions()
                .signature(f.getSignature())
                .format(DecodeFormat.PREFER_RGB_565)
                .centerCrop()
                .placeholder(R.drawable.ic_empty_white)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        Glide.with(holder.picture.getContext())
                .load(f.getUri())
                .apply(options)
                .thumbnail(0.5f)
                .into(holder.picture);

        holder.card.setOnClickListener(v -> {
            if (selecting()) {
                notifySelected(f.toggleSelected());
                notifyItemChanged(holder.getAdapterPosition());
            } else
                actionsListener.onItemSelected(holder.getAdapterPosition());
        });

    }

    public void clear() {
        media.clear();
        notifyDataSetChanged();
    }

    public int add(Media album) {
        int i = 0;
        media.add(i, album);
        notifyItemInserted(i);
        return i;
    }

    @Override
    public int getItemCount() {
        return media.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.media_card)
        CardView card;
        @BindView(R.id.photo_preview)
        AppCompatImageView picture;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }
}
