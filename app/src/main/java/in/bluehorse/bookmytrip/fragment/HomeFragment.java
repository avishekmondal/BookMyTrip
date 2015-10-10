package in.bluehorse.bookmytrip.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import in.bluehorse.bookmytrip.R;


public class HomeFragment extends Fragment {

    private View rootView;
    private MapView mapView;
    private GoogleMap googleMap;
    private TextView tvPickupLocation, tvSetLocation;
    private ImageView marker;
    View locationButton;

    private LatLng latlng;
    private double latitude = 0.0, longitude = 0.0;

    private Geocoder geocoder;
    private List<Address> addresses;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);

        initialize(savedInstanceState);
        onClick();

        return rootView;
    }

    private void initialize(Bundle savedInstanceState){

        tvPickupLocation = (TextView) rootView.findViewById(R.id.tvPickupLocation);
        tvSetLocation = (TextView) rootView.findViewById(R.id.tvSetLocation);
        marker = (ImageView) rootView.findViewById(R.id.marker);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        googleMap = mapView.getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(getActivity());

        // Get the button view
        locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).zoom(12f).tilt(70).build();
        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        marker.animate().translationY(-marker.getHeight() / 2);
        tvSetLocation.animate().translationY(-tvSetLocation.getHeight() / 2);


        try {
            new GetLocationAsync().execute(latitude, longitude);

        } catch (Exception e) {

        }

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                if (!(googleMap.getMyLocation() == null)) {

                    latitude = googleMap.getMyLocation().getLatitude();
                    longitude = googleMap.getMyLocation().getLongitude();

                    //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(latitude, longitude)).zoom(15f).build();
                    googleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));

                    marker.animate().translationY(-marker.getHeight() / 2);
                    tvSetLocation.animate().translationY(-tvSetLocation.getHeight() / 2);


                    try {
                        new GetLocationAsync().execute(latitude, longitude);

                    } catch (Exception e) {

                    }
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    private void onClick() {

        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                //x_point = (int) (marker.getX() + (marker.getWidth() / 2));
                //y_point = (int) (marker.getY() - (marker.getHeight()));
                //projection = googleMap.getProjection();
                //point_x_y = new Point(x_point,y_point);
                //latlng = googleMap.getProjection().fromScreenLocation(point_x_y);

                latlng = googleMap.getCameraPosition().target;
                latitude = latlng.latitude;
                longitude = latlng.longitude;

                try {
                    new GetLocationAsync().execute(latitude, longitude);

                } catch (Exception e) {

                }

            }
        });

        tvSetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvSetLocation.setVisibility(View.GONE);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(latitude, longitude)).zoom(17f).build();
                googleMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));


            }
        });

        /*locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                googleMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(new CameraPosition.Builder()
                                .target(new LatLng(latitude, longitude)).zoom(12f).build()));


            }
        });*/
    }

    private class GetLocationAsync extends AsyncTask<Double, Void, String> {

        double x, y;

        @Override
        protected void onPreExecute() {
            tvPickupLocation.setText("Go To Pin");

        }

        @Override
        protected String doInBackground(Double... params) {

            x = params[0];
            y = params[1];

            try {
                geocoder = new Geocoder(getActivity(), Locale.ENGLISH);
                addresses = geocoder.getFromLocation(x, y, 1);
                Log.v("addresses", addresses.toString());

            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                tvPickupLocation.setText(addresses.get(0).getAddressLine(0)
                        + ", " + addresses.get(0).getAddressLine(1));
                tvPickupLocation.setPadding(50, 0, 30, 0);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

}
