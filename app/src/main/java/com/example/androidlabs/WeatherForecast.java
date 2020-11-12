package com.example.androidlabs;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class WeatherForecast extends AppCompatActivity
{
    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=ottawa,ca&APPID=7e943c97096a9784391a981c4d878b22&mode=xml&units=metric";
    private static final String UV_URL = "http://api.openweathermap.org/data/2.5/uvi?appid=7e943c97096a9784391a981c4d878b22&lat=45.348945&lon=-75.759389";
    private static final String IMAGE_ADDRESS = "http://openweathermap.org/img/w/";

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        (progressBar = findViewById(R.id.progress_bar)).setVisibility(View.VISIBLE);
        new ForecastQuery().execute(UV_URL, WEATHER_URL);
    }

    @SuppressLint({"StaticFieldLeak"})
    private class ForecastQuery extends AsyncTask<String, Integer, String>
    {
        private String uv;
        private String min;
        private String max;
        private String cur;

        Bitmap pic;

        @Override
        protected String doInBackground(String... args)
        {
            try
            {
                for(String arg : args)
                {
                    InputStream response = getInputStream(arg);

                    if(arg.equals(UV_URL)) // Could do this condition better if the URLs were organized properly
                    {
                        populateFieldsWith(response, true);
                        publishProgress(0);
                    }
                    else
                        populateFieldsWith(response, false);
                }
            }
            catch (Exception e)
            { Log.i("AsyncTask Failure", Objects.requireNonNull(e.getMessage())); }

            return "Done";
        }

        private int configureXMLParserAndGetEventType(XmlPullParser xpp, InputStream response) throws XmlPullParserException
        {
            xpp.setInput(response, "UTF-8"); //response is data from the server
            return xpp.getEventType(); //The parser is currently at START_DOCUMENT
        }

        private InputStream getInputStream(String urlString) throws IOException
        {
            //create a URL object of what server to contact:
            URL url = new URL(urlString);

            //open the connection
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            //wait for data and return
            return urlConnection.getInputStream();
        }

        private void populateFieldsWith(InputStream response, boolean isJson) throws IOException, XmlPullParserException, JSONException
        {
            if(isJson)
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    sb.append(line).append("\n");
                JSONObject jObject = new JSONObject(sb.toString());
                this.uv = String.format(Locale.CANADA, "%.2f", jObject.getDouble("value"));
            }
            else
            {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();

                int eventType = configureXMLParserAndGetEventType(xpp, response);

                while (eventType != XmlPullParser.END_DOCUMENT)
                {

                    if (eventType == XmlPullParser.START_TAG)
                    {
                        //If you get here, then you are pointing at a start tag
                        if (xpp.getName().equals("weather"))
                        {
                            String imageName = xpp.getAttributeValue(null, "icon") + ".png";
                            Log.i("FileName", imageName);
                            if (!fileExists(imageName))
                            {
                                if((this.pic = getImageAt(buildImageAddress(imageName))) != null)
                                    saveImage(this.pic, imageName);
                                Log.i(imageName, "Downloaded");
                            } else
                                {
                                this.pic = loadFile(imageName);
                                Log.i(imageName, "Found locally");
                            }
                            publishProgress(100);
                        }
                        else if (xpp.getName().equals("temperature"))
                        {
                            this.cur = xpp.getAttributeValue(null, "value");
                            publishProgress(25);
                            this.min = xpp.getAttributeValue(null, "min");
                            publishProgress(50);
                            this.max = xpp.getAttributeValue(null, "max");
                            publishProgress(75);
                        }
                    }
                    eventType = xpp.next(); //move to the next xml event and store it in a variable
                }
            }
        }

        private boolean fileExists(String fileName)
        {
            return getBaseContext().getFileStreamPath(fileName).exists();
        }

        private Bitmap loadFile(String imageFile)
        {
            FileInputStream fis = null;
            try { fis = openFileInput(imageFile); }
            catch (Exception e) { e.printStackTrace();  }
            return BitmapFactory.decodeStream(fis);
        }

        private String buildImageAddress(String imageName)
        {
            return IMAGE_ADDRESS + imageName;
        }

        private void saveImage(Bitmap image, String imageName) throws IOException
        {
            FileOutputStream outputStream = openFileOutput( imageName, Context.MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
            outputStream.flush();
            outputStream.close();
        }

        private Bitmap getImageAt(String address) throws IOException
        {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200)
                return BitmapFactory.decodeStream(connection.getInputStream());
            return null;
        }

        //Type 2
        @Override
        public void onProgressUpdate(Integer ... args)
        {
            progressBar.setVisibility(View.VISIBLE);
            for(int i = 0; i < new Random(10000000).nextInt(); i++) { i += 1; i-=1; }
            progressBar.setProgress(args[0]);
        }
        //Type3
        @Override
        public void onPostExecute(String fromDoInBackground)
        {
            ((ImageView)findViewById(R.id.disp_weather)).setImageBitmap(this.pic);
            ((TextView)findViewById(R.id.cur_temp)).setText(getString(R.string.current_temp, this.cur));
            ((TextView)findViewById(R.id.min_temp)).setText(getString(R.string.min_temp, this.min));
            ((TextView)findViewById(R.id.max_temp)).setText(getString(R.string.max_temp, this.max));
            ((TextView)findViewById(R.id.uv_rating)).setText(getString(R.string.uv_index, this.uv));
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}