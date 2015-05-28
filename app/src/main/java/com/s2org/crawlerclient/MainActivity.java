package com.s2org.crawlerclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
1. get user name and password.
2. push login button.
3. app change state to login.
4.
 */
public class MainActivity extends ActionBarActivity {

//    private SharedPreferences sharedPrefs;

    // ㅅㅂ 상태 저장 SHaredPreferences니 onSave니 다 필요없고 그냥 변수 static으로 바꾸니까 처리됨 -_-
    private static String userName = "";
    private static String userPassword = "";

    private static boolean isLogin = false;

    // 이거 쓰는걸로 일단 고려.
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if(android.os.Build.VERSION.SDK_INT > 9) {
//
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//
//            StrictMode.setThreadPolicy(policy);
//
//        }

        setContentView(R.layout.activity_main);

//        if(savedInstanceState != null) {
//            userName = savedInstanceState.getString("userName");
//        }

        Log.i("start login state", String.valueOf(isLogin));
        Log.i("userName at f", userName);

//        sharedPrefs = getSharedPreferences("userPref", Activity.MODE_PRIVATE);
//        userName = sharedPrefs.getString("userName", "");
//        userPassword = sharedPrefs.getString("userPassword", "");
//        String loginOutButtonText = sharedPrefs.getString("loginOutButtonText", "");
//        isLogin = sharedPrefs.getBoolean("isLogin", false);

        Log.i("prefs login state", String.valueOf(isLogin));

        final Button loginOutButton = (Button) findViewById(R.id.loginout_button);
        if(isLogin) {
            loginOutButton.setText("Logout");
        } else {
            loginOutButton.setText("Login");
        }

        // loginOutButton.setOnClickListener((v)->{});    // possible?
        loginOutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO: change button text, change state to login

                EditText userNameEditText = (EditText) findViewById(R.id.userNameEditText);
                userName = userNameEditText.getText().toString();
                Log.i("userName", userName);

                EditText userPasswordEditText = (EditText) findViewById(R.id.userPasswordEditText);
                userPassword = userPasswordEditText.getText().toString();
                Log.i("userPassword", userPassword);

                Log.i("isLogin", String.valueOf(isLogin));
                isLogin = !isLogin;
                Log.i("isLogin", String.valueOf(isLogin));

                if(isLogin) {
                    loginOutButton.setText("Logout");
                } else {
                    loginOutButton.setText("Login");
                    userNameEditText.setText("");
                    userName = "";
                    userPassword = "";

//                    SharedPreferences.Editor ed = sharedPrefs.edit();
//                    ed.remove("userName");
//                    ed.remove("userPassword");
//                    ed.commit();
                }

            }
        });


        Intent intent = getIntent();
        Log.i("intent", intent.toString());

        String receivedIntentAction = intent.getAction();

        // If run app without intent, receivedIntentAction is Intent.ACTION_MAIN
        if(receivedIntentAction.equals(Intent.ACTION_SEND)) {
//            Log.i("intentAction", receivedIntentAction);
//            Log.i("intentType", intent.getType());

            String receivedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.i("receivedText", receivedText);

            String urlRegex = "http.+html"; // TODO: 후에 .html 형식이 아닌 주소도 처리할 수 있게 정규표현식 다르게 하는 것도 생각해볼 것.
            Pattern pattern = Pattern.compile(urlRegex);
            Matcher matcher = pattern.matcher(receivedText);
            String url = "";

            while(matcher.find()) {
                url = matcher.group();
            }

            Log.i("url", url);

            Log.i("userName in getIntent", userName);

            EditText userNameEditText = (EditText) findViewById(R.id.userNameEditText);
            userNameEditText.setText(userName);

            /*
            TODO: 여기서 http POST 날릴 것.
            POST데이터에 유저명/패스워드를 같이 날려서 서버측에서 그걸 이용해서
            해당 사용자의 다운로드 폴더에 화상을 다운받게 하는 것이 맞는듯 함.
            */

            new HttpTask().execute(url);




//            loginOutButton.setText(loginOutButtonText);


        } else {
            Log.i("Not SEND", "Not SEND");
        }



    }

    class HttpTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            HttpRequestFactory httpRequestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                }
            });

            GenericUrl url = new GenericUrl("http://s2org.com:8888/upload");
            HashMap<String, String> params2 = new HashMap<String, String>();

            if(params != null) {
                for(String param : params) {
                    Log.i("param", param);
                    //params2.put("data", "http://otanews.livedoor.biz/archives/52018592.html");
                    params2.put("data", param);
                }
            }


            try {
                HttpRequest httpRequest = httpRequestFactory.buildPostRequest(url, new UrlEncodedContent(params2));

                HttpResponse httpResponse = httpRequest.execute();
                int statusCode = httpResponse.getStatusCode();

                Log.i("statusCode", String.valueOf(statusCode));

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        Log.i("onPause", "called");
//
//        Button loginOutButton = (Button) findViewById(R.id.loginout_button);
//
//        SharedPreferences.Editor ed = sharedPrefs.edit();
//        ed.putString("userName", userName);
//        ed.putString("userPassword", userPassword);
//        ed.putString("loginOutButtonText", loginOutButton.getText().toString());
//        ed.putBoolean("isLogin", isLogin);
//        ed.commit();
//    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        outState.putString("userName", userName);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        userName = savedInstanceState.getString("userName");
//    }
}
