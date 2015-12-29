package com.clothapp;

import android.content.SharedPreferences;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.Intent;


public class SplashScreen extends AppCompatActivity {

    //  ms to wait for the splash screen
    private final int TIME_TO_WAIT = 48000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen);

        /**
         * Showing splashscreen while making network calls to download necessary
         * data before launching the app Will use AsyncTask to make http call
         */
        //  faking the fetching phase


        //  calling the class to fetch the images
        new PrefetchData().execute();
        System.out.println("debug: finito prefetch data");
        Runnable r = new Runnable(){
            public void run(){
                try{
                    Thread.sleep(TIME_TO_WAIT);
                    System.out.println("debug: finito di aspettare");
                }
                catch (InterruptedException e){
                    System.out.println("debug: eccezioe");
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(r);
        System.out.println("debug: sto per fare .start");
        t.start();

        System.out.println("debug: finito sto per passare altra activity");

        // After completing http call
        // will close this activity and lauch homepage activity
        Intent i = new Intent(getApplicationContext(), Homepage.class);
        SharedPreferences userInformation = getSharedPreferences(getString(R.string.info), MODE_PRIVATE);
        startActivity(i);

        // close this activity
        finish();

    }

    /**
     * Async Task to make http call
     */
    private class PrefetchData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making http calls

        }

        //TODO  FETCHING AN ARRAY OF PICS FROM DB
        @Override
        protected Void doInBackground(Void... arg0) {
            /*
             * Will make http call here This call will download required data
             * before launching the app
             * example:
             * 1. Downloading and storing in SQLite
             * 2. Downloading images
             * 3. Fetching and parsing the xml / json
             * 4. Sending device information to server
             * 5. etc.,
             */

          /*
            JsonParser jsonParser = new JsonParser();
            String json = jsonParser
                    .getJSONFromUrl("http://api.androidhive.info/game/game_stats.json");

            Log.e("Response: ", "> " + json);

            if (json != null) {
                try {
                    JSONObject jObj = new JSONObject(json)
                            .getJSONObject("game_stat");
                    now_playing = jObj.getString("now_playing");
                    earned = jObj.getString("earned");

                    Log.e("JSON", "> " + now_playing + earned);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }*/
            System.out.println("Debug: I just downloaded all the images from the server!!");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


        }

    }

}

