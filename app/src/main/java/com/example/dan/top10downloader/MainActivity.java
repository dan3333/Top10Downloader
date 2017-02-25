package com.example.dan.top10downloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listApps;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = ( ListView)findViewById(R.id.xmlListView);



        downloadUrl(String.format(feedUrl,feedLimit));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu,menu);
        return true;
    }

    @Override
    // called whenever a menu option is selected
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id)
        {
            case R.id.mnuFree:
                feedUrl ="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=&d/xml";
                break;
            case R.id.mnuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=&d/xml";
                break;
            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.mnu10:
                break;
            case R.id.mnu25:
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        downloadUrl(String.format(feedUrl,feedLimit));
        return true;

    }

    private void downloadUrl (String feedUrl)
    {
        Log.d(TAG, "downloadUrl: starting Async task");
        DownloadData downloadData = new DownloadData();
        downloadData.execute(feedUrl);
        Log.d(TAG, "downloadUrl: done");
    }

    private class DownloadData extends AsyncTask<String, Void, String>{
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter value: "+s);
            ParseApplications parseApp = new ParseApplications();
            parseApp.parse(s);

            //ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this,R.layout.list_item,parseApp.getApplications());
            //listApps.setAdapter(arrayAdapter);

            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this,R.layout.list_record2,parseApp.getApplications());
            listApps.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG,"doInBackground: starts with "+params[0]);
            String rssFeed = downloadXML (params[0]);
            return rssFeed;

        }

        private String downloadXML (String urlPath){
            StringBuilder xmlResult = new StringBuilder();

            try
            {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                int res = connection.getResponseCode();
                Log.d(TAG, "downloadXML: response code was "+res);
                InputStream inputStream = connection.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(streamReader);

                int charsRead;
                char[] inputBuffer = new char[500];
                while (true)
                {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead<0)
                    {
                        break;
                    }
                    if (charsRead>0)
                    {
                        xmlResult.append(String.copyValueOf(inputBuffer,0,charsRead));
                    }
                }
                reader.close();
            }
            catch (MalformedURLException e)
            {
                Log.e(TAG, "downloadXML: Invalid URL "+e.getMessage());
            }
            catch (IOException e)
            {
                Log.e(TAG, "downloadXML: IO Exception reading data: "+e.getMessage());
            }
            catch (SecurityException e)
            {
                Log.e(TAG, "downloadXML: Security Exception. Needs Permission? "+ e.getMessage());
                e.printStackTrace();
            }

            return xmlResult.toString();
        }
    }
}
