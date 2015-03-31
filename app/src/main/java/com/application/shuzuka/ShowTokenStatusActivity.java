package com.application.shuzuka;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;
import android.widget.ViewSwitcher;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by dinesh on 28/3/15.
 */
public class ShowTokenStatusActivity extends ActionBarActivity {

    RequestQueue requestQueue;
    public ShowTokenStatusActivity() {

    }
    TextView estimated_time;
    TextSwitcher token_count;
    String token_id="";
    SharedPreferences _prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.token_status_activity);
        token_count = (TextSwitcher)findViewById(R.id.token_count);
        estimated_time = (TextView)findViewById(R.id.estimated_time);
        requestQueue = Volley.newRequestQueue(this);
        _prefs = PreferenceManager.getDefaultSharedPreferences(this);
        token_id = _prefs.getString("acquired_token_id","308");
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
        token_count.setInAnimation(in);
        token_count.setOutAnimation(out);
        token_count.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                TextView myText = new TextView(ShowTokenStatusActivity.this);
                myText.setGravity(Gravity.CENTER);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);
                myText.setLayoutParams(params);
                myText.setTextSize(30);
                myText.setTextColor(Color.BLACK);
                return myText;
            }
        });
    }


    public void onResume() {
        super.onResume();
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleWithFixedDelay(new StatusTimerTask(), 0,10, TimeUnit.SECONDS);
    }

    class StatusTimerTask implements Runnable {

        @Override
        public void run() {
            checkTokenStatus();
        }
    }

    public void checkTokenStatus() {
        String url = "http://shizuka2-noalpha.rhcloud.com/patient-api/status-of-token";
        StringRequest getTokenDetailsRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.w("Inside continuous",response);
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject messageObject = jsonObject.getJSONObject("message");
                            token_count.setText(messageObject.getString("before_you"));
                            estimated_time.setText("Estimated time " + messageObject.getString("expected_time"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", "Error");
                    }
                }
        ){
           @Override
          protected Map<String, String> getParams()
        {
            Map<String, String>  params = new HashMap<String, String>();
            params.put("id",token_id);
            Log.w("Token id",token_id);
            return params;

        }};
        requestQueue.add(getTokenDetailsRequest);
    }
}
