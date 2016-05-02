package in.net.codestar.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view of grid of movie posters
 */

public class MainActivityFragment extends Fragment {

    private GridView moviesGrid;
    private String[] movieName, posterLinks, overview, rating, release, url_arr;
    private static String sortPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        moviesGrid = (GridView) view.findViewById(R.id.gridView);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        sortPref = pref.getString(getString(R.string.sort_key), getString(R.string.pref_sort_default));


        LoadMovies loadMovies = new LoadMovies();
        loadMovies.execute(sortPref);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String str = pref.getString(getString(R.string.sort_key), getString(R.string.pref_sort_default));

        if (!str.equals(sortPref)) {
            sortPref = str;
            LoadMovies loadMovies = new LoadMovies();
            loadMovies.execute(sortPref);
        }
    }

    class LoadMovies extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = getClass().getSimpleName();
        private final String BASE_URL = "http://api.themoviedb.org/3/movie";
        private final String POPULAR = "/popular";
        private final String TOP_RATED = "/top_rated";
        private final String BASE_URL_IMAGE = "http://image.tmdb.org/t/p";
        private final String SIZE_POSTER = "/w342";
        private final String SIZE_BACKDROP = "/w342";
        private final String API_KEY = "?api_key="; //Insert API Key here
        private URL listUrl;
        private StringBuilder builder;

        @Override
        protected String[] doInBackground(String... params) {

            //Fetch JSON Object, get JSON array inside, get Movie pictures
            try {
                if (params[0].equals(getString(R.string.pref_sort_popularity))) {
                    listUrl = new URL(BASE_URL + POPULAR + API_KEY);
                }
                else {
                    listUrl = new URL(BASE_URL + TOP_RATED + API_KEY);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                HttpURLConnection connection = (HttpURLConnection) listUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                if (inputStream == null) {
                    Log.w(LOG_TAG, "InputStream is null");
                    return null;
                }

                builder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                String str;

                while ((str = br.readLine()) != null) {
                    builder.append(str);
                }

                if (builder == null | builder.length() == 0) {
                    Log.w(LOG_TAG, "No String Received");
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String jsonStr = builder.toString();

            try {
                JSONObject resObj = new JSONObject(jsonStr);
                JSONArray movieArr = resObj.getJSONArray("results");
                url_arr = new String[movieArr.length()];

                movieName = new String[movieArr.length()];
                posterLinks = new String[movieArr.length()];
                overview = new String[movieArr.length()];
                rating = new String[movieArr.length()];
                release = new String[movieArr.length()];

                JSONObject movieObj;

                //Compile a string array of image links
                for (int i = 0; i < movieArr.length(); i++) {
                    movieObj = movieArr.getJSONObject(i);
                    String imageLink = movieObj.getString("poster_path");

                    builder = new StringBuilder();
                    builder.append(BASE_URL_IMAGE).append(SIZE_POSTER).append(imageLink);
                    url_arr[i] = builder.toString();

                    //Save moviename, poster link, overview, vote_avg, release date
                    movieName[i] = movieObj.getString("original_title");

                    builder = new StringBuilder();
                    builder.append(BASE_URL_IMAGE).append(SIZE_BACKDROP)
                            .append(movieObj.getString("backdrop_path"));
                    posterLinks[i] =  builder.toString();

                    overview[i] = movieObj.getString("overview");
                    rating[i] = movieObj.getString("vote_average");
                    release[i] = movieObj.getString("release_date");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return url_arr;
        }

        @Override
        protected void onPostExecute(String[] s) {
            if (s != null) {
                moviesGrid.setAdapter(new ImageAdapter(getContext(), R.layout.image_style, url_arr));
                moviesGrid.setOnItemClickListener(new onMovieClickListener());
            }
        }
    }

    class onMovieClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            MovieParcel movieParcel = new MovieParcel(movieName[position],
                    posterLinks[position], overview[position], rating[position], release[position]);

            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra(getString(R.string.movie_parcel), movieParcel);

            startActivity(intent);
        }
    }
}

class MovieParcel implements Parcelable {
    private String name, link, synopsis, avg, rel_date;

    public MovieParcel(String name, String link, String synopsis, String avg, String rel_date) {
        this.name = name;
        this.link = link;
        this.synopsis = synopsis;
        this.avg = avg;
        this.rel_date = rel_date;
    }

    public MovieParcel(Parcel source) {
        name = source.readString();
        link = source.readString();
        synopsis = source.readString();
        avg = source.readString();
        rel_date = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(link);
        dest.writeString(synopsis);
        dest.writeString(avg);
        dest.writeString(rel_date);
    }

    public static final Parcelable.Creator<MovieParcel> CREATOR = new Parcelable.Creator<MovieParcel>() {
        @Override
        public MovieParcel createFromParcel(Parcel source) {
            return new MovieParcel(source);
        }

        @Override
        public MovieParcel[] newArray(int size) {
            return new MovieParcel[size];
        }
    };

    public HashMap<String, String> getHash() {
        HashMap<String, String> details = new HashMap<>();
        details.put("Movie Name", name);
        details.put("Backdrop Link", link);
        details.put("Synopsis", synopsis);
        details.put("Rating", avg);
        details.put("Release Date", rel_date);

        return details;
    }
}
