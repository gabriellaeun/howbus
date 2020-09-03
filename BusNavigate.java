package com.example.howbus;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BusNavigate extends AppCompatActivity {
    TextView textView;
    Intent intent;
    ListView listView;
    private OkHttpClient mClient;
    ArrayList<String> Direction = new ArrayList<>();
    ArrayList<String> StationNm = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_navigate);
        intent = getIntent();
        StrictMode.enableDefaults();

        final String busRouteId = intent.getStringExtra("busname");

        textView = findViewById(R.id.textView3);
        textView.setText(busRouteId);
        textView.setMovementMethod(new ScrollingMovementMethod());

        Button loadbutton = (Button)findViewById(R.id.loadbutton);
        loadbutton.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v){
                busroutename(busRouteId);
            }
        }));


    }
    private static class BusRouteName{
        String direction;
        String stationNm;
    }

    public void busroutename(final String busrouteId) {

        mClient = new OkHttpClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String APIkey = "iqucPIkN%2Br9Zm%2FI6ALZlkapFDvJIgC1gySMHPDOd0aTP%2BbDajaKTYeE680W97iVo0EEg4PZcEGozg37slj%2FlcA%3D%3D";
                    String urlstr = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?serviceKey=" + APIkey + "&busRoutedId=" + busrouteId;

                    Request request = new Request.Builder().url(urlstr).build();
                    Response response = mClient.newCall(request).execute();
                    String xml = response.body().string();
                    List<BusRouteName> data = parse_route(xml);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private List<BusRouteName> parse_route(String xml){
        try{
            return new BusrouteParser().parse(xml);
        } catch (XmlPullParserException | IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private class BusrouteParser{
        public List<BusRouteName> parse(String xml) throws XmlPullParserException, IOException {
            List<BusRouteName> busroutenameList = new ArrayList<>();
            BusRouteName busRoutename = null;
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
                            busRoutename = new BusRouteName();
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
                            if (tagName.equals("direction")) {
                                busRoutename.direction = text;
                            }  else if (tagName.equals("stationNm")) {
                                busRoutename.stationNm = text;
                            } else if (tagName.equals("itemList")) {
                                size++;
                                busroutenameList.add(busRoutename);
                                isItem = false;
                            }
                        }
                        break;
                    default:
                }
                eventType = parser.next();
            }

            for(int i = 0; i < size; i++){

                BusRouteName busstopname = busroutenameList.get(i);
                String direction = busstopname.direction;
                String stationNm = busstopname.stationNm;
                Direction.add(direction);
                StationNm.add(stationNm);
            }
            Button button = findViewById(R.id.button);
            button.setOnClickListener((new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    textView.setText("1");
                    setlistview(Direction, StationNm);
                }
            }));


            return busroutenameList;
        }
    }

    public void setlistview(final ArrayList<String> direction, ArrayList<String> stationNm){
        listView = (ListView) findViewById(R.id.buslistview);
        ListAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,stationNm);
        listView.setAdapter(adapter);

    }
}
