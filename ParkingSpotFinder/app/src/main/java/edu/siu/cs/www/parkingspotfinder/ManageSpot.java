package edu.siu.cs.www.parkingspotfinder;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.simplify.android.sdk.CardEditor;
import com.simplify.android.sdk.CardToken;
import com.simplify.android.sdk.Simplify;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ManageSpot extends AppCompatActivity {

    private final String SIMPLIFY_API_KEY = getResources().getString(R.string.simplify_api_key);
    private final String TAG = "MANAGE_SPOT_ACTIVITY::";

    private Button backArrowButton, pageInfoButton, addMoreTimeButton;

    private Simplify simplify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_spot_activity);

        backArrowButton = (Button) findViewById(R.id.backArrowButton);
        pageInfoButton = (Button) findViewById(R.id.pageInfoButton);
        addMoreTimeButton = (Button) findViewById(R.id.addMoreTimeButton);

        addMoreTimeButton.setEnabled(false);

        final CardEditor cardEditor = (CardEditor) findViewById(R.id.cardEditor);

        simplify = new Simplify();
        simplify.setApiKey(SIMPLIFY_API_KEY);

        // Listen for the changes in state of the card editor
        cardEditor.addOnStateChangedListener(new CardEditor.OnStateChangedListener() {
            @Override
            public void onStateChange(CardEditor cardEditor) {
                addMoreTimeButton.setEnabled(cardEditor.isValid());
            }
        });

        // Listen for when the user submits a new payment for processing
        addMoreTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simplify.createCardToken(cardEditor.getCard(), new CardToken.Callback(){
                    @Override
                    public void onSuccess(CardToken cardToken){
                        URL serviceURL = null;
                        HttpURLConnection con = null;
                        try{
                            // Configure connection to the PPaaS
                            serviceURL = new URL("https://parkr-payment-proc.herokuapp.com/charge.php");
                            con = (HttpURLConnection) serviceURL.openConnection();

                            con.setRequestMethod("POST");
                            con.setRequestProperty("User-Agent", "Mozilla/5.0");
                            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                            // This needs to be changed for when considering time and rate
                            String urlParams = "simplifyToken="+cardToken.getId()+"&amount=1000";

                            con.setDoOutput(true);
                            DataOutputStream writeStream = new DataOutputStream(con.getOutputStream());
                            writeStream.writeBytes(urlParams);
                            writeStream.flush();
                            writeStream.close();

                            int respCode = con.getResponseCode();
                            System.out.println("\nSending 'POST' request to URL : " + serviceURL);
                            System.out.println("Post parameters : " + urlParams);
                            System.out.println("Response Code : " + respCode);

                            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();
                            //print result
                            System.out.println(response.toString());

                        } catch (MalformedURLException e){
                            Log.d(TAG, "MALORMED URL DETECTED");
                            e.printStackTrace();
                        } catch (IOException e){
                            Log.d(TAG, "IO EXCEPTION THROWN");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable){
                        // Handle errors here
                    }
                });
            }
        });


        pageInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ManageSpot.this)
                        .setTitle("Information")
                        .setMessage("Here you can see and add more time to your current time.  You can also leave the spot if you so choose.")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Does nothing except close
                            }}).show();
            }
        });

        // Start the menu again if the user decides to go back
        backArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManageSpot.this, MenuActivity.class));
            }
        });
    }
}