package android.google.com.androidspeedometer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

public class TripResult extends AppCompatActivity implements View.OnClickListener
{
  double startLat, startLng, stopLat, stopLng;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_trip_result);

    Intent intent = getIntent();
    @SuppressWarnings("unchecked")
    HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra("map");

    startLat = Double.valueOf(hashMap.get("start_lat"));
    startLng = Double.valueOf(hashMap.get("start_lng"));
    stopLat = Double.valueOf(hashMap.get("stop_lat"));
    stopLng = Double.valueOf(hashMap.get("stop_lng"));

    TextView startTime = (TextView) findViewById(R.id.results_start_time);
    startTime.setText(hashMap.get("start_time"));

    TextView startAddress = (TextView) findViewById(R.id.results_start_location);
    startAddress.setText(hashMap.get("start_address"));

    TextView stopTime = (TextView) findViewById(R.id.results_end_time);
    stopTime.setText(hashMap.get("stop_time"));

    TextView stopAddress = (TextView) findViewById(R.id.results_end_location);
    stopAddress.setText(hashMap.get("stop_address"));

    ImageButton buttonStart = (ImageButton) findViewById(R.id.imageButton1);
    buttonStart.setOnClickListener(this);
    ImageButton buttonStop = (ImageButton) findViewById(R.id.imageButton2);
    buttonStop.setOnClickListener(this);

    TextView distance = (TextView) findViewById(R.id.results_distance);
    distance.setText(String.valueOf(utils.getDistance(startLat, startLng, stopLat, stopLng)));

    TextView maxSpeed = (TextView) findViewById(R.id.results_max_speed);
    double temp1 = Double.valueOf(hashMap.get("max_speed"));
    maxSpeed.setText(String.format(Locale.getDefault(), "%.2f", temp1));

    TextView avgSpeed = (TextView) findViewById(R.id.results_avg_speed);
    double temp2 = Double.valueOf(hashMap.get("avg_speed"));
    avgSpeed.setText(String.format(Locale.getDefault(), "%.2f", temp2));

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



}
