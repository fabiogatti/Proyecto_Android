package activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.desperatesoft.zero.chattingup.R;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    SQLiteDatabase myDatabase;

    private ArrayList<Integer> getTotalUsers(){
        myDatabase = openOrCreateDatabase("ChattingUpDB",MODE_PRIVATE,null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS USERS(USER_ID INTEGER,USERNAME VARCHAR,NAME VARCHAR);");
        Cursor resultSet = myDatabase.rawQuery("Select * from USERS",null);
        resultSet.moveToFirst();
        ArrayList<Integer> users = new ArrayList<>();
        for(int i=0;i<resultSet.getCount();i++){
            users.add(resultSet.getInt(0));
            resultSet.moveToNext();
        }
        //resultSet = myDatabase.rawQuery("Select * from USERS",null);

        /*resultSet.moveToFirst();
        Log.d("Tag", "Database Result: " + resultSet.getInt(0) + "; " + resultSet.getString(1) + "; " + resultSet.getString(2));*/

        resultSet.close();
        myDatabase.close();
        return users;
    }

    private void dropTableMessages(){
        myDatabase = openOrCreateDatabase("ChattingUpDB",MODE_PRIVATE,null);
        myDatabase.execSQL("DROP TABLE IF EXISTS MESSAGES;");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_sign_up));
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        //dropTableMessages();

        /*LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,(ViewGroup) findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text_toast);
        text.setText("Este es un mensaje Toast");

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();*/
    }

    public void signUp(View v) {
        EditText tv = (EditText) findViewById(R.id.input_userID);
        HttpRequestTask task = new HttpRequestTask();
        String json = "[]";
        if(tv.getText().toString().trim().length()==0){
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast,(ViewGroup) v.findViewById(R.id.toast_layout_root));
            ChatToast toast = new ChatToast(v,layout,getResources().getString(R.string.invalid_user_id));
            return;
        }
        try{
            ObjectMapper mapper = new ObjectMapper();
            json = task.execute("contacts/"+Integer.parseInt(tv.getText().toString())).get();
            //Log.d("Tag",""+lists.get(0).getMessage());
        } catch (java.lang.InterruptedException e) {
            e.printStackTrace();
        } catch (java.util.concurrent.ExecutionException e){
            e.printStackTrace();
        } catch (java.lang.NullPointerException e){
            e.printStackTrace();
        }
        if(json.equals("[]")){
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast,(ViewGroup) v.findViewById(R.id.toast_layout_root));
            ChatToast toast = new ChatToast(v,layout,getResources().getString(R.string.invalid_user_id));
        }
        else{
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("userID",Integer.parseInt(tv.getText().toString()));
            this.finish();
            startActivity(intent);
        }
        /*EditText tv = (EditText) findViewById(R.id.input_userID);
        ArrayList<Integer> users = getTotalUsers();
        if(tv.getText().toString().trim().length()==0){
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast,(ViewGroup) v.findViewById(R.id.toast_layout_root));
            Toast toast = new Toast(v.getContext());
            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 50);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            TextView toastText = (TextView) layout.findViewById(R.id.text_toast);
            toastText.setText(getResources().getString(R.string.invalid_user_id));
            //toast.setText(getResources().getString(R.string.invalid_string_messages));
            toast.show();
        }
        else if(users.contains(Integer.parseInt(tv.getText().toString()))){
            Intent intent = new Intent(this, MainActivity.class);
            this.finish();
            startActivity(intent);
        }
        else{
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast,(ViewGroup) v.findViewById(R.id.toast_layout_root));
            Toast toast = new Toast(v.getContext());
            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 50);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            TextView toastText = (TextView) layout.findViewById(R.id.text_toast);
            toastText.setText(getResources().getString(R.string.invalid_user_id));
            //toast.setText(getResources().getString(R.string.invalid_string_messages));
            toast.show();
        }*/
    }
}
