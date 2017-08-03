package android.google.com.androidspeedometer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class PrefActivity extends AppCompatActivity
{

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pref);

    //Reading from SharedPreferences
    SharedPreferences pref = getApplicationContext().getSharedPreferences("speedPref", MODE_PRIVATE);
    String myPref_type = pref.getString("pref_type", null);

    if (myPref_type != null)
    {
      setRadioButtonChecked(myPref_type);
    }

    RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
    {

      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId)
      {
        // find which radio button is selected
        if (checkedId == R.id.radioButton1)
        {
          saveTypeToPrefs("ms");
        } else if (checkedId == R.id.radioButton2)
        {
          saveTypeToPrefs("mph");
        } else if (checkedId == R.id.radioButton3)
        {
          saveTypeToPrefs("kph");
        }
      }

    });

  }

  private void saveTypeToPrefs(String mValue)
  {
    // Writing data to SharedPreferences
    SharedPreferences pref = getApplicationContext().getSharedPreferences("speedPref", MODE_PRIVATE);
    SharedPreferences.Editor editor = pref.edit();
    editor.remove("pref_type");
    editor.apply(); // commit changes
    editor.putString("pref_type", mValue);

    // Save the changes in SharedPreferences
    editor.apply(); // commit changes
  }


  private void setRadioButtonChecked(String mValue)
  {
    switch (mValue)
    {
      case "ms":
        RadioButton rad1 = (RadioButton) findViewById(R.id.radioButton1);
        rad1.setChecked(true);
        break;
      case "mph":
        RadioButton rad2 = (RadioButton) findViewById(R.id.radioButton2);
        rad2.setChecked(true);
        break;
      default:
        RadioButton rad3 = (RadioButton) findViewById(R.id.radioButton3);
        rad3.setChecked(true);
        break;
    }
  }

}
