package com.speechrecanimaltest.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.concurrent.Future;


public class MainActivity extends Activity {

    ImageView background = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        (findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                try {
                    startActivityForResult(i, REQUEST_OK);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
                }
            }
        });
        background = (ImageView)findViewById(R.id.resultimage);
    }

    protected static final int REQUEST_OK = 1;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_OK  && resultCode==RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ((TextView)findViewById(R.id.text1)).setText(thingsYouSaid.get(0));
            searchImage(thingsYouSaid);
        }
    }

    private void searchImage(ArrayList<String> thingsYouSaid) {
        if(thingsYouSaid.size()==0){
            return;
        }
        String mainString  = thingsYouSaid.get(0);
        if (loading != null && !loading.isDone() && !loading.isCancelled())
            return;

        // query googles image search api
        loading = Ion.with(MainActivity.this)
                .load(String.format("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s&start=%d&imgsz=medium", Uri.encode(mainString), 1))
                        // get the results as json
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        try {
                            if (e != null)
                                throw e;
                            // find the results and populate
                            JsonArray results = result.getAsJsonObject("responseData").getAsJsonArray("results");
                            String resultUrl =  results.get(0).getAsJsonObject().get("url").getAsString();
                            Ion.with(background)
                                    .centerCrop()
                                    .placeholder(R.drawable.abc_spinner_mtrl_am_alpha)
                                    .error(R.drawable.abc_switch_track_mtrl_alpha)
                                    .load(resultUrl);


                        }
                        catch (Exception ex) {
                            // toast any error we encounter (google image search has an API throttling limit that sometimes gets hit)
                            Toast.makeText(MainActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
                        }

                    }
                });

    }

    Future<JsonObject> loading;

}
