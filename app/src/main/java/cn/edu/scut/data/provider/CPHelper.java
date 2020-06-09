package cn.edu.scut.data.provider;

import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

import cn.edu.scut.data.Album;
import io.reactivex.Observable;


/**
 * Created by dnld on 24/07/16.
 */

public class CPHelper {

//    public static Observable<Album> getAlbums(Context context, boolean hidden, ArrayList<String> excluded ) {
//        return getAlbums(context);
//    }

    private static String getHavingCluause(int excludedCount){

        if (excludedCount == 0)
            return "(";

        StringBuilder res = new StringBuilder();
        res.append("HAVING (");

        res.append(MediaStore.Images.Media.DATA).append(" NOT LIKE ?");

        for (int i = 1; i < excludedCount; i++)
            res.append(" AND ")
                    .append(MediaStore.Images.Media.DATA)
                    .append(" NOT LIKE ?");

        // NOTE: dont close ths parenthesis it will be closed by ContentResolver
        //res.append(")");

        return res.toString();

    }

    public static Observable<Album> getAlbums(Context context ) {
        ArrayList<String> excludedAlbums = new ArrayList<>();
        excludedAlbums.add("/storage/1607-4107/Android") ;
        excludedAlbums.add("/storage/emulated/0/Android");
        Query.Builder query = new Query.Builder()
                .uri(MediaStore.Files.getContentUri("external"))
                .projection(Album.getProjection())
                .sort(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
                .ascending( true );

        ArrayList<Object> args = new ArrayList<>();
        query.selection(String.format("%s=? or %s=?) group by (%s) %s ",
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.PARENT,
                getHavingCluause(excludedAlbums.size())));
        args.add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
        args.add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);

        for (String s : excludedAlbums)
            args.add(s+"%");
        query.args(args.toArray());

        Log.v("query:",query.build().toString());
        return QueryUtils.query(query.build(), context.getContentResolver(), Album::new);
    }
}


