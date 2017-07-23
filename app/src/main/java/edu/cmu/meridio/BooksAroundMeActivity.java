package edu.cmu.meridio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class BooksAroundMeActivity extends BaseActivity {

    private ListView listView;
    private ArrayList<String> stringArrayList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_around_me);

        listView = (ListView) findViewById(R.id.list_book);
        setData();
        adapter = new BookViewAdapter(this, R.layout.bookitem_listview, stringArrayList);
        listView.setAdapter(adapter);
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
}
