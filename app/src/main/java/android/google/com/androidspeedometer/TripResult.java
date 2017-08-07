package android.google.com.androidspeedometer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TripResult extends AppCompatActivity implements View.OnClickListener
{

  double xxSpeed;
  double newSpeed, newTime;
  double totDistance;
  HashMap<String, String> hashMap;
  String mPreference;
  double startLat, startLng, stopLat, stopLng;

  final static double MS = 0.44704;
  final static double MPH = 0.27777;
  final static double KPH = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_trip_result);

    //Reading from SharedPreferences - ms , mph , kph
    SharedPreferences pref = getApplicationContext().getSharedPreferences("speedPref", MODE_PRIVATE);
    mPreference = pref.getString("pref_type", null);

    Intent intent = getIntent();

    hashMap = (HashMap<String, String>) intent.getSerializableExtra("map");

    startLat = Double.valueOf(hashMap.get("start_lat"));
    startLng = Double.valueOf(hashMap.get("start_lng"));
    stopLat = Double.valueOf(hashMap.get("stop_lat"));
    stopLng = Double.valueOf(hashMap.get("stop_lng"));

    xxSpeed = Double.valueOf(hashMap.get("avg_speed"));

    newSpeed = getSpeedInMeters();
    try
    {
      newTime = getTimeInSeconds();
    } catch (ParseException e)
    {
      e.printStackTrace();
    }

    getDistanceInMPS(newSpeed, newTime);

    TextView startTime = (TextView) findViewById(R.id.results_start_time);
    startTime.setText(hashMap.get("start_time"));

    TextView startAddress = (TextView) findViewById(R.id.results_start_location);
    startAddress.setText(hashMap.get("start_address"));

    TextView stopTime = (TextView) findViewById(R.id.results_end_time);
    stopTime.setText(hashMap.get("stop_time"));

    TextView stopAddress = (TextView) findViewById(R.id.results_end_location);
    stopAddress.setText(hashMap.get("stop_address"));

    TextView totalTime = (TextView) findViewById(R.id.results_total_time);
    totalTime.setText(hashMap.get("total_time"));

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



    TextView distance = (TextView) findViewById(R.id.results_distance);
    distance.setText(String.valueOf(utils.getDistance(startLat, startLng, stopLat, stopLng)));
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

  public double getSpeedInMeters()
  {
    double speedInMeters;
    double currSpeed = Double.valueOf(hashMap.get("avg_speed"));

    switch (mPreference)
    {
      case "mph":
        speedInMeters = currSpeed * MPH;
        break;
      case "kph":
        speedInMeters = currSpeed * KPH;
        break;
      default:
        speedInMeters = currSpeed;
        break;
    }
    return speedInMeters;
  }

  public double getTimeInSeconds() throws ParseException
  {
    long seconds = 0;

    String dateStart = hashMap.get("start_time");
    String dateStop = hashMap.get("stop_time");

    //HH converts hour in 24 hours format (0-23), day calculation
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    Date d1 = null;
    Date d2 = null;

    try
    {
      d1 = format.parse(dateStart);
      d2 = format.parse(dateStop);

      //in milliseconds
      long diff = d2.getTime() - d1.getTime();
      seconds = TimeUnit.MILLISECONDS.toSeconds(diff);

      Log.w("new ", "secs" + seconds);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
      return (double) seconds;

  }
  public void getDistance()
  {

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


  // distance = speed X time

 /// ms   mph   kph

  public void doCalcs()
  {

  }

  public void doTime() throws ParseException
  {
    String dateStart = hashMap.get("start_time");
    String dateStop = hashMap.get("stop_time");

    //HH converts hour in 24 hours format (0-23), day calculation
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    Date d1 = null;
    Date d2 = null;

    try {
      d1 = format.parse(dateStart);
      d2 = format.parse(dateStop);

      //in milliseconds
      long diff = d2.getTime() - d1.getTime();

      long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);

      Log.w("new ","secs"+seconds);

      long diffSeconds = diff / 1000 % 60;
      long diffMinutes = diff / (60 * 1000) % 60;
      long diffHours = diff / (60 * 60 * 1000) % 24;
      long diffDays = diff / (24 * 60 * 60 * 1000);

      long totHours = diffHours + (24*diffDays);

      Log.w("totHours   ","totHours  : "+totHours);

      Log.w("Time ","diffDays: "+diffDays);
      Log.w("Time ","diffHours: "+diffHours);
      Log.w("Time ","diffMinutes: "+diffMinutes);
      Log.w("Time ","diffSeconds: "+diffSeconds);

      String all = diffDays + ":" + diffHours + ":" + diffMinutes + ":" +diffSeconds;

      Log.w("xx "," "+all);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void getDistanceInMPS(double speed, double time)
  {
    double distance = speed * time;

    Log.w("distanceMethod"," "+distance + " in meters");
  }



}
