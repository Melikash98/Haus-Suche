package com.melikash98.housesuche.Email;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
/**
 * EmailJs
 *
 * Utility class responsible for sending emails from the Android application
 * using the EmailJS REST API.
 *
 * This class constructs a JSON request containing template parameters
 * and sends it to the EmailJS service using the OkHttp HTTP client.
 *
 * Typical use case in the application:
 * When a user wants to contact an item owner (e.g., for renting a house),
 * the app sends an email request containing the user's contact information
 * and the requested appointment time.
 *
 * External service used:
 * https://www.emailjs.com/
 *
 * The email is generated using a predefined EmailJS template.
 */
public class EmailJs {
    private static final String TAG = "EMAIL_JS";
    private static final String SERVICE_ID = "XXXXXX";
    private static final String TEMPLATE_ID = "XXXXXXXX";
    private static final String PUBLIC_KEY = "XXXXXXXXX";
    private static final String URL = "https://api.emailjs.com/api/v1.0/email/send";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    /**
     * Sends an email request to an item owner using EmailJS.
     *
     * @param ownerEmail Email address of the item owner
     * @param ownerName  Name of the item owner
     * @param appEmail   Application contact email
     * @param userName   Name of the user sending the request
     * @param userEmail  Email of the user
     * @param userPhone  Phone number of the user
     * @param term       Rental or contact term
     * @param time       Requested appointment time
     * @param message    Optional message from the user
     * @param itemId     ID of the related item
     * @param callback   Callback used to report success or failure
     */
    public static void sendEmail(
            String ownerEmail,
            String ownerName,
            String appEmail,
            String userName,
            String userEmail,
            String userPhone,
            String term,
            String time,
            String message,
            String itemId,
            Callback callback
    ) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject params = new JSONObject();
            /**
             * Template parameters sent to EmailJS.
             * These fields must match the variables
             * defined in the EmailJS template.
             */

            params.put("name", userName);
            params.put("time", time);
            params.put("owner_email", ownerEmail != null ? ownerEmail : "");
            params.put("owner_name", ownerName != null ? ownerName : "");
            params.put("app_email", appEmail);
            params.put("user_name", userName);
            params.put("user_email", userEmail != null ? userEmail : "");
            params.put("user_phone", userPhone != null ? userPhone : "");
            params.put("term", term);
            params.put("message", message != null ? message : "");
            params.put("item_id", itemId);
            /**
             * EmailJS request body.
             */
            JSONObject body = new JSONObject();
            body.put("service_id", SERVICE_ID);
            body.put("template_id", TEMPLATE_ID);
            body.put("user_id", PUBLIC_KEY);
            body.put("template_params", params);

            Log.d(TAG, "=== REQUEST TO EMAILJS ===");
            Log.d(TAG, "To: " + ownerEmail);
            Log.d(TAG, "Body: " + body.toString(2));

            RequestBody requestBody = RequestBody.create(body.toString(), JSON);
            Request request = new Request.Builder()
                    .url(URL)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("origin", "http://localhost")
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network Failure", e);
                    if (callback != null) callback.onError("Internet connection is down:" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resp = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "Response Code: " + response.code());
                    Log.d(TAG, "Response Body: " + resp);

                    if (response.isSuccessful()) {
                        if (callback != null) callback.onSuccess();
                    } else {
                        if (callback != null) callback.onError("EmailJS Error " + response.code() + ": " + resp);
                    }
                    response.close();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
            if (callback != null) callback.onError("Internal error: " + e.getMessage());
        }
    }

    public interface Callback {
        void onSuccess();
        void onError(String error);
    }
}
