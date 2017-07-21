package edu.cmu.demologin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {
    LoginButton loginButton;
    TextView textView;

    CallbackManager callBackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);
        textView = (TextView) findViewById(R.id.text_view);
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        callBackManager = CallbackManager.Factory.create();

        loginButton.registerCallback(callBackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        textView.setText("Happy happy "+loginResult.getAccessToken().getUserId() + "\n" + loginResult.getAccessToken().getToken());

                        Intent myIntent = new Intent(LoginActivity.this, PostActivity.class);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callBackManager.onActivityResult(requestCode, resultCode, data);
    }




}
