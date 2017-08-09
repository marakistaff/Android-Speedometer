package android.google.com.androidspeedometer;


import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

  /**
   *
   */
  static double getSpeedInMeters(String mPreference, Double currSpeed)
  {
    double MILES = 0.000621371;
    double KILOMETERS = 0.001;

    double speedInMeters;

    switch (mPreference)
    {
      case "MILES":
        speedInMeters = currSpeed * MILES;
        break;
      case "KILOMETERS":
        speedInMeters = currSpeed * KILOMETERS;
        break;
      default:
        speedInMeters = currSpeed;
        break;
    }
    return speedInMeters;
  }

  /**
   *
   */
  static String getDurationString(int seconds)
  {

    int hours = seconds / 3600;
    int minutes = (seconds % 3600) / 60;
    seconds = seconds % 60;

    return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " + twoDigitString(seconds);
  }

  private static String twoDigitString(int number)
  {

    if (number == 0)
    {
      return "00";
    }

    if (number / 10 == 0)
    {
      return "0" + number;
    }

    return String.valueOf(number);
  }

  /*

   */
  static double getDistanceInMPS(double speed, double time)
  {
    return speed * time;
  }

  /**
   *
   */
  static int getTimeInSeconds(String startDate, String stopDate)
  {
    long seconds = 0;

    //HH converts hour in 24 hours format (0-23), day calculation
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

    try
    {
      Date d1 = format.parse(startDate);
      Date d2 = format.parse(stopDate);

      //in milliseconds
      long diff = d2.getTime() - d1.getTime();
      seconds = TimeUnit.MILLISECONDS.toSeconds(diff);

    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return (int) seconds;

  }

}
