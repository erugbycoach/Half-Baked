package com.erugbycoach.halfbaked.Models;

/**
 * Created by William D Howell on 11/21/2017.
 */

public class Ingredient {
    double quantity;
    String measurement;
    String ingredient;

    public Ingredient(double quantity, String measurement, String ingredient) {
        this.quantity = quantity;
        this.measurement = measurement;
        this.ingredient = ingredient;
    }

    //Setters & Getters

    public double getQuantity() {
        return quantity;
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getIngredient() {
        return ingredient;
    }
}