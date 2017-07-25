package sample.laiguanyu.taipeiyoubike;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String url = "http://data.ntpc.gov.tw/api/v1/rest/datastore/382000000A-000352-001";
    ListView listView;
    ArrayAdapter arrayAdapter;
    boolean run = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listView);
        listView.setOnItemClickListener(onItemClickListener);
        getData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (run){
                    try {
                        Thread.sleep(3*60*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    reloadData(0);
                }
            }
        }).start();


    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reorganize, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_reorganize){
            //Toast.makeText(MainActivity.this,"menu_reorganize",Toast.LENGTH_LONG).show();
            reloadData(500);

        }
        if(item.getItemId() == R.id.menu_map){
            //Toast.makeText(MainActivity.this,"menu_map",Toast.LENGTH_LONG).show();
            startActivity(new Intent(MainActivity.this,MapsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
    void reloadData(final int time){
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(null);
                        Toast.makeText(MainActivity.this,getResources().getString(R.string.refresh),Toast.LENGTH_SHORT).show();
                    }
                });
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                            arrayAdapter.notifyDataSetChanged();
//
//                            listView.setAdapter(arrayAdapter);
                        getData();

                    }
                });
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        run = false;
    }

    private void getData() {
        String urlParkingArea = "http://data.ntpc.gov.tw/api/v1/rest/datastore/382000000A-000352-001";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                urlParkingArea,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("TAG", "response = " + response.toString());
                        //Toast.makeText(MainActivity.this,response.toString(),Toast.LENGTH_LONG).show();
                        String[] result = parserJson(response);
                        arrayAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,result);
                        listView.setAdapter(arrayAdapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", "error : " + error.toString());
                        Toast.makeText(MainActivity.this,getResources().getString(R.string.Networkstatusisabnormal),Toast.LENGTH_LONG).show();
                        Toast.makeText(MainActivity.this,getResources().getString(R.string.Pleasecheckyournetworkconnectionstatus),Toast.LENGTH_LONG).show();
                    }
                }
        );
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private String[] parserJson(JSONObject jsonObject) {
        try {
            JSONArray data = jsonObject.getJSONObject("result").getJSONArray("records");
            String[] result = new String[data.length()];
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.getJSONObject(i);
                String sna = o.getString("snaen");
                int total = o.getInt("tot");
                int sbi = o.getInt("sbi");
                long mday = o.getLong("mday");
                Resources res = getResources();
                Configuration conf = res.getConfiguration();

                if(conf.locale.toString().startsWith("zh")){
                    sna = o.getString("sna");
                }else{

                }
                String temp = sna +"\n"+getResources().getString(R.string.tot)+total+"   "+getResources().getString(R.string.sbi)+sbi+"\n"+getResources().getString(R.string.refreshtime)+mday;
                result[i] = temp;


            }
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(MainActivity.this,MapsActivity.class);
            intent.putExtra("sno",position);
            startActivity(intent);
        }
    };

}
