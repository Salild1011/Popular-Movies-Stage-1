package in.net.codestar.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportFragmentManager().findFragmentByTag(getString(R.string.detail_frag_tag)) == null) {
            MovieDetails movie = new MovieDetails();
            getSupportFragmentManager().beginTransaction().add(R.id.container,
                    movie, getString(R.string.detail_frag_tag)).commit();
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class MovieDetails extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            HashMap<String, String> details;
            MovieParcel parcel = null;

            if (intent != null && intent.hasExtra(getString(R.string.movie_parcel))) {
                parcel = intent.getExtras().getParcelable(getString(R.string.movie_parcel));
            }

            if (parcel != null) {
                details = parcel.getHash();

                ImageView poster = (ImageView) view.findViewById(R.id.imageid);
                RatingBar rating = (RatingBar) view.findViewById(R.id.ratingBar);

                Picasso.with(getContext()).load(details.get("Backdrop Link")).into(poster);
                rating.setRating(Float.parseFloat(details.get("Rating")));

                TextView movieDetails = (TextView) view.findViewById(R.id.movieText);
                TextView movieRate = (TextView) view.findViewById(R.id.rateText);

                movieRate.setText(Html.fromHtml("<h4>(" + details.get("Rating") + " / 10)</h4>"));
                movieDetails.setText(Html.fromHtml(
                        "<h1>" + details.get("Movie Name") + "</h1>"
                        + "<h4>Release: " + details.get("Release Date")
                        + "</h4>" + "<h4>Synopsis:</h4>" + details.get("Synopsis")
                ));
            }

            return view;
        }
    }

}
