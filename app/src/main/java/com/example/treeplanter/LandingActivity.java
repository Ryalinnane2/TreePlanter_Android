package com.example.treeplanter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import com.google.firebase.auth.FirebaseAuth;

public class LandingActivity extends AppCompatActivity {
    /*
    References: https://developer.android.com/docs
     */

    private FirebaseAuth mAuth;
    public static String treeType;
    private Spinner dropdown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //setActionBar(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //change colour of status bar
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.stautsBar));

        mAuth = FirebaseAuth.getInstance();

        dropdown = findViewById(R.id.treeType_Spinner);
        //Dropdown Menu (spinner) for Tree Type selection
        //Adapter describes how the items are displayed
        ArrayAdapter adapter = new ArrayAdapter<String>(
                this,
                //{Birch,Oak,Willow} in values/strings.xml
                R.layout.colour_spinner_layout,
                getResources().getStringArray(R.array.Spinner_Items)
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        //set the spinners adapter
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            //Reference: https://developer.android.com/guide/topics/ui/controls/spinner#java
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected. Retrieve the selected item
                //convert to String and store in Variable treeType
                treeType = parent.getItemAtPosition(pos).toString();
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });


    }

    // sign out if back button pressed
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAuth.signOut();
    }

    public void location_btn(View view) {
        //Change to Maps page
        Intent intent = new Intent(this,MapsActivity.class);
        this.startActivity(intent);
    }

    public static String getTreeType(){
        return treeType;
    }
}
