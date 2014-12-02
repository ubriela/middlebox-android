package middlebox.activities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
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
import android.util.Log;

import middlebox.utils.CommonUtils;
import middlebox.utils.CustomHttpClient;
import middlebox.utils.MyJsonResponse;
import middlebox.utils.MyJsonResponse.TYPES_ENUM;
import org.apache.http.NameValuePair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import android.telephony.TelephonyManager;
import android.content.Context;
//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuInflater;


import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;


import middlebox.utils.DeviceUuidFactory;

import middlebox.R;

public class MainActivity extends Activity {

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
    private boolean remember_me = false;

    private SharedPreferences Preferences;

    /**
     * Test features
     */
    private CacheTask mCacheTask = null;    // type = 1
    private DelayHandshakeTask mDelayHandshakeTask = null;  // type = 2
    private RedirectionTask mRedirectionTask = null;

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

        if (savedInstanceState == null) {

             if (dataList.get(0).getTitle() != null) {
                SelectItem(1);
            } else {
                SelectItem(0);
            }
        }

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
			fragment = new FragmentTwo();
			args.putString(FragmentTwo.ITEM_NAME, dataList.get(position)
					.getItemName());
			args.putInt(FragmentTwo.IMAGE_RESOURCE_ID, dataList.get(position)
					.getImgResID());
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
            case R.id.action_settings:
                openSettings();
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
        System.out.println("collecting data ...");
        mCacheTask = new CacheTask();
        mCacheTask.execute((Void) null);

        mDelayHandshakeTask = new DelayHandshakeTask();
        mDelayHandshakeTask.execute((Void) null);

        mRedirectionTask = new RedirectionTask();
        mRedirectionTask.execute((Void) null);


        ContentRewrite mDownloadTask = new ContentRewrite();
        mDownloadTask.execute((Void) null);
    }

    private void openSettings() {
        System.out.println("search");
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

                url = CommonUtils.getFilePath(IP, "/PEP/images/", "test_cache_" + rand + ".jpg");
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
            if (size_jpg != 211258 || size_css != 3999 || size_js != 7999 || size_html != 3999)
                return size_jpg + "|" + size_css + "|" + size_js + "|" + size_html;
            else
                return null;
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
        protected void onPostExecute(String metadata) {
            // dismiss the dialog after the file was downloaded
//            dismissDialog(progress_bar_type);

            if (metadata != null)
                new GenericTask().execute("4", "1", metadata);
            else
                new GenericTask().execute("4", "0", metadata);
        }

        @Override
        protected void onCancelled() {
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
}
