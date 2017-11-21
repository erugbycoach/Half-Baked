package com.erugbycoach.halfbaked.Widget;

/**
 * Created by William D Howell on 11/21/2017.
 */

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.erugbycoach.halfbaked.R;
import com.erugbycoach.halfbaked.UI.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class UpdateWidgetService extends IntentService {
    public static final String LOG_TAG = UpdateWidgetService.class.getSimpleName();

    public static final String ACTION_UPDATE_WIDGET =
            "com.erugbycoach.halfbaked.action.update_widget";

    public UpdateWidgetService() {
        super("UpdateWidgetService");
    }

    //Set Intent
    public static void startActionWidgetUpdate(Context context) {
        Intent intent = new Intent(context, UpdateWidgetService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_UPDATE_WIDGET.equals(action)) {
                int previousRecipe = intent.getIntExtra(RecipeWidgetProvider.RECIPE_TAG, 0);
                handleUpdateWidget(previousRecipe);
            }
        }
    }

    private void handleUpdateWidget(final int previousRecipeNumber) {

        RequestQueue mRequestQueue = Volley.newRequestQueue(this);
        final List<String> recipesAvailable = new ArrayList<>();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                MainActivity.DATA_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject currentRecipe = response.getJSONObject(i);
                        String recipeName = currentRecipe.getString("name");
                        recipesAvailable.add(recipeName);
                    }

                    int totalRecipes = recipesAvailable.size();
                    int nextRecipe;
                    if (previousRecipeNumber < totalRecipes) {
                        nextRecipe = previousRecipeNumber + 1;
                    } else {
                        nextRecipe = 0;
                    }

                    //Update the widget view
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());
                    int[] appWidgetIds = appWidgetManager.getAppWidgetIds
                            (new ComponentName(getBaseContext(), RecipeWidgetProvider.class));

                    appWidgetManager.notifyAppWidgetViewDataChanged
                            (appWidgetIds, R.id.widget_lv_steps);

                    RecipeWidgetProvider.updateWidgets(getBaseContext(), appWidgetManager,
                            nextRecipe, totalRecipes, appWidgetIds);

                } catch (JSONException JSONE) {
                    JSONE.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error + "");
            }
        }
        );

        mRequestQueue.add(jsonArrayRequest);
    }
}

