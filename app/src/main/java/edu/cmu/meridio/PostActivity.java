package edu.cmu.meridio;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostActivity extends BaseActivity {
    CallbackManager callbackManager;
    ShareDialog shareDialog;
    Button button;
    Button cancelButton;
    String imageURL;
    Bitmap imageToPost;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        TextView postText = (TextView) findViewById(R.id.CustomisedMessage);
        postText.setText(getString(R.string.post_to_fb_msg));
        imageView = (ImageView) findViewById(R.id.imgBook) ;
        button = (Button) findViewById(R.id.Post_Button);
        cancelButton = (Button) findViewById(R.id.CancelPost);
        Intent intent = getIntent();
        imageURL = (intent.getStringExtra("bookImageURL") == null)? "":intent.getStringExtra("bookImageURL");
        Log.v("imageURL from intent", imageURL);


        Picasso.with(getApplicationContext()).load(imageURL).into(imageView);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        imageToPost = drawable.getBitmap();
        if(imageToPost != null){
            Log.i("Tag", "notnull");
        }
        if(imageToPost == null){
            Log.i("Tag", "null");
        }
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                callbackManager = CallbackManager.Factory.create();
                shareDialog = new ShareDialog(PostActivity.this);

                Log.i("Tag", "iamhere");

                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse(imageURL)).setQuote(getString(R.string.post_to_fb_msg))
                            .build();
                    shareDialog.show(linkContent);
                }


            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(), LibraryActivity.class);
                startActivity(i);

            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent i = new Intent(getApplicationContext(), LibraryActivity.class);
        startActivity(i);
    }
}
