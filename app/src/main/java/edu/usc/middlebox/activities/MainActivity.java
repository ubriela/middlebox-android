package edu.usc.middlebox.activities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.ContextMenu.ContextMenuInfo;
import 	android.widget.AdapterView.AdapterContextMenuInfo;
import android.provider.Settings.SettingNotFoundException;
import edu.usc.middlebox.utils.CommonUtils;
import edu.usc.middlebox.utils.CustomHttpClient;
import edu.usc.middlebox.utils.MyJsonResponse;
import edu.usc.middlebox.utils.MyJsonResponse.TYPES_ENUM;
import org.apache.http.NameValuePair;
import android.provider.Settings;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Random;
import android.text.TextUtils;
import android.location.LocationManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.Toast;
//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuInflater;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;


import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;


import edu.usc.middlebox.utils.DeviceUuidFactory;

import edu.usc.middlebox.R;


import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class MainActivity extends FragmentActivity implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	CustomDrawerAdapter adapter;

	List<DrawerItem> dataList;


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private CreateAccountTask mCreateAccountTask = null;

    private SavePhoneStateTask mPhoneStateTask = null;

    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mUsername;
    private String mPassword;
    private String mPasswordHashed;
    private String cellularNetwork;
    private String storedAddress;
    private boolean remember_me = false;

    private SharedPreferences Preferences;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    /**
     * Test features
     */
    private CacheTask mCacheTask = null;    // type = 1
    private DelayHandshakeTask mDelayHandshakeTask = null;  // type = 2
    private RedirectionTask mRedirectionTask = null;    // type = 3
    private ContentRewrite mContentRewrite = null;    // type = 4
    private CheckMiddlebox mCheckMiddlebox = null;  // type = 5

    public static JSONObject summary_all_jObj = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initializing
		dataList = new ArrayList<DrawerItem>();
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		// Add Drawer Item to dataList

        dataList.add(new DrawerItem("Main Options"));// adding a header to the list

        dataList.add(new DrawerItem("All Features", R.drawable.ic_action_all));
        dataList.add(new DrawerItem("Delay Handshaking", R.drawable.ic_action_delay_handshaking));
        dataList.add(new DrawerItem("Connection Persistence", R.drawable.ic_action_connection_persistence));
        dataList.add(new DrawerItem("Content Rewriting", R.drawable.ic_action_rewriting));
        dataList.add(new DrawerItem("Redirection", R.drawable.ic_action_redirection));
        dataList.add(new DrawerItem("Caching", R.drawable.ic_action_caching));

        // initialize and set the adapter to the  drawer listview
        dataList.add(new DrawerItem("Other Option")); // adding a header to the list
		dataList.add(new DrawerItem("About", R.drawable.ic_action_about));
		dataList.add(new DrawerItem("Settings", R.drawable.ic_action_settings));
		dataList.add(new DrawerItem("Help", R.drawable.ic_action_help));
		adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,
				dataList);

		mDrawerList.setAdapter(adapter);

        // enable this listview listen to click event
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable the HomeUpButton for action bar and set the toggle to the Drawer Layout
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

