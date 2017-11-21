package com.erugbycoach.halfbaked.UI;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.erugbycoach.halfbaked.Adapters.RecipeAdapter;
import com.erugbycoach.halfbaked.Models.Recipe;
import com.erugbycoach.halfbaked.R;
import com.erugbycoach.halfbaked.Testing.SimpleIdlingResource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.ListItemClicked {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String RECIPE_INDEX_NUMBER = "recipeNumber";
    public static final String RECIPE_LIST = "recipeList";
    public static final String DATA_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    RequestQueue mRequestQueue;
    List<Recipe> recipes;
    SimpleIdlingResource mIdlingResource;

    //Menu Variables
    private List<String> recipeNames;
    private RecipeAdapter rvAdpter;
    private ArrayAdapter<String> arrayAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    GridLayoutManager gridLayoutManager;


    @BindView(R.id.recipe_list_rv)
    RecyclerView recipeListRV;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.left_drawer)
    ListView mDrawerList;

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recipes = new ArrayList<>();
        recipeNames = new ArrayList<>();
        mActivityTitle = getTitle().toString();


        //https://developer.android.com/training/volley/request-custom.html as a resource
        mRequestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, DATA_URL,
                null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject currentRecipe = response.getJSONObject(i);
                        int recipeId = currentRecipe.getInt("id");
                        String recipeName = currentRecipe.getString("name");
                        String recipeImage = currentRecipe.getString("image");
                        recipes.add(new Recipe(recipeId, recipeName, recipeImage));
                        recipeNames.add(recipeName);
                    }

                    rvAdpter.notifyDataSetChanged();

                    // http://codetheory.in/android-navigation-drawer/ used as basis for Hamburger
                    // menu code
                    setupMenuItems();
                    setupMenu();

                    mDrawerToggle.setDrawerIndicatorEnabled(true);

                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeButtonEnabled(true);

                    mDrawerToggle.syncState();

                } catch (JSONException JSONE) {
                    JSONE.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error + "");
                Toast.makeText(getBaseContext(), "Error Fetching Data", Toast.LENGTH_SHORT).show();
            }
        }
        );

        mRequestQueue.add(jsonArrayRequest);

        // Fixed issue with screen sizes using
        // https://stackoverflow.com/questions/1016896/get-screen-dimensions-in-pixels
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpWidth < 600) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                gridLayoutManager = new GridLayoutManager(this, 1);
            } else {
                gridLayoutManager = new GridLayoutManager(this, 2);
            }
        } else {
            gridLayoutManager = new GridLayoutManager(this, 3);
        }

        recipeListRV.setLayoutManager(gridLayoutManager);

        rvAdpter = new RecipeAdapter(this, recipes, this);

        recipeListRV.setAdapter(rvAdpter);
    }

    private void setupMenuItems() {

        arrayAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, recipeNames);
        mDrawerList.setAdapter(arrayAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getBaseContext(), RecipeDetails.class);
                intent.putExtra(RECIPE_INDEX_NUMBER, position);
                intent.putStringArrayListExtra(RECIPE_LIST, (ArrayList<String>) recipeNames);
                startActivity(intent);
            }
        });
    }

    private void setupMenu() {

        mDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation");
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(mActivityTitle);
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intent = new Intent(this, RecipeDetails.class);
        intent.putExtra(RECIPE_INDEX_NUMBER, clickedItemIndex);
        intent.putStringArrayListExtra(RECIPE_LIST, (ArrayList<String>) recipeNames);
        startActivity(intent);
    }
}