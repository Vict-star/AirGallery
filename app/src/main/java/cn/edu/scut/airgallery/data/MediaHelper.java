package cn.edu.scut.airgallery.data;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

import cn.edu.scut.airgallery.progress.ProgressException;
import io.reactivex.Observable;

public class MediaHelper {

    private static Uri external = MediaStore.Files.getContentUri("external");

    public static Observable<Media> deleteMedia(Context context, Media media) {
        return Observable.create(subscriber -> {
            try {
                internalDeleteMedia(context, media);
                subscriber.onNext(media);
            } catch (ProgressException e) {
                subscriber.onError(e);
            }
            subscriber.onComplete();
        });
    }

    public static boolean internalDeleteMedia(Context context, Media media) throws ProgressException {
        File file = new File(media.getPath());
        StorageHelper.deleteFile(context, file);
        context.getContentResolver().delete(external, MediaStore.MediaColumns.DATA + "=?", new String[]{file.getPath()});
        return true;
    }
}
