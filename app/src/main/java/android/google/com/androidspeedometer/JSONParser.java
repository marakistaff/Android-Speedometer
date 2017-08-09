package android.google.com.androidspeedometer;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class JSONParser
{

  private HttpURLConnection conn;
  private StringBuilder result;
  private JSONObject jObject = null;


  JSONObject makeHttpRequest(String url)
  {

    try
    {
      URL urlObj = new URL(url);

      conn = (HttpURLConnection) urlObj.openConnection();
      conn.setDoOutput(false);
      conn.setRequestMethod("GET");

      String charset = "UTF-8";
      conn.setRequestProperty("Accept-Charset", charset);
      conn.setConnectTimeout(15000);
      conn.connect();

    } catch (IOException e)
    {
      e.printStackTrace();
    }

    try
    {
      //Server response
      InputStream in = new BufferedInputStream(conn.getInputStream());
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      result = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null)
      {
        result.append(line);
      }
    } catch (IOException e)
    {
      e.printStackTrace();
    }

    conn.disconnect();

    // try to parse to a JSON object
    try
    {
      jObject = new JSONObject(result.toString());
    } catch (JSONException e)
    {
      Log.e("JSON Parser", "Error parsing data " + e.toString());
    }

    // Return JSON Object
    return jObject;
  }
}
