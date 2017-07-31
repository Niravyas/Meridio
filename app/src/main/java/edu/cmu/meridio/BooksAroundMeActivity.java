package edu.cmu.meridio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.Locale;

public class BooksAroundMeActivity extends BaseActivity implements LocationListener {

    private ListView listView;
    private ArrayList<String> stringArrayList;
    private ArrayList<String> isbnArrayList;
    private ArrayList<String> bookidArrayList;
    private ArrayAdapter<Book> adapter;
    private ArrayList<Book> bookArrayList;
    Location lastKnownLocation;
    private LocationManager mLocationManager;
    User fbUser;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        if(bookArrayList != null && bookArrayList.size() > 0) {
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
                Collections.sort(bookArrayList);
                adapter.notifyDataSetChanged();
                break;
            case R.id.sort_desc:
                Collections.sort(bookArrayList, Collections.reverseOrder());
                adapter.notifyDataSetChanged();
                break;
        }
        return true;
    }

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

        if(lastKnownLocation == null){
            Log.v("battery draining", "code in action");
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            try {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        ISBNActivity.LOCATION_INTERVAL, ISBNActivity.LOCATION_DISTANCE, BooksAroundMeActivity.this);
            } catch (java.lang.SecurityException ex) {
                Log.v("requestLocation", "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.v("requestLocation", "gps provider does not exist " + ex.getMessage());
            } catch (Exception e){
                Log.v("other exception caught", e.getMessage());
            }
        }

        if(lastKnownLocation == null){
            Log.v("lastknownlocation", "is still null");
            setCMULocation();
        } else
            Log.v("lsatknownLocation", lastKnownLocation.toString());
            getData();
           // setData();




    }

    private void getData(){

        Log.i("Tag", "Then herer");
        new getBookData().execute();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastKnownLocation.set(location);
        Log.v("onlocationChanged", "true");
        Log.v("new location", location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(BooksAroundMeActivity.this, "Please enable location service", Toast.LENGTH_SHORT).show();
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
                    bookArrayList = new ArrayList<Book>();

                    stringArrayList = new ArrayList<String>();
                    isbnArrayList = new ArrayList<String>();
                    bookidArrayList = new ArrayList<String>();
                    JSONArray reqArray = responseJson.getJSONArray("books");
                    for(int i = 0; i<reqArray.length(); i++){

                        JSONObject book = reqArray.getJSONObject(i);
                        //create new book object
                        Book b = new Book(Integer.parseInt(book.getString("id")),
                                book.getString("user_id"),
                                book.getString("isbn"),
                                book.getString("title"),
                                book.getString("genre"),
                                book.getString("description"),
                                book.getString("image_url"));
                        bookArrayList.add(b);
                        stringArrayList.add(book.getString("title"));
                        isbnArrayList.add(book.getString("isbn"));
                        bookidArrayList.add(book.getString("id"));
                    }

                    String a = " " + stringArrayList.size();
                    Log.i("size", a);
                    adapter = new BooksAroundMeViewAdapter(BooksAroundMeActivity.this, R.layout.booksaroundmeitem_listview, bookArrayList);

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

                          i.putExtra("isbn", adapter.getItem(position).getIsbn());
                          i.putExtra("bookId", String.valueOf(adapter.getItem(position).getId()));
                          i.putExtra("title", String.valueOf(adapter.getItem(position).getTitle()));
                          i.putExtra("genre", String.valueOf(adapter.getItem(position).getGenre()));
                          i.putExtra("description", String.valueOf(adapter.getItem(position).getDescription()));
                          i.putExtra("imageUrl", adapter.getItem(position).getImageUrl());
                          startActivity(i);
                      }
                  });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //stop requesting location updates
            if(mLocationManager != null)
                mLocationManager.removeUpdates(BooksAroundMeActivity.this);
        }
    }

    public void setCMULocation(){
        lastKnownLocation = new Location("");
        lastKnownLocation.setLongitude(new Double(-79.947100));
        lastKnownLocation.setLatitude(new Double(40.441688));
    }
}
