package cn.edu.scut.airgallery.data.provider;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import cn.edu.scut.airgallery.data.Album;
import cn.edu.scut.airgallery.data.Media;
import io.reactivex.Observable;

public class CPHelper {

    private static String getHavingCluause(int excludedCount){

        if (excludedCount == 0)
            return "(";

        StringBuilder res = new StringBuilder();
        res.append("HAVING (");

        res.append(MediaStore.Images.Media.DATA).append(" NOT LIKE ?");

        for (int i = 1; i < excludedCount; i++)
            res.append(" AND ")
                    .append(MediaStore.Images.Media.DATA)
                    .append(" NOT LIKE ?");;

        return res.toString();
    }

    public static Observable<Album> getAlbums(Context context) {
        ArrayList<String> excludedAlbums = new ArrayList<>();
        excludedAlbums.add("/storage/1607-4107/Android") ;
        excludedAlbums.add("/storage/emulated/0/Android");
        Query.Builder query = new Query.Builder()
                .uri(MediaStore.Files.getContentUri("external"))
                .projection(Album.getProjection())
                .sort(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
                .ascending( false );

        ArrayList<Object> args = new ArrayList<>();
        query.selection(String.format("%s=?) group by (%s) %s ",
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.PARENT,
                getHavingCluause(excludedAlbums.size())));
        args.add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);

        for (String s : excludedAlbums)
            args.add(s+"%");
        query.args(args.toArray());

        Log.v("query:",query.build().toString());
        return QueryUtils.query(query.build(), context.getContentResolver(), Album::new);
    }

    public static Observable<Media> getMedia(Context context, Album album) {

        if (album.getId() == -1) return getMediaFromStorage(context, album);
        else if (album.getId() == Album.ALL_MEDIA_ALBUM_ID)
            return getAllMediaFromMediaStore(context);
        else
            return getMediaFromMediaStore(context, album);
    }

    private static Observable<Media> getAllMediaFromMediaStore(Context context) {
        Query.Builder query = new Query.Builder()
                .uri(MediaStore.Files.getContentUri("external"))
                .projection(Media.getProjection())
                .sort(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
                .ascending(false);

        query.selection(String.format("%s=?",
                MediaStore.Files.FileColumns.MEDIA_TYPE));
        query.args(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);

        return QueryUtils.query(query.build(), context.getContentResolver(), new Media());
    }

    private static Observable<Media> getMediaFromStorage(Context context, Album album) {

        return Observable.create(subscriber -> {
            File dir = new File(album.getPath());
            File[] files = dir.listFiles();
            try {
                if (files != null && files.length > 0)
                    for (File file : files)
                        subscriber.onNext(new Media(file));
                subscriber.onComplete();

            }
            catch (Exception err) { subscriber.onError(err); }
        });

    }

    private static Observable<Media> getMediaFromMediaStore(Context context, Album album) {

        Query.Builder query = new Query.Builder()
                .uri(MediaStore.Files.getContentUri("external"))
                .projection(Media.getProjection())
                .sort(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
                .ascending(false);

        query.selection(String.format("(%s=? or %s=?) and %s=?",
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.PARENT));
        query.args(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                album.getId());

        return QueryUtils.query(query.build(), context.getContentResolver(), Media::new);
    }

}

