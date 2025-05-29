package com.example.app2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private LatLng selectedLocation;
    private Polyline polyline;
    private SearchView searchView;
    private ListView suggestionsListView;
    private PlacesClient placesClient;
    private ArrayAdapter<String> suggestionsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialiser la barre de recherche
        searchView = view.findViewById(R.id.searchView);
        suggestionsListView = view.findViewById(R.id.suggestionsListView);

        // Initialiser l'adapter pour la ListView des suggestions
        suggestionsAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        suggestionsListView.setAdapter(suggestionsAdapter);

        // Gérer les changements de texte dans la SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query); // Rechercher l'emplacement
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    getAutocompletePredictions(newText); // Obtenir des suggestions
                } else {
                    suggestionsListView.setVisibility(View.GONE); // Cacher la ListView si le texte est trop court
                }
                return false;
            }
        });

        // Gérer les clics sur les suggestions
        suggestionsListView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedSuggestion = suggestionsAdapter.getItem(position);
            fetchPlaceDetails(selectedSuggestion); // Récupérer les détails de l'emplacement
            suggestionsListView.setVisibility(View.GONE); // Cacher la ListView après la sélection
        });

        // Initialiser l'API Places
        Places.initialize(requireContext(), "AIzaSyA9CamnVenbDKPKO050TiGh3Q8yt3h1YfQ");
        placesClient = Places.createClient(requireContext());

        // Initialiser la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialiser FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Changer le type de carte (exemple : mode hybride)
       // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Vérifier les permissions de localisation
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        // Obtenir la localisation actuelle
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Position Actuelle"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            } else {
                Toast.makeText(requireContext(), "Impossible de récupérer la localisation actuelle", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Erreur lors de la récupération de la localisation : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        // Gérer les clics sur la carte
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;

            // Ajouter un marqueur personnalisé à l'emplacement sélectionné
            BitmapDescriptor icon = getResizedMarkerIcon(R.drawable.markerper, 100, 100); // Redimensionner le marqueur
            mMap.addMarker(new MarkerOptions()
                    .position(selectedLocation)
                    .title("Emplacement Sélectionné")
                    .icon(icon) // Utiliser l'icône personnalisée
            );

            drawStraightLine(); // Tracer une ligne droite
            calculateDistanceAndTime();
        });
    }

    private BitmapDescriptor getResizedMarkerIcon(int resId, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), resId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }

    private void searchLocation(String query) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            if (!response.getAutocompletePredictions().isEmpty()) {
                AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                String placeId = prediction.getPlaceId();

                // Récupérer les détails de l'emplacement
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchResponse -> {
                    Place place = fetchResponse.getPlace();
                    selectedLocation = place.getLatLng();

                    // Ajouter un marqueur personnalisé à l'emplacement sélectionné
                    BitmapDescriptor icon = getResizedMarkerIcon(R.drawable.markerper, 100, 100); // Redimensionner le marqueur
                    mMap.addMarker(new MarkerOptions()
                            .position(selectedLocation)
                            .title(place.getName())
                            .icon(icon) // Utiliser l'icône personnalisée
                    );

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
                    drawStraightLine(); // Tracer une ligne droite
                    calculateDistanceAndTime();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Erreur lors de la récupération des détails : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Erreur lors de la recherche : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void getAutocompletePredictions(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountry("FR") // Restreindre la recherche à un pays (exemple : France)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            List<String> suggestions = new ArrayList<>();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                suggestions.add(prediction.getFullText(null).toString());
            }

            suggestionsAdapter.clear();
            suggestionsAdapter.addAll(suggestions);
            suggestionsAdapter.notifyDataSetChanged();
            suggestionsListView.setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Erreur lors de la recherche : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchPlaceDetails(String query) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            if (!response.getAutocompletePredictions().isEmpty()) {
                AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                String placeId = prediction.getPlaceId();

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchResponse -> {
                    Place place = fetchResponse.getPlace();
                    selectedLocation = place.getLatLng();

                    // Ajouter un marqueur personnalisé à l'emplacement sélectionné
                    BitmapDescriptor icon = getResizedMarkerIcon(R.drawable.markerper, 100, 100); // Redimensionner le marqueur
                    mMap.addMarker(new MarkerOptions()
                            .position(selectedLocation)
                            .title(place.getName())
                            .icon(icon) // Utiliser l'icône personnalisée
                    );

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
                    drawStraightLine(); // Tracer une ligne droite
                    calculateDistanceAndTime();
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Erreur lors de la récupération des détails : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Erreur lors de la recherche : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void drawStraightLine() {
        if (currentLocation != null && selectedLocation != null) {
            // Supprimer la ligne précédente si elle existe
            if (polyline != null) {
                polyline.remove();
            }

            // Tracer une ligne droite entre les deux points avec une couleur bleue
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(currentLocation, selectedLocation)
                    .width(10)
                    .color(Color.BLUE); // Changer la couleur de la ligne en bleu

            polyline = mMap.addPolyline(polylineOptions);
        }
    }

    private void calculateDistanceAndTime() {
        if (currentLocation != null && selectedLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                    selectedLocation.latitude, selectedLocation.longitude, results);
            float distanceInMeters = results[0];

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
            String distanceText = distanceInMeters >= 1000
                    ? numberFormat.format(distanceInMeters / 1000) + " km"
                    : numberFormat.format(distanceInMeters) + " m";

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentTime = timeFormat.format(new Date());

            double travelTimeMinutes = (distanceInMeters / 1000) * 10;
            String arrivalTime = timeFormat.format(new Date(System.currentTimeMillis() + (long) (travelTimeMinutes * 60 * 1000)));

            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Distance et Temps")
                    .setMessage("Distance : " + distanceText + "\nHeure de départ : " + currentTime + "\nHeure d'arrivée : " + arrivalTime)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}