package android.google.com.androidspeedometer;


import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

class utils
{
  /**
   * Gets the current date and time
   * @return - formatted String date
   */
  static String getCurrentDateTime()
  {
    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

    return df.format(c.getTime());
  }

  // returns 'float' distance IN METERS from point A to point B
  static float getDistance(double latitudePointA, double longitudePointA, double latitudePointB, double longitudePointB)
  {
    Location locationA = new Location("point A");
    locationA.setLatitude(latitudePointA);
    locationA.setLongitude(longitudePointA);

    Location locationB = new Location("point B");
    locationB.setLatitude(latitudePointB);
    locationB.setLongitude(longitudePointB);

    return locationA.distanceTo(locationB);
  }

  

  /**
   * Calculates average double of list
   * 
   * @param mList - List<Double>
   * @return double value
   */
  static double calculateAverage(List<Double> mList)
  {
    double sum = 0;
    if (!mList.isEmpty())
    {
      for (double item : mList)
      {
        sum += item;
      }
      return sum / mList.size();
    }
    return sum;
  }

}
