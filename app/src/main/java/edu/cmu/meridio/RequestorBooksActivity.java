package edu.cmu.meridio;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.ArrayList;
import java.util.Collections;

public class RequestorBooksActivity extends BaseActivity {
    private ConnectivityManager mConnectivityManager = null;
    ProgressDialog mProgress;
    private ListView listView;
    private ArrayList<Book> bookArrayList;
    private ArrayAdapter<Book> adapter;
    private String body;
    private String userId;
    private String requestId;
    private String acceptorWantsBookId;
    private String fromEmail;

    public static final String ISBN = "isbn";

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.sort_asc).setVisible(true);
        menu.findItem(R.id.sort_desc).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.sort_asc:
                Collections.sort(bookArrayList);
                adapter.notifyDataSetChanged();
                break;
            case R.id.sort_desc:
                Collections.sort(bookArrayList, Collections.<Book>reverseOrder());
                adapter.notifyDataSetChanged();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requestor_books);

        Intent i = getIntent();
        userId = i.getStringExtra(RequestsReceivedActivity.USERID);
        requestId = i.getStringExtra(RequestsReceivedActivity.REQUESTID);
        fromEmail = i.getStringExtra(RequestsReceivedActivity.FROMEMAIL);

        if(userId != null)
            Log.v("userID 4m extra", userId);
        else
            Log.v("userID 4m extra", "null");
        if(requestId != null)
            Log.v("requestID 4m extra", requestId);
        else Log.v("reqID 4m extra", "null");

        listView = (ListView) findViewById(R.id.list_requestor_books);
        body = buildRequestorLibraryBody();
        new fetchRequestorLibrary(body).execute();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(RequestorBooksActivity.this, RequestorISBNActivity.class);
                i.putExtra(ISBN, adapter.getItem(position).getIsbn());
                i.putExtra(RequestsReceivedActivity.REQUESTID, requestId);
                i.putExtra(RequestsReceivedActivity.ACCEPTORWANTSBOOKID, Integer.toString(adapter.getItem(position).getId()));
                i.putExtra(RequestsReceivedActivity.FROMEMAIL, fromEmail);
                startActivity(i);
            }
        });
    }

    private class fetchRequestorLibrary extends AsyncTask<Void, Void, JSONObject> {
        String body;
        fetchRequestorLibrary(String body){
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
            mProgress = new ProgressDialog(RequestorBooksActivity.this);
            mProgress.setMessage("Fetching books in requestor library...");
            mProgress.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            Log.v("fetchRequestorLibrary1", "true");
            // Stop if cancelled
            if (isCancelled()) {
                Log.v("cancelled", "in doInBackground");
                return null;
            }

            String apiUrlString = "http://ec2-54-85-207-189.compute-1.amazonaws.com:4000/booksAvailableWithRequestor";
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
                Log.v("fetchRequestorLibrary3", "true");
                if (responseCode != 200) {
                    Log.v(getClass().getName(), "booksavailablewithrequestor failed. Response Code: " + responseCode);
                    connection.disconnect();
                    showLibraryFailDialog(getString(R.string.requestor_library_null_response));
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
                showSimpleDialog(getResources().getString(R.string.requestor_library_null_response));
            }
            else{
                mProgress.hide();
                if(responseJson.has("status") ) {
                    try {
                        String result = responseJson.getString("status");
                        if (result.equals("success") && responseJson.getJSONArray("books").length() > 0){
                            setBooksInRequestorLibrary(responseJson);
                            adapter = new RequestorBooksViewAdapter(RequestorBooksActivity.this, R.layout.requestorbookitem_listview, bookArrayList);
                            listView.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.v("requestor lib sucess", "in post execute");
                }
            }
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

    private void showLibraryFailDialog(String showString) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Error");
            alertDialog.setMessage(showString);
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.show();
        }catch(Exception e) {
            Log.v("Fetching library failed", "Show Dialog: "+e.getMessage());
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
            Log.v("Library", "Show Dialog: "+e.getMessage());
        }
    }

    private void setBooksInRequestorLibrary(JSONObject jsonObject){
        try {
            JSONArray listOfBooks = jsonObject.getJSONArray("books");
            if(listOfBooks.length() > 0)
                bookArrayList = new ArrayList<Book>();
            for(int i = 0; i< listOfBooks.length(); i++){
                JSONObject book = (JSONObject) listOfBooks.get(i);
                Book b = new Book(Integer.parseInt(book.get("bookId").toString()), book.get("isbn").toString(), book.get("title").toString());
                bookArrayList.add(b);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String buildRequestorLibraryBody(){
        String body = "{"
                + "\"userId\":" + userId
                + "}";
        Log.v("request body", body);
        return body;
    }


}
