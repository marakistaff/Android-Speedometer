package android.google.com.androidspeedometer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class PrefActivity extends AppCompatActivity
{
  private SeekBar seekBar;
  private TextView textViewSetSpeed;
  private RadioGroup radioGroup;
  private ToggleButton toggle;
  private String myPref_type, onOrOff, user_top_speed;
  private Button btnSave;
  private SharedPreferences pref;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pref);

    initialiseWidgets();

    this.pref = getApplicationContext().getSharedPreferences("speedPref", MODE_PRIVATE);

    //Reading from SharedPreferences - gets which preference type
    this.myPref_type = pref.getString("pref_type", null);

    if (this.myPref_type != null)
    {
      setRadioButtonChecked(this.myPref_type);
    }

    this.textViewSetSpeed.setText("0 " + myPref_type);

    this.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
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

    //
    this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
    {
      int progress = 0;
      String mProgress;

      @Override
      public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser)
      {
        //progress = progresValue;
        this.mProgress = String.valueOf(progresValue);
        textViewSetSpeed.setText(this.mProgress + " " + myPref_type);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar)
      {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar)
      {
        textViewSetSpeed.setText(this.mProgress + " " + myPref_type);
      }
    });

    toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
        if (isChecked)
        {
          toggle.setBackgroundResource(R.color.colorGreen);
        } else
        {
          toggle.setBackgroundResource(R.color.colorRed);
        }
      }
    });

    this.btnSave.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        user_top_speed = textViewSetSpeed.getText().toString();
        onOrOff = toggle.getText().toString();

        saveSpeedToPrefs(user_top_speed, onOrOff);

      }
    });
  }


  // Writing data to SharedPreferences
  private void saveTypeToPrefs(String mValue)
  {
    SharedPreferences.Editor editor = pref.edit();
    editor.remove("pref_type");
    editor.apply(); // commit changes
    editor.putString("pref_type", mValue);

    // Save the changes in SharedPreferences
    editor.apply(); // commit changes
  }

  // Writing data to SharedPreferences
  private void saveSpeedToPrefs(String val1, String val2)
  {
    SharedPreferences.Editor editor = pref.edit();
    editor.remove("top_speed");
    editor.remove("on_off");
    editor.apply(); // commit changes
    editor.putString("top_speed", val1);
    editor.putString("on_off", val2);

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

  private void initialiseWidgets()
  {
    this.textViewSetSpeed = (TextView) findViewById(R.id.textViewSetSpeed);
    this.seekBar = (SeekBar) findViewById(R.id.seekBar1);
    this.radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
    this.toggle = (ToggleButton) findViewById(R.id.toggle);
    this.btnSave = (Button) findViewById(R.id.button_save);
  }

}
