package com.example.treeplanter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LandingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public static String treeType;
    private Spinner dropdown;
    //private String mapTreeLocation;
    //create a list of items for the spinner.
    //private String[] items = new String[]{"Birch", "Oak", "Willow"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
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





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.treemenu, menu);
        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()== R.id.logout){
            mAuth.signOut();
            finish();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);

        } else if (item.getItemId()== R.id.purchase){
            Intent intent = new Intent(this,PurchaseActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    // sign out if back button pressed
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAuth.signOut();
    }

    public void location_btn(View view) {
        //Change to Maps page and send the selected tree type data
        Intent intent = new Intent(this,MapsActivity.class);
        this.startActivity(intent);
    }

    public static String getTreeType(){
        return treeType;
    }
}
