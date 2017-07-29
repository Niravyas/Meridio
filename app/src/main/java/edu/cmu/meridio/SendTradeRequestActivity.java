package edu.cmu.meridio;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class SendTradeRequestActivity extends BaseActivity{

    private ConnectivityManager mConnectivityManager = null;
    User fbUser;
    ProgressDialog mProgress;
    TextView title;
    TextView genre;
    TextView description;
    String strTitle;
    String strGenre;
    String strDescription;
    String strUrl;
    Button request;
    Button cancel;
    String bookId;
    private String isbnForImage;
    private ImageView imageView;
    private boolean getFromCoversLibrary = false;
    private String imageURLString;
    private String postBookRequest;
    private boolean userLibraryEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_trade_request);
        Intent data = getIntent();
        fbUser = User.getInstance();
        isbnForImage = data.getStringExtra("isbn");
        bookId = data.getStringExtra("bookId");
        strTitle = data.getStringExtra("title");
        strGenre = data.getStringExtra("genre");
        strDescription = data.getStringExtra("description");
        strUrl = data.getStringExtra("imageUrl");
        imageView = (ImageView) findViewById(R.id.imageReq);

        title = (TextView) findViewById(R.id.titleReq);
        genre = (TextView) findViewById(R.id.genreReq);
        description = (TextView) findViewById(R.id.descriptionReq);

        request = (Button) findViewById(R.id.btn_post_bookReq);
        cancel = (Button) findViewById(R.id.btn_cancel_post_bookReq);

        setUIDetails(strTitle, strGenre, strDescription, strUrl);

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestForUserLibraryCount = buildRequestLibraryCountbody();
                new GetUserLibraryBookCount(requestForUserLibraryCount).execute();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mProgress != null)
                    mProgress.hide();
                finish();
            }
        });


    }

    private void showNetworkDialog() {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Info");
            alertDialog.setMessage(getString(R.string.internet_unavailable));
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if(mProgress != null)
                        mProgress.hide();
                    finish();

                }
            });

            alertDialog.show();
        }catch(Exception e) {
            Log.v("Network", "Show Dialog: "+e.getMessage());
        }
    }

    private void showSimpleDialog(String showString) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Info");
            alertDialog.setMessage(showString);
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if(mProgress != null)
                        mProgress.hide();
                    finish();

                }
            });

            alertDialog.show();
        }catch(Exception e) {
            Log.v("Network", "Show Dialog: "+e.getMessage());
        }
    }

    protected boolean isNetworkConnected(){

        // Instantiate mConnectivityManager if necessary
        if(mConnectivityManager == null){
            mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        // Is device connected to the Internet?
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            return true;
        } else {
            return false;
        }
    }

    private class RequestBook extends AsyncTask<Void, Void, JSONObject>{
        String body;
        RequestBook(String body){
            this.body = body;
        }

        @Override
        protected void onPreExecute() {
            // Check network connection.
            if(isNetworkConnected() == false){
                // Cancel request.
                Log.v(getClass().getName(), "Not connected to the internet");
                cancel(true);
                return;
            }
            mProgress = new ProgressDialog(SendTradeRequestActivity.this);
            mProgress.setMessage("Sending your request...");
            mProgress.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            Log.v("posting: point1", "true");
            // Stop if cancelled
            if (isCancelled()) {
                Log.v("cancelled", "in doInBackground");
                return null;
            }

            String apiUrlString = "http://ec2-54-85-207-189.compute-1.amazonaws.com:4000/createTradeRequest";
            try {
                HttpURLConnection connection = null;
                // Build Connection.
                try {
                    URL url = new URL(apiUrlString);
                    Log.v("URL called", apiUrlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setReadTimeout(10000); // 10 seconds
                    connection.setConnectTimeout(10000); // 10 seconds
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    byte[] outputInBytes = this.body.getBytes("UTF-8");
                    OutputStream os = connection.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                } catch (MalformedURLException e) {
                    // Impossible: The only two URLs used in the app are taken from string resources.
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    // Impossible: "GET" is a perfectly valid request method.
                    e.printStackTrace();
                }
                int responseCode = connection.getResponseCode();
                Log.v("post:point3", "true");
                if (responseCode != 200) {
                    Log.v(getClass().getName(), "createTradeRequest API request failed. Response Code: " + responseCode);
                    connection.disconnect();
                    showRequestFailDialog(getString(R.string.request_fail));
                    return null;
                }

                // Read data from response.
                StringBuilder builder = new StringBuilder();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = responseReader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = responseReader.readLine();
                }
                String responseString = builder.toString();
                Log.v(getClass().getName(), "Response String: " + responseString);
                JSONObject responseJson = new JSONObject(responseString);
                // Close connection and return response code.
                connection.disconnect();

//                isCoverInCoversLibrary();

                return responseJson;
            } catch (SocketTimeoutException e) {
                Log.v(getClass().getName(), "Connection timed out. Returning null");
                return null;
            } catch (IOException e) {
                Log.v(getClass().getName(), "IOException when connecting to bookpost API.");
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                Log.v(getClass().getName(), "JSONException when connecting to bookpost API.");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject responseJson) {
            if(isCancelled()){
                // Request was cancelled due to no network connection.
                showNetworkDialog();
            } else if(responseJson == null){
                showRequestFailDialog(getString(R.string.request_fail));
            }
            else{
                mProgress.hide();
                if(responseJson.has("status") ) {
                    try {
                        String result = responseJson.getString("status");
                        if (result.equals("success")){
                            showRequestSuccessfulDialog(getString(R.string.request_success));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.v("book post success", "in post execute");
                }
            }
        }
    }


    private void showRequestSuccessfulDialog(String showString) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Info");
            alertDialog.setMessage(showString);
            alertDialog.setIcon(R.drawable.success);
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //redirect to Landing
                    Intent i = new Intent(getApplicationContext(), LandingActivity.class);
                    startActivity(i);
                }
            });

            alertDialog.show();
        }catch(Exception e) {
            Log.v("Book post", "Show Dialog: "+e.getMessage());
        }
    }

    private void showRequestFailDialog(String showString) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Error");
            alertDialog.setMessage(showString);
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.show();
        }catch(Exception e) {
            Log.v("Request create failed", "Show Dialog: "+e.getMessage());
        }
    }

    private String buildRequestBookRequestBody(){
        String body = "{"
                + "\"fromUserId\":" + fbUser.getUserID()
                + ",\"requestorWantsBookId\": \"" + bookId + "\""
                + "}";
        Log.v("request body", body);
        return body;
    }

    private void setUIDetails(String title, String genre, String description, String imageUrl){
        this.title.setText(title);
        this.genre.setText(genre);
        this.description.setText(description);
        Picasso.with(getApplicationContext()).load(imageUrl).into(imageView);
        Log.v("trying to set UI", title + "\n" + genre + "\n" + description + "\n" + imageUrl);
    }

    private class GetUserLibraryBookCount extends AsyncTask<Void, Void, JSONObject>{
        String body;
        GetUserLibraryBookCount(String body){
            this.body = body;
        }

        @Override
        protected void onPreExecute() {
            // Check network connection.
            if(isNetworkConnected() == false){
                // Cancel request.
                Log.v(getClass().getName(), "Not connected to the internet");
                cancel(true);
                return;
            }
            mProgress = new ProgressDialog(SendTradeRequestActivity.this);
            mProgress.setMessage("Verifying that you have books...");
            mProgress.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            Log.v("verifying count: point1", "true");
            // Stop if cancelled
            if (isCancelled()) {
                Log.v("cancelled", "in doInBackground");
                return null;
            }

            String apiUrlString = "http://ec2-54-85-207-189.compute-1.amazonaws.com:4000/getMyBooks";
            try {
                HttpURLConnection connection = null;
                // Build Connection.
                try {
                    URL url = new URL(apiUrlString);
                    Log.v("URL called", apiUrlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setReadTimeout(10000); // 10 seconds
                    connection.setConnectTimeout(10000); // 10 seconds
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    byte[] outputInBytes = this.body.getBytes("UTF-8");
                    OutputStream os = connection.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                } catch (MalformedURLException e) {
                    // Impossible: The only two URLs used in the app are taken from string resources.
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    // Impossible: "GET" is a perfectly valid request method.
                    e.printStackTrace();
                }
                int responseCode = connection.getResponseCode();
                Log.v("verifying cnt:point3", "true");
                if (responseCode != 200) {
                    Log.v(getClass().getName(), "verifying API request failed. Response Code: " + responseCode);
                    connection.disconnect();
                    showRequestFailDialog("Failed to get count :(\n Please try after some time!");
                    return null;
                }

                // Read data from response.
                StringBuilder builder = new StringBuilder();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = responseReader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = responseReader.readLine();
                }
                String responseString = builder.toString();
                Log.v(getClass().getName(), "Response String: " + responseString);
                JSONObject responseJson = new JSONObject(responseString);
                // Close connection and return response code.
                connection.disconnect();

//                isCoverInCoversLibrary();

                return responseJson;
            } catch (SocketTimeoutException e) {
                Log.v(getClass().getName(), "Connection timed out. Returning null");
                return null;
            } catch (IOException e) {
                Log.v(getClass().getName(), "IOException when connecting to bookpost API.");
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                Log.v(getClass().getName(), "JSONException when connecting to bookpost API.");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject responseJson) {
            if(isCancelled()){
                // Request was cancelled due to no network connection.
                showNetworkDialog();
            } else if(responseJson == null){
                showRequestFailDialog("Failed to verify your library. Please retry after some time!");
            }
            else{
                mProgress.hide();
                try {
                    if(responseJson.has("status") && responseJson.has("books")) {
                        if (responseJson.getJSONArray("books").length() == 0) {
                            Log.v("user library", "empty");
                            userLibraryEmpty = true;
                            Toast.makeText(getApplicationContext(), "Post at least one book to request a book", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            userLibraryEmpty = false;
                        }

                        //call request if applicable
                        if(!userLibraryEmpty) {
                            String request = buildRequestBookRequestBody();
                            Log.v("will send request", "true");
                            new RequestBook(request).execute();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String buildRequestLibraryCountbody(){
        String body = "{"
                + "\"userId\":" + fbUser.getUserID()
                + "}";
        Log.v("request body", body);
        return body;
    }
}