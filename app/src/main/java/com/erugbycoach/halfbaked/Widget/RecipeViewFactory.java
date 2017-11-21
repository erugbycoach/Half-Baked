package com.erugbycoach.halfbaked.Widget;

/**
 * Created by William D Howell on 11/21/2017.
 */

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.erugbycoach.halfbaked.Models.Ingredient;
import com.erugbycoach.halfbaked.R;
import com.erugbycoach.halfbaked.UI.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class RecipeViewFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final String LOG_TAG = RecipeViewFactory.class.getSimpleName();

    private Context context;
    private int requestedRecipe;
    private List<Ingredient> mIngredients;
    private int totalRecipes;

    public RecipeViewFactory(Context applicationContext, int requestedRecipe, int totalRecipes) {
        context = applicationContext;
        this.requestedRecipe = requestedRecipe;
        this.totalRecipes = totalRecipes;
    }

    @Override
    public void onCreate() {
        mIngredients = new ArrayList<>();
    }

    @Override
    public void onDataSetChanged() {
        RequestQueue mRequestQueue = Volley.newRequestQueue(context);

        if (requestedRecipe == totalRecipes - 1) {
            requestedRecipe = 0;
        } else {
            requestedRecipe++;
        }
        mIngredients.clear();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, MainActivity.DATA_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject currentRecipe = response.getJSONObject(requestedRecipe);
                    String currentRecipeName = currentRecipe.getString("name");
                    mIngredients.add(new Ingredient(0.0, "fakeMesurement", currentRecipeName));

                    //Make Ingredients List
                    JSONArray currentIngredientsList = currentRecipe.getJSONArray("ingredients");
                    for (int i = 0; i < currentIngredientsList.length(); i++) {
                        JSONObject currentIngredient = currentIngredientsList.getJSONObject(i);
                        double quantity = currentIngredient.getDouble("quantity");
                        String measure = currentIngredient.getString("measure");
                        String ingredient = currentIngredient.getString("ingredient");

                        mIngredients.add(new Ingredient(quantity, measure, ingredient));
                    }

                } catch (JSONException JSONE) {
                    JSONE.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error + "");
            }
        });

        mRequestQueue.add(jsonArrayRequest);

    }

    @Override
    public void onDestroy() {
        //Cleanup Volley
        mIngredients.clear();
    }

    @Override
    public int getCount() {
        //Return item count
        if (mIngredients.isEmpty()) {
            return 0;
        }
        return mIngredients.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //implement changes to one row/view
        if (mIngredients.isEmpty() || mIngredients.size() == 0) return null;

        Ingredient currentIngredient = mIngredients.get(position);
        String stepIngredient = currentIngredient.getIngredient();
        String capIngredient = stepIngredient.substring(0, 1).toUpperCase() + stepIngredient.substring(1);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);

        if (position == 0) {
            views.setTextViewText(R.id.list_item_widget, capIngredient.toUpperCase() + ":" );
        } else {
            views.setTextViewText(R.id.list_item_widget, capIngredient);
        }

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // Delay the try to account for Bind/unbind times
        //Adapted from http://grepcode.com/file/repository.grepcode.com/java/ext/
        // com.google.android/android/4.0.1_r1/android/widget/RemoteViewsAdapter
        // .java#RemoteViewsAdapter.0sUnbindServiceDelay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

