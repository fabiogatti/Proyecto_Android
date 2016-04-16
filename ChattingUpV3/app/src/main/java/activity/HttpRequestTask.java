package activity;

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Pcs on 10/04/2016.
 */
public class HttpRequestTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        try {
            //final String url = "http://10.0.2.2:8191/rest/messages/1/2";
            final String url = "http://192.168.1.4:8191/rest/" + params[0];
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
            //restTemplate.getMessageConverters().add(new StringHttpMessageConverter(/*url, String.class*/));
            String result = "[]";
            try {
                result = restTemplate.getForObject(url, String.class);
            } catch (Exception e){
                e.printStackTrace();
            }
            //Log.d("Tag", "Greeting in doInBackground: " + result);
            //String reportKey = object.get("reportKey").textValue();
            //Greeting greeting = restTemplate.getForEntity(object, Greeting.class);
            return result;
        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }

        return null;
    }
}