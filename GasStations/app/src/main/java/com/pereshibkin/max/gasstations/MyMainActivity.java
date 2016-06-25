package com.pereshibkin.max.gasstations;

import android.graphics.Point;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MyMainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    Map<String, String> mMapLocation2Description = new HashMap<String, String>();

    private static String MakeLocationMapKeyString( LatLng aLocation ) {
        return  String.format("%f-%f",aLocation.latitude,aLocation.longitude);
    }
    private void RegisterLocationDescription( LatLng aLocation, String strDescription ) {
        mMapLocation2Description.put(MakeLocationMapKeyString(aLocation),strDescription);
    }
    private  String GetLocationDescription( LatLng aLocation ) {
        return mMapLocation2Description.get(MakeLocationMapKeyString(aLocation));
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.setMapType( GoogleMap.MAP_TYPE_HYBRID );
        //mMap.setMapType( GoogleMap.MAP_TYPE_SATELLITE );
        //mMap.setMapType( GoogleMap.MAP_TYPE_TERRAIN );

        mMap.setOnMarkerClickListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
//        UiSettings uis = mMap.getUiSettings();
//        uis.setZoomControlsEnabled(true);
//        uis.setCompassEnabled(true);
//        uis.setIndoorLevelPickerEnabled(true);
//        uis.setScrollGesturesEnabled(true);
//        uis.setZoomGesturesEnabled(true);
//        uis.setRotateGesturesEnabled(true);
//        uis.setTiltGesturesEnabled(true);
//        uis.setAllGesturesEnabled(true);
//        uis.setMyLocationButtonEnabled(true);
//        uis.setMapToolbarEnabled(true);
        LatLng myKharkiv = new LatLng(49.991002, 36.230676);
//        mMap.addMarker( new MarkerOptions().position( myKharkiv ).title( "Marker in Kharkiv" ) );
        mMap.moveCamera( CameraUpdateFactory.newLatLng( myKharkiv ) );
        mMap.animateCamera( CameraUpdateFactory.zoomTo(13.0f ) );

        String strPathToXML = getResources().getString(R.raw.my_map_data);
        Document xmlDoc = null;
        try {
            //File f = new File( strPathToXML );
            //FileInputStream fis = new FileInputStream( f );
            InputStream fis = getResources().openRawResource( getResources().getIdentifier( "my_map_data", "raw", getPackageName() ) );
            xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( fis );
        } catch ( ParserConfigurationException e ) {
            Log.e( "ParserConfigException", e.getMessage() );
        } catch ( SAXException e ) {
            Log.e( "SAXException", e.getMessage() );
        } catch ( IOException e ) {
            Log.e("IOException", e.getMessage());
        }
        if( xmlDoc != null) {
            Element root = xmlDoc.getDocumentElement();
            if (root != null) {
                NodeList nodesPlacemark = root.getElementsByTagName("Placemark");
                int idxPlacemark, cntPlacemarks = nodesPlacemark.getLength();
                for (idxPlacemark = 0; idxPlacemark < cntPlacemarks; ++idxPlacemark) {
                    Node currNodePlacemark = nodesPlacemark.item(idxPlacemark);
                    String strNodeNamePlacemark = currNodePlacemark.getNodeName().toLowerCase();
                    if (strNodeNamePlacemark.equals("placemark")) {
                        String strName = "", strDescription = "";
                        LatLng aLatLng = null;
                        for( Node currNodeProp = currNodePlacemark.getFirstChild(); currNodeProp != null; currNodeProp = currNodeProp.getNextSibling() ) {
                            String strNodeNameProp = currNodeProp.getNodeName().toLowerCase();
                            switch (strNodeNameProp) {
                                case "name":
                                    strName += currNodeProp.getTextContent();
                                    break;
                                case "description":
                                    strDescription += currNodeProp.getTextContent();
                                    break;
                                case "point": {
                                    for( Node currSubnode = currNodeProp.getFirstChild(); currSubnode != null; currSubnode = currSubnode.getNextSibling() ) {
                                        String strNodeNameCoordinates = currSubnode.getNodeName().toLowerCase();
                                        if (strNodeNameCoordinates.equals( "coordinates" )) {
                                            String str = currSubnode.getTextContent();
                                            StringTokenizer tokens = new StringTokenizer(str, ",");
                                            String strLong = tokens.nextToken();
                                            String strLat = tokens.nextToken();
                                            aLatLng = new LatLng(Double.parseDouble(strLat), Double.parseDouble(strLong));
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        if( aLatLng != null ) {
                            RegisterLocationDescription(aLatLng, strDescription);
                            mMap.addMarker(
                                    new MarkerOptions()
                                            .position(aLatLng)
                                            .title(strName)
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.gas_station_003))
                            );
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker aMarker) {
        Projection projection = mMap.getProjection();
        //aMarker.setVisible( false );
        LatLng markerLocation = aMarker.getPosition();
        Point screenPosition = projection.toScreenLocation(markerLocation);

//        String str = String.format("%s\n%.3f long\n%.3f lat", aMarker.getTitle(), markerLocation.longitude, markerLocation.latitude);
        String str = String.format("<b>Name:</b> %s<br/><b>Description:</b> %s<br/><b>Long:</b> %.3f<br/><b>Lat:</b> %.3f<br/>", aMarker.getTitle(), GetLocationDescription(markerLocation), markerLocation.longitude, markerLocation.latitude);

//        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
        Toast toast = Toast.makeText(getApplicationContext(), Html.fromHtml(str), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.LEFT, screenPosition.x, screenPosition.y);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MyMain Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.pereshibkin.max.gasstations/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "MyMain Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.pereshibkin.max.gasstations/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
