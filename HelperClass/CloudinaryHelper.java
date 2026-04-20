package com.melikash98.housesuche.HelperClass;

import android.content.Context;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The CloudinaryHelper class is a singleton helper class for managing file and image uploads
 * to the Cloudinary cloud service in an Android application.
 *
 * This class handles all upload-related operations in a unified manner, with support for progress tracking
 * and error handling. The Singleton design pattern is used to ensure that only one instance of the class
 * exists throughout the application lifecycle, preventing unnecessary multiple connections to Cloudinary.
 *
 * Main features of this class:
 * - Initializes Cloudinary with the cloud name and base configuration.
 * - Uploads a single image (uploadImageWithProgress) with real-time progress percentage display.
 * - Uploads a general file (uploadFile) with the ability to specify the resource type (e.g., image, video, raw, etc.).
 * - Uploads multiple images simultaneously (uploadMultipleImages), collecting uploaded URLs and reporting overall progress.
 *
 * All methods use the Cloudinary Android UploadCallback and return results
 * (success, progress, error) to the caller via listener interfaces.
 * This class is designed to be easily reusable across different parts of the application
 * (such as property listing submission, portfolio uploads, etc.) and simplifies unified file management.
 *
 * Note: The application context is used to prevent memory leaks.
 */

public class CloudinaryHelper {
    private static final String CLOUD_NAME = "xxxxxxxx";
    private static final String UPLOAD_PRESET = "xxxxxxxx";

    private static CloudinaryHelper instance;
    private Context context;

    private CloudinaryHelper(Context context) {
        this.context = context.getApplicationContext();
        initCloudinary();
    }

    public static synchronized CloudinaryHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CloudinaryHelper(context);
        }
        return instance;
    }

    private void initCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            MediaManager.init(context, config);
        } catch (Exception e) {
        }
    }

    public void uploadImageWithProgress(File imageFile, String folderName, String publicId,
                                        UploadProgressListener listener) {

        MediaManager.get()
                .upload(imageFile.getAbsolutePath())
                .unsigned(UPLOAD_PRESET)
                .option("folder", folderName)
                .option("public_id", publicId)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        if (listener != null) listener.onStart();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        if (listener != null) {
                            int progress = (int) ((bytes * 100) / totalBytes);
                            listener.onProgress(progress);
                        }
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        if (listener != null) listener.onSuccess(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (listener != null) listener.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }
    public void uploadFile(File file, String folderName, String publicId,
                           String resourceType, UploadProgressListener listener) {

        if (file == null || !file.exists()) {
            if (listener != null) listener.onError("File does not exist.");
            return;
        }

        MediaManager.get()
                .upload(file.getAbsolutePath())
                .unsigned(UPLOAD_PRESET)
                .option("folder", folderName)
                .option("public_id", publicId)
                .option("resource_type", resourceType)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        if (listener != null) listener.onStart();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        if (listener != null) {
                            int progress = (int) ((bytes * 100) / totalBytes);
                            listener.onProgress(progress);
                        }
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        if (listener != null) listener.onSuccess(url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (listener != null) listener.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }
    public void uploadMultipleImages(List<File> imageFiles, String folderName,
                                     String basePublicId, UploadMultipleListener listener) {

        if (imageFiles == null || imageFiles.isEmpty()) {
            if (listener != null) listener.onError("Es ist kein Problem mehr");
            return;
        }

        List<String> uploadedUrls = new ArrayList<>();
        final int total = imageFiles.size();
        final int[] completed = {0};

        for (int i = 0; i < imageFiles.size(); i++) {
            File file = imageFiles.get(i);
            String publicId = basePublicId + "_" + i;

            uploadImageWithProgress(file, folderName, publicId, new UploadProgressListener() {
                @Override
                public void onStart() {}

                @Override
                public void onProgress(int percent) {}

                @Override
                public void onSuccess(String imageUrl) {
                    uploadedUrls.add(imageUrl);
                    completed[0]++;
                    if (listener != null) listener.onProgress(completed[0], total);
                    checkIfAllDone();
                }

                @Override
                public void onError(String errorMessage) {
                    completed[0]++;
                    Log.e("CloudinaryHelper", errorMessage);
                    checkIfAllDone();
                }

                private void checkIfAllDone() {
                    if (completed[0] == total && listener != null) {
                        listener.onSuccess(new ArrayList<>(uploadedUrls));
                    }
                }
            });
        }
    }
    public interface UploadProgressListener {
        void onStart();
        void onProgress(int percent);
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }
    public interface UploadMultipleListener {
        void onStart();
        void onProgress(int uploadedCount, int totalCount);
        void onSuccess(List<String> imageUrls);
        void onError(String errorMessage);
    }
}
