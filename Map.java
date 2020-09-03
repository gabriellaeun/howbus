package com.example.howbus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.android.gms.common.internal.constants.ListAppsActivityContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Map extends AppCompatActivity{
    TextView textView, textView2;
    ListView listView;

    ArrayList<String> busname = new ArrayList<>();
    ArrayList<String> busRouteId = new ArrayList<>();

    Fragment fragment;

    SupportMapFragment mapFragment;
    GoogleMap map;
    MarkerOptions busstation, myLocationMarker, searchlocation,busstop;
    private OkHttpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        StrictMode.enableDefaults();
        textView2 = findViewById(R.id.textView2);
        textView = findViewById(R.id.textView);
        textView2.setMovementMethod(new ScrollingMovementMethod());


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Toast.makeText(getApplicationContext(), "지도 불러옴.", Toast.LENGTH_SHORT).show();
                Log.d("Map","지도 준비됨.");
                map = googleMap;
                LatLng kwangwoon = new LatLng(37.619524, 127.059014);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(kwangwoon,17));
                busstationService();
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        final String arsId = marker.getSnippet();
                        busroute(arsId);
                        return false;
                    }
                });
            }
        });


        Button searchbutton = findViewById(R.id.searchbutton);
        searchbutton.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v){
                searchLoactionService();
            }
        }));

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e){
            e.printStackTrace();
        }
        Button locatebutton = findViewById(R.id.locatebutton);
        locatebutton.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startLocationService();
            }
        }));

        Button busstop = findViewById(R.id.busbutton);

    }

    private static class Busstation{
        String gpsX;
        String gpsY;
        String arsId;
        String stationNm;
    }
    private static class BusRoute{
        String busRouteName;
        String busRouteId;
    }

    public void nearbybusService(final Double latitude, final Double longitude){
        mClient = new OkHttpClient();
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{

                    String ratitus = "500";
                    String APIkey = "iqucPIkN%2Br9Zm%2FI6ALZlkapFDvJIgC1gySMHPDOd0aTP%2BbDajaKTYeE680W97iVo0EEg4PZcEGozg37slj%2FlcA%3D%3D";
                    String urlstr = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByPos?serviceKey="+APIkey+"&tmX="+longitude+"&tmY="+latitude+"&radius="+ratitus;
                    Request request = new Request.Builder().url(urlstr).build();
                    Response response = mClient.newCall(request).execute();
                    String xml = response.body().string();
                    List<Busstation> data = parse(xml);
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private List<Busstation> parse(String xml){
        try{
            return new BusParser().parse(xml);
        } catch (XmlPullParserException | IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private class BusParser{
        public List<Busstation> parse(String xml) throws XmlPullParserException, IOException {
            List<Busstation> busstationList = new ArrayList<>();
            Busstation busstation = null;
            boolean isItem = false;
            String text = "";
            int size = 0;
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false);
            parser.setInput(new StringReader(xml));
            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT){
                String tagName = parser.getName();
                switch(eventType){
                    case XmlPullParser.START_TAG:
                        if(tagName.equals("itemList")){
                            busstation = new Busstation();
                            isItem = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if(isItem) {
                            text = parser.getText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(isItem) {
                            if (tagName.equals("arsId")) {
                                busstation.arsId = text;
                            }  else if (tagName.equals("gpsY")) {
                                busstation.gpsY = text;
                            } else if (tagName.equals("gpsX")) {
                                    busstation.gpsX = text;
                            } else if (tagName.equals("stationNm")) {
                                busstation.stationNm = text;
                                size++;
                            } else if (tagName.equals("itemList")) {
                                busstationList.add(busstation);
                                isItem = false;
                            }
                        }
                        break;
                    default:
                }
                eventType = parser.next();
            }
            try{
                textView2.setText("주변버스정류장\n");
                if(size==0)
                {
                    textView2.setText("주변에 버스가 없습니다");
                }
                for(int i = 0; i < size; i++){

                    Busstation busstop1 = busstationList.get(i);
                    final String arsId = busstop1.arsId;
                    String stationName = busstop1.stationNm;
                    Double gpsX = Double.parseDouble(busstop1.gpsY);
                    Double gpsY = Double.parseDouble(busstop1.gpsX);
                    textView2.append("\n"+stationName);
                    //makemarker(gpsX,gpsY,stationName); //오류발생
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.d("fail", "Bud parser error");
            }
            return busstationList;
        }
    }

    public void searchLoactionService(){
        EditText search = (EditText)findViewById(R.id.search_input);
        String value = search.getText().toString();
        mClient = new OkHttpClient();

        Geocoder geocoder = new Geocoder(Map.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(value,1);
        }catch (IOException e){
            Log.e("search", "geoLocate: IOException: "+ e.getMessage());
        }
        if(list.size()>0){
            Address address = list.get(0);

            final Double searchlatitute = address.getLatitude();
            final Double searchlongitude = address.getLongitude();
            String locate = address.getAddressLine(0);
            LatLng searchcurpoint = new LatLng(searchlatitute,searchlongitude);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(searchcurpoint,17));
            searchlocation = new MarkerOptions();
            searchlocation.position(searchcurpoint);
            searchlocation.title(locate);
            searchlocation.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
            map.addMarker(searchlocation);

            new Thread(new Runnable(){
                @Override
                public void run(){
                    try{
                        String APIkey = "iqucPIkN%2Br9Zm%2FI6ALZlkapFDvJIgC1gySMHPDOd0aTP%2BbDajaKTYeE680W97iVo0EEg4PZcEGozg37slj%2FlcA%3D%3D";
                        String urlstr = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByPos?serviceKey="+APIkey+"&tmX="+searchlongitude+"&tmY="+searchlatitute+"&radius=500";

                        Request request = new Request.Builder().url(urlstr).build();
                        Response response = mClient.newCall(request).execute();
                        String xml = response.body().string();
                        List<Busstation> data = parse(xml);

                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }

            }).start();

            Log.d("success", "geoLocate: fount a location:"+address.toString());
        }
    }

    public void makemarker(Double gpsX, Double gpsY, String stationNm) {
        LatLng curpoint = new LatLng(gpsY,gpsX);
        busstop = new MarkerOptions();
        busstop.position(curpoint);
        busstop.title(stationNm);
        busstop.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstation));
        map.addMarker(busstop);
    }

    public void getarsId(){
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final String arsId = marker.getSnippet();

                Button recombutton = (Button) findViewById(R.id.recombutton);
                recombutton.setOnClickListener((new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        busroute(arsId);
                    }
                }));
                return false;
            }
        });

    }

    public void busroute(final String arsId) {

        mClient = new OkHttpClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String APIkey = "iqucPIkN%2Br9Zm%2FI6ALZlkapFDvJIgC1gySMHPDOd0aTP%2BbDajaKTYeE680W97iVo0EEg4PZcEGozg37slj%2FlcA%3D%3D";
                    String urlstr = "http://ws.bus.go.kr/api/rest/stationinfo/getRouteByStation?serviceKey=" + APIkey + "&arsId=" + arsId;

                    Request request = new Request.Builder().url(urlstr).build();
                    Response response = mClient.newCall(request).execute();
                    String xml = response.body().string();
                    List<BusRoute> data = parse_route(xml);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private List<BusRoute> parse_route(String xml){
        try{
            return new BusrouteParser().parse(xml);
        } catch (XmlPullParserException | IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private class BusrouteParser{
        public List<BusRoute> parse(String xml) throws XmlPullParserException, IOException {
            List<BusRoute> busrouteList = new ArrayList<>();
            BusRoute busRoute = null;
            boolean isItem = false;
            String text = "";
            int size = 0;
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false);
            parser.setInput(new StringReader(xml));
            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT){
                String tagName = parser.getName();
                switch(eventType){
                    case XmlPullParser.START_TAG:
                        if(tagName.equals("itemList")){
                            busRoute = new BusRoute();
                            isItem = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if(isItem) {
                            text = parser.getText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(isItem) {
                            if (tagName.equals("busRouteId")) {
                                busRoute.busRouteId = text;
                            }  else if (tagName.equals("busRouteNm")) {
                                    busRoute.busRouteName = text;
                                    size++;
                            } else if (tagName.equals("itemList")) {
                                busrouteList.add(busRoute);
                                isItem = false;
                            }
                        }
                        break;
                    default:
                }
                eventType = parser.next();
            }
            try{
                busname.clear();
                busRouteId.clear();
                textView2.setText("버스 목록\n");
                if(size==0)
                {
                    textView2.setText("지나가는 버스가 없습니다");
                }
                for(int i = 0; i < size; i++){

                    BusRoute busstop1 = busrouteList.get(i);
                    String busRouteName = busstop1.busRouteName;
                    String busrouteId = busstop1.busRouteId;
                    busRouteId.add(busrouteId);
                    busname.add(busRouteName);
                }
                Button locatebutton = findViewById(R.id.recombutton);
                locatebutton.setOnClickListener((new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        setlistview(busname, busRouteId);
                    }
                }));

            }catch (Exception e){
                e.printStackTrace();
                Log.d("fail", "Bud Route parser error");
            }

            return busrouteList;
        }
    }

    public void setlistview(final ArrayList<String> busname, ArrayList<String> busroute){
        listView = (ListView) findViewById(R.id.listview);
        final ListAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,busname);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String name = adapter.getItem(i).toString();
                int idxnum = busname.indexOf(name);
                String busrouteId = busRouteId.get(idxnum);
                Intent intent = new Intent(Map.this,BusNavigate.class);
                intent.putExtra("busname",busrouteId.toString());
                startActivity(intent);

            }
        });

    }

    public void busstationService(){
        String json = null;

        try {
            InputStream is = getAssets().open("seoulbusstop.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            JSONObject jsonObject = new JSONObject(json);
            JSONArray array = jsonObject.getJSONArray("DATA");

            for (int i = 0; i < array.length(); i++) {

                JSONObject o = array.getJSONObject(i);
                Double buslatitude = o.getDouble("ycode");
                Double buslongitude = o.getDouble("xcode");
                String busname = o.getString("stop_nm");
                String arsId = o.getString("stop_no");
                LatLng buscurPoint = new LatLng(buslatitude, buslongitude);
                busstation = new MarkerOptions();
                busstation.position(buscurPoint);
                busstation.title(busname);
                busstation.snippet(arsId);
                busstation.icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop));
                map.addMarker(busstation);

            }
        } catch (Exception e){
                e.printStackTrace();
            }


    }

    class GPSListner implements LocationListener {
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();
            String message = "위도 : " + latitude + "\n경도 : " + longitude;
            textView.setText(message);
            showCurrentLocation(latitude, longitude);
        }
        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String Provider, int Status, Bundle extras) {
        }
    }

    private void showCurrentLocation(Double latitude, Double longitude){
        LatLng curPoint = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint,17));
        showMyLocationMarker(curPoint);
    }

    private void showMyLocationMarker(LatLng curPoint){
        if(myLocationMarker == null){
            myLocationMarker = new MarkerOptions();
            myLocationMarker.position(curPoint);
            myLocationMarker.title("내 위치");
            myLocationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocate));
            map.addMarker(myLocationMarker);
        } else{
            myLocationMarker.position(curPoint);
        }
    }

    public void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();
                String message = "위도 : " + latitude + "\n경도 : " + longitude;
                showCurrentLocation(latitude, longitude);
                textView.setText(message);
                Button busstop = findViewById(R.id.busbutton);
                busstop.setOnClickListener((new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        nearbybusService(latitude,longitude);
                    }
                }));

            }
            GPSListner gpsListner = new GPSListner();
            long minTime = 30000;
            float minDistance = 0;
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListner);
            Toast.makeText(getApplicationContext(), "내 위치확인 요청함", Toast.LENGTH_SHORT).show();

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

}




