package com.example.treeplanter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
    private EditText treeName;
    private EditText treeLocation;
    private EditText treeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();


       treeName = findViewById(R.id.treeName_TV);
       treeLocation = findViewById(R.id.treeLocation_TV);
       treeType = findViewById(R.id.treeType_TV);
       treeName.setInputType(InputType.TYPE_CLASS_TEXT);
       treeLocation.setInputType(InputType.TYPE_CLASS_TEXT);
       treeType.setInputType(InputType.TYPE_CLASS_TEXT);
    }

    public void purchaseButton(View view) {
        String name = treeName.getText().toString();
        String location = treeLocation.getText().toString();
        String type = treeType.getText().toString();

        //create a hashmap of data from this purchase
        HashMap<String,String> purchaseInfo = new HashMap<>();
        purchaseInfo.put("Tree Name",name);
        purchaseInfo.put("Tree Location",location);
        purchaseInfo.put("Tree Type",type);

        // add hashmap to firebase database
        myRef.child("users").child("Purchases").push().setValue(purchaseInfo);

        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();



    }

}
