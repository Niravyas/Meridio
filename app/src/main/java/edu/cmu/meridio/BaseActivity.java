package edu.cmu.meridio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by yadav on 7/22/2017.
 */

public class BaseActivity extends AppCompatActivity {
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
                startActivity(i);
            case R.id.action_req_from_me:
                i = new Intent(getApplicationContext(), MyRequestsActivity.class);
                startActivity(i);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
