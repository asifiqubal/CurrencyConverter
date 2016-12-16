package com.asif.currencyconverter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConverterActivity extends AppCompatActivity {

    private TextView modeview;
    private TextView rateview;
    private TextView resultview;
    private EditText edittext;
    private double value=0;
    private double convertedvalue=0;
    private double usd2bdt=0;
    private double bdt2usd=0;
    private String[] items=null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ToggleButton myswitch;
    private boolean rut=false;
    private String mode=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        this.resultview=(TextView)this.findViewById(R.id.resultview);
        this.modeview=(TextView)this.findViewById(R.id.modeview);
        this.rateview=(TextView)this.findViewById(R.id.rateview);
        this.edittext=(EditText) this.findViewById(R.id.edittext);
        this.myswitch = (ToggleButton) this.findViewById(R.id.switch1);
    }


    public String[] getvalue() throws IOException {
        InputStream is = null;
        String[] value=null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL("http://hrhafiz.com/converter/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(1000000 /* milliseconds */);
            conn.setConnectTimeout(1500000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("test", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            //return contentAsString;
            //this.resultview.setText(contentAsString);
            value = contentAsString.split("[{},:]+");
            //this.resultview.setText(items[2]+" "+items[4]);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.

        } finally {
            if (is != null) {
                is.close();
            }
        }
        return value;
    }

    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        outState.putString("textViewValue", ConverterActivity.this.resultview.getText().toString());
        outState.putString("editTextValue", ConverterActivity.this.edittext.getText().toString());

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(savedInstanceState);
        ConverterActivity.this.edittext.setText(savedInstanceState.get("editTextValue").toString());
        ConverterActivity.this.resultview.setText(savedInstanceState.get("textViewValue").toString());
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    haveConnectedWifi = true;
                }

            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    haveConnectedMobile = true;
                }
               // else

            }
            //break;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (haveNetworkConnection()==true)
        {
            mode="Online";
            try {
                items=getvalue();
                usd2bdt=Double.parseDouble(items[2]);
                bdt2usd=Double.parseDouble(items[4]);
                //this.resultview.setText(usd2bdt+" "+bdt2usd);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            mode="Offline";
            sharedPreferences = getSharedPreferences("myData", Context.MODE_PRIVATE);
            usd2bdt=Double.parseDouble(sharedPreferences.getString("usd2bdt", "0"));
            bdt2usd=Double.parseDouble(sharedPreferences.getString("bdt2usd", "0"));
            //this.resultview.setText(usd2bdt+" "+usd2bdt);

        }

        this.rateview.setText("Rate:\n 1 USD ="+usd2bdt+" BDT \n 1 BDT = "+bdt2usd+" USD");
        this.modeview.setText("Rate Source: "+mode);


    }

    public void convert(View v)
    {
        if (this.edittext.getText().toString().equals(""))
        {
            Toast.makeText(this, "Please enter a valied number", Toast.LENGTH_SHORT).show();
        }
        else
        value=Double.parseDouble(this.edittext.getText().toString());


            if(chengerate()){
                convertedvalue=value*bdt2usd;
            }else{
                convertedvalue=value*usd2bdt;
            }

        this.resultview.setText(Double.toString(convertedvalue));
    }

    public boolean chengerate()
    {

        myswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if(isChecked){
                    rut=true;
                }else{
                    rut=false;
                }
            }
        });
        return rut;
    }


    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences = getSharedPreferences("myData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("usd2bdt",Double.toString(usd2bdt));
        editor.putString("bdt2usd",Double.toString(bdt2usd));
        editor.commit();
    }
}
