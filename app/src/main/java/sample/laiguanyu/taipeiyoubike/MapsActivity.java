package sample.laiguanyu.taipeiyoubike;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng mylatLng,bikelatLng;
    boolean navigationable = false;
    boolean run = true;
    private JSONObject jsonObject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        run = false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try{
            mMap.setMyLocationEnabled(true);
        }catch (SecurityException e){

        }
        mMap.setOnMarkerClickListener(onMarkerClickListener);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(MapsActivity.this, "getData2", Toast.LENGTH_LONG).show();
                            getData2();
                        }
                    });
                    try {
                        Thread.sleep(3*60*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        getLocation();
        final int sno = getIntent().getIntExtra("sno",0);
        if(sno != 0){
            listClick(sno);
        }else{
            LatLng sydney = new LatLng(25.017755, 121.458308);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,13));
            firstLocate();
        }

    }
    private void listClick(final int sno){
        navigationable = true;
        String urlParkingArea = "http://data.ntpc.gov.tw/api/v1/rest/datastore/382000000A-000352-001";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                urlParkingArea,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", "response = " + response.toString());
                        //Toast.makeText(MapsActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        try {
                            JSONObject o = response.getJSONObject("result").getJSONArray("records").getJSONObject(sno);
                            bikelatLng = new LatLng(o.getDouble("lat"), o.getDouble("lng"));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bikelatLng,17));
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", "error : " + error.toString());
                    }
                }
        );
        Volley.newRequestQueue(this).add(jsonObjectRequest);

        navigation();
    }
    private void getData2() {//取得資料 畫marker
        String urlParkingArea = "http://data.ntpc.gov.tw/api/v1/rest/datastore/382000000A-000352-001";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                urlParkingArea,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", "response = " + response.toString());
                        jsonObject = response;
                        //Toast.makeText(MapsActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        parserJson(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", "error : " + error.toString());
                    }
                }
        );
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void parserJson(JSONObject jsonObject) {
        try {
            JSONArray data = jsonObject.getJSONObject("result").getJSONArray("records");
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.getJSONObject(i);


                String sna = o.getString("snaen");
                Resources res = getResources();
                Configuration conf = res.getConfiguration();

                if(conf.locale.toString().startsWith("zh")){
                    sna = o.getString("sna");
                }else{

                }

                mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(o.getDouble("lat"), o.getDouble("lng")))
                                .title(sna)
                        .snippet(getResources().getString(R.string.tot)+o.getInt("tot")+"    "+getResources().getString(R.string.sbi)+o.getInt("sbi"))
                        //.icon(BitmapDescriptorFactory.fromResource(R.mipmap.parkingicon))
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    void getLocation(){
        //Toast.makeText(MapsActivity.this,"getLocation",Toast.LENGTH_LONG).show();
        //String provider = locationManager.getBestProvider(new Criteria(),true);
        //String provider = LocationManager.NETWORK_PROVIDER;
        String provider = LocationManager.GPS_PROVIDER;
        //Toast.makeText(this,provider,Toast.LENGTH_LONG).show();
        try{
            locationManager.requestLocationUpdates(provider, 3 * 1000, 5, locationListener);
        }catch(SecurityException e){
        }
    }
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            mylatLng = new LatLng(location.getLatitude(),location.getLongitude());
            if(navigationable){
                navigation();
            }


        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MapsActivity.this,getResources().getString(R.string.lossNavigation),Toast.LENGTH_LONG).show();
        }
    };
    void navigation(){
//        if(navigrtionState == 0){
//            return;
//        }
//        if(carLatLng == null){
//            Toast.makeText(this,R.string.carLocation_not_set,Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if(myLatLng == null){
//            Toast.makeText(this,R.string.location_not_really, Toast.LENGTH_SHORT).show();
//            return;
//        }
        if(mylatLng == null){
            Toast.makeText(MapsActivity.this,getResources().getString(R.string.gpspositioning),Toast.LENGTH_LONG).show();
            return;
        }
        mMap.clear();

        parserJson(jsonObject);
        //getData2();



        //mMap.addMarker(new MarkerOptions().position(parklatLng).title("marker"));

//        if(parklatLng == null){
//            Toast.makeText(MapsActivity.this,"parklatLng=null",Toast.LENGTH_LONG).show();
//            return;
//        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String strUrl;

                strUrl="https://maps.googleapis.com/maps/api/directions/json?origin="+mylatLng.latitude+","+mylatLng.longitude+"&destination="+bikelatLng.latitude+","+bikelatLng.longitude;


                StringBuilder sb=null;
                try {
                    URL url=new URL(strUrl);
                    URLConnection conn=url.openConnection();
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    sb=new StringBuilder();
                    String temp="";
                    while((temp=br.readLine())!=null){
                        sb.append(temp).append("\n");
                    }
                    JSONObject jo=new JSONObject(sb.toString());
                    String encodeString=jo.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                    points=new ArrayList<LatLng>();
                    decodePolylines(encodeString);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PolylineOptions polylineOptions=new PolylineOptions()
                                    .width(10)
                                    .color(Color.BLUE);
                            for(int i=0;i<points.size();i++){
                                polylineOptions.add(points.get(i));
                            }
                            mMap.addPolyline(polylineOptions);
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch(IOException e){
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    ArrayList<LatLng> points=new ArrayList<>();
    private void decodePolylines(String poly){
        int len = poly.length();
        int index = 0;
        int lat = 0;
        int lng = 0;
        while (index < len){
            int b, shift = 0, result = 0;
            do{
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do{
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            points.add(p);
        }

    }
    GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            bikelatLng = marker.getPosition();
            navigationable = true;
            navigation();
            return false;
        }
    };
    void firstLocate(){
        try {
            //String provider = locationManager.getBestProvider(new Criteria(),true);
            String provider= LocationManager.NETWORK_PROVIDER;
            Location firstLocation = locationManager.getLastKnownLocation(provider);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(firstLocation.getLatitude(),firstLocation.getLongitude()),14));
        }catch (SecurityException e){

        }catch (NullPointerException e){
            Toast.makeText(this,"NullPointerException",Toast.LENGTH_SHORT).show();
        }
    }
}
