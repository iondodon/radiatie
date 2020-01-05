package com.example.radiatie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button b;
    private TextView t;
    private LocationManager locationManager;
    private LocationListener listener;

    private static final String FILE_NAME = "example.txt";

    private ArrayList<Double> longitudes = new ArrayList<>();
    private ArrayList<Double> latitudes = new ArrayList<>();
    private ArrayList<Double> distances = new ArrayList<>();

    private int partition(ArrayList<Double> arr, int begin, int end) {
        Double pivot = arr.get(end);
        int i = (begin-1);

        for (int j = begin; j < end; j++) {
            if (arr.get(j) <= pivot) {
                i++;

                Double swapTemp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, swapTemp);

                swapTemp = longitudes.get(i);
                longitudes.set(i, longitudes.get(j));
                longitudes.set(j, swapTemp);

                swapTemp = latitudes.get(i);
                latitudes.set(i, latitudes.get(j));
                latitudes.set(j, swapTemp);
            }
        }

        Double swapTemp = arr.get(i+1);
        arr.set(i+1, arr.get(end));
        arr.set(end, swapTemp);

        swapTemp = longitudes.get(i+1);
        longitudes.set(i+1, longitudes.get(end));
        longitudes.set(end, swapTemp);

        swapTemp = latitudes.get(i+1);
        latitudes.set(i+1, latitudes.get(end));
        latitudes.set(end, swapTemp);

        return i+1;
    }

    public void quickSort(ArrayList<Double> arr, int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(arr, begin, end);

            quickSort(arr, begin, partitionIndex-1);
            quickSort(arr, partitionIndex+1, end);
        }
    }

    public void saveToFile(Location location) {
        String longitude = Double.toString(location.getLongitude());
        String latitude = Double.toString(location.getLatitude());
        String textToWrite = longitude + " " + latitude + "\n";
        FileOutputStream fos = null;

        try {
            fos = openFileOutput(FILE_NAME, MODE_APPEND);
            fos.write(textToWrite.getBytes());
            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadFromFile() {
        FileInputStream fis = null;
        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String text;
            while ((text = br.readLine()) != null) {
                String[] ll = text.split(" ");
                Double longitudeDouble = Double.parseDouble(ll[0]);
                Double latitudeDouble = Double.parseDouble(ll[1]);
                longitudes.add(longitudeDouble);
                latitudes.add(latitudeDouble);
                distances.add(0.0);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateTextView() {
        t.setText("");
        StringBuilder text = new StringBuilder();
        for(int i = 0; i < longitudes.size(); i++) {
            text.append(longitudes.get(i)).append(" ").append(latitudes.get(i)).append("\n");
        }
        t.setText(text.toString());
    }

    public void calculateDistances(Location locationM) {
        for (int i = 0; i < longitudes.size(); i++) {
            distances.set(i, Math.sqrt(
                    (locationM.getLongitude() - longitudes.get(i)) * (locationM.getLongitude() - longitudes.get(i)) +
                            (locationM.getLatitude() - latitudes.get(i)) * (locationM.getLatitude() - latitudes.get(i))
            ));
        }

        quickSort(distances, 0, distances.size() - 1);
        updateTextView();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFromFile();

        t = (TextView) findViewById(R.id.textView);
        t.setTextSize(15);
        b = (Button) findViewById(R.id.button);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                saveToFile(location);
                calculateDistances(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configure_button();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }

        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        b.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates("gps", 500, 0, listener);
            }
        });
    }

}