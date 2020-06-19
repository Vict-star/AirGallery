package cn.edu.scut.airgallery.util;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import cn.edu.scut.airgallery.R;

public class AlertDialogsHelper {
    @SuppressLint("ResourceAsColor")
    public static AlertDialog getTextDialog(AppCompatActivity activity, @StringRes int title, @StringRes int Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_text, null);

        TextView dialogTitle = dialogLayout.findViewById(R.id.text_dialog_title);
        TextView dialogMessage = dialogLayout.findViewById(R.id.text_dialog_message);

        dialogLayout.findViewById(R.id.message_card);
        dialogTitle.setBackgroundColor(R.color.colorPrimaryDark);
        dialogTitle.setText(title);
        dialogMessage.setText(Message);
        dialogMessage.setTextColor(R.color.accent_black);
        builder.setView(dialogLayout);
        return builder.create();
    }
}
