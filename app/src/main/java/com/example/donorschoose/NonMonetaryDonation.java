package com.example.donorschoose;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Controller class for non-monetary donation page XML
 */
public class NonMonetaryDonation extends AppCompatActivity {

    String charityName;     // holds details of charity being donated to
    String charityID;

    /**
     * loads user profile page
     * @param view is the view calling the method using it's onClick method
     */
    public void loadUser(View view) { startActivity(new Intent(this, UserProfile.class)); }

    /**
     * loads home page
     * @param view is the view calling the method using it's onClick method
     */
    public void goHome(View view) { startActivity(new Intent(this, Home.class)); }

    /**
     * signs out user and loads login page
     * @param view is the view calling the method using it's onClick method
     */
    public void logout(View view)
    {
        FirebaseAuth.getInstance().signOut();
        startActivity((new Intent(this, Login.class)).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nonmonetary_donation); // load layout
        Bundle extras = getIntent().getExtras();                // retreive charity details passed by previous activity and store in global variables
        charityID = extras.getString("Charity ID");
        charityName = extras.getString("Charity Name");

        // retrieve user email from database and set in email field
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener(task -> {
            String userEmail = task.getResult().get("email").toString();
            ((EditText) findViewById(R.id.donorEmail)).setText(userEmail);
        });
    }

    /**
     * sends non-monetary donation data to chosen charity
     * @param view is the view calling the method using it's onClick method
     */
    public void sendData(View view)
    {
        TextView nameField = findViewById(R.id.donorName);
        TextView emailField = findViewById(R.id.donorEmail);
        TextView numberField = findViewById(R.id.donorPhone);
        TextView locationField = findViewById(R.id.pickUpAddress);
        TextView donationField = findViewById(R.id.donationData);

        String name = nameField.getText().toString();
        String email = emailField.getText().toString();
        String phNum = numberField.getText().toString();
        String pickUp = locationField.getText().toString();
        String donation = donationField.getText().toString();

        if (name.isEmpty())                                             // allowing phone number to remain blank (privacy)
        {                                                               // allowing pick up location to remain empty (may be unknown)
            nameField.setError("Please enter your name");
            nameField.requestFocus();
        }
        else if (email.isEmpty())
        {
            emailField.setError("Please enter your email address");
            emailField.requestFocus();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())      // input does not match pattern for email address
        {
            emailField.setError("Please enter a valid email!");
            emailField.requestFocus();
        }
        else if (donation.isEmpty())
        {
            donationField.setError("Please enter donation category");
            donationField.requestFocus();
        }
        else
        {
            String recipient = "donorschoosebd@gmail.com";      // TODO replace with charity email
            String subject = "You have a new donation!";
            String message = "Hello!\n\nYou have received a new donation on your Donors Choose profile. Here is the donor's information:\n\n" +
                    "Name: " + name + "\nEmail: " + email + "\nPhone Number: " + phNum + "\nPick Up Location: " + pickUp + "\nDonation Details: " + donation +
                    "\n\n Please contact the donor and arrange to meet at the specified location at a time that is convenient to both parties.\n\n" +
                    "Regards,\nThe Donors Choose Team";

            JavaMailAPI javaMailAPI = new JavaMailAPI(recipient, subject, message);   // create email
            javaMailAPI.execute();  // send email

            getDonationData();  // stores donation details in user donation history
        }
    }

    /**
     * Create new donation history document for user with no previous donations
     * @param userID is the ID of the user
     */
    private void createDoc(String userID)
    {
        Map<String, Object> donation = new HashMap<>();     // create document with required lists
        donation.put("charity", new ArrayList<String>());
        donation.put("date", new ArrayList<String>());
        donation.put("details", new ArrayList<String>());

        FirebaseFirestore.getInstance().collection("donations").document(userID).set(donation); // store document in firebase
    }

    /**
     * retrieves old donation data
     */
    private void getDonationData()
    {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();   // retrieve user ID
        FirebaseFirestore.getInstance().collection("donations").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (!documentSnapshot.exists()) // if user has no previous donations, donation history document will not exist
                {
                    createDoc(userID);  // create a new donation history document
                    addDonationData(userID, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>()); // add new details to document
                }
                else
                    addDonationData(userID, (List<String>) documentSnapshot.get("charity"), (List<String>) documentSnapshot.get("date"), (List<String>) documentSnapshot.get("details"));
                    // add new details to existing details
            }
        });
    }

    /**
     * adds new donation data to existing data
     * @param userID is the current user's ID
     * @param charities is the list of charities previously donated to
     * @param dates is the list of previous donation dates
     * @param details is the list of previous donation details
     */
    private void addDonationData(String userID, List<String> charities, List<String> dates, List<String> details)
    {
        charities.add(charityName);     // add new charity being donated to to list
        dates.add((new SimpleDateFormat("MMMM dd, yyyy")).format(Calendar.getInstance().getTime()));    // add new donation date to list
        details.add(((TextView) findViewById(R.id.donationData)).getText().toString()); // add new donation details to list

        // update lists in firebase
        FirebaseFirestore.getInstance().collection("donations").document(userID).update("charity", charities);
        FirebaseFirestore.getInstance().collection("donations").document(userID).update("date", dates);
        FirebaseFirestore.getInstance().collection("donations").document(userID).update("details", details);

        goCharity();    // go back to charity profile
    }

    /**
     * loads charity profile page
     */
    public void goCharity()
    {
        Intent charityProfile = new Intent(this, CharityProfile.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        charityProfile.putExtra("Charity ID", charityID);       // send charity details to load to new activity
        charityProfile.putExtra("Access Level", "View");
        startActivity(charityProfile);
    }

    /**
     * opens navigation menu
     * @param view is the view calling the method using it's onClick method
     */
    public void openMenu(View view)
    {
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.animate().translationX(0);   // bring navigation menu into view
        LinearLayout menuBackground = findViewById(R.id.menuBackground);
        menuBackground.setVisibility(View.VISIBLE); // bring background into view
    }

    /**
     * closes navigation menu
     * @param view is the view calling the method using it's onClick method
     */
    public void closeMenu(View view)
    {
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.animate().translationX(1000);    // hide navigation menu
        LinearLayout menuBackground = findViewById(R.id.menuBackground);
        menuBackground.setVisibility(View.GONE);        // hide background
    }
}