package com.application.shuzuka;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wunderlist.slidinglayer.SlidingLayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity  implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener,View.OnClickListener {

    GoogleMap googleMap;
    SlidingLayer slidingLayer;
    RequestQueue requestQueue;
    Button token_btn,previous_token_status_btn;
    SharedPreferences _prefs;
    private Map<Marker,Integer> allMapMarkers;
    TextView sliding_layer_doctor_name,sliding_layer_token_count_text,sliding_layer_doctor_address;
    ProgressBar sliding_layer_progress_bar,get_token_progress_bar;
    Locations selected_location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allMapMarkers = new HashMap<Marker, Integer>();
        slidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer);
        sliding_layer_doctor_name = (TextView) findViewById(R.id.doctor_name);
        sliding_layer_token_count_text = (TextView) findViewById(R.id.token_count);
        sliding_layer_doctor_address = (TextView) findViewById(R.id.doctor_address);
        sliding_layer_progress_bar = (ProgressBar)findViewById(R.id.sliding_layer_progress_bar);
        get_token_progress_bar = (ProgressBar)findViewById(R.id.get_token_progress_bar);
        previous_token_status_btn = (Button) findViewById(R.id.previous_token_status_btn);
        _prefs = PreferenceManager.getDefaultSharedPreferences(this);
        token_btn = (Button)findViewById(R.id.get_token);
        token_btn.setOnClickListener(this);
        previous_token_status_btn.setOnClickListener(this);
        requestQueue = Volley.newRequestQueue(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL );
        //this.googleMap.addMarker(new MarkerOptions().position(harlapur).title("Karnataka"));
        getMapData();
        //this.googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(kotturu,hombal),10));
        this.googleMap.setOnMarkerClickListener(this);
        getMapData();

    }

    @Override
    public void onResume() {
        super.onResume();
        boolean token_acquired = _prefs.getBoolean("token_acquired",false);
        if (token_acquired) {
            if(previous_token_status_btn !=null){
                previous_token_status_btn.setVisibility(View.VISIBLE);
            }
        }
    }

    public void getMapData()
    {
        String url = "http://shizuka2-noalpha.rhcloud.com/patient-api/list-doctor-locations";
        StringRequest getTokenRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject locations = new JSONObject(response);
                            JSONArray jsonArray = locations.getJSONArray("message");
                            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            Log.w("Locations",response);
                            for(int i=0;i<jsonArray.length();i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                double lat = jsonObject.getDouble("latitude");
                                double lng = jsonObject.getDouble("longitude");
                                int location_id = jsonObject.getInt("id");
                                LatLng latLng = new LatLng(lat,lng);
                                Marker marker = googleMap.addMarker(new MarkerOptions()
                                        .position(latLng));
                                builder.include(latLng);
                                allMapMarkers.put(marker, location_id);
                            }

                            final int padding = 15; // offset from edges of the map in pixels
                            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                                @Override
                                public void onMapLoaded() {
                                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
                                    googleMap.moveCamera(cu);
                                    googleMap.animateCamera(cu);
                                }
                            });
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
        ) ;
        requestQueue.add(getTokenRequest);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        slidingLayer.setVisibility(View.VISIBLE);
        clearSlidingLayerData();
        slidingLayer.openLayer(true);
        sliding_layer_progress_bar.setVisibility(View.VISIBLE);
        final Integer doctor_clinic_id = allMapMarkers.get(marker);
        String url = "http://shizuka2-noalpha.rhcloud.com/patient-api/details-of-doctor-location";
        StringRequest getDoctorDetailsRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONObject messageObject = jsonObject.getJSONObject("message");
                            Locations location = new Locations();
                            location.setDoctorClinicId(doctor_clinic_id);
                            location.setDoctorClinicName(messageObject.getString("doctor_name"));
                            location.setDoctorClinicAddress(messageObject.getString("address"));
                            location.setLatLng(messageObject.getDouble("doctor_location_latitude"), messageObject.getDouble("doctor_location_longitude"));
                            location.setTotalTokensCheckedIn(messageObject.getInt("empty_token_count"));
                            location.setTotalTokensCount(messageObject.getInt("total_token_count"));
                            selected_location = location;
                            setDataonSlidingLayer(location);

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
        ) {@Override
        protected Map<String, String> getParams()
        {
            Map<String, String>  params = new HashMap<String, String>();
            params.put("token_timestamp", "2015-04-28 00:00:00");
            params.put("doctor_location_id", doctor_clinic_id+"");

            return params;
        }};
        requestQueue.add(getDoctorDetailsRequest);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (slidingLayer.isOpened()) {
                    slidingLayer.closeLayer(true);
                    return true;
                }

            default:
                return super.onKeyDown(keyCode, event);
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.get_token:
                getTokenFromtheClinic();
                break;
            case R.id.previous_token_status_btn:
                showTokenStatus();
                break;
        }

    }

    public void showTokenStatus()
    {
       MainActivity.this.finish();
       Intent intent = new Intent(this,ShowTokenStatusActivity.class);
       startActivity(intent);
    }

    private void getTokenFromtheClinic() {

        String url = "http://shizuka2-noalpha.rhcloud.com/patient-api/acquire-token";
        hideTokenButton();
        Log.w("URL for acquired token",url);
        StringRequest getTokenRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            SharedPreferences.Editor editor = _prefs.edit();
                            editor.putBoolean("token_acquired",true);
                            editor.putString("acquired_token_number",jsonObject.getString("tkoen_serial_no"));
                            editor.putString("acquired_token_id",jsonObject.getString("token_id"));
                            editor.commit();
                            showTokenStatus();
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
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("patient_name", "Dineshswamy");
                params.put("patient_id", "1");
                params.put("doctor_location_id", selected_location.getDoctorClinicId()+"");


                return params;
            }
        };
        requestQueue.add(getTokenRequest);
    }

    private void hideTokenButton(){
        token_btn.setVisibility(View.GONE);

    }

    public void setDataonSlidingLayer(Locations locations){
        sliding_layer_doctor_name.setText(locations.getDoctorClinicName());
        sliding_layer_doctor_address.setText(locations.getDoctorClinicAddress());
        sliding_layer_token_count_text.setText(locations.getTotalTokensCheckedIn() +" out of "+ locations.getTotalTokensCount());
        sliding_layer_progress_bar.setVisibility(View.GONE);
    }

    public void clearSlidingLayerData(){
        if (sliding_layer_doctor_name !=null)
            sliding_layer_doctor_name.setText(" ");
        if (sliding_layer_doctor_address !=null)
            sliding_layer_doctor_address.setText(" ");
        if (sliding_layer_token_count_text !=null)
            sliding_layer_token_count_text.setText(" ");
    }

}
