package edu.cmu.meridio;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.widget.TextView;

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

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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

        loginButton.registerCallback(callBackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {


                        //initialise the singleton
                        fbUser = User.getInstance();
                        // App code
                        Log.i("UserToken", loginResult.getAccessToken().getToken());
                        Log.i("UserToken", loginResult.getAccessToken().getUserId());

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

                                             name = object.getString("name");
                                            email = object.getString("email");
                                            Log.i("Name", name);
                                            Log.i("Name", email);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        request.setParameters(parameters);
                        request.executeAsync();



                       setUser(loginResult);
                      //  textView.setText("Happy happy "+loginResult.getAccessToken().getUserId() + "\n" + loginResult.getAccessToken().getToken());
                        fbUser.setUserID(loginResult.getAccessToken().getUserId());
                        Intent myIntent = new Intent(LoginActivity.this, LandingActivity.class);
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
                });
    }
    private void setUser(LoginResult loginResult){
        fbLoginResult = loginResult;
        new sendUserInfo().execute();
    }

    private class sendUserInfo extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
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

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("name", name);//B92E1BCC-BE26-45AB-ACA2-27F9AF627306
                jsonParam.put("sessionToken", fbLoginResult.getAccessToken().getToken());
                jsonParam.put("emailId", email);

                DataOutputStream os = new DataOutputStream(myConnection.getOutputStream());

                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                if (myConnection.getResponseCode() == 200) {
                    String a = ""+myConnection.getResponseCode();
                    Log.i("Success- Status Code", a);
                    Log.i("Success- StatusMessage", myConnection.getResponseMessage());
                    // Success
                    // Further processing here
                    InputStream responseBody = myConnection.getInputStream();

                    InputStreamReader responseBodyReader =
                            new InputStreamReader(responseBody, "UTF-8");

                    jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginObject(); // Start processing the JSON object
                    while (jsonReader.hasNext()) { // Loop through all keys
                        String key = jsonReader.nextName(); // Fetch the next key
                        if (key.equals("userId")) { // Check if desired key
                            // Fetch the value as a String
                            fbUser.setUserID(jsonReader.nextString());

                            // Do something with the value
                            // ...
                            Log.i("value of userID", fbUser.getUserID());

                            break; // Break out of the loop
                        } else {
                            jsonReader.skipValue(); // Skip values of other keys
                        }
                    }
                    jsonReader.close();
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
        protected void onPostExecute(String res) {
            //TextView messText = (TextView) findViewById(R.id.barcode);
           // messText.setText(messageTweet);

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callBackManager.onActivityResult(requestCode, resultCode, data);
    }
}
