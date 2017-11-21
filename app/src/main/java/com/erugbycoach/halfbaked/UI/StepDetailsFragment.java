package com.erugbycoach.halfbaked.UI;

/**
 * Created by William D Howell on 11/21/2017.
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.erugbycoach.halfbaked.Models.Instructions;
import com.erugbycoach.halfbaked.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class StepDetailsFragment extends Fragment implements ExoPlayer.EventListener{

    public static final String TAG = "tag";
    public static final String LIST_INDEX = "index";
    public static final String INSTRUCTIONS_LIST = "instructions";
    public static final String PLAYER_POSITION = "playerPosition";

    private SimpleExoPlayerView mExoPlayerView;
    private SimpleExoPlayer mExoPlayer;
    private Button previousButton;
    private Button nextButton;
    private RequestQueue mRequestQueue;
    private int recipeNumber;
    private int requestedStep;
    private TextView stepTextView;
    private boolean isTwoPanel;
    private ImageView thumbnailIV;
    private long playerPosition;

    List<Instructions> mInstructions;

    public StepDetailsFragment () {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_step_details, container, false);

        mExoPlayerView = rootView.findViewById(R.id.exoplayer);
        previousButton = rootView.findViewById(R.id.previous_button);
        nextButton = rootView.findViewById(R.id.next_button);
        stepTextView = rootView.findViewById(R.id.step_instructions_frag_tv);
        thumbnailIV = rootView.findViewById(R.id.exoplayer_replacement_thumbnail);

        mInstructions = new ArrayList<>();

        Intent intent = getActivity().getIntent();

        if (savedInstanceState != null) {
            requestedStep = savedInstanceState.getInt(LIST_INDEX);
            mInstructions = savedInstanceState.getParcelableArrayList(INSTRUCTIONS_LIST);
            playerPosition = savedInstanceState.getLong(PLAYER_POSITION);

            setupInstructions();
            setupExoPlayer();
            addOrRemoveButtons();
        } else if (isTwoPanel) {
            makeVolleyRequest();
        } else {
            //happening here
            recipeNumber = intent.getIntExtra(MainActivity.RECIPE_INDEX_NUMBER, 0);
            requestedStep = intent.getIntExtra(RecipeDetails.INSTRUCTION_STEP, 0);

            makeVolleyRequest();
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                releasePlayer();

                //Move to the next step
                requestedStep++;
                addOrRemoveButtons();
                setupInstructions();
                setupExoPlayer();

            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                releasePlayer();

                //Move to the next step
                requestedStep--;
                addOrRemoveButtons();
                setupInstructions();
                setupExoPlayer();
            }
        });

        return rootView;
    }

    private void makeVolleyRequest() {
        mRequestQueue = Volley.newRequestQueue(getContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, MainActivity.DATA_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject currentRecipe = response.getJSONObject(recipeNumber);

                    //Fetch Instruction object from Array
                    JSONArray requestedInstructions = currentRecipe.getJSONArray("steps");

                    for (int i = 0; i < requestedInstructions.length(); i++) {
                        JSONObject currentStep = requestedInstructions.getJSONObject(i);
                        int id = currentStep.getInt("id");
                        String shortDescription = currentStep.getString("shortDescription");
                        String description = currentStep.getString("description");
                        String videoUrl = currentStep.getString("videoURL");
                        String thumbnail = currentStep.getString("thumbnailURL");

                        mInstructions.add(new Instructions(id, shortDescription, description, videoUrl, thumbnail));
                    }

                    stepTextView.setText(mInstructions.get(requestedStep).getInstruction());
                    setupExoPlayer();
                    addOrRemoveButtons();

                } catch (JSONException JSONE) {
                    JSONE.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error + "");
            }
        });

        jsonArrayRequest.setTag(TAG);

        mRequestQueue.add(jsonArrayRequest);
    }


    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {

            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
            mExoPlayerView.setPlayer(mExoPlayer);


            mExoPlayer.addListener(this);


            String userAgent = Util.getUserAgent(getContext(), "Half Baked");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    getContext(), userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);

            if (playerPosition != 0L) {
                mExoPlayer.seekTo(playerPosition);
            }

            mExoPlayer.setPlayWhenReady(true);
        }
    }

    //Clear exoplayer
    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    //Set button states
    private void addOrRemoveButtons() {

        //Add or remove previous button
        if (requestedStep == 0) {
            previousButton.setVisibility(View.GONE);
        } else {
            previousButton.setVisibility(View.VISIBLE);
        }

        //Add or remove next button
        if (mInstructions.size() -1 == requestedStep) {
            nextButton.setVisibility(View.GONE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    //Setting up ExoPlayer
    private void setupExoPlayer() {
        //Validate viddeo exists
        if (!mInstructions.get(requestedStep).getVideoUrl().equals("")) {
            if (thumbnailIV != null) {
                thumbnailIV.setVisibility(View.INVISIBLE);
            }
            mExoPlayerView.setVisibility(View.VISIBLE);
            initializePlayer(Uri.parse(mInstructions.get(requestedStep).getVideoUrl()));
        } else if (!mInstructions.get(requestedStep).getThumbnailUrl().equals("")) {
            if (mInstructions.get(requestedStep).getThumbnailUrl().contains(".mp4")) {
                if (thumbnailIV != null) {
                    thumbnailIV.setVisibility(View.INVISIBLE);
                }
                mExoPlayerView.setVisibility(View.VISIBLE);
                showThumbnail(mInstructions.get(requestedStep).getThumbnailUrl());
            } else {
                thumbnailIV.setVisibility(View.VISIBLE);
                mExoPlayerView.setVisibility(View.INVISIBLE);
                imageViewThumbnail(mInstructions.get(requestedStep).getThumbnailUrl());
            }
        } else {
            mExoPlayerView.setVisibility(View.GONE);
        }
    }


    private void setupInstructions() {

        stepTextView.setText(mInstructions.get(requestedStep).getInstruction());
    }


    private void imageViewThumbnail(String thumbnailUrl) {
        Picasso.with(getContext()).load(thumbnailUrl).into(thumbnailIV);
    }


    private void showThumbnail(String url) {

        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
            mExoPlayerView.setPlayer(mExoPlayer);
        }

        //Create a bitmap from an MP4
        Bitmap thumbnail = null;
        try {
            thumbnail = convertMP4(url);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        //Attach bitmap to ExoPlayerView
        mExoPlayerView.setDefaultArtwork(thumbnail);
    }


    private Bitmap convertMP4(String path) throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(path, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(path);
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo " + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    //Setters
    public void setRecipeNumber(int recipeNumber) {
        this.recipeNumber = recipeNumber;
    }

    public void setRequestedStep(int requestedStep) {
        this.requestedStep = requestedStep;
    }

    public void setIsTwoPanel(boolean isTwoPanel) {
        this.isTwoPanel = isTwoPanel;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onResume() {
        super.onResume();

        if(mInstructions.size() != 0) {
            initializePlayer(Uri.parse(mInstructions.get(requestedStep).getVideoUrl()));
        }
    }

    // Save position and Index for state change like screen rotation.
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(INSTRUCTIONS_LIST, (ArrayList<Instructions>) mInstructions);
        outState.putInt(LIST_INDEX, requestedStep);

        playerPosition = mExoPlayer.getCurrentPosition();
        outState.putLong(PLAYER_POSITION, playerPosition);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }

        releasePlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        playerPosition = 0L;
    }
}

