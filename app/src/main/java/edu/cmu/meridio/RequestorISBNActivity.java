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
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class RequestorISBNActivity extends BaseActivity {
    private ConnectivityManager mConnectivityManager = null;
    ProgressDialog mProgress;
    TextView title;
    TextView genre;
    TextView description;
    Button approve;
    Button decline;
    private String isbn;
    private String imageURLString;
    private String requestId;
    private String acceptorWantsBookId;
    private static final String coverLibraryURL = "http://covers.openlibrary.org/b/isbn/";
    private static final String suffixLarge = "-L.jpg?default=false";
    private static final String suffixMedium = "-M.jpg?default=false";
    private ImageView imageView;
    private boolean getFromCoversLibrary = false;
    boolean approvedStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requestor_isbn);
        Intent data = getIntent();

        if (data.hasExtra(RequestsReceivedActivity.REQUESTID)) {
            requestId = data.getStringExtra(RequestsReceivedActivity.REQUESTID);
        }
        if (data.hasExtra(RequestorBooksActivity.ISBN)) {
            isbn = data.getStringExtra(RequestorBooksActivity.ISBN);
        }
        if(data.hasExtra(RequestsReceivedActivity.ACCEPTORWANTSBOOKID)){
            acceptorWantsBookId = data.getStringExtra(RequestsReceivedActivity.ACCEPTORWANTSBOOKID);
        }

        imageView = (ImageView) findViewById(R.id.image);

        title = (TextView) findViewById(R.id.title);
        genre = (TextView) findViewById(R.id.genre);
        description = (TextView) findViewById(R.id.description);
        imageURLString = coverLibraryURL + isbn + suffixLarge;
        approve = (Button) findViewById(R.id.btn_approve);
        decline = (Button) findViewById(R.id.btn_decline);
        new GoogleApiRequest(isbn).execute();

        approve.setOnClickListener(new ProcessRequestListener("approved"));
        decline.setOnClickListener(new ProcessRequestListener("declined"));


    }

    // Received ISBN from intent. Send to GoogleBooks to obtain book information.
    public class GoogleApiRequest extends AsyncTask<String, Object, JSONObject> {
        String isbn;
        GoogleApiRequest(String isbn){
            this.isbn = isbn;
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
            mProgress = new ProgressDialog(RequestorISBNActivity.this);
            mProgress.setMessage("Fetching book details...");
            mProgress.show();
        }
        @Override
        protected JSONObject doInBackground(String... isbns) {
            Log.v("point1", "true");
            // Stop if cancelled
            if(isCancelled()){
                Log.v("cancelled", "in doInBackground");
                return null;
            }

            String apiUrlString = "https://www.googleapis.com/books/v1/volumes?q=isbn:"
//            + this.isbn;
            + "9780141439600";
            try{
                HttpURLConnection connection = null;
                // Build Connection.
                try{
                    URL url = new URL(apiUrlString);
                    Log.v("URL called", apiUrlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(60000); // 1 minute
                    connection.setConnectTimeout(60000); // 1 minute
                } catch (MalformedURLException e) {
                    // Impossible: The only two URLs used in the app are taken from string resources.
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    // Impossible: "GET" is a perfectly valid request method.
                    e.printStackTrace();
                }
                int responseCode = connection.getResponseCode();
                Log.v("point3", "true");
                if(responseCode != 200){
                    Log.v(getClass().getName(), "GoogleBooksAPI request failed. Response Code: " + responseCode);
                    connection.disconnect();
                    return null;
                }

                // Read data from response.
                StringBuilder builder = new StringBuilder();
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = responseReader.readLine();
                while (line != null){
                    builder.append(line);
                    line = responseReader.readLine();
                }
                String responseString = builder.toString();
                Log.v(getClass().getName(), "Response String: " + responseString);
                JSONObject responseJson = new JSONObject(responseString);
                // Close connection and return response code.
                connection.disconnect();

                isCoverInCoversLibrary();

                return responseJson;
            } catch (SocketTimeoutException e) {
                Log.v(getClass().getName(), "Connection timed out. Returning null");
                return null;
            } catch(IOException e){
                Log.v(getClass().getName(), "IOException when connecting to Google Books API.");
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                Log.v(getClass().getName(), "JSONException when connecting to Google Books API.");
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
                showSimpleDialog(getResources().getString(R.string.dialog_null_response));
            }
            else{
                mProgress.hide();
                Log.v("all went well", "in post execute");
                // All went well. Do something with your new JSONObject.
                bookJSONToTitle(responseJson);
                displayCoverArt(responseJson);
            }
        }
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

    private void setTitle(String title){
        try{
            this.title.setText(title, TextView.BufferType.EDITABLE);
        } catch(NullPointerException e){
            e.printStackTrace();
        }
    }
    private void bookJSONToTitle(JSONObject jsonObject){
        try {
            if (jsonObject.length() == 0 || jsonObject.getInt("totalItems") == 0) {
                mProgress.cancel();
                finish();
                return;
            }
//            JSONArray books;
            if(!jsonObject.has("items"))
                throw new JSONException(String.format(getString(R.string.json_missing_key), "items"));
            JSONArray books = jsonObject.getJSONArray("items");
            Log.v("books",books.toString());

            //Set title
            JSONObject firstBook = (JSONObject) books.get(0);
            if(!firstBook.has("volumeInfo"))
                throw new JSONException(String.format(getString(R.string.json_missing_key), "volumeInfo"));
            JSONObject firstBookVolumeInfo = firstBook.getJSONObject("volumeInfo");
            setTitle(firstBookVolumeInfo.getString("title"));
            Log.v("firstBookVolumeInfo", firstBookVolumeInfo.toString());

            //Set Genre
            if(!firstBookVolumeInfo.has("categories")){
                throw new JSONException(String.format(getString(R.string.json_missing_key), "categories"));
            }
            JSONArray jsonCategories = firstBookVolumeInfo.getJSONArray("categories");
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i< jsonCategories.length(); i++){
                sb.append(jsonCategories.get(i).toString());
                if( i != jsonCategories.length() - 1){
                    sb.append(", ");
                }
            }
            this.genre.setText(sb.toString(), TextView.BufferType.EDITABLE);

            //set description
            if(firstBookVolumeInfo.has("description")) {
                String description = firstBookVolumeInfo.getString("description");
                this.description.setText(description, TextView.BufferType.EDITABLE);
            }

            Log.v("firstBookVlIn.getString", firstBookVolumeInfo.getString("title"));
        } catch(JSONException e){
            e.printStackTrace();
        }
    }

    private void displayCoverArt(JSONObject jsonObject){

        if(getFromCoversLibrary == true){
            Picasso.with(getApplicationContext()).load(imageURLString).into(imageView);
        } else {
            try {
                fetchFromGoogleBookThumbnails(jsonObject);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void isCoverInCoversLibrary() throws IOException {

        Log.v("coverlibraryURL", imageURLString);
        HttpURLConnection connection = null;
        try {
            // Build Connection.
            URL url = new URL(imageURLString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000); // 10 seconds
            connection.setConnectTimeout(10000); // 10 seconds
        } catch (MalformedURLException e) {
            // Impossible: The only two URLs used in the app are taken from string resources.
            e.printStackTrace();
        } catch (ProtocolException e) {
            // Impossible: "GET" is a perfectly valid request method.
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int responseCode = connection.getResponseCode();
        if(responseCode == 404){
            Log.v(getClass().getName(), "Failed to get book covers from covers library: " + responseCode);
            connection.disconnect();
        }
        if(responseCode == 200)
            getFromCoversLibrary = true;
    }

    private void fetchFromGoogleBookThumbnails(JSONObject jsonObject) throws JSONException {
        JSONArray books = jsonObject.getJSONArray("items");
        JSONObject firstBook = (JSONObject) books.get(0);
        JSONObject firstBookVolumeInfo = firstBook.getJSONObject("volumeInfo");
        JSONObject imageLinksJSON = firstBookVolumeInfo.getJSONObject("imageLinks");
        String thumbnailURL = imageLinksJSON.getString("thumbnail");
        Picasso.with(getApplicationContext()).load(thumbnailURL).into(imageView);
    }

    private class ProcessRequest extends AsyncTask<Void, Void, JSONObject>{
        String body;
        ProcessRequest(String body){
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
            mProgress = new ProgressDialog(RequestorISBNActivity.this);
            mProgress.setMessage("Processing...");
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

            String apiUrlString = "http://ec2-54-85-207-189.compute-1.amazonaws.com:4000/updateTradeRequest";
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
                    Log.v(getClass().getName(), "posting API request failed. Response Code: " + responseCode);
                    connection.disconnect();
                    showProcessFailDailog(getString(R.string.post_fail));
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
                showSimpleDialog(getResources().getString(R.string.dialog_null_response));
            }
            else{
                mProgress.hide();
                if(responseJson.has("status") ) {
                    try {
                        String result = responseJson.getString("status");
                        if (result.equals("success")){
                            if(this.body.contains("approved"))
                                showProcessSuccessfulDialog(String.format(getString(R.string.approve_decline_success), "approved"));
                            if(this.body.contains("declined"))
                                showProcessSuccessfulDialog(String.format(getString(R.string.approve_decline_success), "declined"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.v("book post success", "in post execute");
                }
            }
        }
    }


    private void showProcessSuccessfulDialog(String showString) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Info");
            alertDialog.setMessage(showString);
            alertDialog.setIcon(R.drawable.success);
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //redirect to library
                    Intent i = new Intent(getApplicationContext(), PostActivity.class);
                    i.putExtra("bookImageURL", imageURLString);
                    startActivity(i);
                }
            });

            alertDialog.show();
        }catch(Exception e) {
            Log.v("Book post", "Show Dialog: "+e.getMessage());
        }
    }

    private void showProcessFailDailog(String showString) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Error");
            alertDialog.setMessage(showString);
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.show();
        }catch(Exception e) {
            Log.v("Book post failed", "Show Dialog: "+e.getMessage());
        }
    }

    private String buildApproveRequestBody(){
        String body = "{"
                + "\"id\":" + requestId
                + ",\"acceptorWantsBookId\": \"" + acceptorWantsBookId + "\""
                +",\"status\": \"approved\""
                + "}";
        Log.v("request body", body);
        return body;
    }

    private String buildDeclineRequestBody(){
        String body = "{"
                + "\"id\":" + requestId
                + ",\"acceptorWantsBookId\": \"" + acceptorWantsBookId + "\""
                +",\"status\": \"declined\""
                + "}";
        Log.v("request body", body);
        return body;
    }

    private class ProcessRequestListener implements View.OnClickListener{
        String status;
        ProcessRequestListener(String status){
            this.status = status;
        }

        @Override
        public void onClick(View v) {
            String body ;
            switch (this.status){
                case "approved":
                    body = buildApproveRequestBody();
                    new ProcessRequest(body).execute();
                    approvedStatus = true;
                    break;
                case "declined":
                    body = buildDeclineRequestBody();
                    new ProcessRequest(body).execute(); break;
            }
        }
    }
}
