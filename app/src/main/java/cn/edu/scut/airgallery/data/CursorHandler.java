package cn.edu.scut.airgallery.data;

import android.database.Cursor;

public interface CursorHandler<T> {

    T handle(Cursor cu);
    static String[] getProjection() {
        return new String[0];
    }
}
