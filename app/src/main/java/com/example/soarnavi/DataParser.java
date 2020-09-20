package com.example.soarnavi;


import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DataParser {

    DataParser(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.pobiednikaera);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String readLine = null;

        try {
            // While the BufferedReader readLine is not null
            while ((readLine = br.readLine()) != null) {
                Log.d("TEXT", readLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class Zone
    {

        List<Double> listX, listY;


        Zone()
        {
            listX = new ArrayList<>();
            listY = new ArrayList<>();
        }
    }
}
