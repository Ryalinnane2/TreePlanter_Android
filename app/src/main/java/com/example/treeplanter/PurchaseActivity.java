package com.example.treeplanter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
    private String name;
    private Cart cart;
    private static int countB = 0;
    private int countO = 0;
    private int countW = 0;
    private static HashMap<String, String> purchaseInfo;
    private ArrayList<String> arrayList = new ArrayList<>();
    private int price = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        //cart = new Cart(treeLocation, treeType);
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
        }
        return super.onOptionsItemSelected(item);
    }
    // sign out if back button pressed
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void purchaseButton(View view) {
        if (!Cart.getPurchaseInfo().isEmpty()) {
            countB = cart.getCountB();
            countO = cart.getCountO();
            countW = cart.getCountW();
          //  purchaseInfo = new HashMap<>(cart.getPurchaseInfo());
            //Send info to checkout activity, where it will be added to firebase
            Intent intent = new Intent(PurchaseActivity.this, CheckoutActivityJava.class);
            //send cart object
            //intent.putExtra("countB",String.valueOf(countB));
            //intent.putExtra("countB",String.valueOf(countO));
            //intent.putExtra("countB",String.valueOf(countW));
            //intent.putExtra("map",purchaseInfo);
            intent.putExtra("arrayList",arrayList);
        /*
        intent.putExtra("treeLocation", treeLocation);
        intent.putExtra("treeName", name);
        intent.putExtra("treeType", treeType);
        */
            this.startActivity(intent);

            Cart.setCountB(countB);
            Cart.setCountO(countO);
            Cart.setCountW(countW);
        }else{
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void addMore_btn(View view){

        if (Cart.getPurchaseInfo() != null) {
            Intent intent = new Intent(PurchaseActivity.this, LandingActivity.class);
            this.startActivity(intent);
        }else{
            Toast.makeText(this, "Cart is empty, please add to Cart before continuing", Toast.LENGTH_SHORT).show();
        }

    }

    public void addToCart(View view){
        name = treeName_TV.getText().toString();
        if (name != null) {
            Cart.setType(treeType);
            Cart.setName(name);
            Cart.setLocation(treeLocation);
            Cart.addToMap(treeType);
            setPrice(treeType);
            String purchaseInfo = ("Tree Name: "+ name +'\n' + "Tree Location: "+ treeLocation + '\n' + "Tree Type: " + treeType + '\n' + "Price: " + price);
            Cart.setPurchaseInfo(purchaseInfo);

            //Toast.makeText(this, Integer.toString(cart.getCountB()), Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Item added to cart", Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, Cart.getPurchaseInfo().toString(), Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
        }
    }
    public void setPrice(String s){
        if (s.equals("Birch")){
            this.price = 1;
        }else if(s.equals("Willow")){
            this.price = 2;
        }else if (s.equals("Oak")){
            this.price = 3;
        }else{
            this.price = 0;
        }
    }
}


