package edu.cmu.meridio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.facebook.login.LoginManager;

/**
 * Created by yadav on 7/22/2017.
 */

public class BaseActivity extends AppCompatActivity {
    public static final String LOGOUTUSER = "logoutUser";
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i ;
        switch (item.getItemId()) {
            case R.id.action_library:
                i = new Intent(getApplicationContext(), LibraryActivity.class);
                startActivity(i); break;
            case R.id.action_req_from_me:
                i = new Intent(getApplicationContext(), RequestsSentActivity.class);
                startActivity(i); break;
            case R.id.action_req_to_me:
                i = new Intent(getApplicationContext(), RequestsReceivedActivity.class);
                startActivity(i); break;
            case R.id.action_logout:
                LoginManager.getInstance().logOut();
                i = new Intent(getApplicationContext(), LoginActivity.class);
                i.putExtra(LOGOUTUSER, true);
                startActivity(i); break;
        }
        return super.onOptionsItemSelected(item);
    }
}
