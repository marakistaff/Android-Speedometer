package android.google.com.androidspeedometer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

public class TripResult extends AppCompatActivity implements View.OnClickListener
{

  String start_address = "", stop_address = "";

  double newSpeedInMPS, currSpeed;
  int newTime;
  double totDistance;

  String mPreference, distancePreference = " meters";
  double startLat, startLng, stopLat, stopLng;

  String startDate, stopDate;
  String startLatLng, stopLatLng;

  final static double MILES = 0.000621371;
  final static double KILOMETERS = 0.001;

  final static double MPH = 2.23694;
  final static double KPH = 3.6;

  private static String urlString;

  TextView startAddress, stopAddress;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_trip_result);

    //Reading from SharedPreferences - ms , mph , kph
    SharedPreferences pref = getApplicationContext().getSharedPreferences("speedPref", MODE_PRIVATE);
    mPreference = pref.getString("pref_type", null);

    Intent intent = getIntent();
    @SuppressWarnings("unchecked")
    HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra("map");

    startDate = hashMap.get("start_time");
    stopDate = hashMap.get("stop_time");


    startLat = Double.valueOf(hashMap.get("start_lat"));
    startLng = Double.valueOf(hashMap.get("start_lng"));
    startLatLng = startLat + "," + startLng;
    urlString = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + startLatLng;
    new GetAddresses().execute();

    stopLat = Double.valueOf(hashMap.get("stop_lat"));
    stopLng = Double.valueOf(hashMap.get("stop_lng"));
    stopLatLng = stopLat + "," + stopLng;
    urlString = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + stopLatLng;
    new GetAddresses().execute();

    currSpeed = Double.valueOf(hashMap.get("avg_speed"));
    newSpeedInMPS = utils.getSpeedInMeters(mPreference, currSpeed);

    newTime = utils.getTimeInSeconds(startDate, stopDate);


    String xxx = utils.getDurationString(newTime);
    double distance = utils.getDistanceInMPS(newSpeedInMPS, newTime);

    switch (mPreference)
    {
      case "mph":
        totDistance = distance * MILES;
        newSpeedInMPS = newSpeedInMPS * MPH;
        distancePreference = " miles";
        break;
      case "kph":
        totDistance = distance * KILOMETERS;
        newSpeedInMPS = newSpeedInMPS * KPH;
        distancePreference = " kilometers";
        break;
      default:
        totDistance = distance;
        break;
    }

    TextView startTime = (TextView) findViewById(R.id.results_start_time);
    startTime.setText(startDate);

    startAddress = (TextView) findViewById(R.id.results_start_location);

    TextView stopTime = (TextView) findViewById(R.id.results_end_time);
    stopTime.setText(stopDate);

    stopAddress = (TextView) findViewById(R.id.results_end_location);

    TextView totalTime = (TextView) findViewById(R.id.results_total_time);
    totalTime.setText(xxx);

    ImageButton buttonStart = (ImageButton) findViewById(R.id.imageButton1);
    buttonStart.setOnClickListener(this);
    ImageButton buttonStop = (ImageButton) findViewById(R.id.imageButton2);
    buttonStop.setOnClickListener(this);

    TextView maxSpeed = (TextView) findViewById(R.id.results_max_speed);
    String max1 = hashMap.get("max_speed") + " " + mPreference;
    maxSpeed.setText(max1);

    TextView avgSpeed = (TextView) findViewById(R.id.results_avg_speed);
    String avg1 = hashMap.get("avg_speed") + " " + mPreference;
    avgSpeed.setText(avg1);

    TextView totalDisance = (TextView) findViewById(R.id.results_distance);
    String mDistance = String.format(Locale.getDefault(), "%.2f", totDistance) + distancePreference;
    totalDisance.setText(mDistance);

  }


  @Override
  public void onClick(View view)
  {
    switch (view.getId())
    {
      case R.id.imageButton1:
        openMap(startLat, startLng);
        break;

      case R.id.imageButton2:
        openMap(stopLat, stopLng);
        break;

      default:
        break;
    }
  }



  // Opens Google maps
  public void openMap(double lat, double lng)
  {
    Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng);
    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
    mapIntent.setPackage("com.google.android.apps.maps");
    if (mapIntent.resolveActivity(getPackageManager()) != null)
    {
      startActivity(mapIntent);
    }
  }








  private class GetAddresses extends AsyncTask<Void, Void, Void>
  {

    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onPreExecute()
    {
      Log.w("onPreExecute", "");
    }

    @Override
    protected Void doInBackground(Void... arg0)
    {

      try
      {

        JSONObject json = jsonParser.makeHttpRequest(urlString);

        if (json != null)
        {
          JSONArray contacts = json.getJSONArray("results");
          JSONObject c = contacts.getJSONObject(0);
          String foundAddress = c.getString("formatted_address");

          Log.w("foundAddress ",""+foundAddress);

          if(foundAddress.equals(""))
          {
            foundAddress = "ADDRESS NOT FOUND";
          }

          if (start_address.equals(""))
          {
            start_address = foundAddress;

            Log.w("start_address ",""+start_address);

            setAddressText("start");

          } else
          {
            stop_address = foundAddress;
            Log.w("stop_address ",""+stop_address);
            setAddressText("stop");
          }
        }

      } catch (Exception e)
      {
        e.printStackTrace();
      }

      return null;
    }

    protected void onPostExecute(Void result)
    {

    }
  }

  public void setAddressText(final String foo)
  {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {

        if(foo.equals("start"))
        {
          startAddress.setText(start_address);
        }
        else
        {
          stopAddress.setText(stop_address);
        }

      }
    });


  }
}