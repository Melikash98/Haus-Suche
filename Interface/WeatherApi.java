package com.melikash98.housesuche.Interface;

import com.melikash98.housesuche.Models.CityModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;
/**
 * WeatherApi
 *
 * Retrofit interface used to communicate with the OpenWeatherMap API.
 *
 * This interface defines the endpoint used to retrieve geographical
 * information about cities based on a search query.
 *
 * The API returns a list of matching cities including:
 * - city name
 * - country
 * - latitude
 * - longitude
 *
 * These coordinates can later be used for weather requests
 * or location-based features inside the application.
 *
 * API Documentation:
 * https://openweathermap.org/api/geocoding-api
 *
 * Endpoint used:
 * /geo/1.0/direct
 */
public interface WeatherApi {
    /**
     * Retrieves a list of cities matching the provided query.
     *
     * The query usually contains a city name and optionally a country code.
     *
     * Example query:
     * Berlin,DE
     *
     * Example request:
     * https://api.openweathermap.org/geo/1.0/direct?q=Berlin,DE&limit=5&appid=API_KEY
     *
     * @param cityCountry City name with optional country code (e.g., "Berlin,DE")
     * @param limit       Maximum number of results returned by the API
     * @param apiKey      OpenWeatherMap API key
     *
     * @return Retrofit Call containing a list of CityModel objects
     */
    @GET("geo/1.0/direct")
    Call<List<CityModel>> getCities(

            @Query("q") String cityCountry,

            @Query("limit") int limit,

            @Query("appid") String apiKey
    );
}
