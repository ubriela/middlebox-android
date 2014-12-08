package edu.usc.middlebox.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.os.Handler;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.net.URL;

import edu.usc.middlebox.R;

/**
 * Helper class handling the responses from the server
 */
public class CommonUtils {

    // Shared Preferences
    public static final String SHARED_PREFS = "BS_PREFERENCES";

    // Used for activity result from intent
    public static final int MAIN = 1;
    public static final int WIRELESS_CONNECTIVITY = 2;
    public static final int CREATE_ACCOUNT = 3;
    public static final int FORGOT_PASSWORD = 5;

    static final int TIME_OUT = 3000;
    static final int MSG_DISMISS_DIALOG = 0;
    private AlertDialog mAlertDialog;

    /**
     * Init image loader
     *
     * @param context the context that called this method
     */
    public static void initImageLoader(final Context context) {

        // Get singletone instance of ImageLoader
        ImageLoader imageLoader = ImageLoader.getInstance();
        // Initialize ImageLoader with configuration. Do it once.
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
    }

    /**
     * Returns the absolute constructed by the parameters
     *
     * @param IP         the IP of the server
     * @param PORT       the PORT of the server
     * @param WebService the webservice to call
     * @return the absolute url
     */
    public static String getAbsoluteURL(final String IP, final String PORT, final String WebService) {
        String url = IP + ":" + PORT + WebService;
        if (!url.startsWith("http://"))
            url = "http://" + url;
        return url;
    }

    public static String getFilePath (String IP, String folder, String fileObject) {
        String _IP = "";
        if (!IP.startsWith("http://"))
            _IP = "http://" + IP;
        return _IP + folder + fileObject;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_DIALOG:
                    if (mAlertDialog != null && mAlertDialog.isShowing()) {
                        mAlertDialog.dismiss();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * Method used to pop up message to user
     *
     * @param context the context that called this method
     * @param msg     the message string
     * @param title   the title string
     * @param imageID the id of an image in resources
     */
    public static void popup_msg(Context context, String msg, String title, int imageID) {

        AlertDialog.Builder alert_box = new AlertDialog.Builder(context);
        alert_box.setTitle(title);
        alert_box.setMessage(msg);
        alert_box.setIcon(imageID);

        alert_box.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alert_box.create();
        alert.show();

        // dismiss dialog in TIME_OUT ms
        Handler mHandler = new Handler();
        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, TIME_OUT);
    }


    /**
     * Checks for Internet connectivity
     *
     * @param context the context that called this method
     * @return true if wifi are enabled and in connected, otherwise false
     */
    public static boolean isOnline(final Context context) {
        NetworkInfo netInfo = CommonUtils.getNetworkInfo(context);
        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Get the network info
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity to a Wifi network (value = 1)
     * @param context
     * @return
     */
    public static boolean isConnectedWifi(Context context){
        NetworkInfo info = CommonUtils.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }


    /**
     * Check if there is any connectivity to a mobile network (value = 0)
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context){
        NetworkInfo info = CommonUtils.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is any connectivity to a mobile network (value = 0)
     * @param context
     * @return
     */
    public static boolean isConnectedEthernet(Context context){
        NetworkInfo info = CommonUtils.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_ETHERNET);
    }

    /**
     * Prompts user to connect to Internet
     *
     * @param context the context that called this method
     */
    public static void AlertDialogInternetConnection(final Context context) {
        // Display a dialog to enable wifi
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.titleIntenetConnectivity));
        builder.setMessage(context.getString(R.string.msgInternetConnectivity));
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Start new intent for wifi settings
                Intent wireless_settings_intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                ((Activity) context).startActivityForResult(wireless_settings_intent, WIRELESS_CONNECTIVITY);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Cancel the dialog
                dialog.cancel();
            }
        });

        // Here is the actual display code
        AlertDialog alert = builder.create();
        alert.show();
    }
}
