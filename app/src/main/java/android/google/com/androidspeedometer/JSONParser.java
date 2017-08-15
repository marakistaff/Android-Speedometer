package android.google.com.androidspeedometer;

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

      this.conn = (HttpURLConnection) urlObj.openConnection();
      this.conn.setDoOutput(false);
      this.conn.setRequestMethod("GET");

      String charset = "UTF-8";
      this.conn.setRequestProperty("Accept-Charset", charset);
      this.conn.setConnectTimeout(15000);
      this.conn.connect();

    } catch (IOException e)
    {
      e.printStackTrace();
    }

    try
    {
      //Server response
      InputStream in = new BufferedInputStream(conn.getInputStream());
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      this.result = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null)
      {
        this.result.append(line);
      }
    } catch (IOException e)
    {
      e.printStackTrace();
    }

    this.conn.disconnect();

    // try to parse to a JSON object
    try
    {
      this.jObject = new JSONObject(result.toString());
    } catch (JSONException ignored)
    {
    }

    // Return JSON Object
    return this.jObject;
  }
}
