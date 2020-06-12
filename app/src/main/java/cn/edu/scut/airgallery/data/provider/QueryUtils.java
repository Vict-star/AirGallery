package cn.edu.scut.airgallery.data.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import cn.edu.scut.airgallery.data.CursorHandler;
import io.reactivex.Observable;

public class QueryUtils {

    public static <T> Observable<T> query(Query q, ContentResolver cr, CursorHandler<T> ch) {
        Log.i("uri:", "here");
        return Observable.create(subscriber -> {
            Cursor cursor = null;
            try {
                cursor = q.getCursor(cr);
                if (cursor != null && cursor.getCount() > 0)
                    while (cursor.moveToNext()) {
                        subscriber.onNext(ch.handle(cursor));
                        int indexPhotoPath = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        Log.i("uri:", cursor.getString(indexPhotoPath));
                    }
                subscriber.onComplete();
            }
            catch (Exception err) { subscriber.onError(err); }
            finally { if (cursor != null) cursor.close(); }
        });
    }

    /**
     * return only the first element if there is one
     *
     * @param q
     * @param cr
     * @param ch
     * @param <T>
     * @return
     */
    public static <T> Observable<T> querySingle(Query q, ContentResolver cr, CursorHandler<T> ch) {
        return Observable.create(subscriber -> {
            Cursor cursor = null;
            try {
                cursor = q.getCursor(cr);
                if (cursor != null && cursor.moveToFirst())
                    subscriber.onNext(ch.handle(cursor));
                subscriber.onComplete();
            }
            catch (Exception err) { subscriber.onError(err); }
            finally { if (cursor != null) cursor.close(); }
        });
    }

}
