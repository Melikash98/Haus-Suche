package com.melikash98.housesuche.HelperClass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.melikash98.housesuche.R;

import java.time.Duration;

/**
 * The CustomToast class is a utility helper class for displaying custom, visually enhanced Toasts
 * in an Android application.
 *
 * Instead of using the default Android Toast, this class utilizes a custom layout (toast_layout)
 * to present a Toast that includes the application logo, a message text, and a shake animation
 * applied to the icon.
 * The main goal of this class is to provide a more consistent and professional user experience
 * when displaying short messages to the user (such as operation success, errors, notifications, etc.).
 *
 * Key features:
 * - Supports a custom icon (by default, R.drawable.logo_app is used).
 * - Automatically applies a shake animation to the icon to attract user attention.
 * - Provides simple helper methods: show() for short Toasts and showLong() for long Toasts.
 * - Includes a core method showWithLogo() that allows customization of duration and icon.
 * - Performs basic safety checks (null checks for context and message) to prevent crashes.
 *
 * All methods are static, allowing the custom Toast to be displayed easily from anywhere
 * in the application without the need to create an instance.
 *
 * This class is commonly used in areas such as form submission, network operations,
 * input validation, and similar scenarios.
 */

public class CustomToast {
    public static void showWithLogo(Context context, String message, int duration) {
        showWithLogo(context, message, duration, R.drawable.logo_app);
    }
    public static void showWithLogo(Context context, String message, int duration, int iconResId) {
        if (context == null || message == null || message.trim().isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View toastLayout = inflater.inflate(R.layout.toast_layout, null);

        TextView textToast = toastLayout.findViewById(R.id.textToast);
        textToast.setText(message);

        ImageView iconToast = toastLayout.findViewById(R.id.iconToast);
        iconToast.setImageResource(iconResId);

        Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        iconToast.startAnimation(shake);

        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(toastLayout);
        toast.show();
    }

    public static void show(Context context, String message) {
        showWithLogo(context, message, Toast.LENGTH_SHORT);
    }

    public static void showLong(Context context, String message) {
        showWithLogo(context, message, Toast.LENGTH_LONG);
    }
}
