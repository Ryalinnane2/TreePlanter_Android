package com.example.treeplanter;

import android.app.Activity;
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
import android.widget.ImageView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.GooglePayConfig;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.util.Map.Entry;
import java.util.TreeMap;

public class CheckoutActivityJava extends AppCompatActivity {
    /**
     * This example collects card payments, implementing the guide here: https://stripe.com/docs/payments/accept-a-payment#android
     * <p>
     * To run this app, follow the steps here: https://github.com/stripe-samples/accept-a-card-payment#how-to-run-locally
     */
    // 10.0.2.2 is the Android emulator's alias to localhost
    private static final String BACKEND_URL = "http://10.0.2.2:4242/";

    private OkHttpClient httpClient = new OkHttpClient();
    private String paymentIntentClientSecret;
    private Stripe stripe;
    private static FirebaseDatabase database;
    private static DatabaseReference myRef;
    private static FirebaseAuth mAuth;
    private boolean paymentResultSuccess;
    private static Context mContext;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private int imageID;
    private long itemPrice;
    private ItemInfo mTreeItem;
    private String userEmail;
    private static FirebaseUser currentUser;
    private static String treeLocation;
    private static String treeName;
    private static String treeType;
    private static HashMap<String, String> purchaseInfo;
    private String json;
    private ArrayList<String> arrayList;

    //private static Cart cart = new Cart();
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
        TreeMap<String, String> sortedMap = new TreeMap<>(map);

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
                                Cart.removeItem(item_selected);
                                arrayAdapter.notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton("No",null)
                        .show();
                return true;

            }
        });

        //Set price
        TextView price_TV = findViewById(R.id.text_item_price);
        price_TV.setText("€ " + Cart.getPrice());

        //Firebase set-up
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        paymentResultSuccess = false;
        mContext = this;
        currentUser = mAuth.getCurrentUser();


        userEmail = currentUser.getEmail();
        getIntentInfo();
        PaymentConfiguration.init(
                getApplicationContext(),
                "pk_test_ukVFEk9tOFrf0yHBMlxXTDhk00vWdBDKjT"
        );

        startCheckout();

    }

    public void getIntentInfo() {
        json = "{"
                + "\"currency\":\"eur\","
                + "\"email\":\"" + userEmail + "\"}";

        StringBuilder b = new StringBuilder(json);

        if (Cart.getCountB() != 0) {
            countB = Cart.getCountB();
            b.insert(b.indexOf("eur") + 5, "\"numBirch\":\"" + countB + "\",");
        }
        if (Cart.getCountO() != 0) {
            countO = Cart.getCountO();
            b.insert(b.indexOf("eur") + 5, "\"numOak\":\"" + countO + "\",");
        }
        if (Cart.getCountW() != 0) {
            countW = Cart.getCountW();
            b.insert(b.indexOf("eur") + 5, "\"numWillow\":\"" + countW + "\",");
        }
        json = b.toString();

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
        // Create a PaymentIntent by calling the server's /create-payment-intent endpoint.
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create-payment-intent")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new PayCallback(this));

        Button pay = findViewById(R.id.popup_btn);
        pay.bringToFront();;
        pay.setOnClickListener((View view) -> {
            CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
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
                              @Nullable String message,
                              boolean restartDemo) {
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
        // For added security, our sample app gets the publishable key from the server
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
                //log info
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Log.w("onSuccess: ", gson.toJson(paymentIntent));

                //add purchase details to user's database as a HashMap
                myRef.child("users").child(currentUser.getUid()).child("Purchases").push().setValue(Cart.getPurchaseInfoMap());
                //to payment confirmation page
                toPayConfirmation();

            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage(),
                        false
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
            activity.displayAlert("Error", e.toString(), false);
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
    /*
    public void popup(android.view.View view) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = 400;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        Button popup_btn = findViewById(R.id.popup_btn);
        //make button invisible
        //popup_btn.setVisibility(View.INVISIBLE);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                //make button visible again
                //popup_btn.setVisibility(View.VISIBLE);
                return true;

            }
        });
    }
    */



}



