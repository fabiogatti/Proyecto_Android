package activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.desperatesoft.zero.chattingup.R;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.wicket.response.ByteArrayResponse;
import org.apache.wicket.util.upload.MultipartFormInputStream;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Pcs on 09/04/2016.
 */
public class FilesFragment extends Fragment {
    private View v;
    private FloatingActionButton addFile;
    private int userID;
    private int friendID;
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    List<restFile> sharedFiles;

    private List<restFile> sortDates(List<restFile> restFiles){
        if(restFiles.size()<=1){
            return restFiles;
        }
        Comparator<restFile> sorted = new Comparator<restFile>() {
            @Override
            public int compare(restFile lhs, restFile rhs) {
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
        Collections.sort(restFiles, sorted);
        /*for (restMessage i : restMessages) {
            Log.d("Tag","Date of rest messages sorted: "+i.getDate());
        }*/
        return restFiles;
    }

    private void showSharedFiles(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.fragment_files,container,false);

        layout = (LinearLayout) v.findViewById(R.id.linear_files);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(0, 20, 0, 0);

        Bundle b = getArguments();
        this.userID = b.getInt("userID");
        this.friendID = b.getInt("friendID");

        /*InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if(view!=null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }*/

        addFile = (FloatingActionButton) v.findViewById(R.id.addFiles);
        /*Log.d("Tag","Dir 1: "+Environment.getDataDirectory());
        Log.d("Tag","Dir 2: "+Environment.getExternalStorageDirectory());
        Log.d("Tag","Dir 3: "+Environment.getRootDirectory());
        Log.d("Tag","Dir 4: "+Environment.DIRECTORY_DOCUMENTS);
        Log.d("Tag", "Dir 5: " + Environment.DIRECTORY_DOWNLOADS);
        Log.d("Tag", "Dir 6: " + Environment.DIRECTORY_PICTURES);*/
        addFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //loadFileList();
                //onCreateDialog(DIALOG_LOAD_FILE);
                createDialog();
//                if(camera){
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(intent, 0);
//                }
//                else {
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    //intent.setType("file/*");
//                    intent.setType("*/*");
//                    startActivityForResult(intent, 1);
//                }

            }
        });

        HttpRequestTask task = new HttpRequestTask();
        //HttpRequestTask task2 = new HttpRequestTask();
        List<restFile> restFiles;
        //List<restMessage> restMessages2;
        try{
            ObjectMapper mapper = new ObjectMapper();
            String json = task.execute("shared_files/"+userID+"/"+friendID).get();
            restFiles = mapper.readValue(json,mapper.getTypeFactory().constructCollectionType(List.class,restFile.class));
            Log.d("Tag","Primer json (Shared Files): "+json);
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
            restFiles = sortDates(restFiles);

            //restMessages = newMessages;

            //showRestMessages(restMessages);
            Log.d("Tag","File (Byte[]): "+restFiles.get(0).getData());
            sharedFiles = restFiles;
            showSharedFiles();
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
        }

        return v;
    }

    boolean camera;
    ImageView imageView;
    LinearLayout layout;
    LinearLayout.LayoutParams params;
    byte[] bytes;
    String filename;
    String fileType;
    File file;

    private Dialog createDialog(){
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String items[] = {getResources().getString(R.string.dialog_item1_files),getResources().getString(R.string.dialog_item2_files)};
        builder.setTitle(R.string.file_send_files);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 0);
                    camera = true;
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    //intent.setType("file/*");
                    intent.setType("*/*");
                    startActivityForResult(intent, 1);
                    camera = false;
                }
                //mChosenFile = mFileList[which];
            }
        });
        dialog = builder.show();
        return dialog;
    }

    public void addToLayout(Object object,boolean image, int sender, boolean bottom){
        imageView = new ImageView(v.getContext());
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(0, 20, 0, 0);
        if(object instanceof Bitmap){
            imageView.setImageBitmap((Bitmap) object);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ((Bitmap) object).compress(Bitmap.CompressFormat.PNG, 100, stream);
            bytes = stream.toByteArray();
            filename = new Date().toString();
            fileType = "image/jpg";
        }
        else if(image){
            Bitmap bmp = BitmapFactory.decodeByteArray((byte[]) object,0,bytes.length);
            imageView.setImageBitmap(bmp);
        }
        else{
            imageView.setImageResource(R.drawable.ic_description_white_24dp);
        }
        if(sender==0){
            imageView.setBackground(getResources().getDrawable(R.drawable.messages_text_view_1));
            params.gravity = RelativeLayout.ALIGN_PARENT_END;
        }
        else{
            imageView.setBackground(getResources().getDrawable(R.drawable.messages_text_view_2));
            params.gravity = RelativeLayout.ALIGN_PARENT_START;
        }
        if (bottom){
            Log.d("Tag", "Deberia agregar la imagen");
            layout.addView(imageView, params);
        }
        else{
            layout.addView(imageView,0,params);
        }
    }

    public void addToRest(){
        /*if(data instanceof Bitmap){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ((Bitmap) data).compress(Bitmap.CompressFormat.PNG, 100, stream);
            data = stream.toByteArray();
        }*/
        HttpRequestTask2 task = new HttpRequestTask2();
        task.execute("");
    }

    public static byte[] inputStreamToFile(InputStream input){
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            return output.toByteArray();
        }catch (java.io.IOException e){
            e.printStackTrace();
        }
        return buffer;
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        /*Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        addToLayout(bitmap);*/
        if(resultCode == Activity.RESULT_OK) {
            Bitmap bp;
            if (camera) {
                bp = (Bitmap) data.getExtras().get("data");
                addToLayout(bp,true, 0, true);
                //addToRest(bp);
            } else {
                Uri data2 = data.getData();
                /*if (v.getContext().getContentResolver().getType(data2).contains("image")) {
                    Log.d("Tag","Data: "+data2);
                    Log.d("Tag","Data: "+data2.getPath());
                    bp = BitmapFactory.decodeFile(data2.getPath());
                    addToLayout(bp, 0, true);
                    //addToRest(bp);
                } else {*/
                    Log.d("Tag","MYME TYPE: "+v.getContext().getContentResolver().getType(data2));
                    fileType = v.getContext().getContentResolver().getType(data2);
                    /*Log.d("Tag","Data first: "+data2);
                    Log.d("Tag", "Data: " + data2.getPath());
                    Log.d("Tag", "Data: " + data2.toString());
                    File file = new File(Environment.getExternalStorageDirectory()+data2.getPath());
                    Log.d("Tag", "Storage: " + Environment.getExternalStorageDirectory());*/
                    /*Log.d("Tag", "Data: " + file.getAbsolutePath());
                    Log.d("Tag", "Data: " + file);
                    String[] filepath = {"_data"};
                    Cursor h= v.getContext().getContentResolver().query(data2,filepath,null,null,null);
                    int index = h.getColumnIndexOrThrow("_data");
                    h.moveToFirst();
                    Log.d("Tag", "Data: " + h.getString(index));
                    Log.d("Tag", "Data: " + v.getContext().getFilesDir() + "/" +h.getString(0));
                    file = new File(v.getContext().getFilesDir() + "/" +data2.getPath());
                    Log.d("Tag", "Data: " + v.getContext().getFilesDir() + "/" + h.getString(0));
                    String mimeType = v.getContext().getContentResolver().getType(data2);
                    Log.d("Tag", "Data last: " + mimeType);
                    h.close();*/
                    //int size = (int) file.length();
                    byte[] bytes2;
                    //h.close();
                    try {
                        file = new File(data2.getPath());
                        filename = file.getName();
                        //Log.d("Tag", "NOMBRE DEL ARCHIVO" +  file.getName());
                        /*file = new File(data2.toString());
                        Log.d("Tag", "NOMBRE DEL ARCHIVO 2" +  file.getName());*/
                        InputStream input = v.getContext().getContentResolver().openInputStream(data2);
                        Log.d("Tag",""+v.getContext().getContentResolver().openInputStream(data2));
                        //Log.d("Tag","STRING PODEROSA"+v.getContext().getContentResolver().openFileDescriptor(data2, ));
                        bytes2 = inputStreamToFile(input);
                       /* BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                        buf.read(bytes, 0, bytes.length);*/
                        bytes = bytes2;
                        if (v.getContext().getContentResolver().getType(data2).contains("image")) {
                            addToLayout(bytes2,true, 0, true);
                        }
                        else{
                            addToLayout(bytes2,false, 0, true);
                        }
                        addToRest();
                        //buf.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } /*catch (IOException e) {
                        e.printStackTrace();
                    }*/
                //}
            }
        }
    }

    public class HttpRequestTask2 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                //final String url = "http://10.0.2.2:8191/rest/messages/1/2";
                final String url = "http://10.0.2.2:8191/rest/files/"+userID+"/"+friendID;
                JSONObject json = new JSONObject();
                //json.put("data", bytes);
                //String encodedBytes = Base64.encodeToString(bytes,Base64.DEFAULT);
                json.put("file", bytes);
                //json.put("data", encodedBytes);
                RestTemplate restTemplate = new RestTemplate(true);
                /*HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                HttpEntity<String> requestEntity = new HttpEntity<>(json.toString(),httpHeaders);
                restTemplate.exchange(url, HttpMethod.POST,requestEntity,String.class);*/
                MultiValueMap<String,Object> map = new LinkedMultiValueMap<String, Object>();
                ByteArrayResource rs = new ByteArrayResource(bytes){
                    @Override
                    public String getFilename(){
                        return filename;
                    }
                };
                //FileInputStream input = new FileInputStream(file);
                //MultipartFile file = new MockMultipartFile(filename,filename,fileType,bytes);
                //org.apache.wicket.request.resource.ByteArrayResource rs = new org.apache.wicket.request.resource.ByteArrayResource("lol",bytes,filename);
                //map.add("file",bytes);
                map.add("file",rs);
                //String result = restTemplate.postForObject(url,map,String.class);
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
                //httpHeaders.setAccept(Arrays.asList(MediaType.MULTIPART_FORM_DATA));
                httpHeaders.setAccept(Arrays.asList(MediaType.MULTIPART_FORM_DATA));
                HttpEntity<MultiValueMap<String,Object>> request = new HttpEntity<MultiValueMap<String,Object>>(map,httpHeaders);
                //HttpEntity<MultiValueMap<String,Object>> request = new HttpEntity<MultiValueMap<String,Object>>(json,httpHeaders);
                //HttpEntity<String> request= new HttpEntity<>(json.toString(),httpHeaders);
                //restTemplate.exchange(url,HttpMethod.POST,request,String.class);
                //restTemplate.postForLocation(url,request);
                restTemplate.postForLocation(url, map);
                //Log.d("Tag", "File (Byte[] Upload bytes): " + rs.getByteArray().toString());
                Log.d("Tag", "File (Byte[] Upload bytes): " + bytes);
                //HttpPost x=

                return null;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }
    }

    static public class restFile {
        private int id;
        private byte[] data;
        private String name;
        private String contentType;
        private int from;
        private int to;
        private String date;
        public int getId() { return this.id; }
        public byte[] getData() { return this.data; }
        public String getName() { return this.name; }
        public String getContentType() { return this.contentType; }
        public int getFrom() { return this.from; }
        public int getTo() { return this.to; }
        public String getDate(){ return this.date; }
        public void setId(int id) { this.id = id; }
        public void setFrom(int from) { this.from = from; }
        public void setTo(int to) { this.to = to; }
        public void setData(byte[] data){ this.data = data; }
        public void setName(String name){ this.name = name; }
        public void setContentType(String contentType){ this.contentType = contentType; }
        public void setDate(String date){ this.date = date; }
        @JsonCreator
        public restFile(@JsonProperty("id") int id,
                        @JsonProperty("data") byte[] data,
                        @JsonProperty("name") String name,
                        @JsonProperty("contentType") String contentType,
                        @JsonProperty("from") int from,
                        @JsonProperty("to") int to,
                        @JsonProperty("date") String date){
            this.id = id;
            this.data = data;
            this.name = name;
            this.contentType = contentType;
            this.from = from;
            this.to = to;
            this.date = date;}
    }

    /*private String[] mFileList;
    private File mPath = new File(Environment.DIRECTORY_DOWNLOADS);
    private String mChosenFile;
    private static final String FTYPE = ".txt";
    private static final int DIALOG_LOAD_FILE = 1000;

    private void loadFileList(){
        try{
            mPath.mkdirs();
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
        if(mPath.exists()){
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    //File sel = new File(dir,filename);
                    //return filename.contains(FTYPE) || sel.isDirectory();
                    return true;
                    //return false;
                }
            };
            mFileList = mPath.list(filter);
        }
        else{
            mFileList = new String[0];
        }
    }

    protected Dialog onCreateDialog(int id){
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String items[] = {"Camara","Local"};
        switch (id){
            case DIALOG_LOAD_FILE:
                builder.setTitle(R.string.file_send_files);
                if(mFileList == null){
                    Log.e("Tag","Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];
                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }*/
}
