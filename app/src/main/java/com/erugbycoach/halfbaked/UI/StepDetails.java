package com.erugbycoach.halfbaked.UI;

/**
 * Created by William D Howell on 11/21/2017.
 */

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.erugbycoach.halfbaked.R;


public class StepDetails extends AppCompatActivity {

    public static final String TEST = "test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_details);

        if (savedInstanceState == null) {
            StepDetailsFragment stepDetailsFragment = new StepDetailsFragment();

            FragmentManager fragmentManager = getSupportFragmentManager();

            fragmentManager.beginTransaction()
                    .add(R.id.step_details_frag_container, stepDetailsFragment)
                    .commit();
        }
    }
}