package com.liorginsberg.lgtasks;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public final class CommonUtilities {
	
	// give your server registration url here
    static final String SERVER_URL = "http://liorginsberg.com/lgtasks/php/GCM/register.php"; 

    // Google project id
    public static final String SENDER_ID = "1059318831401"; 



    public static final String DISPLAY_MESSAGE_ACTION =
            "com.liorginsberg.lgtasks.DISPLAY_MESSAGE";

    public static final String EXTRA_MESSAGE = "message";

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void notifyApp(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
    
    public static float convertDpToPixel(float dp,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi/160f);
	    return px;
	}
}
