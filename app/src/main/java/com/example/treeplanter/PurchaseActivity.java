package com.example.treeplanter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PurchaseActivity extends AppCompatActivity {


    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private EditText treeName_TV;
    private TextView treeLocation_TV;
    private TextView treeType_TV;
    private String treeType;
    private String treeLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        treeType_TV = findViewById(R.id.treeType_TV);
        treeName_TV = findViewById(R.id.treeName_TV);
        treeLocation_TV = findViewById(R.id.treeLocation_TV);
        treeName_TV.setInputType(InputType.TYPE_CLASS_TEXT);
        treeType = LandingActivity.getTreeType();

        // Get the location of tree selected from MapsActivity
        Intent i = getIntent();
        treeLocation = i.getStringExtra("treeLocation");
        //treeType = i.getStringExtra("treeType");
        treeLocation_TV.setText("Tree location: " + treeLocation);
        treeType_TV.setText("Tree Type: " + treeType);

    }

    public void purchaseButton(View view) {
        String name = treeName_TV.getText().toString();

        //create a hashmap of data from this purchase
        HashMap<String, String> purchaseInfo = new HashMap<>();
        //add users selected data to hashmap
        purchaseInfo.put("Tree Name", name);
        purchaseInfo.put("Tree Location", treeLocation);
        purchaseInfo.put("Tree Type", treeType);

        //Get the logged in user
        FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        // add purchase hashmap under users UUID in database
        myRef.child("users").child(currentUser.getUid()).child("Purchases").push().setValue(purchaseInfo);
        //pop up message
        Intent intent = new Intent(PurchaseActivity.this,GooglePayActivity.class);
        this.startActivity(intent);

    }

}


