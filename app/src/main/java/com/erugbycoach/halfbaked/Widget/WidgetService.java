package com.erugbycoach.halfbaked.Widget;

/**
 * Created by William D Howell on 11/21/2017.
 */

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService{

    public static final String REQUESTED_RECIPE = "requestedRecipe";
    public static final String TOTAL_RECIPES = "totalRecipes";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int requestedRecipe = intent.getIntExtra(REQUESTED_RECIPE, 0) - 2;
        int totalRecipes = intent.getIntExtra(TOTAL_RECIPES, 0);
        return new RecipeViewFactory(this.getApplicationContext(), requestedRecipe, totalRecipes);
    }
}
