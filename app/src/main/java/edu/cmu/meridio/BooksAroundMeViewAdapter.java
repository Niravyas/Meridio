package edu.cmu.meridio;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.ConnectivityManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yadav on 7/28/2017.
 */

public class BooksAroundMeViewAdapter extends ArrayAdapter<Book> {
    private Activity activity;
    private List<Book> bookList;
    private ConnectivityManager mConnectivityManager = null;
    ProgressDialog mProgress;

    public BooksAroundMeViewAdapter(Activity context, int resource, ArrayList<Book> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.bookList = objects;
    }

    @Override
    public int getCount() {
        return bookList.size();
    }

    @Override
    public Book getItem(int position) {
        return bookList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // If holder not exist then locate all view from UI file.
        if (convertView == null) {
            // inflate UI from XML file
            convertView = inflater.inflate(R.layout.booksaroundmeitem_listview, parent, false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        } else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bookTitle.setText(getItem(position).getTitle());

        //get first letter of each String item
        String firstLetter = String.valueOf(getItem(position).getTitle().charAt(0));

        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color = generator.getColor(getItem(position));

        TextDrawable drawable = TextDrawable.builder()
                .buildRound(firstLetter, color); // radius in px
        Picasso.with(getContext()).load(getItem(position).getImageUrl()).into(holder.imageView);
//        holder.imageView.setImageDrawable(drawable);

        return convertView;
    }

    private class ViewHolder {
        private ImageView imageView;
        private TextView bookTitle;

        public ViewHolder(View v) {
            imageView = (ImageView) v.findViewById(R.id.book_image);
            bookTitle = (TextView) v.findViewById(R.id.book_title);
        }
    }
}
