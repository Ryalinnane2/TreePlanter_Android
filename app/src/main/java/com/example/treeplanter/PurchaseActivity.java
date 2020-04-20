package com.example.treeplanter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
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
    private ArrayList<String> arrayList = new ArrayList<>();
    private int price = 0;
    private String purchaseInfo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
        // set Toolbar instead of ActionBar
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

        //set views to variables
        treeType_TV = findViewById(R.id.treeType_TV);
        treeName_TV = findViewById(R.id.treeName_TV);
        treeLocation_TV = findViewById(R.id.treeLocation_TV);
        treeName_TV.setInputType(InputType.TYPE_CLASS_TEXT);
        treeType = LandingActivity.getTreeType();

        // Get the location of tree selected from MapsActivity
        Intent i = getIntent();
        treeLocation = i.getStringExtra("treeLocation");
        treeLocation_TV.setText("Tree location: " + treeLocation);
        treeType_TV.setText("Tree Type: " + treeType);
    }
    //add menu to toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.treemenu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //when items selected in the menu do the following
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()== R.id.logout){
            mAuth.signOut();
            //clear cart
            Cart.clearHashMap();
            Cart.clearArrayList();
            Cart.clearCountersAndPrice();
            finish();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //when purchase button is pressed do the following
    public void purchaseButton(View view) {
        if (!Cart.getPurchaseInfo().isEmpty()) {
            //Send info to checkout activity, where it will be displayed in the listview
            Intent intent = new Intent(PurchaseActivity.this, CheckoutActivityJava.class);
            this.startActivity(intent);

        }else{
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
        }
    }

    //allow users to add more trees to their cart
    public void addMore_btn(View view){
        // if is not empty send user back to selec type of next tree
        if (Cart.getPurchaseInfo() != null) {
            Intent intent = new Intent(PurchaseActivity.this, LandingActivity.class);
            this.startActivity(intent);
        }else{
            //else let user know cart is empty
            Toast.makeText(this, "Cart is empty, please add to Cart before continuing", Toast.LENGTH_SHORT).show();
        }

    }

    public void setPurchaseInfo(){
        Cart.setType(treeType);
        Cart.setName(name);
        Cart.setLocation(treeLocation);
        //add info to hashmap for adding to firebase
        Cart.addToMap();
        //update the price based on the the tree type added to cart
        Cart.setPriceAndCount(treeType);
        //price changes depeding on tree type selected.
        setPrice(treeType);
        //update Cart with prices
        //Cart.setPrice(price);
        // add purchase info to arraylist for displaying in listview
        purchaseInfo = ("Tree Name: " + name + '\n' + "Tree Location: " + treeLocation + '\n' + "Tree Type: " + treeType + '\n' + "Price: " + price);
        // add info to arraylist in cart
        Cart.setPurchaseInfo(purchaseInfo);
        Toast.makeText(this, "Item added to cart", Toast.LENGTH_SHORT).show();
    }

    public void addToCart(View view){
        // get the name entered on this activity
        name = treeName_TV.getText().toString();
        if (name != null) {
            if (Cart.containsStr(name)) {
                new AlertDialog.Builder(PurchaseActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Duplicate Name")
                        .setMessage("You have already added a tree with the same name, would you like to add another tree with this name?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //remove from tree map for payment purposes
                                setPurchaseInfo();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }else {
                setPurchaseInfo();
            }

        }else{
            //user must enter name before adding to cart
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
        }
    }
    public void setPrice(String s){
        //update price depending on tree type selected
        //this price is for display purposes only
        //prices for billing is calculated on stripe server using same prices
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


