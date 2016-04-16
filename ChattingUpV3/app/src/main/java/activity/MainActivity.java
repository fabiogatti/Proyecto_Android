package activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.desperatesoft.zero.chattingup.R;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import com.miguelcatalan.materialsearchview.utils.AnimationUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MaterialSearchView searchView;
    private friendAdapter2 adapter;
    private friendView friends[];
    private ListView lv;
    private SQLiteDatabase myDatabase;
    private int userID;
    private ArrayList<Integer> contactosID;

    private void SQLiteCreateDatabase(){
        myDatabase = openOrCreateDatabase("ChattingUpDB",MODE_PRIVATE,null);
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS USERS(USER_ID INTEGER,USERNAME VARCHAR,NAME VARCHAR);");
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS CONTACTS(USER_FROM INTEGER,USER_TO INTEGER);");
        myDatabase.execSQL("CREATE TABLE IF NOT EXISTS MESSAGES(MESSAGE_ID INTEGER, USER_FROM INTEGER, USER_TO INTEGER, MESSAGE_TEXT VARCHAR, M_DATE VARCHAR);"); //CREANDO M_DATE COMO VARCHAR
        //myDatabase.execSQL("CREATE TABLE IF NOT EXISTS USERS(USER_ID INTEGER,USERNAME TEXT,NAME TEXT)");
        Cursor resultSet = myDatabase.rawQuery("Select * from USERS",null);
        if(resultSet.getCount()==0){
            myDatabase.execSQL("INSERT INTO USERS VALUES(1,'jcrada','Juan Camilo Rada');");
            myDatabase.execSQL("INSERT INTO USERS VALUES(2,'lgonzales','Luz Gonzalez');");
        }
        resultSet = myDatabase.rawQuery("Select * from CONTACTS",null);
        if(resultSet.getCount()==0){
            myDatabase.execSQL("INSERT INTO CONTACTS VALUES(1,2);");
            myDatabase.execSQL("INSERT INTO CONTACTS VALUES(2,1);");
        }
        resultSet = myDatabase.rawQuery("Select * from MESSAGES",null);
        if(resultSet.getCount()==0){
            myDatabase.execSQL("INSERT INTO MESSAGES VALUES(1, 1, 2, 'Hola Luz', '2016-04-07 20:08:27');");
            myDatabase.execSQL("INSERT INTO MESSAGES VALUES(2, 2, 1, 'Hola Juan', '2016-04-07 20:08:27');");
            //String strFormat = "yyyy-MM-dd HH:mm:ss";
            //SimpleDateFormat format = new SimpleDateFormat(strFormat);
            //Date d = new Date();
            //String date = d.getYear()+"-"+d.getMonth()+"-"+d.getDay()+" "+d.getHours()+":"+d.getMinutes()+":"+d.getSeconds();
            //d.parse(strFormat);
            //Log.d("Tag", "Date: " + date);
        }
        //resultSet = myDatabase.rawQuery("Select * from USERS",null);

        /*resultSet.moveToFirst();
        Log.d("Tag", "Database Result: " + resultSet.getInt(0) + "; " + resultSet.getString(1) + "; " + resultSet.getString(2));*/

        resultSet.close();
        myDatabase.close();
    }

    static private class Contact{
        private int userId;
        private String userName;
        private String nombre;
        public int getuserId() { return this.userId; }
        public String getuserName(){ return this.userName; }
        public String getnombre(){ return this.nombre; }
        @JsonCreator
        public Contact(@JsonProperty("userID") int userId,@JsonProperty("userName") String userName,@JsonProperty("nombre") String nombre){this.userId = userId; this.userName = userName; this.nombre = nombre;}
    }

    private friendView[] getContacts(){
        ArrayList<friendView> friends = new ArrayList<friendView>();
        HttpRequestTask task = new HttpRequestTask();
        String json = "[]";
        ArrayList<Contact> lists = new ArrayList<>();
        contactosID = new ArrayList<>();
        try{
            ObjectMapper mapper = new ObjectMapper();
            json = task.execute("contacts/"+userID).get();
            lists = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Contact.class));
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
        }
        for(int i=0;i<lists.size();i++){
            //Log.d("Tag","Nombre: "+lists.get(0).getnombre());
            friends.add(new friendView(R.drawable.ic_account_circle_black_24dp, lists.get(i).getuserName(), "" + lists.get(i).getnombre() + "; " + lists.get(i).getuserId()));
            contactosID.add(lists.get(i).getuserId());
        }

        friendView newItems2[] = new friendView[friends.size()];
        int x = 0;
        for(friendView i: friends){
            newItems2[x++] =  (friendView) i;
        }
        return newItems2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createFolders();

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Intent myIntent = getIntent();
        userID = myIntent.getIntExtra("userID", 1);

        //SQLiteCreateDatabase();

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setVoiceSearch(false);
        searchView.setHint(getResources().getString(R.string.action_search));
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTextChanged(newText);
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                /*AnimationUtil.AnimationListener animationListener = new AnimationUtil.AnimationListener() {
                    @Override
                    public boolean onAnimationStart(View view) {
                        return false;
                    }

                    @Override
                    public boolean onAnimationEnd(View view) {
                        return false;
                    }

                    @Override
                    public boolean onAnimationCancel(View view) {
                        return false;
                    }
                };
                AnimationUtil x = new AnimationUtil();
                x.fadeOutView(searchView,AnimationUtil.ANIMATION_DURATION_MEDIUM,animationListener);*/
            }
        });
        searchView.animate();
        FragmentDrawer drawerFragment = (FragmentDrawer) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);


        //Gatti
        lv = (ListView) findViewById(R.id.list);
        /*friends = new friendView[]{
                new friendView(R.drawable.ic_account_circle_black_24dp, "User 1", "Yesterday"),
                new friendView(R.drawable.ic_account_circle_black_24dp, "User 2", "Today"),
                new friendView(R.drawable.ic_account_circle_black_24dp, "User 3", "Tomorrow"),
        };*/
        friends = getContacts();

        adapter = new friendAdapter2(getBaseContext(), android.R.layout.simple_list_item_1, friends);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Log.d("Tag", "View: "+view);
                Log.d("Tag", "Position: "+position);
                Log.d("Tag", "Id: "+id);*/
                Intent myIntent = new Intent(MainActivity.this, ChatActivity.class);
                if(friends[position].username==null){
                    myIntent.putExtra("userName",friends[position].username2.toString());
                }
                else{
                    myIntent.putExtra("userName",friends[position].username);
                }
                myIntent.putExtra("userID",userID); //My user ID
                myIntent.putExtra("friendID",contactosID.get(position)); //The user id of the friend being clicked
                MainActivity.this.startActivity(myIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void createFolders () {
        File folder = new File(Environment.getExternalStorageDirectory() + "/ChattingUp");
        if (!folder.exists()) {
            folder.mkdir();
            folder = new File(Environment.getExternalStorageDirectory() + "/ChattingUp/Media");
            folder.mkdir();
            folder = new File(Environment.getExternalStorageDirectory() + "/ChattingUp/Database");
            folder.mkdir();
        }
    }

    public int validateString(String str1, String str2){ //Checks if str1 contains str2 ignoring lowercase
        int res = -1;
        int x = 0;
        if(str2.length()==1){
            return str1.toLowerCase().indexOf(str2.toLowerCase());
        }
        for(int i=0;i<str1.length();i++){
            if (str1.substring(i, i + 1).equalsIgnoreCase(str2.substring(x, x + 1))){
                if(x==str2.length()-1){
                    return res;
                }
                x = x + 1;
                if(res == -1) {
                    res = i;
                }
            }
            else{
                x=0;
                res = -1;
            }
        }
        if(str2.length() + res > str1.length()){
            res = -1;
        }
        return res;
    }

    public void searchTextChanged(String text) {
        ArrayList<friendView> newItems = new ArrayList<friendView>();
        friendView newFriend;
        Spannable itemHighlighted;
        ForegroundColorSpan color = new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark));
        //TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, null, null); //PARA NEGRILLA
        int start;
        String oldItem;
        if (!text.equals("")) {
            for (int i = 0; i < friends.length; i++) {
                oldItem = friends[i].username;
                start = validateString(oldItem, text);
                if (start!=-1) {
                    itemHighlighted = new SpannableString(oldItem);
                    //itemHighlighted.setSpan(highlightSpan, start, start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //PARA NEGRILLA
                    itemHighlighted.setSpan(color, start, start + text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    newFriend = new friendView(friends[i].profilePic, itemHighlighted, friends[i].lastText);
                    newItems.add(newFriend);
                }
            }
            friendView newItems2[] = new friendView[newItems.size()];
            int x = 0;
            for(friendView i: newItems){
                newItems2[x++] =  (friendView) i;
            }
            adapter = new friendAdapter2(getBaseContext(), android.R.layout.simple_list_item_1, newItems2);
            lv.setAdapter(adapter);
        }
        else{
            adapter = new friendAdapter2(getBaseContext(), android.R.layout.simple_list_item_1, friends);
            lv.setAdapter(adapter);
        }
    }

    static class friendView{
        public int profilePic;
        public String username;
        public String lastText;
        public Spannable username2;
        public friendView(){
            super();
        }

        public friendView(int profilePic, String username, String lastText){
            super();
            this.profilePic = profilePic;
            this.username = username;
            this.lastText = lastText;
        }
        public friendView(int profilePic, Spannable username2, String lastText){
            super();
            this.profilePic = profilePic;
            this.username2 = username2;
            this.lastText = lastText;
        }
    }

    public class friendAdapter2 extends ArrayAdapter<friendView> {
        Context context;
        int layoutResourceId;
        friendView data[] = null;

        public friendAdapter2(Context context, int layoutResourceId, friendView[] data){
            super(context,layoutResourceId,data);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View row = convertView;
            friendHolder2 holder = null;

            if(row==null){
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.list_item_1,null,false);

                holder = new friendHolder2(row);

                row.setTag(holder);
            }
            else{
                holder = (friendHolder2)row.getTag();
            }
            friendView friend = data[position];
            holder.profilePic.setImageResource(friend.profilePic);
            if(friend.username==null){
                holder.username.setText(friend.username2);
            }
            else{
                holder.username.setText(friend.username);
            }
            holder.lastText.setText(friend.lastText);

            return row;
        }
    }

    static class friendHolder2{
        ImageView profilePic;
        TextView username;
        TextView lastText;
        public friendHolder2(View row){
            super();
            if(profilePic == null){
                this.profilePic = (ImageView) row.findViewById(R.id.profile_pic);
            }
            if(username == null){
                this.username = (TextView) row.findViewById(R.id.username);
            }
            if(lastText == null){
                this.lastText = (TextView) row.findViewById(R.id.lastText);
            }
        }
    }

}
