package com.bounajm.fares.bucketlist;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.bounajm.fares.bucketlist.ListAndHistoryActivity.editMode;
import static com.bounajm.fares.bucketlist.ListAndHistoryActivity.todoItem;
import static com.bounajm.fares.bucketlist.LoginActivity.connected;
import static com.bounajm.fares.bucketlist.LoginActivity.myRef;
import static com.bounajm.fares.bucketlist.LoginActivity.userID;

public class AddItemActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Date dueDate = new Date();
    private EditText nameEt, descriptionEt, longEt, latEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        nameEt = (EditText)findViewById(R.id.name_et);
        descriptionEt = (EditText)findViewById(R.id.description_et);
        longEt = (EditText)findViewById(R.id.longEt);
        latEt = (EditText)findViewById(R.id.latEt);

        final Button datePickBtn = (Button)findViewById(R.id.selectDateBtn);
        Button saveCreate = (Button)findViewById(R.id.saveOrCreate);

        final Calendar c2 = Calendar.getInstance();
        final DateFormat txtDate = new SimpleDateFormat("dd MMM, yyyy");
        final DatePickerDialog.OnDateSetListener d2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                c2.set(year, month, dayOfMonth);
                dueDate = c2.getTime();
                datePickBtn.setText(txtDate.format(c2.getTime()));
            }
        };

        datePickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editMode){
                    c2.setTime(todoItem.dueDate);
                }
                int mYear = c2.get(Calendar.YEAR);
                int mMonth = c2.get(Calendar.MONTH);
                int mDay = c2.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(AddItemActivity.this, d2, mYear, mMonth, mDay).show();
            }
        });

        if(editMode){
            setTitle("Edit");
            saveCreate.setText("Save");
            nameEt.setText(todoItem.name);
            descriptionEt.setText(todoItem.description);
            datePickBtn.setText(txtDate.format(todoItem.dueDate));
            dueDate = todoItem.dueDate;
            if(todoItem.locationSet){
                longEt.setText(String.valueOf(todoItem.longitude));
                latEt.setText(String.valueOf(todoItem.latitude));
            }
        }else{
            datePickBtn.setText(txtDate.format(new Date()));
            setTitle("Create");
            saveCreate.setText("Create");
        }

    }

    private Marker markerPosition;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(editMode && todoItem.locationSet){
            LatLng pos = new LatLng(todoItem.latitude, todoItem.longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));
            markerPosition = mMap.addMarker(new MarkerOptions().position(pos).title("Location").draggable(true));
        }else{
            try {
                Geocoder geocoder = new Geocoder(this);
                List<Address> dubaiLocaltion = geocoder.getFromLocationName("Dubai", 1);
                if(dubaiLocaltion.size() > 0){
                    LatLng dubaiLoc = new LatLng(dubaiLocaltion.get(0).getLatitude(), dubaiLocaltion.get(0).getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dubaiLoc, 10));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if(markerPosition != null){
                    markerPosition.remove();
                }
                markerPosition = mMap.addMarker(new MarkerOptions().position(point).title("Location").draggable(true));
                longEt.setText(String.valueOf(markerPosition.getPosition().longitude));
                latEt.setText(String.valueOf(markerPosition.getPosition().latitude));
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                markerPosition = marker;
            }
        });
    }

    private boolean validateForm() {


        if(!TextUtils.isEmpty(longEt.getText().toString()) && TextUtils.isEmpty(latEt.getText().toString())) {
            longEt.setError("Leave both latitude & longitude fields empty if you don't want to save a location.");
            return false;
        }else if(TextUtils.isEmpty(longEt.getText().toString()) && !TextUtils.isEmpty(latEt.getText().toString())){
            latEt.setError("Leave both latitude & longitude fields empty if you don't want to save a location.");
            return false;
        }

        String email = nameEt.getText().toString();
        if (TextUtils.isEmpty(email)) {
            nameEt.setError("Required.");
            return false;
        }

        nameEt.setError(null);
        longEt.setError(null);
        latEt.setError(null);
        return true;
    }

    public void newEntrySave(View view){

        if(!validateForm()){
            return;
        }

        ListAndHistoryActivity.ListItem x = new ListAndHistoryActivity.ListItem();
        x.name = nameEt.getText().toString();
        x.description = descriptionEt.getText().toString();
        x.dueDate = dueDate;
        x.completed = false;

        if(!TextUtils.isEmpty(longEt.getText().toString()) && !TextUtils.isEmpty(latEt.getText().toString())){
            x.locationSet = true;
            x.longitude = Double.parseDouble(longEt.getText().toString());
            x.latitude = Double.parseDouble(latEt.getText().toString());
        }else{
            Toast.makeText(this,
                    "No Location Saved.",
                    Toast.LENGTH_SHORT).show();
            x.locationSet = false;
        }


        if(connected){
            x.isOnline = true;
        }else{
            x.isOnline = false;
        }

        if(editMode){
            myRef.child(userID()).child("bucketItem").child(todoItem.dbKey).setValue(x);
        }
        else
        {
            myRef.child(userID()).child("bucketItem").push().setValue(x);
        }

        startActivity(new Intent(this, ListAndHistoryActivity.class));
        finish();
    }
}
