package edu.cmu.meridio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import static com.facebook.Profile.fetchProfileForCurrentAccessToken;
import static com.facebook.Profile.getCurrentProfile;


public class LoginActivity extends AppCompatActivity {
    LoginButton loginButton;
    TextView textView;
    User fbUser;
    CallbackManager callBackManager;
    LoginResult fbLoginResult;
    JsonReader jsonReader;
    String name;
    String email;
    String sessionToken;
    AccessTokenTracker accessTokenTracker;
    private boolean directUserToLogout = false;
    private static final int LOGIN = 1;
//    Context context = getBaseContext();
    SharedPreferences userIdPref;
    public static final String USERID = "userId";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);
        textView = (TextView) findViewById(R.id.text_view);
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
        callBackManager = CallbackManager.Factory.create();
        final Intent myIntent = new Intent(LoginActivity.this, LandingActivity.class);
        Intent receivedIntent = getIntent();
        if(receivedIntent.hasExtra(BaseActivity.LOGOUTUSER)){
            directUserToLogout = receivedIntent.getBooleanExtra(BaseActivity.LOGOUTUSER, false);
        }

        userIdPref = this.getPreferences(Context.MODE_PRIVATE);
        Log.v("userIdPref.contains", String.valueOf(userIdPref.getAll()));
        if (userIdPref.contains(USERID) && !directUserToLogout){
            Log.v("found userId", "in sharedPref");
            fbUser = User.getInstance();
            fbUser.setUserID(userIdPref.getString(USERID, null));
            Log.v("user singleton", "should" +
                    "" +
                    "/ be set to" + fbUser.getUserID());
            startActivityForResult(myIntent, LOGIN);
        }else {
            loginButton.registerCallback(callBackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {


                        //initialise the singleton
                        fbUser = User.getInstance();
                        // App code
                        Log.i("UserToken", loginResult.getAccessToken().getToken());
                        Log.i("userID", loginResult.getAccessToken().getUserId());

                       /* Profile.fetchProfileForCurrentAccessToken();

                        Profile pro = Profile.getCurrentProfile();

                        Log.i("name", pro.getFirstName());*/

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());

                                        // Application code
                                        try {
                                            LoginActivity.this.name = object.getString("name");
                                            LoginActivity.this.email = object.getString("email");
                                            Log.i("LoginActivityName", LoginActivity.this.name);
                                            Log.i("LoginActivityEmail", LoginActivity.this.email);

                                            setUser(loginResult);


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                        Log.v("calling setUser now", "true");
                        //  textView.setText("Happy happy "+loginResult.getAccessToken().getUserId() + "\n" + loginResult.getAccessToken().getToken());
                        //                        fbUser.setUserID(loginResult.getAccessToken().getUserId());
                        Log.v("FB returned User ID", "should be set to:" + loginResult.getAccessToken().getUserId());
                        //myIntent.putExtra("key", value); //Optional parameters
                        LoginActivity.this.startActivity(myIntent);

                    }

                    @Override
                    public void onCancel() {
                        // App code
                        textView.setText("Sad Sad");
                    }


                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                }
            );
        }

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    //clear user singleton
                    if(fbUser != null)
                        fbUser.clearUserID();

                    //clear sharedPref
                    SharedPreferences.Editor editor = userIdPref.edit();
                    editor.remove(USERID);
                    editor.apply();
                }
            }
        };
    }

    private void setUser(LoginResult loginResult){

        if(name == null)
            Log.v("name", "set to null");
        else
            Log.v("name ", "should be set");


        fbLoginResult = loginResult;
        String requestBody = buildSetUserSessionRequestBody();
        new sendUserInfo(requestBody).execute();
    }

    private class sendUserInfo extends AsyncTask<String, String, JSONObject> {
        String body;
        sendUserInfo(String body) {this.body = body;}

        @Override
        protected JSONObject doInBackground(String... strings) {
            // Create URL
            try {
                URL myEndpoint = new URL("http://ec2-54-85-207-189.compute-1.amazonaws.com:4000/setUserSession");

                HttpURLConnection myConnection
                        = (HttpURLConnection) myEndpoint.openConnection();

                myConnection.setRequestMethod("POST");


                myConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                myConnection.setRequestProperty("Accept","application/json");
                myConnection.setDoOutput(true);
                myConnection.setDoInput(true);

                byte[] outputInBytes = this.body.getBytes("UTF-8");
                OutputStream os = myConnection.getOutputStream();
                os.write(outputInBytes);
                os.close();

                if (myConnection.getResponseCode() == 200) {

                    // Read data from response.
                    StringBuilder builder = new StringBuilder();
                    BufferedReader responseReader = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                    String line = responseReader.readLine();
                    while (line != null) {
                        builder.append(line);
                        line = responseReader.readLine();
                    }
                    String responseString = builder.toString();
                    Log.v(getClass().getName(), "Response String: " + responseString);
                    JSONObject responseJson = new JSONObject(responseString);
                    // Close connection and return response code.
                    myConnection.disconnect();

                    return responseJson;
                } else {
                    // Error handling code goes here
                    String a = ""+myConnection.getResponseCode();
                    Log.i("Failure- Status Code", a);
                    Log.i("Failure- StatusMessage", myConnection.getResponseMessage());

                }
                myConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }
        protected void onPostExecute(JSONObject responseJson) {
            if(responseJson!= null && responseJson.has("status") ) {
                try {
                    String result = responseJson.getString("status");
                    Log.v("result in postexecute", result);
                    if (result.equals("success")){
                        Log.v("session API success", "result");
                        Log.v("responseJson", responseJson.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(responseJson!= null && responseJson.has("userId")){
                try {
                    Log.v("set sharedPref", "coz not found in sharedPreferences");
                    fbUser.setUserID(responseJson.get("userId").toString());
                    SharedPreferences.Editor editor = userIdPref.edit();
                    editor.putString(LoginActivity.USERID, responseJson.get("userId").toString());
                    editor.commit();
                    Log.v(LoginActivity.USERID, responseJson.get("userId").toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callBackManager.onActivityResult(requestCode, resultCode, data);
    }

    private String buildSetUserSessionRequestBody(){
        String body = "{"
                + "\"name\": \"" + this.name + "\""
                + ",\"emailId\": \"" + this.email + "\""
                + ",\"sessionToken\": \"" + this.fbLoginResult.getAccessToken().getToken() + "\""
                + "}";
        Log.v("setuserreq body", body);
        return body;
    }
}
