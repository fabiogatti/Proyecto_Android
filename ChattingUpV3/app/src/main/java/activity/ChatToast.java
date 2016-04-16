package activity;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.desperatesoft.zero.chattingup.R;

import java.util.zip.Inflater;

/**
 * Created by Pcs on 10/04/2016.
 */
public class ChatToast {
    public ChatToast(View v, View layout, String s){
        Toast toast = new Toast(v.getContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 50);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        TextView toastText = (TextView) layout.findViewById(R.id.text_toast);
        toastText.setText(s);
        //toast.setText(getResources().getString(R.string.invalid_string_messages));
        toast.show();
    }
}
