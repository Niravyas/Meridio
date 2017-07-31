package edu.cmu.meridio;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class RequestsSentActivity extends BaseActivity {

    private ConnectivityManager mConnectivityManager = null;
    ProgressDialog mProgress;
    private ListView listView;
    private ArrayList<Request> requestArrayList;
    private ArrayAdapter<Request> adapter;
    private String body;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        if(requestArrayList!= null && requestArrayList.size() > 0) {
            menu.findItem(R.id.sort_asc).setVisible(true);
            menu.findItem(R.id.sort_desc).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.sort_asc:
                Collections.sort(requestArrayList);
                adapter.notifyDataSetChanged();
                break;
            case R.id.sort_desc:
                Collections.sort(requestArrayList, Collections.<Request>reverseOrder());
                adapter.notifyDataSetChanged();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_sent);

        listView = (ListView) findViewById(R.id.list_sent_requests);
        body = buildMyRequestsRequestBody();

        //fetch requests here, using asynctask
        new fetchRequests(body).execute();
    }

    private String buildMyRequestsRequestBody(){
        User user = User.getInstance();
        String body = "{"
                + "\"fromUserId\":" + user.getUserID()
                + "}";
        Log.v("request body", body);
        return body;
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

    private void showRequestFailDialog(String showString) {
        try {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("Error");
            alertDialog.setMessage(showString);
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.show();
        }catch(Exception e) {
            Log.v("request fetch failed", "Show Dialog: "+e.getMessage());
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

    private class fetchRequests extends AsyncTask<Void, Void, JSONObject> {
        String body;
        fetchRequests(String body){
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
            mProgress = new ProgressDialog(RequestsSentActivity.this);
            mProgress.setMessage("Fetching your requests...");
            mProgress.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            Log.v("fetchrequests: point1", "true");
            // Stop if cancelled
            if (isCancelled()) {
                Log.v("cancelled", "in doInBackground");
                return null;
            }

            String apiUrlString = "http://ec2-54-85-207-189.compute-1.amazonaws.com:4000/getTradeRequests";
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
                Log.v("fetchrequests:point3", "true");
                if (responseCode != 200) {
                    Log.v(getClass().getName(), "Fetcheq API request failed. Response Code: " + responseCode);
                    connection.disconnect();
                    showRequestFailDialog(getString(R.string.request_null_response));
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
                Log.v(getClass().getName(), "IOException when connecting to getTradeRequest API.");
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                Log.v(getClass().getName(), "JSONException when connecting to getTradeRequest API.");
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
                showSimpleDialog(getResources().getString(R.string.request_null_response));
            }
            else{
                mProgress.hide();
                if(responseJson.has("status") ) {
                    try {
                        String result = responseJson.getString("status");
                        if (result.equals("success") && responseJson.getJSONArray("tradeRequests").length() > 0){
                            setRequests(responseJson);
                            adapter = new RequestsSentViewAdapter(RequestsSentActivity.this, R.layout.requestsentitem_listview, requestArrayList);
                            listView.setAdapter(adapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.v("fetch requests success", "in post execute");
                }
            }
        }
    }

    private void setRequests(JSONObject jsonObject){
        try {
            JSONArray listOfRequests = jsonObject.getJSONArray("tradeRequests");
            if(listOfRequests.length() > 0)
                requestArrayList = new ArrayList<Request>();
            for(int i = 0; i< listOfRequests.length(); i++){
                JSONObject request = (JSONObject) listOfRequests.get(i);
                int reqId = (request.has("id"))?Integer.parseInt(request.get("id").toString()):null;
                int fromUserId = (request.has("fromUserId"))?Integer.parseInt(request.get("fromUserId").toString()):null;
                int toUserId = (request.has("toUserId"))?Integer.parseInt(request.get("toUserId").toString()):null;
                String status = (request.has("status"))?request.get("status").toString():null;
                String acceptorWantsBook = (request.has("acceptorWantsBook"))?request.get("acceptorWantsBook").toString():null;
                String requestorWantsBook = (request.has("requestorWantsBook"))?request.get("requestorWantsBook").toString():null;
                Request r = new Request(reqId, fromUserId, toUserId, status, acceptorWantsBook, requestorWantsBook, "");
                requestArrayList.add(r);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            Log.v("Null pointer exc", "in setRequest");
            e.printStackTrace();
        }
    }
}
