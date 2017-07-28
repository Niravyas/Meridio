package edu.cmu.meridio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class BooksAroundMeActivity extends BaseActivity {

    private ListView listView;
    private ArrayList<String> stringArrayList;
    private ArrayList<String> isbnArrayList;
    private ArrayList<String> bookidArrayList;
    private ArrayAdapter<String> adapter;
    Location lastKnownLocation;
    User fbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_around_me);

        listView = (ListView) findViewById(R.id.list_book);
        String gpsProvider = LocationManager.GPS_PROVIDER;
        fbUser = User.getInstance();
        Log.i("Tag", "Camehere");
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions here to request the missing permissions, and then overriding
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
            lastKnownLocation = locationManager.getLastKnownLocation(gpsProvider);
            getData();
           // setData();




    }

    private void setData() {
        stringArrayList = new ArrayList<>();
        stringArrayList.add("Harry Potter and Sorcerer's Stone");
        stringArrayList.add("Harry Potter and Chamber of Secrets");
        stringArrayList.add("Game of Thrones");
        stringArrayList.add("Feast for Crows");
        stringArrayList.add("A Suitable Boy");
        stringArrayList.add("Satanic Verses");
        stringArrayList.add("Winds of Winter");
        stringArrayList.add("Foutainhead");
        stringArrayList.add("A Brief History of Time");
        stringArrayList.add("And the Mountains Echoed");
        stringArrayList.add("Kafka on the Shore");
        stringArrayList.add("American Psycho");
    }

    private void getData(){

        Log.i("Tag", "Then herer");
        new getBookData().execute();
    }

    private class getBookData extends AsyncTask<String, String, JSONObject> {
        /*String body;
        getBookData(String body) {this.body = body;}*/

        @Override
        protected JSONObject doInBackground(String... strings) {
            // Create URL
            try {
                Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

                Double latitude = lastKnownLocation.getLatitude();
                Double longitude = lastKnownLocation.getLongitude();

                URL myEndpoint = new URL("http://ec2-54-85-207-189.compute-1.amazonaws.com:4000/booksAroundMe");

                HttpURLConnection myConnection
                        = (HttpURLConnection) myEndpoint.openConnection();

                myConnection.setRequestMethod("POST");


                myConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                myConnection.setRequestProperty("Accept","application/json");
                myConnection.setDoOutput(true);
                myConnection.setDoInput(true);

                String body = "{"
                        + "\"userId\": \"" + fbUser.getUserID() + "\""
                        + ",\"latitude\": \"" + latitude + "\""
                        + ",\"longitude\": \"" + longitude + "\""
                        + "}";

                byte[] outputInBytes = body.getBytes("UTF-8");
                OutputStream os = myConnection.getOutputStream();
                os.write(outputInBytes);
                os.close();


                Log.v("setuserreq body", body);
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

                    stringArrayList = new ArrayList<String>();
                    isbnArrayList = new ArrayList<String>();
                    bookidArrayList = new ArrayList<String>();
                    JSONArray reqArray = responseJson.getJSONArray("books");
                    for(int i = 0; i<reqArray.length(); i++){

                        JSONObject book = reqArray.getJSONObject(i);
                        stringArrayList.add(book.getString("title"));
                        isbnArrayList.add(book.getString("isbn"));
                        bookidArrayList.add(book.getString("id"));
                    }

                    String a = " " + stringArrayList.size();
                    Log.i("size", a);
                    adapter = new BookViewAdapter(BooksAroundMeActivity.this, R.layout.bookitem_listview, stringArrayList);

                   listView.setAdapter(adapter);
                  /*  String result = responseJson.getString("status");
                    Log.v("result in postexecute", result);
                    if (result.equals("success")){
                        Log.v("session API success", "result");
                        Log.v("responseJson", responseJson.toString());

                        }*/

                  listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                      @Override
                      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                          //TODO
                          // call books available with requestor w/ {"userId":getItem(position).getFromUsrId()
                        //Log.v("books avlblwReq", String.valueOf(adapter.getItem(position).getFromUserID()));
                          Intent i  = new Intent(BooksAroundMeActivity.this, SendTradeRequestActivity.class);
                          Bundle extras = new Bundle();
                          extras.putString("isbn", String.valueOf(isbnArrayList.get(position)));
                          extras.putString("bookId", String.valueOf(bookidArrayList.get(position)));
                         // extras.putString(REQUESTID, String.valueOf(adapter.getItem(position).getId()));
                          Log.v("isbn put in extra", isbnArrayList.get(position));
                         // Log.v("reqID put in extra", extras.getString(REQUESTID));
                          i.putExtra("isbn", isbnArrayList.get(position));
                          i.putExtra("bookId", bookidArrayList.get(position));
                         // i.putExtra(REQUESTID, String.valueOf(adapter.getItem(position).getId()));
                          startActivity(i);
                      }
                  });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
