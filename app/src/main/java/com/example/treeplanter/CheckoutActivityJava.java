package com.example.treeplanter;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.Set;
import java.util.TreeMap;

public class CheckoutActivityJava extends AppCompatActivity {
    /**
     * Reference to Stripe guide followed:  https://stripe.com/docs/payments/accept-a-payment#android
     * Note: Stripe server.js must be run locally on the desktop.
     *  - open command terminal, change directory to where the stripe.js is stored.
     *  - enter the command: 'npm start' to run the server.
     *
     * Else the following step can be followed here: https://github.com/stripe-samples/accept-a-card-payment#how-to-run-locally.
     */
    // 10.0.2.2 is the Android emulator's alias to localhost
    private static final String BACKEND_URL = "http://10.0.2.2:4242/";

    private OkHttpClient httpClient = new OkHttpClient();
    private String paymentIntentClientSecret;
    private Stripe stripe;
    private static FirebaseDatabase database;
    private static DatabaseReference myRef;
    private static FirebaseAuth mAuth;
    private static Context mContext;
    private String userEmail;
    private static FirebaseUser currentUser;
    private String json;
    private ArrayList<String> arrayList;
    private TreeMap<String, String> sortedMap;
    private TextView price_TV;

    //counter for types of trees purchased
    private int countB = 0;
    private int countO = 0;
    private int countW = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        Toolbar toolbar = findViewById(R.id.toolbar);

