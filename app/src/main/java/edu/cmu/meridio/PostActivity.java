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
        postText.setText("Nurture your love for reading at no cost! Share your books using the Meridio App! I just did ;)");
        imageView = (ImageView) findViewById(R.id.imgBook) ;
        button = (Button) findViewById(R.id.Post_Button);
        cancelButton = (Button) findViewById(R.id.CancelPost);
        Intent intent = getIntent();
        imageURL = intent.getStringExtra("bookImageURL");
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



              //  new getBMfromURL().execute();

                Log.i("Tag", "iamhere");
                // Bitmap imageToPost = getBitmapFromURL(url);
               /* if (ShareDialog.canShow(SharePhotoContent.class)) {
                    SharePhoto photo = new SharePhoto.Builder()
                            .setBitmap(imageToPost)
                            .build();
                    SharePhotoContent content = new SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .build();

                    shareDialog.show(PostActivity.this, content);
                }*/

                if (ShareDialog.canShow(ShareLinkContent.class)) {
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse(imageURL)).setQuote("Nurture your love for reading at no cost! Share your books using the Meridio App! I just did ;)")
                            .build();



                    //ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentUrl(Uri.parse("http://google.com")).setQuote("Hello people").build();

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

   /*

    public class getBMfromURL extends AsyncTask<String, String, String> {

        private Exception exception;

        protected String doInBackground(String... params) {
            try {
                URL url = new URL(imageURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                imageToPost = BitmapFactory.decodeStream(input);

            } catch (Exception e) {
                this.exception = e;


            }
            return null;
        }

        protected void onPostExecute(String feed) {
Log.i("Tag", "iamhere");
           *//* if (ShareDialog.canShow(ShareLinkContent.class)) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(imageURL)).setQuote("Nurture your love for reading at no cost! Share your books using the Meridio App! I just did ;)")
                        .build();


                //ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentUrl(Uri.parse("http://google.com")).setQuote("Hello people").build();

                shareDialog.show(linkContent);
            }
*//*

          *//*  if (ShareDialog.canShow(ShareLinkContent.class)) {
                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(imageToPost)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();

                shareDialog.show(content);
            }
        }*//*


        }
    }*/
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent i = new Intent(getApplicationContext(), LibraryActivity.class);
        startActivity(i);
    }
}
