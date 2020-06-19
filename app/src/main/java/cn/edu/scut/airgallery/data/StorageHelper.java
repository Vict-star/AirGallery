package cn.edu.scut.airgallery.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import com.orhanobut.hawk.Hawk;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.HashSet;

import cn.edu.scut.airgallery.progress.ErrorCause;
import cn.edu.scut.airgallery.progress.ProgressException;
import cn.edu.scut.airgallery.util.ApplicationUtils;
import cn.edu.scut.airgallery.util.StringUtils;
import cn.edu.scut.airgallery.R;

public class StorageHelper {

    private static final String TAG = "StorageHelper";

    public static void scanFile(Context context, String[] path) {
        MediaScannerConnection.scanFile(context.getApplicationContext(), path, null, null);
    }

    private static boolean isWritable(@NonNull final File file) {
        boolean isExisting = file.exists();

        try {
            FileOutputStream output = new FileOutputStream(file, true);
            try {
                output.close();
            }
            catch (IOException e) {
                // do nothing.
            }
        }
        catch (java.io.FileNotFoundException e) {
            return false;
        }
        boolean result = file.canWrite();

        if (!isExisting) {
            file.delete();
        }

        return result;
    }

    private static File getTargetFile(File source, File targetDir) {
        File file = new File(targetDir, source.getName());
        if (!source.getParentFile().equals(targetDir) && !file.exists())
            return file;

        return new File(targetDir, StringUtils.incrementFileNameSuffix(source.getName()));
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, ApplicationUtils.getPackageName() + ".provider", file);
    }

    public static boolean copyFile(Context context, @NonNull final File source, @NonNull final File targetDir) {
        InputStream inStream = null;
        OutputStream outStream = null;

        boolean success = false;
        File target = getTargetFile(source, targetDir);

        try {
            inStream = new FileInputStream(source);

            if (isWritable(target)) {
                FileChannel inChannel = new FileInputStream(source).getChannel();
                FileChannel outChannel = new FileOutputStream(target).getChannel();
                inChannel.transferTo(0, inChannel.size(), outChannel);
                success = true;
                try { inChannel.close(); } catch (Exception ignored) { }
                try { outChannel.close(); } catch (Exception ignored) { }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isFileOnSdCard(context, source)) {
                        DocumentFile sourceDocument = getDocumentFile(context, source, false, false);
                        if (sourceDocument != null) {
                            inStream = context.getContentResolver().openInputStream(sourceDocument.getUri());
                        }
                    }

                    DocumentFile targetDocument = getDocumentFile(context, target, false, false);
                    if (targetDocument != null) {
                        outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
                    }
                }
                else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {

                    Uri uri = getUriFromFile(context,target.getAbsolutePath());
                    if (uri != null) {
                        outStream = context.getContentResolver().openOutputStream(uri);
                    }
                }

                if (outStream != null) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) outStream.write(buffer, 0, bytesRead);
                    success = true;
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
            return false;
        }
        finally {
            try { inStream.close(); } catch (Exception ignored) { }
            try { outStream.close(); } catch (Exception ignored) { }
        }

        if (success) scanFile(context, new String[] { target.getPath() });
        return success;
    }

    /**
     * Get an Uri from an file path.
     *
     * @param path The file path.
     * @return The Uri.
     */
    private static Uri getUriFromFile(Context context, final String path) {
        ContentResolver resolver = context.getContentResolver();

        Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
                new String[] {BaseColumns._ID}, MediaStore.MediaColumns.DATA + " = ?",
                new String[] {path}, MediaStore.MediaColumns.DATE_ADDED + " desc");
        if (filecursor == null) {
            return null;
        }
        filecursor.moveToFirst();

        if (filecursor.isAfterLast()) {
            filecursor.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, path);
            return resolver.insert(MediaStore.Files.getContentUri("external"), values);
        }
        else {
            int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
            Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
                    Integer.toString(imageId)).build();
            filecursor.close();
            return uri;
        }
    }

    private static boolean isFileOnSdCard(Context context, File file) {
        String sdcardPath = getSdcardPath(context);
        return sdcardPath != null && file.getPath().startsWith(sdcardPath);
    }


    public static HashSet<File> getStorageRoots(Context context) {
        HashSet<File> paths = new HashSet<File>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
                else
                    paths.add(new File(file.getAbsolutePath().substring(0, index)));
            }
        }
        return paths;
    }

    public static String getSdcardPath(Context context) {
        for(File file : context.getExternalFilesDirs("external")) {
            if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
                else
                    return new File(file.getAbsolutePath().substring(0, index)).getPath();
            }
        }
        return null;
    }


    /**
     * Delete all files in a folder.
     *
     * @param folder the folder
     * @return true if successful.
     */

    public static boolean deleteFilesInFolder(Context context, @NonNull final File folder) {

        // TODO: 07/05/18 check
        boolean totalSuccess = true;

        String[] children = folder.list();
        if (children != null) {
            for (String child : children) {
                File file = new File(folder, child);
                if (!file.isDirectory()) {
                    try {
                        deleteFile(context, file);
                    } catch (ProgressException e) {
                        Log.e(TAG, "Failed to delete file", e);
                        totalSuccess = false;
                    }
                }
            }
        }
        return totalSuccess;
    }

    /**
     * Delete a file. May be even on external SD card.
     *
     * @param file the file to be deleted.
     * @return True if successfully deleted.
     */
    public static void deleteFile(Context context, @NonNull final File file) throws ProgressException {
        ErrorCause error = new ErrorCause(file.getName());

        boolean success = false;

        try {
            success = file.delete();
        } catch (Exception e) {
            error.addCause(e.getLocalizedMessage());
        }

        if (!success && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DocumentFile document = getDocumentFile(context, file, false, false);
            success = document != null && document.delete();
            error.addCause("Failed SAF");
        }

        if (!success && Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            ContentResolver resolver = context.getContentResolver();

            try {
                Uri uri = getUriForFile(context, file);
                if (uri != null) {
                    resolver.delete(uri, null, null);
                }
                success = !file.exists();
            }
            catch (Exception e) {
                error.addCause(String.format("Failed CP: %s", e.getLocalizedMessage()));
                Log.e(TAG, "Error when deleting file " + file.getAbsolutePath(), e);
                success = false;
            }
        }

        if (success) scanFile(context, new String[]{file.getPath()});
        else throw new ProgressException(error);
    }

    /**
     * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
     * existing, it is created.
     *
     * @param file              The file.
     * @param isDirectory       flag indicating if the file should be a directory.
     * @param createDirectories flag indicating if intermediate path directories should be created if not existing.
     * @return The DocumentFile
     */
    private static DocumentFile getDocumentFile(Context context, @NonNull final File file, final boolean isDirectory, final boolean createDirectories) {

        Uri treeUri = getTreeUri(context);

        if (treeUri == null) return null;

        DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
        String sdcardPath = getSavedSdcardPath(context);
        String suffixPathPart = null;

        if (sdcardPath != null) {
            if((file.getPath().indexOf(sdcardPath)) != -1)
                suffixPathPart = file.getAbsolutePath().substring(sdcardPath.length());
        } else {
            HashSet<File> storageRoots = StorageHelper.getStorageRoots(context);
            for(File root : storageRoots) {
                if (root != null) {
                    if ((file.getPath().indexOf(root.getPath())) != -1)
                        suffixPathPart = file.getAbsolutePath().substring(file.getPath().length());
                }
            }
        }

        if (suffixPathPart == null) {
            Log.d(TAG, "unable to find the document file, filePath:"+ file.getPath()+ " root: " + ""+sdcardPath);
            return null;
        }

        if (suffixPathPart.startsWith(File.separator)) suffixPathPart = suffixPathPart.substring(1);

        String[] parts = suffixPathPart.split("/");

        for (int i = 0; i < parts.length; i++) { // 3 is the

            DocumentFile tmp = document.findFile(parts[i]);
            if (tmp != null)
                document = document.findFile(parts[i]);
            else {
                if (i < parts.length - 1) {
                    if (createDirectories) document = document.createDirectory(parts[i]);
                    else return null;
                }
                else if (isDirectory) document = document.createDirectory(parts[i]);
                else return document.createFile("image", parts[i]);
            }
        }

        return document;
    }

    /**
     * Get the stored tree URIs.
     *
     * @return The tree URIs.
     * @param context context
     */
    private static Uri getTreeUri(Context context) {
        String uriString = Hawk.get(context.getString(R.string.preference_internal_uri_extsdcard_photos), null);

        if (uriString == null) return null;
        return Uri.parse(uriString);
    }

    private static String getSavedSdcardPath(Context context) {
        return Hawk.get("sd_card_path", null);
    }

}
