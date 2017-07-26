package edu.cmu.meridio;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.List;

/**
 * Created by yadav on 7/24/2017.
 */

public class RequestsReceivedViewAdapter extends ArrayAdapter {
    private AppCompatActivity activity;
    private List<Request> requestList;

    public RequestsReceivedViewAdapter(AppCompatActivity context, int resource, List<Request> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.requestList = objects;
    }

    @Override
    public int getCount() {
        return requestList.size();
    }

    @Override
    public Request getItem(int position) {
        return requestList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RequestsReceivedViewAdapter.ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // If holder not exist then locate all view from UI file.
        if (convertView == null) {
            // inflate UI from XML file
            convertView = inflater.inflate(R.layout.requestreceiveditem_listview, parent, false);
            // get all UI view
            holder = new RequestsReceivedViewAdapter.ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        } else {
            // if holder created, get tag from view
            holder = (RequestsReceivedViewAdapter.ViewHolder) convertView.getTag();
        }

        holder.bookTitle.setText(Integer.toString(getItem(position).getFromUserID()));
//        holder.bookTitle.setText(getItem(position).getRequestorWantsBook());
        //get first letter of each String item
        String firstLetter = String.valueOf(Integer.toString(getItem(position).getFromUserID()).charAt(0));

        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color = generator.getColor(getItem(position));

        TextDrawable drawable = TextDrawable.builder()
                .buildRound(firstLetter, color); // radius in px

        holder.imageView.setImageDrawable(drawable);

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
