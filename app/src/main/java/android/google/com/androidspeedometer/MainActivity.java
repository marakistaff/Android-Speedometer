package android.google.com.androidspeedometer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
{

  private static final String TAG = MainActivity.class.getSimpleName();

  //Code used in requesting runtime permissions.
  private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

  //Constant used in the location settings dialog.
  private static final int REQUEST_CHECK_SETTINGS = 0x1;

  //The desired interval for location updates. Inexact. Updates may be more or less frequent.
  private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

  // The fastest rate for active location updates. Exact. Updates will never be more frequent than this value.
  private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
    UPDATE_INTERVAL_IN_MILLISECONDS / 2;

  // Keys for storing activity state in the Bundle.
  private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
  private final static String KEY_LOCATION = "location";
  private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

  //Provides access to the Fused Location Provider API.
  private FusedLocationProviderClient mFusedLocationClient;

  //Provides access to the Location Settings API.
  private SettingsClient mSettingsClient;

  //Stores parameters for requests to the FusedLocationProviderApi.
  private LocationRequest mLocationRequest;

  /**
   * Stores the types of location services the client is interested in using. Used for checking
   * settings to determine if the device has optimal location settings.
   */
  private LocationSettingsRequest mLocationSettingsRequest;

  //Callback for Location events.
  private LocationCallback mLocationCallback;

  //Represents a geographical location.
  private Location mCurrentLocation;

  //UI Widgets.
  Button mStartStopButton;
  TextView mSpeedTextView, mStopWatch, mMaxSpeed, mAvgSpeed, mSpeedTypeTextView;

  //Tracks the status of the location updates request. Value changes when the user presses the start/stop button.
  private Boolean mRequestingLocationUpdates;

  //Time when the location was updated represented as a String.
  private String mLastUpdateTime;

  //Arraylist to hold each location update data
  private ArrayList<String> locationList;

  //User preferences
  private String mPreference;
  String mOnOrOff = "";
  int mTopSpeedLimit = -1;

  //
  private static final double mph = 2.23694;
  private static final double kph = 3.6;

  //
  private double topSpeed;

  //
  private List<Double> speedArray = new ArrayList<>();

  private String startTime;
  private String startLatitude;
  private String startLongitude;

  private String stopTime, stopLatitude, stopLongitude;

  private long MillisecondTime, StartTime, TimeBuff ;

  private Handler handler;

  private String avgSpeed;

  private Typeface custom_font;

  // tone generator
  private int streamType = AudioManager.STREAM_MUSIC;
  private int volume = 100;
  private ToneGenerator toneGenerator = new ToneGenerator(streamType, volume);


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // keep sceen on
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    this.locationList = new ArrayList<>();
    this.handler = new Handler() ;

    initialiseWidgets();
    retrieveSharedPreferences();

    // custom font from assets folder
    this.custom_font = Typeface.createFromAsset(getAssets(), "fonts/myfont.ttf");

    //Main start/stop button button listener
    this.mStartStopButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        String buttonText = mStartStopButton.getText().toString();

        if (buttonText.equalsIgnoreCase("START"))
        {
          speedArray.clear();
          mStartStopButton.setBackgroundResource(R.drawable.stop_button);
          startUpdatesButtonHandler(v);
        } else
        {
          stopUpdatesButtonHandler(v);
          mStartStopButton.setBackgroundResource(R.drawable.start_button);
        }
      }
    });

    this.mRequestingLocationUpdates = false;
    this.mLastUpdateTime = "";

    // Update values using data stored in the Bundle.
    updateValuesFromBundle(savedInstanceState);

    this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    this.mSettingsClient = LocationServices.getSettingsClient(this);

    // Kick off the process of building the LocationCallback, LocationRequest, and
    // LocationSettingsRequest objects.
    createLocationCallback();
    createLocationRequest();
    buildLocationSettingsRequest();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    Intent intent = new Intent(this, PrefActivity.class);
    this.startActivity(intent);

    return super.onOptionsItemSelected(item);
  }

  /**
   * Updates fields based on data stored in the bundle.
   *
   * @param savedInstanceState The activity state saved in the Bundle.
   */
  private void updateValuesFromBundle(Bundle savedInstanceState)
  {
    if (savedInstanceState != null)
    {
      // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
      // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
      if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES))
      {
        this.mRequestingLocationUpdates = savedInstanceState.getBoolean(
          KEY_REQUESTING_LOCATION_UPDATES);
      }

      // Update the value of mCurrentLocation from the Bundle and update the UI to show the
      // correct latitude and longitude.
      if (savedInstanceState.keySet().contains(KEY_LOCATION))
      {
        // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
        // is not null.
        this.mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
      }

      // Update the value of mLastUpdateTime from the Bundle and update the UI.
      if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING))
      {
        this.mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
      }
      updateUI();
    }
  }

  //Sets up the location request.
  private void createLocationRequest()
  {
    this.mLocationRequest = new LocationRequest();

    // Sets the desired interval for active location updates.
    this.mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

    // Sets the fastest rate for active location updates.
    this.mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
    this.mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
  }

  /**
   * Creates a callback for receiving location events.
   */
  private void createLocationCallback()
  {
    mLocationCallback = new LocationCallback()
    {
      @Override
      public void onLocationResult(LocationResult locationResult)
      {
        super.onLocationResult(locationResult);

        mCurrentLocation = locationResult.getLastLocation();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateLocationUI();
      }
    };
  }

  /**
   * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
   * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
   * if a device has the needed location settings.
   */
  private void buildLocationSettingsRequest()
  {
    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
    builder.addLocationRequest(mLocationRequest);
    this.mLocationSettingsRequest = builder.build();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    switch (requestCode)
    {
      // Check for the integer request code originally supplied to startResolutionForResult().
      case REQUEST_CHECK_SETTINGS:
        switch (resultCode)
        {
          case Activity.RESULT_OK:
            break;
          case Activity.RESULT_CANCELED:
            this.mRequestingLocationUpdates = false;
            updateUI();
            break;
        }
        break;
    }
  }

  /**
   * Handles the start button and requests start of location updates. Does nothing if
   * updates have already been requested.
   */
  public void startUpdatesButtonHandler(View view)
  {
    if (!this.mRequestingLocationUpdates)
    {
      this.mRequestingLocationUpdates = true;
      setButtonsEnabledState();
      startLocationUpdates();
    }
  }

  //Handles the stop button, and requests removal of location updates.
  public void stopUpdatesButtonHandler(View view)
  {
    stopLocationUpdates();
    getResults();
  }

  /**
   * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
   * runtime permission has been granted.
   */
  private void startLocationUpdates()
  {
    // Begin by checking if the device has the necessary location settings.
    this.mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
      .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>()
      {
        @Override
        public void onSuccess(LocationSettingsResponse locationSettingsResponse)
        {
          //noinspection MissingPermission
          mFusedLocationClient.requestLocationUpdates(mLocationRequest,
            mLocationCallback, Looper.myLooper());

          updateUI();
        }
      })
      .addOnFailureListener(this, new OnFailureListener()
      {
        @Override
        public void onFailure(@NonNull Exception e)
        {
          int statusCode = ((ApiException) e).getStatusCode();
          switch (statusCode)
          {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
              try
              {
                // Show the dialog by calling startResolutionForResult(), and check the
                // result in onActivityResult().
                ResolvableApiException rae = (ResolvableApiException) e;
                rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
              } catch (IntentSender.SendIntentException ignored)
              {
              }
              break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
              String errorMessage = "Location settings are inadequate, and cannot be " +
                "fixed here. Fix in Settings.";
              Log.e(TAG, errorMessage);
              Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
              mRequestingLocationUpdates = false;
          }

          updateUI();
        }
      });
  }

  /**
   * Updates all UI fields.
   */
  private void updateUI()
  {
    setButtonsEnabledState();
    updateLocationUI();
  }

  private void setButtonsEnabledState()
  {
    if (this.mRequestingLocationUpdates)
    {
      startStopWatch();
      this.mStartStopButton.setText(R.string.stop_updates);
    } else
    {
      this.mStartStopButton.setText(R.string.start_updates);
    }
  }

  //Sets the value of each UI field
  private void updateLocationUI()
  {
    if (this.mCurrentLocation != null)
    {
      String currTime = utils.getCurrentDateTime();
      String xx = currTime + "," + this.mCurrentLocation.getLatitude() + "," + this.mCurrentLocation.getLongitude();

      this.locationList.add(xx);

      double mSpeed;
      float currentSpeed = this.mCurrentLocation.getSpeed();

      switch (this.mPreference)
      {
        case "mph":
          mSpeed = (double) currentSpeed * mph;
          break;
        case "kph":
          mSpeed = (double) currentSpeed * kph;
          break;
        default:
          mSpeed = (double) currentSpeed;
          break;
      }

      if (mSpeed > this.topSpeed)
      {
        this.topSpeed = mSpeed;
        String maxSpeedStr = String.format(Locale.getDefault(), "%.2f", this.topSpeed);
        this.mMaxSpeed.setText(String.valueOf(maxSpeedStr));
      }
      if (mSpeed != 0)
      {
        this.speedArray.add(mSpeed);
      }

      double mAverage = utils.calculateAverage(speedArray);
      this.avgSpeed = String.format(Locale.getDefault(), "%.2f", mAverage);
      this.mAvgSpeed.setText(this.avgSpeed);

      String speedStr = String.format(Locale.getDefault(), "%.2f", mSpeed);
      this.mSpeedTextView.setText(speedStr);

      // check if user has top-speed alert on
      if(this.mOnOrOff.equalsIgnoreCase("on"))
      {
        if(this.topSpeed > this.mTopSpeedLimit)
        {
          int toneType = ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE;
          int durationMs = 500;
          this.toneGenerator.startTone(toneType, durationMs);
        }
      }
    }
  }

  //Removes location updates from the FusedLocationApi.
  private void stopLocationUpdates()
  {
    if (!this.mRequestingLocationUpdates)
    {
      return;
    }

    this.mFusedLocationClient.removeLocationUpdates(this.mLocationCallback)
      .addOnCompleteListener(this, new OnCompleteListener<Void>()
      {
        @Override
        public void onComplete(@NonNull Task<Void> task)
        {
          mRequestingLocationUpdates = false;
          setButtonsEnabledState();
        }
      });
  }


  @Override
  public void onResume()
  {
    super.onResume();

    if (this.mRequestingLocationUpdates && checkPermissions())
    {
      startLocationUpdates();
    } else if (!checkPermissions())
    {
      requestPermissions();
    }
    updateUI();
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    // Remove location updates to save battery.
    stopLocationUpdates();
  }

  //Stores activity data in the Bundle.
  public void onSaveInstanceState(Bundle savedInstanceState)
  {
    savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
    savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
    savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
    super.onSaveInstanceState(savedInstanceState);
  }

  /**
   * Shows a {@link Snackbar}.
   *
   * @param mainTextStringId The id for the string resource for the Snackbar text.
   * @param actionStringId   The text of the action item.
   * @param listener         The listener associated with the Snackbar action.
   */
  private void showSnackbar(final int mainTextStringId, final int actionStringId,
                            View.OnClickListener listener)
  {
    Snackbar.make(
      findViewById(android.R.id.content),
      getString(mainTextStringId),
      Snackbar.LENGTH_INDEFINITE)
      .setAction(getString(actionStringId), listener).show();
  }

  /**
   * Return the current state of the permissions needed.
   */
  private boolean checkPermissions()
  {
    int permissionState = ActivityCompat.checkSelfPermission(this,
      Manifest.permission.ACCESS_FINE_LOCATION);
    return permissionState == PackageManager.PERMISSION_GRANTED;
  }

  private void requestPermissions()
  {
    boolean shouldProvideRationale =
      ActivityCompat.shouldShowRequestPermissionRationale(this,
        Manifest.permission.ACCESS_FINE_LOCATION);

    // Provide an additional rationale to the user. This would happen if the user denied the
    // request previously, but didn't check the "Don't ask again" checkbox.
    if (shouldProvideRationale)
    {
      Log.i(TAG, "Displaying permission rationale to provide additional context.");
      showSnackbar(R.string.permission_rationale,
        android.R.string.ok, new View.OnClickListener()
        {
          @Override
          public void onClick(View view)
          {
            // Request permission
            ActivityCompat.requestPermissions(MainActivity.this,
              new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
              REQUEST_PERMISSIONS_REQUEST_CODE);
          }
        });
    } else
    {
      ActivityCompat.requestPermissions(MainActivity.this,
        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
        REQUEST_PERMISSIONS_REQUEST_CODE);
    }
  }

  /**
   * Callback received when a permissions request has been completed.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults)
  {
    Log.i(TAG, "onRequestPermissionResult");
    if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE)
    {
      if (grantResults.length <= 0)
      {
        // If user interaction was interrupted, the permission request is cancelled and you
        // receive empty arrays.
        Log.i(TAG, "User interaction was cancelled.");
      } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
      {
        if (mRequestingLocationUpdates)
        {
          Log.i(TAG, "Permission granted, updates requested, starting location updates");
          startLocationUpdates();
        }
      } else
      {
        // Permission denied.
        showSnackbar(R.string.permission_denied_explanation,
          R.string.settings, new View.OnClickListener()
          {
            @Override
            public void onClick(View view)
            {
              // Build intent that displays the App settings screen.
              Intent intent = new Intent();
              intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
              Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
              intent.setData(uri);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
            }
          });
      }
    }
  }

  public void getResults()
  {
    stopStopWatch();

    if (this.locationList.size() > 0)
    {
      String str1 = this.locationList.get(0);
      List<String> item1 = Arrays.asList(str1.split("\\s*,\\s*"));

      this.startTime = item1.get(0);
      this.startLatitude = item1.get(1);
      this.startLongitude = item1.get(2);

      String str2 = this.locationList.get(this.locationList.size() - 1);
      List<String> item2 = Arrays.asList(str2.split("\\s*,\\s*"));

      this.stopTime = item2.get(0);
      this.stopLatitude = item2.get(1);
      this.stopLongitude = item2.get(2);

      passData();
    }

  }

  public void passData()
  {

    //  do total time

    //String totalTime = mStopWatch.getText().toString();
    HashMap<String, String> hashMap = new HashMap<>();

    hashMap.put("start_time", this.startTime);
    hashMap.put("start_lat", this.startLatitude);
    hashMap.put("start_lng", this.startLongitude);

    hashMap.put("total_time", String.valueOf(this.MillisecondTime));

    hashMap.put("stop_time", this.stopTime);
    hashMap.put("stop_lat", this.stopLatitude);
    hashMap.put("stop_lng", this.stopLongitude);

    hashMap.put("avg_speed", this.avgSpeed);

    String maxSpeed = String.format(Locale.getDefault(), "%.2f", this.topSpeed);
    hashMap.put("max_speed", String.valueOf(maxSpeed));

    Intent intent = new Intent(this, TripResult.class);
    intent.putExtra("map", hashMap);
    startActivity(intent);
  }

  public void startStopWatch()
  {
    this.StartTime = SystemClock.uptimeMillis();
    this.handler.postDelayed(runnable, 0);
  }

  public void stopStopWatch()
  {
    this.TimeBuff += this.MillisecondTime;
    this.handler.removeCallbacks(runnable);
  }

  public Runnable runnable = new Runnable() {

    public void run() {

      //long UpdateTime = 0L;
      MillisecondTime = SystemClock.uptimeMillis() - StartTime;

      long UpdateTime = TimeBuff + MillisecondTime;
      int seconds = (int) (UpdateTime / 1000);
      int minutes = seconds / 60;
      seconds = seconds % 60;
      mStopWatch.setText("" + minutes + ":"
        + String.format(Locale.getDefault(),"%02d", seconds));

      handler.postDelayed(this, 0);
    }

  };


  public void initialiseWidgets()
  {
    // Locate the UI widgets and set font.
    this.mStartStopButton = (Button) findViewById(R.id.start_updates_button);
    this.mStartStopButton.setTypeface(this.custom_font);

    this.mSpeedTextView = (TextView) findViewById(R.id.speed_text);
    this.mSpeedTextView.setTypeface(this.custom_font);

    this.mStopWatch = (TextView) findViewById(R.id.tvStopWatch);
    this.mStopWatch.setTypeface(this.custom_font);


    this.mSpeedTypeTextView = (TextView) findViewById(R.id.speedTypeTextView);
    this.mSpeedTypeTextView.setTypeface(this.custom_font);

    this.mMaxSpeed = (TextView) findViewById(R.id.textViewMaxSpeed);
    this.mMaxSpeed.setTypeface(this.custom_font);

    this.mAvgSpeed = (TextView) findViewById(R.id.textViewAverageSpeed);
    this.mAvgSpeed.setTypeface(this.custom_font);

  }

  public void retrieveSharedPreferences()
  {
    //Reading from SharedPreferences
    SharedPreferences pref = getApplicationContext().getSharedPreferences("speedPref", MODE_PRIVATE);
    this.mPreference = pref.getString("pref_type", null);
    String temp = pref.getString("top_speed", null);
    this.mOnOrOff = pref.getString("on_off", null);

    if (this.mPreference != null)
    {
      this.mSpeedTypeTextView.setText(this.mPreference);
    } else
    {
      this.mPreference = "miles";
    }

    if(temp !=null)
    {
      this.mTopSpeedLimit = Integer.valueOf(temp);
    } else
    {
      this.mTopSpeedLimit = -1;
    }

    if (this.mOnOrOff == null)
    {
      this.mOnOrOff = "off";
    }

    Log.w("mPreference ", "" + mPreference);
    Log.w("mTopSpeedLimit ", "" + mTopSpeedLimit);
    Log.w("mOnOrOff ", "" + mOnOrOff);
  }

}