        //create a custom toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //change colour of status bar
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.stautsBar));

         //get Hashmap to add to firebase
        HashMap<String, String> map = Cart.getPurchaseInfoMap();
        //sort the HashMap by turning it into a tree map
        sortedMap = new TreeMap<>(map);

        //get ArrayList and display in ListView
        arrayList = Cart.getPurchaseInfo();
        ListView list = findViewById(R.id.list_view);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                arrayList);
        list.setAdapter(arrayAdapter);

        //remove item from list when clicked
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int item_selected = position;

                new AlertDialog.Builder(CheckoutActivityJava.this)
                        .setIcon(android.R.drawable.ic_delete)
                        .setTitle("Remove item from Cart")
                        .setMessage("Do you want to remove this item from your Cart?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //remove from tree map for payment purposes
                                removeFromTreeMap(item_selected);
                                // remove from the array for display purposes
                                Cart.removeItem(item_selected);
                                //update display price
                                price_TV.setText("€ " + Cart.getTotalPrice());
                                view.invalidate();
                                arrayAdapter.notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton("No",null)
                        .show();
                return true;

            }
        });
        //set counters
        countB = Cart.getCountB();
        countW = Cart.getCountW();
        countO = Cart.getCountO();
        Toast.makeText(this, countB + " " + countO + " " + countW, Toast.LENGTH_LONG).show();

        //Set price
        price_TV = findViewById(R.id.text_item_price);
        price_TV.setText("€ " + Cart.getTotalPrice());

        //Firebase set-up
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mContext = this;
        currentUser = mAuth.getCurrentUser();


        userEmail = currentUser.getEmail();

        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_ukVFEk9tOFrf0yHBMlxXTDhk00vWdBDKjT"
        );
        serverInfo();



        startCheckout();
    }

    public void removeFromTreeMap(int identifier){
        /****to remove from list, must first find unique value 'Tree Name'***/
        //get string at the position it has been removed.
        String currentItem = arrayList.get(identifier);
        //split each item and add to list
        String [] currentItemSplit = currentItem.split("\\n+");
        String treeNameRemove = currentItemSplit[0];
        String treeLocationRemove = currentItemSplit[1];
        String treeTypeRemove = currentItemSplit[2];

        // remove the key from the string to show value only
        treeNameRemove = treeNameRemove.replace("Tree Name:", "").trim();
        treeTypeRemove = treeTypeRemove.replace("Tree Type:", "").trim();
        treeLocationRemove = treeLocationRemove.replace("Tree Location:", "").trim();
        // update display price & tree counter for server price calculation

        if (treeTypeRemove.equals("Oak")){
            //remove the cost of tree
            Cart.setTotalPrice(Cart.getTotalPrice()-3);
            //remove 1 tree from counter
            Cart.setCountO(Cart.getCountO()-1);

        }else if (treeTypeRemove.equals("Willow")){
            //remove the cost of tree
            Cart.setTotalPrice(Cart.getTotalPrice()-2);
            //remove 1 tree from counter
            Cart.setCountW(Cart.getCountW()-1);

        }else if (treeTypeRemove.equals("Birch")){
            //remove the cost of tree
            Cart.setTotalPrice(Cart.getTotalPrice()-1);
            //remove 1 tree from counter
            Cart.setCountB(Cart.getCountB()-1);
        }
        // remove key and value based on value
        String keyName = "";
        String keyLocation = "";
        String keyType = "";
        Set<String> keys = sortedMap.keySet();
        for (String key: keys){
            if (sortedMap.get(key).equals(treeNameRemove)) {
                keyName = key;
            } else if(sortedMap.get(key).equals(treeTypeRemove)){
                keyType = key;
            }else if(sortedMap.get(key).equals(treeLocationRemove)){
                keyLocation = key;
            }
        }
        sortedMap.remove(keyName);
        sortedMap.remove(keyType);
        sortedMap.remove(keyLocation);
        Toast.makeText(mContext, "updated map: " + sortedMap, Toast.LENGTH_SHORT).show();

    }


    public void serverInfo() {
        //edit string to suit the tree types selected
        //this string will be sent to server to calculate final price.
        json = "{"
                + "\"currency\":\"eur\","
                + "\"numBirch\":\"" + countB + "\","
                + "\"numOak\":\"" + countO + "\","
                + "\"numWillow\":\"" + countW + "\","
                + "\"email\":\"" + userEmail + "\"}";
        /*
        StringBuilder b = new StringBuilder(json);
        b.insert(b.indexOf("eur") + 5, "\"numBirch\":\"" + countB + "\",");
        b.insert(b.indexOf("eur") + 5, "\"numOak\":\"" + countO + "\",");
        b.insert(b.indexOf("eur") + 5, "\"numWillow\":\"" + countW + "\",");
/*
        if (countB != 0) {
            b.insert(b.indexOf("eur") + 5, "\"numBirch\":\"" + countB + "\",");
        }
        if (countO != 0) {
            b.insert(b.indexOf("eur") + 5, "\"numOak\":\"" + countO + "\",");
        }
        if (countW != 0) {
            b.insert(b.indexOf("eur") + 5, "\"numWillow\":\"" + countW + "\",");
        }

 */
        //json = b.toString();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.treemenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            mAuth.signOut();
            //clear cart info
            Cart.clearHashMap();
            Cart.clearArrayList();
            sortedMap.clear();
            Cart.clearCountersAndPrice();
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void startCheckout() {
        Log.d("message", json);
        // Create a PaymentIntent by calling the server's /create-payment-intent endpoint.
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new PayCallback(this));


        Button pay = findViewById(R.id.pay_btn);
        pay.setOnClickListener((View view) -> {
            CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
            cardInputWidget.bringToFront();
            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
            if (params != null) {
                ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                        .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                stripe = new Stripe(
                        this,
                        PaymentConfiguration.getInstance(this).getPublishableKey()
                );
                stripe.confirmPayment(this, confirmParams);
            }else{
                Toast.makeText(mContext, "Card details not complete", Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void displayAlert(@NonNull String title,
                              @Nullable String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);
        builder.setPositiveButton("ok", null);
        builder.create().show();
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).string(),
                type
        );
        // The response from the server includes the Stripe publishable key and
        // PaymentIntent details.
        String stripePublishableKey = responseMap.get("publishableKey");
        paymentIntentClientSecret = responseMap.get("clientSecret");
    }

    private static final class PayCallback implements Callback {
        @NonNull
        private final WeakReference<CheckoutActivityJava> activityRef;

        PayCallback(@NonNull CheckoutActivityJava activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final CheckoutActivityJava activity = activityRef.get();
            if (activity == null) {
                return;
            }

            activity.runOnUiThread(() ->
                    Toast.makeText(
                            activity, "Error: " + e.toString(), Toast.LENGTH_LONG
                    ).show()
            );
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final CheckoutActivityJava activity = activityRef.get();
            if (activity == null) {
                return;
            }

            if (!response.isSuccessful()) {
                activity.runOnUiThread(() ->
                        Toast.makeText(
                                activity, "Error: " + response.toString(), Toast.LENGTH_LONG
                        ).show()
                );
            } else {
                activity.onPaymentSuccess(response);

            }
        }
    }


    public static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull
        private final WeakReference<CheckoutActivityJava> activityRef;

        PaymentResultCallback(@NonNull CheckoutActivityJava activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final CheckoutActivityJava activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();

            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                //add purchase details to user's database as a HashMap
                myRef.child("users").child(currentUser.getUid()).child("Purchases").push().setValue(Cart.getPurchaseInfoMap());
                //to payment confirmation page
                //clear cart info
                Cart.clearHashMap();
                Cart.clearArrayList();
                Cart.clearCountersAndPrice();

                toPayConfirmation();

            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage()
                );
            }

        }

        @Override
        public void onError(@NonNull Exception e) {
            final CheckoutActivityJava activity = activityRef.get();
            if (activity == null) {
                return;
            }

            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error", e.toString());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    public static void toPayConfirmation() {
        Intent changeActivity = new Intent(mContext, activity_payment_confirmation.class);
        mContext.startActivity(changeActivity);
    }


}



