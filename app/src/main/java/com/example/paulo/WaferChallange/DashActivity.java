package com.example.paulo.WaferChallange;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.paulo.WaferChallange.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;


import static android.app.PendingIntent.getActivity;

public class DashActivity extends AppCompatActivity {

    //Declaration of variables
    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private static String url = "https://restcountries.eu/rest/v2/all";
    List<Country> countryList = new LinkedList<>();
    RecyclerView recyclerView;
    CustomAdapter cAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        new GetCountries(this).execute();


        RecyclerView.LayoutManager layout = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layout);

        //Method to handle the view on Item
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            public static final float ALPHA_FULL = 1.0f;


            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    float translationX = 0;
                    View itemView = viewHolder.itemView;
                    Paint p = new Paint();
                    Bitmap icon;

                    if (dX > 0) {

                        viewHolder.itemView.setTranslationX(-translationX);

                        p.setARGB(255, 238,130,238);
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                (float) itemView.getBottom(), p);

                        if (dX > 100) {
                            icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.bomb);

                            c.drawBitmap(icon,
                                    (float) itemView.getLeft() + convertDpToPx(16),
                                    (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                                    p);
                        }
                    }

                    else if (dX<0){

                        p.setARGB(255, 238,130,238);
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);

                        icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.bomb);
                        if (dX < -100) {
                            c.drawBitmap(icon,
                                    (float) itemView.getRight() - convertDpToPx(16) - icon.getWidth(),
                                    (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                                    p);
                            icon.recycle();
                        }

                    }

                    super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
                    final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);


                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            private int convertDpToPx(int dp) {
                return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
            }

            //Method to handle Swipe Gesture
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {
                    countryList.remove(position);
                    cAdapter.notifyItemRemoved(position);

                }

                else if (direction == ItemTouchHelper.RIGHT) {

                   countryList.remove(position);
                    cAdapter.notifyItemRemoved(position);
                }

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

        //Method that get Json from URL
        private class GetCountries extends AsyncTask<Void, Void, Void> {

        private Context mContext;

        public GetCountries(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(DashActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONArray countries = new JSONArray(jsonStr);
                    for (int i = 0; i < countries.length(); i++) {

                        JSONObject c = countries.getJSONObject(i);
                        String countryName = c.getString("name");
                        JSONArray currency = c.getJSONArray("currencies");
                        JSONObject cur = currency.getJSONObject(0);
                        String currencyName = cur.getString("name");
                        JSONArray language = c.getJSONArray("languages");
                        JSONObject lan = language.getJSONObject(0);
                        String languageName = lan.getString("name");


                        Country country1 = new Country(countryName, currencyName, languageName);

                        // adding Country to CountyList
                        countryList.add(country1);

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server, check you connection",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        //Call Adapter
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            cAdapter = new CustomAdapter(countryList,mContext);
            recyclerView.setAdapter(cAdapter);

        }

    }

    //Custom View Holder to the item
    public class CustomViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView curr;
        final TextView lang;

        public CustomViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.list_item_name);
            curr = (TextView) view.findViewById(R.id.list_item_currency);
            lang = (TextView) view.findViewById(R.id.list_item_language);
        }

    }

    //Custom Adpter to populate the items from the list
    public class CustomAdapter extends RecyclerView.Adapter {

        private List<Country> countries;

        private Context context;

        public CustomAdapter(List<Country> countries, Context contex) {
            this.countries = countries;
            this.context = contex;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.list_item, parent, false);
            CustomViewHolder holder = new CustomViewHolder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            CustomViewHolder holder = (CustomViewHolder) viewHolder;
            Country country  = countries.get(position) ;
            holder.name.setText(country.getCountryName());
            holder.curr.setText(country.getCurrency());
            holder.lang.setText(country.getLanguage());
        }

        @Override
        public int getItemCount() {
            return countries.size();
        }
    }
}