package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {

    // Store time when user clicked button
    long prevTimeClickBangkok = 0;
    long prevTimeClickNontha = 0;
    long prevTimeClickPathum = 0;
    String currentCountry = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    public void buttonClicked(View v) {

        long timeMillis = System.currentTimeMillis();
        long getTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);

        System.out.println("time = " + getTimeSeconds);
        System.out.println("bkk clicked = " + prevTimeClickBangkok);
        System.out.println("pathum clicked = " + prevTimeClickPathum);
        System.out.println("nonta clicked = " + prevTimeClickNontha);
        System.out.println("count = " + currentCountry);

        int id = v.getId();
        WeatherTask w = new WeatherTask();
        switch (id) {
            case R.id.btBangkok:
                if(((getTimeSeconds - prevTimeClickBangkok) > 60))
                {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                    prevTimeClickBangkok = getTimeSeconds;
                    prevTimeClickPathum = 0;
                    prevTimeClickNontha = 0;
                }
                break;
            case R.id.btPathum:
                if(((getTimeSeconds - prevTimeClickPathum) > 60))
                {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                    prevTimeClickPathum = getTimeSeconds;
                    prevTimeClickBangkok = 0;
                    prevTimeClickNontha = 0;
                }
                break;
            case R.id.btNon:
                if(((getTimeSeconds - prevTimeClickNontha) > 60))
                {
                    w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                    prevTimeClickNontha = getTimeSeconds;
                    prevTimeClickPathum = 0;
                    prevTimeClickBangkok = 0;
                }
                break;
        }

//        System.out.println("Current time: " + System.currentTimeMillis());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;
        String weather;

        double windSpeed, temp, tempMin, tempMax;
        int humidity;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());

                    // Get weather
                    JSONArray jWeathArr = jWeather.getJSONArray("weather");
                    JSONObject jWeath = jWeathArr.getJSONObject(0);
                    weather = jWeath.getString("main");

                    // Get wind speed
                    JSONObject jWind = jWeather.getJSONObject("wind");
                    windSpeed = jWind.getDouble("speed");

                    // Get humidity
                    JSONObject jMain = jWeather.getJSONObject("main");
                    humidity = jMain.getInt("humidity");

                    // Get temperature
                    temp = jMain.getDouble("temp");
                    tempMin = jMain.getDouble("temp_min");
                    tempMax = jMain.getDouble("temp_max");

                    // Get which country's weather info it is for
                    currentCountry = jWeather.getString("name");

                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle, tvWeather, tvWind, tvHumid, tvTemp;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvWind = (TextView)findViewById(R.id.tvWind);
            tvHumid = (TextView)findViewById(R.id.tvHumid);
            tvTemp = (TextView)findViewById(R.id.tvTemp);

            if (result) {
                tvTitle.setText(title);
                tvWeather.setText(weather);
                tvTemp.setText(String.format("%.2f (max = %.2f, min = %.2f)", temp, tempMax, tempMin));
                tvWind.setText(String.format("%.1f", windSpeed));
                tvHumid.setText(String.format("%d%%", humidity));
            }
            else {
                tvTitle.setText(errorMsg);
                tvTemp.setText("");
                tvWeather.setText("");
                tvWind.setText("");
                tvHumid.setText("");
            }
        }
    }
}
