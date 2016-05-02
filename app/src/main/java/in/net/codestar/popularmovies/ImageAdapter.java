package in.net.codestar.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by salil on 22-04-2016.
 */

public class ImageAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private String[] url_arr;

    public ImageAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        mContext = context;
        url_arr = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.image_style, null);
        }

        imageView = (ImageView) convertView;
        Picasso.with(mContext).load(Uri.parse(url_arr[position])).into(imageView);

        return imageView;
    }
}
