package activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.desperatesoft.zero.chattingup.R;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URI;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * Created by Pcs on 04/04/2016.
 */
public class MessagesFragment extends Fragment {

    View v;
    ArrayList<message> Messages;
    TextView text;
    EditText edit;
    LinearLayout layout;
    LinearLayout.LayoutParams params;
    Button send;
    int screenHeight;
    int screenWidth;
    private SQLiteDatabase myDatabase;
    int userID;
    int friendID;
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat fmtOut = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    int messagesPerLoad = 10;
    int DBcursorUserID = -2;
    int DBcursorFriendID = -2;
    List<restMessage> conversation;
    int conversationCursor = -2;
    boolean newMessagesBottom;
    Toolbar toolbar;
    View addMessage;
    //ArrayList<message> messagesGoing;

    private void showMessage(String s, int sender, boolean bottom) {
        text = new TextView(v.getContext());
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(0, 20, 0, 0);
        text.setText(s);
        text.setMaxWidth((screenWidth * 6) / 7);
        if (sender == 0) {
            text.setBackground(getResources().getDrawable(R.drawable.messages_text_view_1));
            text.setTextColor(Color.WHITE);
            params.gravity = RelativeLayout.ALIGN_PARENT_END;
        } else {
            text.setBackground(getResources().getDrawable(R.drawable.messages_text_view_2));
            text.setTextColor(getResources().getColor(R.color.colorPrimary));
            params.gravity = RelativeLayout.ALIGN_PARENT_START;

        }
        if (bottom){
            layout.addView(text, params);
        }
        else{
            layout.addView(text,0,params);
        }
    }

    private void showMessages(boolean bottom){
        //TextView text = new TextView(getContext());
        //TextView text = new TextView((Activity)context);
        //RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.relative_messages);
        //params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //params.setMargins(0,0,0,15);
        message m;
        /*Space sp = new Space(v.getContext());
        sp.setLayoutParams(params);*/
        for(int i=0;i<Messages.size();i++){
            m = Messages.get(i);
            showMessage(m.message, m.sender, bottom);
            /*text = new TextView(v.getContext());
            text.setText(m.message);
            if(m.sender==0){
                text.setBackground(getResources().getDrawable(R.drawable.messages_text_view_1));
                text.setTextColor(Color.WHITE);
                text.setGravity(Gravity.END);
                //params.gravity = Gravity.LEFT;
            }
            else{
                text.setBackground(getResources().getDrawable(R.drawable.messages_text_view_2));
                text.setTextColor(getResources().getColor(R.color.colorPrimary));
                text.setGravity(Gravity.START);
                //params.gravity = Gravity.NO_GRAVITY;
            }
            Log.d("Tag","sender: "+m.sender);
            //Log.d("Tag","params.gravity: "+params.gravity);
            text.setLayoutParams(params);
            layout.addView(text);
            //layout.addView(sp);*/
        }
        //Log.d("Tag", "Date: " + new Date());
    }

    private void updateScroll(){
        ScrollView sv = (ScrollView) v.findViewById(R.id.scroll_messages);
        sv.fullScroll(View.FOCUS_DOWN);
    }

    private int getScreenMeasures(boolean result){ //Tomar medidas de la pantalla (True = width, False = height)
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        try{
            display.getRealSize(size);
        }
        catch (NoSuchMethodError err){
            display.getSize(size);
        }
        if(result){
            return size.x;
        }
        return size.y;
    }