//        if (savedInstanceState == null) {
//
//             if (dataList.get(0).getTitle() != null) {
//                SelectItem(1);
//            } else {
//                SelectItem(0);
//            }
//        }

        // Configure preferences
        Preferences = getSharedPreferences(CommonUtils.SHARED_PREFS, MODE_PRIVATE);
        loadPreferences();

        if (mUsername != "")
            ProceedToLogin();
        else {
            proceedToSignUp();
            ProceedToLogin();
            mPhoneStateTask = new SavePhoneStateTask();
            mPhoneStateTask.execute((Void) null);
        }


        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        mLocationClient = new LocationClient(this, this, this);

        if (!isLocationEnabled(this))
            enableLocationSharing(this);

        SummaryAllTask summaryAllTask = new SummaryAllTask();
        summaryAllTask.execute((Void) null);
	}

    /**
     *  whether gps provider and network providers are enabled or no
     * @param context
     * @return
     */
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public void enableLocationSharing(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.gps_not_found_title);  // GPS not found
            builder.setMessage(R.string.gps_not_found_message); // Want to enable?
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            builder.setNegativeButton("No", null);
            builder.create().show();
            return;
        }
    }

    /**
     * Loads preferences from disk
     */
    private void loadPreferences() {

        // Get stored values from preferences
        mUsername = Preferences.getString("username_email", "");
        mPassword = Preferences.getString("password", "");
    }


    /**
     * Stores username
     *
     * @param username the username to store
     */
    private void storeUsername(String username) {
        SharedPreferences.Editor edit = Preferences.edit();
        edit.putString("username_email", username);
        edit.commit();
    }
    private void storeUserID(String userid) {
        SharedPreferences.Editor edit = Preferences.edit();
        edit.putString("userid", userid);
        edit.commit();
    }


    /**
     * Stores password and remember me
     *
     * @param password   the password to store
     * @param rememberMe the rememberMe to store
     */
    private void storePassword(String password, Boolean rememberMe) {

        SharedPreferences.Editor edit = Preferences.edit();

        if (rememberMe)
            edit.putString("password", password);
        else
            edit.putString("password", "");

        edit.putBoolean("remember_me", rememberMe);

        edit.commit();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
	}

    /**
     * which will take a one integer parameter, to identify the position of the selected item
     * @param position
     */
	public void SelectItem(int position) {
		Fragment fragment = null;
		Bundle args = new Bundle();
		switch (position) {
//		case 0:
//			fragment = new FragmentOne();
//			args.putString(FragmentOne.ITEM_NAME, dataList.get(position)
//					.getItemName());
//			args.putInt(FragmentOne.IMAGE_RESOURCE_ID, dataList.get(position)
//					.getImgResID());
//			break;
		case 1:
			fragment = new FragmentAll();
//			args.putString(FragmentTwo.ITEM_NAME, dataList.get(position)
//                    .getItemName());
//			args.putInt(FragmentTwo.IMAGE_RESOURCE_ID, dataList.get(position)
//					.getImgResID());

			break;
		case 2:
			fragment = new FragmentThree();
			args.putString(FragmentThree.ITEM_NAME, dataList.get(position)
					.getItemName());
			args.putInt(FragmentThree.IMAGE_RESOURCE_ID, dataList.get(position)
					.getImgResID());
			break;
		case 3:
			fragment = new FragmentOne();
			args.putString(FragmentOne.ITEM_NAME, dataList.get(position)
					.getItemName());
			args.putInt(FragmentOne.IMAGE_RESOURCE_ID, dataList.get(position)
					.getImgResID());
			break;
		case 4:
			fragment = new FragmentTwo();
			args.putString(FragmentTwo.ITEM_NAME, dataList.get(position)
					.getItemName());
			args.putInt(FragmentTwo.IMAGE_RESOURCE_ID, dataList.get(position)
					.getImgResID());
			break;
		case 5:
			fragment = new FragmentThree();
			args.putString(FragmentThree.ITEM_NAME, dataList.get(position)
					.getItemName());
			args.putInt(FragmentThree.IMAGE_RESOURCE_ID, dataList.get(position)
					.getImgResID());
			break;
		case 6:
			fragment = new FragmentOne();
			args.putString(FragmentOne.ITEM_NAME, dataList.get(position)
					.getItemName());
			args.putInt(FragmentOne.IMAGE_RESOURCE_ID, dataList.get(position)
					.getImgResID());
			break;
//		case 7:
//			fragment = new FragmentOne();
//			args.putString(FragmentOne.ITEM_NAME, dataList.get(position)
//					.getItemName());
//			args.putInt(FragmentOne.IMAGE_RESOURCE_ID, dataList.get(position)
//					.getImgResID());
//			break;
		case 8:
			fragment = new FragmentTwo();
			args.putString(FragmentTwo.ITEM_NAME, dataList.get(position)
					.getItemName());
			args.putInt(FragmentTwo.IMAGE_RESOURCE_ID, dataList.get(position)
					.getImgResID());
			break;
		case 9:
			fragment = new FragmentThree();
			args.putString(FragmentThree.ITEM_NAME, dataList.get(position)
					.getItemName());
			args.putInt(FragmentThree.IMAGE_RESOURCE_ID, dataList.get(position)
					.getImgResID());
			break;
        case 10:
            fragment = new FragmentTwo();
            args.putString(FragmentTwo.ITEM_NAME, dataList.get(position)
                    .getItemName());
            args.putInt(FragmentTwo.IMAGE_RESOURCE_ID, dataList.get(position)
                    .getImgResID());
            break;
		default:
			break;
		}

		fragment.setArguments(args);
		FragmentManager frgManager = getFragmentManager();
		frgManager.beginTransaction().replace(R.id.content_frame, fragment)
				.commit();

		mDrawerList.setItemChecked(position, true);
		setTitle(dataList.get(position).getItemName());
		mDrawerLayout.closeDrawer(mDrawerList);
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cellular_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.ATT:
                System.out.println("att");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_collect:
                openCollect();
                return true;
        }

        switch (item.getGroupId()) {
            case R.id.cellular:
                cellularNetwork = item.toString();
                System.out.println(cellularNetwork);
                getAddress();
                System.out.println(cellularNetwork + " selected..");
                return true;
//            default:
//                return super.onOptionsItemSelected(item);
        }


        if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return false;
	}

    private void openCollect(){
        if (cellularNetwork == null) {
            CommonUtils.popup_msg(MainActivity.this, getString(R.string.select_sellular_network), "Info", R.drawable.info);
            return;
        }

        if (!CommonUtils.isConnectedMobile(this)) {
            CommonUtils.popup_msg(MainActivity.this, getString(R.string.use_mobile_network), "Info", R.drawable.info);
            return;
        }

        if (CommonUtils.isConnectedMobile(this) || CommonUtils.isConnectedWifi(this) || CommonUtils.isConnectedEthernet(this)) {
            CommonUtils.popup_msg(MainActivity.this, getString(R.string.collecting_data), "Info", R.drawable.info);

            System.out.println("collecting data ...");
            mCacheTask = new CacheTask();
            mCacheTask.execute((Void) null);

            mDelayHandshakeTask = new DelayHandshakeTask();
            mDelayHandshakeTask.execute((Void) null);

            mRedirectionTask = new RedirectionTask();
            mRedirectionTask.execute((Void) null);

            mContentRewrite = new ContentRewrite();
            mContentRewrite.execute((Void) null);

            mCheckMiddlebox = new CheckMiddlebox();
            mCheckMiddlebox.execute((Void) null);
        }
    }

    private void openSettings() {
        System.out.println("search");
    }

    @Override
    public void onConnected(Bundle bundle) {
        startPeriodicUpdates();
    }

    @Override
    public void onDisconnected() {
        startPeriodicUpdates();
    }

    /*
         * Called by Location Services if the attempt to
         * Location Services fails.
         */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
        }
    }

    private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (dataList.get(position).getTitle() == null) {
                SelectItem(position);
            }
        }
	}




    /****************************************************************************************************************/

    /**
     * This is called when user clicks the sign up button. Proceeds to create
     * account if we have Internet connectivity. If not user is prompted to
     * connect.
     */
    private void proceedToSignUp() {

        // if not connected
        if (!CommonUtils.isOnline(this)) {
            CommonUtils.AlertDialogInternetConnection(this);
        } else {
            // We have Internet. Just signup
            signUp();
        }

    }

    /**
     * Creates an asynchronous http post request that contacts our web server
     * and provides the new user's details.
     */
    private void signUp() {

        if (mCreateAccountTask != null) {
            return;
        }


        // Store values at the time of the login attempt.
        DeviceUuidFactory uuidFactory = new DeviceUuidFactory(this);
        mUsername = uuidFactory.getDeviceUuid().toString();
        mPassword = "123456";

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("SHA-512");
            byte[] bytes = mPassword.getBytes();
            int byteCount = bytes.length;
            digester.update(bytes, 0, byteCount);
            byte[] digest = digester.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }
            mPasswordHashed = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return;
        }

        mCreateAccountTask = new CreateAccountTask();
        mCreateAccountTask.execute((Void) null);


    }


    /**
     * Save phone state
     */
    public class SavePhoneStateTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            //Get the instance of TelephonyManager
            TelephonyManager  tm=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

            //Calling the methods of TelephonyManager the returns the information
            String devideId=tm.getDeviceId();
            String subscriberId = tm.getSubscriberId();
            int phoneType = tm.getPhoneType();
            int networkType = tm.getNetworkType();
            String networkCountryISO=tm.getNetworkCountryIso();
            String networkOperator = tm.getNetworkOperator();
            String networkOperatorName = tm.getNetworkOperatorName();
            String simCountryISO=tm.getSimCountryIso();
            String simOperator = tm.getSimOperator();
            String simOperatorName = tm.getSimOperatorName();
            String softwareVersion=tm.getDeviceSoftwareVersion();
            String cellLocation = tm.getCellLocation().toString();
            int dataActivity = tm.getDataActivity();
            int dataState = tm.getDataState();

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", mUsername));
            postParameters.add(new BasicNameValuePair("devideId", devideId));
            postParameters.add(new BasicNameValuePair("subscriberId", subscriberId));
            postParameters.add(new BasicNameValuePair("phoneType", String.valueOf(phoneType)));
            postParameters.add(new BasicNameValuePair("networkType", String.valueOf(networkType)));
            postParameters.add(new BasicNameValuePair("networkCountryISO", networkCountryISO));
            postParameters.add(new BasicNameValuePair("networkOperator", networkOperator));
            postParameters.add(new BasicNameValuePair("simCountryISO", simCountryISO));
            postParameters.add(new BasicNameValuePair("simOperator", simOperator));
            postParameters.add(new BasicNameValuePair("simOperatorName", simOperatorName));
            postParameters.add(new BasicNameValuePair("softwareVersion", softwareVersion));
            postParameters.add(new BasicNameValuePair("cellLocation", cellLocation));
            postParameters.add(new BasicNameValuePair("dataActivity", String.valueOf(dataActivity)));
            postParameters.add(new BasicNameValuePair("dataState", String.valueOf(dataState)));

            for (NameValuePair p : postParameters)
                System.out.println(p.toString());

            String response = null;
            try {
                String IP = getString(R.string.IP);
                String PORT = getString(R.string.PORT);
                String WebService = getString(R.string.StorePhoneStateService);
                String url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                response = CustomHttpClient.executeHttpPost(url, postParameters);
                String res = response.toString();
                res = res.replaceAll("\\s+", "");

                Log.d("SavePhoneState", res);
                MyJsonResponse resp = MyJsonResponse.getResponse(new JSONObject(res));

                if (resp.getType() == TYPES_ENUM.SUCCESS)
                    return resp.getContent();
                else
                    return null;
            } catch (Exception e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(final String msg) {
            mPhoneStateTask = null;

            CommonUtils.popup_msg(MainActivity.this, getString(R.string.save_phone_state), "Info", R.drawable.info);
        }

        @Override
        protected void onCancelled() {
            mPhoneStateTask = null;
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class CreateAccountTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", mUsername));
            postParameters.add(new BasicNameValuePair("email", mUsername+"@middlebox.com"));
            postParameters.add(new BasicNameValuePair("password", mPasswordHashed));
            postParameters.add(new BasicNameValuePair("repeatpw", mPasswordHashed));
            Random rand = new Random();
            long channelid = rand.nextLong();
            postParameters.add(new BasicNameValuePair("channelid", String.valueOf(channelid)));

            String response = null;
            try {
                String IP = getString(R.string.IP);
                String PORT = getString(R.string.PORT);
                String WebService = getString(R.string.CreateAccountService);
                String url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                response = CustomHttpClient.executeHttpPost(url, postParameters);
                String res = response.toString();
                res = res.replaceAll("\\s+", "");

                Log.d("CreateAccount", res);
                MyJsonResponse resp = MyJsonResponse.getResponse(new JSONObject(res));

                if (mUsername != "") {
                    Log.d("Store email/password", "xxxx");
                    storeUsername(mUsername);
                    storePassword(mPassword, false);
                }

                if (resp.getType() == TYPES_ENUM.SUCCESS)
                    return resp.getContent();
                else
                    return null;
            } catch (Exception e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(final String msg) {
            mCreateAccountTask = null;

            if (msg == null){
                Log.d("signup fail", "xxxx");
            }
        }

        @Override
        protected void onCancelled() {
            mCreateAccountTask = null;
        }
    }


    /**
     * This is called when user clicks the login button Proceeds to login if we
     * have internet connectivity. If not user is prompted to connect
     */
    private void ProceedToLogin() {

        // if not connected
        if (!CommonUtils.isOnline(this)) {
            CommonUtils.AlertDialogInternetConnection(this);
        } else
            // We have Internet. Just login
            attemptLogin();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }



        // Store values at the time of the login attempt.
//        DeviceUuidFactory uuidFactory = new DeviceUuidFactory(this);
//        mEmail = uuidFactory.getDeviceUuid().toString();
//        mPassword = "123456";

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("SHA-512");
            byte[] bytes = mPassword.getBytes();
            int byteCount = bytes.length;
            digester.update(bytes, 0, byteCount);
            byte[] digest = digester.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }
            mPasswordHashed = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return;
        }

        mAuthTask = new UserLoginTask();
        mAuthTask.execute((Void) null);
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("method", "log_in"));
            postParameters.add(new BasicNameValuePair("username", mUsername));
            postParameters.add(new BasicNameValuePair("password", mPasswordHashed));

            String response = null;
            try {
                String IP = getString(R.string.IP);
                String PORT = getString(R.string.PORT);
                String WebService = getString(R.string.LoginService);
                String url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                response = CustomHttpClient.executeHttpPost(url, postParameters);
                String res = response.toString();
                res = res.replaceAll("\\s+", "");

                MyJsonResponse resp = MyJsonResponse.getResponse(new JSONObject(res));

                if (resp.getType() == TYPES_ENUM.SUCCESS)
                    return resp.getContent();
                else
                    return null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String UserId) {
            mAuthTask = null;

            if (UserId != null) {
                storeUsername(mUsername);
                storePassword(mPassword, true);
                storeUserID(UserId);
            } else {
                storePassword(null, false);
                storeUserID(null);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {

    }

    /*
 * Called when the Activity is restarted, even before it becomes visible.
 */
    @Override
    public void onStart() {

        super.onStart();

        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();

    }

    /*
 * Called when the Activity is no longer visible at all.
 * Stop updates and disconnect.
 */
    @Override
    public void onStop() {

        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();

        super.onStop();
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

    /**
     * Invoked by the "Get Address" button.
     * Get the address of the current location, using reverse geocoding. This only works if
     * a geocoding service is available.
     *
     */
    // For Eclipse with ADT, suppress warnings about Geocoder.isPresent()
    @SuppressLint("NewApi")
    public void getAddress() {
        if (!isLocationEnabled(this)) {
            enableLocationSharing(this);
            return;
        }

        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
            return;
        }

        if (servicesConnected()) {

            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();

            // Start the background task
            (new MainActivity.GetAddressTask(this)).execute(currentLocation);
        }
    }

    /**
     * An AsyncTask that calls getFromLocation() in the background.
     * The class uses the following generic types:
     * Location - A {@link android.location.Location} object containing the current location,
     *            passed as the input parameter to doInBackground()
     * Void     - indicates that progress units are not used by this subclass
     * String   - An address passed to onPostExecute()
     */
    protected class GetAddressTask extends AsyncTask<Location, Void, String> {

        // Store the context passed to the AsyncTask when the system instantiates it.
        Context localContext;

        // Constructor called by the system to instantiate the task
        public GetAddressTask(Context context) {

            // Required by the semantics of AsyncTask
            super();

            // Set a Context for the background task
            localContext = context;
        }

        /**
         * Get a geocoding service instance, pass latitude and longitude to it, format the returned
         * address, and return the address to the UI thread.
         */
        @Override
        protected String doInBackground(Location... params) {
            /*
             * Get a new geocoding service instance, set for localized addresses. This example uses
             * android.location.Geocoder, but other geocoders that conform to address standards
             * can also be used.
             */
            Geocoder geocoder = new Geocoder(localContext, Locale.getDefault());

            // Get the current location from the input parameter list
            Location location = params[0];

            // Create a list to contain the result address
            List<Address> addresses = null;

            // Try to get an address for the current location. Catch IO or network problems.
            try {

                /*
                 * Call the synchronous getFromLocation() method with the latitude and
                 * longitude of the current location. Return at most 1 address.
                 */
                addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1
                );

                // Catch network or other I/O problems.
            } catch (IOException exception1) {

                // Log an error and return an error message
                Log.e(LocationUtils.APPTAG, getString(R.string.IO_Exception_getFromLocation));

                // print the stack trace
                exception1.printStackTrace();

                // Return an error message
                return (getString(R.string.IO_Exception_getFromLocation));

                // Catch incorrect latitude or longitude values
            } catch (IllegalArgumentException exception2) {

                // Construct a message containing the invalid arguments
                String errorString = getString(
                        R.string.illegal_argument_exception,
                        location.getLatitude(),
                        location.getLongitude()
                );
                // Log the error and print the stack trace
                Log.e(LocationUtils.APPTAG, errorString);
                exception2.printStackTrace();

                //
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {

                // Get the first address
                Address address = addresses.get(0);

                // Format the first line of address
                String addressText = getString(R.string.address_output_string,

                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",

                        // Locality is usually a city
                        address.getLocality(),

                        // The country of the address
                        address.getCountryName()
                );

                storedAddress = addressText;
                System.out.println(addressText);

                // Return the text
                return addressText;

                // If there aren't any addresses, post a message
            } else {
                return getString(R.string.no_address_found);
            }
        }
    }
    /**
     * Diagnostic features
     */

    public class CacheTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            String response = null;
            try {
                // start tcpdump
                String IP = getString(R.string.IP_TCPDUMP);
                String PORT = getString(R.string.PORT_TCPDUMP);
                String WebService = getString(R.string.StartTcpDump);

                Random rand = new Random();
                int random_id = (1 + rand.nextInt(1000));
                String logFile = "&log_c_our_server_" + random_id + ".pcap";
                WebService += logFile;

                String url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                CustomHttpClient.executeHttpGet(url);

                url = CommonUtils.getFilePath(IP, "/PEP/images/", "test_cache_" + random_id + ".jpg");
                CustomHttpClient.executeHttpGet(url);

                // sleep
                try {
                    Thread.sleep(3000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                url = CommonUtils.getFilePath(IP, "/PEP/images/", "test_cache_" + random_id + ".jpg");
                CustomHttpClient.executeHttpGet(url);

                // stop tcpdump
                WebService = getString(R.string.StopTcpDump);
                WebService += logFile;

                url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                response = CustomHttpClient.executeHttpGet(url);

                String res = response.toString();
                res = res.replaceAll("\\s+", "");

                return res;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String res) {
            mCacheTask = null;
            if (res != null) {
                try {
                    System.out.println(res);
                    System.out.println("parsing json ...");
                    JSONObject jObj = new JSONObject(res);
                    int result = jObj.getInt("result");
                    String metadata = jObj.getString("metadata");

                    new GenericTask().execute("1", String.valueOf(result), metadata);

                } catch (Exception e) {
                    System.out.println("parsing json exception");
                }
            }
        }

        @Override
        protected void onCancelled() {
            mCacheTask = null;
        }
    }

    public class DelayHandshakeTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            String response = null;
            try {
                // start tcpdump
                String IP = getString(R.string.IP_TCPDUMP);
                String PORT = getString(R.string.PORT_TCPDUMP);
                String WebService = getString(R.string.StartTcpDump);

                Random rand = new Random();
                int random_id = (1 + rand.nextInt(1000));
                String logFile = "&log_dh_our_server_" + random_id + ".pcap";
                WebService += logFile;

                String url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                CustomHttpClient.executeHttpGet(url);

                // open socket
                try {
                    Socket client = new Socket(IP,80);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // sleep
                try {
                    Thread.sleep(3000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                url = CommonUtils.getFilePath(IP, "/PEP/images/", "non_exist_file_dh_" + random_id);
                CustomHttpClient.executeHttpGet(url);

                // stop tcpdump
                WebService = getString(R.string.StopTcpDumpDH);
                WebService += logFile;

                url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                response = CustomHttpClient.executeHttpGet(url);

                String res = response.toString();
                res = res.replaceAll("\\s+", "");

                return res;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String res) {
            mDelayHandshakeTask = null;
            if (res != null) {
                try {
                    System.out.println(res);
                    System.out.println("parsing json ...");
                    JSONObject jObj = new JSONObject(res);
                    int result = jObj.getInt("result");
                    String metadata = jObj.getString("metadata");

                    new GenericTask().execute("2", String.valueOf(result), metadata);

                } catch (Exception e) {
                    System.out.println("parsing json exception");
                }
            }
        }

        @Override
        protected void onCancelled() {
            mDelayHandshakeTask = null;
        }
    }

    public class RedirectionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {

            String response = null;
            try {
                // start tcpdump
                String IP = getString(R.string.IP_TCPDUMP);
                String PORT = getString(R.string.PORT_TCPDUMP);
                String WebService = getString(R.string.StartTcpDump);

                Random rand = new Random();
                int random_id = (1 + rand.nextInt(1000));
                String logFile = "&log_re_our_server_" + random_id + ".pcap";
                WebService += logFile;

                String url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                CustomHttpClient.executeHttpGet(url);

                // make http get request to IP with a third party hostname
                url = CommonUtils.getFilePath(IP, "/PEP/images/", "non_exist_file_re_" + random_id);
                CustomHttpClient.executeHttpGet(url, "www.google.com");

                // stop tcpdump
                WebService = getString(R.string.StopTcpDumpRE);
                WebService += logFile;

                url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                response = CustomHttpClient.executeHttpGet(url);

                String res = response.toString();
                res = res.replaceAll("\\s+", "");

                return res;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String res) {
            mRedirectionTask = null;
            if (res != null) {
                try {
                    System.out.println(res);
                    System.out.println("parsing json ...");
                    JSONObject jObj = new JSONObject(res);
                    int result = jObj.getInt("result");
                    String metadata = jObj.getString("metadata");

                    new GenericTask().execute("3", String.valueOf(result), metadata);

                } catch (Exception e) {
                    System.out.println("parsing json exception");
                }
            }
        }

        @Override
        protected void onCancelled() {
            mRedirectionTask = null;
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class GenericTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String exp_type = params[0];
            String result = params[1];
            String metadata = params[2];

            System.out.println("saving data to db ...");
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("username", mUsername));
            postParameters.add(new BasicNameValuePair("exp_type", exp_type));
            postParameters.add(new BasicNameValuePair("result", result));
            postParameters.add(new BasicNameValuePair("metadata", metadata));
            System.out.println(cellularNetwork);
            postParameters.add(new BasicNameValuePair("cellular", cellularNetwork));

            postParameters.add(new BasicNameValuePair("address", storedAddress));


            String response = null;
            try {
                String IP = getString(R.string.IP);
                String PORT = getString(R.string.PORT);
                String WebService = getString(R.string.StoreDataService);
                String url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                response = CustomHttpClient.executeHttpPost(url, postParameters);
                String res = response.toString();
                res = res.replaceAll("\\s+", "");
                System.out.println(res);
                MyJsonResponse resp = MyJsonResponse.getResponse(new JSONObject(res));

                if (resp.getType() == TYPES_ENUM.SUCCESS)
                    return res;
                else
                    return null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String res) {

            if (res != null) {
                try {
                    CommonUtils.popup_msg(MainActivity.this, getString(R.string.done_generic_task), "Info", R.drawable.info);
                } catch (Exception e) {
                    System.out.println("...");
                }
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    private String saveData(int i, int result, String metadata) {

        System.out.println("saving data to db ...");
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("username", mUsername));
        postParameters.add(new BasicNameValuePair("exp_type", String.valueOf(i)));
        postParameters.add(new BasicNameValuePair("result", String.valueOf(result)));
        postParameters.add(new BasicNameValuePair("metadata", "xxx"));

        String response = null;
        try {
            String IP = getString(R.string.IP);
            String PORT = getString(R.string.PORT);
            String WebService = getString(R.string.StoreDataService);
            String url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
            response = CustomHttpClient.executeHttpPost(url, postParameters);
            String res = response.toString();
            res = res.replaceAll("\\s+", "");
            System.out.println(res);
            MyJsonResponse resp = MyJsonResponse.getResponse(new JSONObject(res));

            if (resp.getType() == TYPES_ENUM.SUCCESS)
                return res;
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }


//    private ProgressDialog pDialog;
//    public static final int progress_bar_type = 0;

    class ContentRewrite extends AsyncTask<Void, Void, String> {

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(Void... params) {
            String IP = getString(R.string.IP_TCPDUMP);
            String url_html = CommonUtils.getFilePath(IP, "/PEP/", "test.html");
            String url_jpg = CommonUtils.getFilePath(IP, "/PEP/", "test.jpg");
            String url_css = CommonUtils.getFilePath(IP, "/PEP/", "test.css");
            String url_js = CommonUtils.getFilePath(IP, "/PEP/", "test.js");

            int size_jpg = downloadFile(url_jpg);
            int size_css = downloadFile(url_css);
            int size_js = downloadFile(url_js);
            int size_html = downloadFile(url_html);

            JSONObject object = new JSONObject();
            try {
                if (size_jpg != 211258 || size_css != 3999 || size_js != 7999 || size_html != 3999)
                    object.put("result", 1);
                else
                    object.put("result", 0);
                object.put("metadata", size_jpg + "|" + size_css + "|" + size_js + "|" + size_html);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object.toString();
        }

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            showDialog(progress_bar_type);
        }



        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
//            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String res) {
            // dismiss the dialog after the file was downloaded
//            dismissDialog(progress_bar_type);
            mContentRewrite = null;
            try {
                System.out.println(res);
                System.out.println("parsing json ...");
                JSONObject jObj = new JSONObject(res);
                int result = jObj.getInt("result");
                String metadata = jObj.getString("metadata");

                new GenericTask().execute("4", String.valueOf(result), metadata);

            } catch (Exception e) {
                System.out.println("parsing json exception");
            }
        }

        @Override
        protected void onCancelled() {
            mContentRewrite = null;
        }
    }


    class CheckMiddlebox extends AsyncTask<Void, Void, String> {

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(Void... params) {
            System.out.println("Test middlebox");
            long average80 = 0;
            long average433 = 0;

            int i = 0;
            while(i < 50)
            {
                long start_time = System.currentTimeMillis();
                try {
                    Socket client = new Socket("74.125.225.147",80);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                long end_time = System.currentTimeMillis();
                long difference = end_time - start_time;
                //System.out.println(difference);
                average80 += difference;
                i++;
            }
            average80 = average80/50;
            System.out.println("the value of port 80 is "+average80);

            i = 0;
            while(i < 50)
            {
                long start_time = System.currentTimeMillis();
                try {
                    Socket client = new Socket("74.125.225.147",443);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                long end_time = System.currentTimeMillis();
                long difference = end_time - start_time;
                //System.out.println(difference);
                average433 += difference;
                i++;
            }
            average433 = average433/50;
            System.out.println("the value of port 443 is "+average433);

            JSONObject object = new JSONObject();
            try {
                if (average433 > average80)
                    object.put("result", 1);
                else
                    object.put("result", 0);
                object.put("metadata", average433 + "|" + average80);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object.toString();
        }

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String res) {
            mCheckMiddlebox = null;

            try {
                System.out.println(res);
                System.out.println("parsing json ...");
                JSONObject jObj = new JSONObject(res);
                int result = jObj.getInt("result");
                String metadata = jObj.getString("metadata");

                new GenericTask().execute("5", String.valueOf(result), metadata);

            } catch (Exception e) {
                System.out.println("parsing json exception");
            }
        }

        @Override
        protected void onCancelled() {
            mCheckMiddlebox = null;
        }
    }



    public int downloadFile(String url_str) {
        final int DOWNLOAD_BUFFER_SIZE = 1024;
        URL url;
        URLConnection conn;
        int fileSize, lastSlash;
        String fileName;
        BufferedInputStream inStream;
        BufferedOutputStream outStream;
        File outFile;
        FileOutputStream fileStream;

        int totalRead = 0;
        try
        {
            url = new URL(url_str);

            conn = url.openConnection();
            conn.setUseCaches(false);
            fileSize = conn.getContentLength();

            // get the filename
            lastSlash = url.toString().lastIndexOf('/');
            fileName = "file.bin";
            if(lastSlash >=0)
            {
                fileName = url.toString().substring(lastSlash + 1);
            }
            if(fileName.equals(""))
            {
                fileName = "file.bin";
            }

            // notify download start
            System.out.println("file size " + fileSize);

            // start download
            inStream = new BufferedInputStream(conn.getInputStream());
            outFile = File.createTempFile(fileName, null, getBaseContext().getCacheDir());
            fileStream = new FileOutputStream(outFile);
            outStream = new BufferedOutputStream(fileStream, DOWNLOAD_BUFFER_SIZE);
            byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
            int bytesRead = 0;
            while((bytesRead = inStream.read(data, 0, data.length)) >= 0)
            {
                outStream.write(data, 0, bytesRead);

                // update progress bar
                totalRead += bytesRead;
            }

            outStream.close();
            fileStream.close();
            inStream.close();
        }
        catch(MalformedURLException e)
        {

        }
        catch(FileNotFoundException e)
        {

        }
        catch(Exception e)
        {

        }
        return totalRead;
    }


    /**
     * Statistics
     */

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class SummaryAllTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            System.out.println("summary all ...");
            String response = null;
            try {
                String IP = getString(R.string.IP);
                String PORT = getString(R.string.PORT);
                String WebService = getString(R.string.SummaryAllService);
                String url = CommonUtils.getAbsoluteURL(IP, PORT, WebService);
                response = CustomHttpClient.executeHttpGet(url);
                String res = response.toString();
                res = res.replaceAll("\\s+", "");
                System.out.println(res);
                MyJsonResponse resp = MyJsonResponse.getResponse(new JSONObject(res));
                if (resp.getType() == MyJsonResponse.TYPES_ENUM.SUCCESS)
                    return res;
                else
                    return null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String res) {
            if (res != null) {
                try {
                    System.out.println("parsing json ...");
                    JSONObject jObj = new JSONObject(res);
                    summary_all_jObj = jObj;
                } catch (Exception e) {
                    System.out.println("parsing json exception");
                }
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}