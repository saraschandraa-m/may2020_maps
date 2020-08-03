package com.appstone.maps;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PlacesAdapter extends ArrayAdapter<String> implements Filterable {


    public ArrayList<Place> searchedPlaces;
    public PlaceSearch searchPlacesAPI;

    public PlacesAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        this.searchedPlaces = new ArrayList<>();
        searchPlacesAPI = new PlaceSearch();

    }

    @Override
    public int getCount() {
        return searchedPlaces != null && searchedPlaces.size() > 0 ? searchedPlaces.size() : 0;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return searchedPlaces != null && searchedPlaces.size() > 0 ? searchedPlaces.get(position).placeName : "";
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new PlaceFilter();
    }

    private class PlaceFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            if (charSequence != null) {

                ArrayList<Place> results = new ArrayList<>();

                results = searchPlacesAPI.autocomplete(charSequence.toString());


                filterResults.count = results.size();
                filterResults.values = results;
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if (filterResults != null) {
                searchedPlaces = (ArrayList<Place>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    }
}