    private void restMessagesToDB(List<restMessage> restMessages){
        myDatabase = getActivity().openOrCreateDatabase("ChattingUpDB",getActivity().MODE_PRIVATE,null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS MESSAGES(MESSAGE_ID INTEGER, USER_FROM INTEGER, USER_TO INTEGER, MESSAGE_TEXT VARCHAR, M_DATE VARCHAR);"); //CREANDO M_DATE COMO VARCHAR
        for(restMessage i : restMessages){
            SQLiteStatement statement = myDatabase.compileStatement("INSERT INTO MESSAGES(MESSAGE_ID, USER_FROM, USER_TO, MESSAGE_TEXT, M_DATE) VALUES(?,?,?,?,?)");
            statement.bindLong(1,i.getId());
            statement.bindLong(2,i.getFrom());
            statement.bindLong(3,i.getTo());
            statement.bindString(4,i.getText());
            statement.bindString(5,i.getDate());
            statement.execute();
        }
        myDatabase.close();
    }

    private List<restMessage> sortDates(List<restMessage> restMessages){
        if(restMessages.size()<=1){
            return restMessages;
        }
        Comparator<restMessage> sorted = new Comparator<restMessage>() {
            @Override
            public int compare(restMessage lhs, restMessage rhs) {
                try {
                    Date date1 = format.parse(lhs.getDate());
                    Date date2 = format.parse(rhs.getDate());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        };
        Collections.sort(restMessages, sorted);
        return restMessages;
    }

    private List<restMessage> returnDBMessages(){
        List<restMessage> conversation = new ArrayList<>();
        myDatabase = getActivity().openOrCreateDatabase("ChattingUpDB",getActivity().MODE_PRIVATE,null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS MESSAGES(MESSAGE_ID INTEGER, USER_FROM INTEGER, USER_TO INTEGER, MESSAGE_TEXT VARCHAR, M_DATE VARCHAR);"); //CREANDO M_DATE COMO VARCHAR
        Cursor resultSet = myDatabase.rawQuery("SELECT * FROM MESSAGES WHERE USER_FROM="+userID+" AND USER_TO="+friendID,null);
        int x = resultSet.getCount();
        resultSet.move(x);
        while(x>0){
            conversation.add(new restMessage(resultSet.getInt(0),resultSet.getInt(1),resultSet.getInt(2),resultSet.getString(3),resultSet.getString(4)));
            resultSet.moveToPrevious();
            x--;
        }
        resultSet = myDatabase.rawQuery("SELECT * FROM MESSAGES WHERE USER_FROM="+friendID+" AND USER_TO="+userID,null);
        x = resultSet.getCount();
        resultSet.move(x);
        while(x>0){
            conversation.add(new restMessage(resultSet.getInt(0),resultSet.getInt(1),resultSet.getInt(2),resultSet.getString(3),resultSet.getString(4)));
            resultSet.moveToPrevious();
            x--;
            /*resultSet.moveToFirst();
            Log.d("Tag", "Database Result: " + resultSet.getInt(0) + "; " + resultSet.getString(1) + "; " + resultSet.getString(2));*/
        }
        return sortDates(conversation);
    }

    private void showDBMessages(){
        if(conversationCursor==-2){
            conversationCursor = conversation.size()-1;
        }
        int x = messagesPerLoad;
        int sender;

        /*ScrollView scrollView = new ScrollView(getContext());
        int y = 0;*/
        /*if(id!=0) {
            scrollView = (ScrollView) v.findViewById(R.id.scroll_messages);
            y = Math.round(v.findViewById(R.id.first_item_scroll).getY());
        }*/

        for(int i = conversationCursor;i>=0;i--){
            conversationCursor = i;
            //Log.d("Tag","conversationCursor: "+conversationCursor);
            if(x==0){
                return;
            }
            if(conversation.get(i).getFrom()==userID){
                sender = 0;
            }
            else {
                sender = 1;
            }
            showMessage(conversation.get(i).getText(),sender,false);
            x--;
            if(i==0){
                conversationCursor = -1;
            }
            /*if(id!=0) {
                scrollView.scrollTo(0, y);
            }*/
        }
    }

    /*private void showRestMessages(List<restMessage> restMessages){
        int messagesLeft = messagesPerLoad;
        Log.d("Tag","Tamaño de restMessages (en showRestMessages): "+restMessages.size());
        for(restMessage i: restMessages){
            Log.d("Tag","Contenido de restMessages (en showRestMessages): "+i);
            if(messagesLeft==0){
                return;
            }
            if(i.getFrom()==userID){
                showMessage(i.getText(),0,true);
            }
            else{
                showMessage(i.getText(),1,true);
            }
            messagesLeft--;
        }
        showDBMessages(messagesLeft);
    }*/

    public void editTextToDB(String text){
        myDatabase = getActivity().openOrCreateDatabase("ChattingUpDB",getActivity().MODE_PRIVATE,null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS MESSAGES(MESSAGE_ID INTEGER, USER_FROM INTEGER, USER_TO INTEGER, MESSAGE_TEXT VARCHAR, M_DATE VARCHAR);"); //CREANDO M_DATE COMO VARCHAR
        Cursor resultSet = myDatabase.rawQuery("SELECT MAX(MESSAGE_ID) FROM MESSAGES", null);
        resultSet.moveToFirst();
        int max = resultSet.getInt(0);
        List<restMessage> message = new ArrayList<>();
        message.add(new restMessage(max+1,userID,friendID,text,fmtOut.format(new Date())));
        restMessagesToDB(message);
    }

    int id = 0;
    boolean loadMore = true;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.fragment_messages,container,false);

        edit = (EditText) v.findViewById(R.id.edit_message);
        layout = (LinearLayout) v.findViewById(R.id.linear_messages);
        screenHeight = getScreenMeasures(false);
        screenWidth = getScreenMeasures(true);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(0, 0, 0, 15);



        layout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                /*Log.d("Tag","OldTop: "+oldTop);
                Log.d("Tag","Top: "+top);
                Log.d("Tag","OldBottom: "+oldBottom);
                Log.d("Tag","Bottom: "+bottom);*/

                if (id!=0) {
                    //addMessage.setEnabled(false);
                    Log.d("Tag", "ScrollYafter: " + v.findViewById(R.id.first_item_scroll).getScrollY());
                    Log.d("Tag", "getYafter: " + v.findViewById(R.id.first_item_scroll).getY());
                    //v.findViewById(R.id.first_item_scroll).requestFocus();
                    final ScrollView scrollView = (ScrollView) v.findViewById(R.id.scroll_messages);
                    //scrollView.scrollTo(0, Math.round(v.findViewById(R.id.first_item_scroll).getY()));
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadMore = true;
                            scrollView.scrollTo(0, Math.round(v.findViewById(R.id.first_item_scroll).getY()));
                        }
                    }, 10);
                }
                if (newMessagesBottom) {
                    newMessagesBottom = false;
                    updateScroll();
                }
                //updateScroll();
                //v.invalidate();
                /*else {
                    updateScroll(false);
                }*/
            }
        });

        //android.support.v7.app.ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        /*final AppCompatActivity act = (AppCompatActivity) getActivity();
        //if(act.getSupportActionBar() != null){
        toolbar = (Toolbar) act.getSupportActionBar().getCustomView();
        addMessage = toolbar.findViewById(R.id.action_add_messages);
        addMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDBMessages();
            }
        });
        addMessage.setEnabled(false);*/
        //}

        ScrollView sv = (ScrollView) v.findViewById(R.id.scroll_messages);
        sv.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View scroll, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scroll.getScrollY()==0 && conversationCursor>-1 && loadMore){
                    //Log.d("Tag","LLego al limite top del scroll");
                    //addMessage.setEnabled(true);
                    id = 1;
                    layout.getChildAt(0).setId(R.id.first_item_scroll);
                    loadMore = false;
                    showDBMessages();
                }
            }
        });

        Bundle b = getArguments();
        this.userID = b.getInt("userID");
        this.friendID = b.getInt("friendID");
        //Log.d("Tag","USER ID: "+userID);
        //Log.d("Tag","FRIEND ID: "+friendID);
        send = (Button) v.findViewById(R.id.send);
        send.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edit.getText().toString();
                //if(!text.equals("") && !text.equals(" ")) {
                if (!(text.trim().length() == 0)) {
                    showMessage(text, 0, true);
                    //Messages.add(new message(text));
                    //String parameters = "{:\"from\": "+userID+"\",to\": "+friendID+"\",text\": \""+text+"\"}:";
                    String parameters = text;
                    /*RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                    restTemplate.put("http://10.0.2.2:8191/rest/messages/",parameters);*/
                    HttpRequestTask2 task = new HttpRequestTask2();
                    task.execute(parameters);

                    editTextToDB(text);  //COMENTAR ESTA LINEA PARA ELIMINAR EL MENSAJE DUPLICADO

                    edit.setText("");
                    newMessagesBottom = true;
                    //updateScroll();
                } else {
                    Bundle b = new Bundle();
                    LayoutInflater inflater = getLayoutInflater(b);
                    View layout = inflater.inflate(R.layout.toast, (ViewGroup) v.findViewById(R.id.toast_layout_root));
                    Toast toast = new Toast(getContext());
                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 50);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    TextView tv = (TextView) layout.findViewById(R.id.text_toast);
                    tv.setText(getResources().getString(R.string.invalid_string_messages));
                    //toast.setText(getResources().getString(R.string.invalid_string_messages));
                    toast.show();
                    edit.setText("");
                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //v.requestFocus();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateScroll();
                    }
                },250);
            }
        });


        HttpRequestTask task = new HttpRequestTask();
        //HttpRequestTask task2 = new HttpRequestTask();
        List<restMessage> restMessages;
        //List<restMessage> restMessages2;
        try{
            ObjectMapper mapper = new ObjectMapper();
            String json = task.execute("messages/"+friendID+"/"+userID).get();
            restMessages = mapper.readValue(json,mapper.getTypeFactory().constructCollectionType(List.class,restMessage.class));
            Log.d("Tag","Primer json: "+json);
            /*json = task2.execute("messages/"+userID+"/"+friendID).get();
            restMessages2 = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, restMessage.class));
            Log.d("Tag", "Segundo json: " + json);
            for(restMessage i: restMessages2){
                restMessages.add(i);
            }*/
            //DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //Date d = format.parse(restMessages.get(0).getDate());
            //SimpleDateFormat fmtOut = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //Log.d("Tag","Date"+fmtOut.format(d));
            restMessages = sortDates(restMessages);

            //restMessages = newMessages;

            //showRestMessages(restMessages);
            restMessagesToDB(restMessages);
            conversation = returnDBMessages();
            showDBMessages();
            //showDBMessages(messagesPerLoad);
            //showRestMessages(restMessages);

            //Log.d("Tag",""+lists.get(0).getMessage());
        } catch (java.lang.InterruptedException e) {
            e.printStackTrace();
        } catch (java.util.concurrent.ExecutionException e){
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (java.lang.NullPointerException e){
            e.printStackTrace();
        } /*catch (ParseException e) {
            e.printStackTrace();
        }*/
        return v;
    }



    private class message{
        public String message;
        public Date time;
        public int sender;
        public message(){
            super();
            this.message = "Empty message madafaka";
            this.time = new Date();
            this.sender = 0;
        }
        public message(String message){
            this.message = message;
            this.time = new Date();
            this.sender = 0;
        }

        public message(String message, Date time){
            this.message = message;
            this.time = time;
            this.sender = 0;
        }

        public message(String message, Date time, int sender){
            this.message = message;
            this.time = time;
            this.sender = sender;
        }
    }

    // Info del rest
    static public class restMessage {
        private int id;
        private int from;
        private int to;
        private String text;
        private String date;
        public int getId() { return this.id; }
        public int getFrom() { return this.from; }
        public int getTo() { return this.to; }
        public String getText(){ return this.text; }
        public String getDate(){ return this.date; }
        public void setId(int id) { this.id = id; }
        public void setFrom(int from) { this.from = from; }
        public void setTo(int to) { this.to = to; }
        public void setText(String text){ this.text = text; }
        public void setDate(String date){ this.date = date; }
        @JsonCreator
        public restMessage(@JsonProperty("id") int id,@JsonProperty("from") int from,@JsonProperty("to") int to, @JsonProperty("text") String text, @JsonProperty("date") String date){
            this.id = id; this.from = from; this.to = to; this.text = text; this.date = date;}
    }


        /*@Override
        protected void onPostExecute(restMessage restMessage) {
            String restMessageText = restMessage.getId()+"; "+restMessage.getId_from()+"; "+restMessage.getId_to()+"; "+age.getMessage()+"; "+restMessage.getDate();
            showMessage(restMessageText,1);
        }*/
        public class HttpRequestTask2 extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                try {
                    //final String url = "http://10.0.2.2:8191/rest/messages/1/2";
                    final String url = "http://10.0.2.2:8191/rest/messages/";
                    JSONObject json = new JSONObject();
                    json.put("from", userID);
                    json.put("to", friendID);
                    json.put("text", params[0]);
                    RestTemplate restTemplate = new RestTemplate(true);
                    //RestTemplate restTemplate = new RestTemplate();
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                    HttpEntity<String> requestEntity = new HttpEntity<String>(json.toString(),httpHeaders);
                    restTemplate.exchange(url,HttpMethod.POST,requestEntity,restMessage.class);
                    /*HttpHeaders requestHeaders = new HttpHeaders();
                    MultiValueMap<String,Object> formData = new LinkedMultiValueMap<String,Object>();
                    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
                    formData.add("from", userID);
                    formData.add("to", friendID);
                    formData.add("text", params[0]);
                    HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(formData,requestHeaders);
                    RestTemplate restTemplate = new RestTemplate(true);
                    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                    ResponseEntity<String> response = restTemplate.exchange(url,HttpMethod.POST,requestEntity,String.class);*/
                    //restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
                    //restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                    //restTemplate.getMessageConverters().add(new StringHttpMessageConverter(/*url, String.class*/));
                    //MultiValueMap<String,String> map = new LinkedMultiValueMap<String,String>();
                    //map.add("from", ""+userID);
                    //map.add("to",""+userID);
                    //map.add("text", params[0]);
                    //restTemplate.postForObject(url, map, String.class);
                    //restTemplate.postForLocation(url,null,params[0]);
                    //HttpEntity<?> httpEntity = new HttpEntity<Object>(map,new HttpHeaders());
                    //restTemplate.exchange(url, HttpMethod.POST, httpEntity, restMessage.class);
                    //Log.d("Tag", "Greeting in doInBackground: " + result);
                    //String reportKey = object.get("reportKey").textValue();
                    //Greeting greeting = restTemplate.getForEntity(object, Greeting.class);
                    //return restTemplate.postForObject(url, map, String.class);
                    //return response.getBody();
                    return null;
                } catch (Exception e) {
                    Log.e("MainActivity", e.getMessage(), e);
                }

                return null;
            }
        }
}

/*
                LEEEE ACA GONORREAAAA!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                FALTA QUE CUANDO ENTRE AL CHAT POR PRIMERA VEZ MUEVA EL SCROLL ABAJO PARA MOSTRAR LOS MENSAJES, ADEMAS DE QUE NO SUBA EL SCROLL HASTA EL PRIMER MENSAJE DESPUES DE PEDIR MAS
                MENSAJES(LLENDO AL LIMITE SUPERIOR DE LA PANTALLA)
                QUEDA FALTANDO TAMBIEN TERMINAR EL LAYOUT Y EL SCRIPT DE LOS ARCHIVOS, ADEMAS DE LA APERTURA DE LOS MISMOS.
                FALTA QUITAR LA TAB DE PERFIL


                FALTA CONSUMIR EL REST PARA SABER QUE ARCHIVOS HAY DISPONIBLES, DIBUJARLOS EN LA PANTALLA CON UN LIMITE DE 10 POR VEZ Y ADEMAS FALTA DAR LA OPCION DE DESCARGAR LA IMAGEN UNA VEZ
                SE LE DE CLICK A LA IMAGEN
 */


























