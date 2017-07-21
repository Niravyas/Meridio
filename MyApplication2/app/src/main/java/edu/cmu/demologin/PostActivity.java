package edu.cmu.demologin;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.CallbackManager;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

public class PostActivity extends AppCompatActivity {


CallbackManager callbackManager;
 ShareDialog shareDialog;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        button = (Button) findViewById(R.id.Post_Button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                callbackManager = CallbackManager.Factory.create();
                shareDialog = new ShareDialog(PostActivity.this);

                if (ShareDialog.canShow(ShareLinkContent.class)) {
                   /* ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
                            .build();*/
                    ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentUrl(Uri.parse("http://google.com")).setQuote("Hello people").build();

                    shareDialog.show(linkContent);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
