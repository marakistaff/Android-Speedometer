package android.google.com.androidspeedometer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
{

  private static final String TAG = MainActivity.class.getSimpleName();

  /**
   * Code used in requesting runtime permissions.
   */
  private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

  /**
   * Constant used in the location settings dialog.
   */
  private static final int REQUEST_CHECK_SETTINGS = 0x1;

  /**
   * The desired interval for location updates. Inexact. Updates may be more or less frequent.
   */
  private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 6000;

  /**
   * The fastest rate for active location updates. Exact. Updates will never be more frequent
   * than this value.
   */
  private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
    UPDATE_INTERVAL_IN_MILLISECONDS / 2;

  // Keys for storing activity state in the Bundle.
  private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
  private final static String KEY_LOCATION = "location";

  /**
   * Provides access to the Fused Location Provider API.
   */
  private FusedLocationProviderClient mFusedLocationClient;

  /**
   * Provides access to the Location API.
   */
  private SettingsClient mSettingsClient;

  /**
   * Stores parameters for requests to the FusedLocationProviderApi.
   */
  private LocationRequest mLocationRequest;

  /**
   * Stores the types of location services the client is interested in using. Used for checking
   * settings to determine if the device has optimal location settings.
   */
  private LocationSettingsRequest mLocationSettingsRequest;

  /**
   * Callback for Location events.
   */
  private LocationCallback mLocationCallback;

  /**
   * Represents a geographical location.
   */
  private Location mCurrentLocation;

  double myDistance;
  double myAverageSpeed;
  private double topSpeed = 0;
  private String averageSpeed;
  private String measurement = "";

  public static final double mph = 2.23694;
  public static final double kph = 3.6;

  // UI Widgets.
  private Button mStartStopButton;
  private TextView mSpeedTextView;
  private Chronometer simpleChronometer;

  /**
   * Tracks the status of the location updates request. Value changes when the user presses the
   * Start Updates and Stop Updates buttons.
   */
  private Boolean mRequestingLocationUpdates;

  private RelativeLayout hiddenLayout;
  private String myPref_type = "ms";
  private List<Double> speedArray = new ArrayList<>();


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // keep sceen on
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    // custom font
    Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/myfont.ttf");
    // Locate the UI widgets.
    mSpeedTextView = (TextView) findViewById(R.id.speed_text);
    mSpeedTextView.setTypeface(custom_font);
    TextView mSpeedTypeTextView = (TextView) findViewById(R.id.speedTypeTextView);
    mSpeedTypeTextView.setTypeface(custom_font);
    simpleChronometer = (Chronometer) findViewById(R.id.simpleChronometer);
    simpleChronometer.setTypeface(custom_font);
    mStartStopButton = (Button) findViewById(R.id.start_updates_button);
    hiddenLayout = (RelativeLayout) findViewById(R.id.hideLayout);

    //Reading from SharedPreferences
    SharedPreferences pref = getApplicationContext().getSharedPreferences("speedPref", MODE_PRIVATE);
    myPref_type = pref.getString("pref_type", null);

    if (this.myPref_type != null)
    {
      mSpeedTypeTextView.setText(myPref_type);

      switch (this.myPref_type)
      {
        case "ms":
          measurement = "meters";
          break;
        case "mph":
          measurement = "miles";
          break;
        default:
          measurement = "kms";
          break;
      }
    }
    else
    {
      this.myPref_type = "kms";
    }

    // Add button listener
    this.mStartStopButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {

        String buttonText = mStartStopButton.getText().toString();

        if (buttonText.equals("Start"))
        {
          hiddenLayout.setVisibility(View.INVISIBLE);
          startUpdatesButtonHandler(v);
          if (mRequestingLocationUpdates)
          {
            simpleChronometer.setBase(SystemClock.elapsedRealtime());
            simpleChronometer.start();
          }
        }
        else
        {
          stopUpdatesButtonHandler(v);
          stopAll();
        }
      }
    });


    this.mRequestingLocationUpdates = false;

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
  public boolean onCreateOptionsMenu(Menu menu)
  {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  // overflow menu
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    int id = item.getItemId();
    // Handle item selection - opens PrefActivity
    if (id == R.id.settings)
    {
      Intent intent = new Intent(this, PrefActivity.class);
      this.startActivity(intent);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
  
  // stops chronometer and displays invisible layout
  private void stopAll()
  {
    TextView mTopSpeedTextView = (TextView) findViewById(R.id.hideLabelTopSpeed);
    TextView mAvgSpeedTextView = (TextView) findViewById(R.id.hideLabelAvgSpeed);
    TextView mDurationTextView = (TextView) findViewById(R.id.hideLabelDuration);
    TextView mDistanceTextView = (TextView) findViewById(R.id.hideLabelDistance);

    int elapsedMillis = (int) (SystemClock.elapsedRealtime() - simpleChronometer.getBase());
    long dd = (long) elapsedMillis;
    double hh = getHour(dd);

    double dist = myAverageSpeed * hh;
    myDistance = round(dist, 3);
    
    mTopSpeedTextView.setText(String.format(Locale.ENGLISH, "%.2f", topSpeed));
    mAvgSpeedTextView.setText(averageSpeed);
    mDurationTextView.setText(String.valueOf(simpleChronometer.getText().toString()));
    String distanceOutput = String.valueOf(myDistance) + " " + measurement;
    mDistanceTextView.setText(distanceOutput);

    this.hiddenLayout.setVisibility(View.VISIBLE);
    this.simpleChronometer.stop();
    this.simpleChronometer.setText(R.string.blank_time);
  }

  public static double round(double value, int places)
  {
    if (places < 0) throw new IllegalArgumentException();

    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  public static double getHour(long ms)
  {
    DecimalFormat df = new DecimalFormat(".######");
    double totalSecs = ms / 1000;
    String hours = df.format(totalSecs / 3600.0);

    return Double.valueOf(hours);
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
        mRequestingLocationUpdates = savedInstanceState.getBoolean(
          KEY_REQUESTING_LOCATION_UPDATES);
      }

      // Update the value of mCurrentLocation from the Bundle and update the UI to show the
      // correct latitude and longitude.
      if (savedInstanceState.keySet().contains(KEY_LOCATION))
      {
        // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
        // is not null.
        mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
      }

      updateLocationUI();
    }
  }

  /**
   * Sets up the location request. Android has two location request settings:
   * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
   * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
   * the AndroidManifest.xml.
   * <p/>
   * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
   * interval (5 seconds), the Fused Location Provider API returns location updates that are
   * accurate to within a few feet.
   * <p/>
   * These settings are appropriate for mapping applications that show real-time location
   * updates.
   */
  private void createLocationRequest()
  {
    mLocationRequest = new LocationRequest();

    // Sets the desired interval for active location updates - not 100% accurate
    mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

    // Sets the fastest rate for active location updates.
    mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
    mLocationSettingsRequest = builder.build();
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
            // Nothing to do. startLocationupdates() gets called in onResume again.
            break;
          case Activity.RESULT_CANCELED:
            mRequestingLocationUpdates = false;
            updateLocationUI();
            break;
        }
        break;
    }
  }

  /**
   * Handles the Start Updates button and requests start of location updates. Does nothing if
   * updates have already been requested.
   */
  public void startUpdatesButtonHandler(View view)
  {
    this.mStartStopButton.setText(R.string.stop_updates);
    this.mStartStopButton.setBackgroundResource(R.drawable.stop_button);
    if (!mRequestingLocationUpdates)
    {
      mRequestingLocationUpdates = true;
      startLocationUpdates();
    }
  }

  /**
   * Handles the Stop Updates button, and requests removal of location updates.
   */
  public void stopUpdatesButtonHandler(View view)
  {
    // It is a good practice to remove location requests when the activity is in a paused or
    // stopped state. Doing so helps battery performance and is especially
    // recommended in applications that request frequent location updates.
    mStartStopButton.setText(R.string.start_updates);
    stopLocationUpdates();
  }

  /**
   * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
   * runtime permission has been granted.
   */
  private void startLocationUpdates()
  {
    // Begin by checking if the device has the necessary location settings.
    mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
      .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>()
      {
        @Override
        public void onSuccess(LocationSettingsResponse locationSettingsResponse)
        {
          //noinspection MissingPermission
          mFusedLocationClient.requestLocationUpdates(mLocationRequest,
            mLocationCallback, Looper.myLooper());

          updateLocationUI();
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
                "fixed here. Fix in PrefActivity.";
              Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
              mRequestingLocationUpdates = false;
          }

          updateLocationUI();
        }
      });
  }

  /**
   * Sets the value of the UI fields for current speed.
   */
  private void updateLocationUI()
  {
    if (mCurrentLocation != null)
    {

      double mSpeed;
      float currentSpeed = mCurrentLocation.getSpeed();

      switch (myPref_type)
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

      if (mSpeed > topSpeed)
      {
        topSpeed = mSpeed;
      }
      if (mSpeed != 0)
      {
        this.speedArray.add(mSpeed);
      }
      String speedStr = String.format(Locale.ENGLISH, "%.2f", mSpeed);
      this.mSpeedTextView.setText(speedStr);
      myAverageSpeed = calculateAverage(speedArray);
      this.averageSpeed = String.format(Locale.ENGLISH, "%.2f", myAverageSpeed);
    }
  }

  // calculates average speed
  private double calculateAverage(List<Double> speedList)
  {
    double sum = 0;
    if (!speedList.isEmpty())
    {
      for (double speed : speedList)
      {
        sum += speed;
      }
      return sum / speedList.size();
    }
    return sum;
  }

  /**
   * Removes location updates from the FusedLocationApi.
   */
  private void stopLocationUpdates()
  {

    if (!mRequestingLocationUpdates)
    {
      return;
    }

    // It is a good practice to remove location requests when the activity is in a paused or
    // stopped state. Doing so helps battery performance and is especially
    // recommended in applications that request frequent location updates.
    mFusedLocationClient.removeLocationUpdates(mLocationCallback)
      .addOnCompleteListener(this, new OnCompleteListener<Void>()
      {
        @Override
        public void onComplete(@NonNull Task<Void> task)
        {
          mRequestingLocationUpdates = false;
          mStartStopButton.setText(R.string.start_updates);
        }
      });
  }

  @Override
  public void onResume()
  {
    super.onResume();
    // Within {@code onPause()}, we remove location updates. Here, we resume receiving
    // location updates if the user has requested them.
    if (mRequestingLocationUpdates && checkPermissions())
    {
      startLocationUpdates();
    } else if (!checkPermissions())
    {
      requestPermissions();
    }

    updateLocationUI();
  }

  @Override
  protected void onPause()
  {
    super.onPause();

    // Remove location updates to save battery.
    stopLocationUpdates();
  }

  /**
   * Stores activity data in the Bundle.
   */
  public void onSaveInstanceState(Bundle savedInstanceState)
  {
    savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
    savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
    super.onSaveInstanceState(savedInstanceState);
  }


  // PERMISSIONS


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
      // Request permission. It's possible this can be auto answered if device policy
      // sets the permission in a given state or the user denied the permission
      // previously and checked "Never ask again".
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
    if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE)
    {
      if (grantResults.length <= 0)
      {
        // If user interaction was interrupted, the permission request is cancelled and you
        // receive empty arrays.
      } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
      {
        if (mRequestingLocationUpdates)
        {
          startLocationUpdates();
        }
      } else
      {
        // Permission denied.

        // Notify the user via a SnackBar that they have rejected a core permission for the
        // app, which makes the Activity useless. In a real app, core permissions would
        // typically be best requested during a welcome-screen flow.

        // Additionally, it is important to remember that a permission might have been
        // rejected without asking the user for permission (device policy or "Never ask
        // again" prompts). Therefore, a user interface affordance is typically implemented
        // when permissions are denied. Otherwise, your app could appear unresponsive to
        // touches or interactions which have required permissions.
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

}
