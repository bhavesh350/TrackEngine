package net.mzi.trackengine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.HashMap;

/**
 * Created by Poonam on 2/21/2017.
 */

public class SessionManager {
    SharedPreferences pref;


    // Editor for Shared preferences
    SharedPreferences.Editor editor;
    // Context
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
   // private static final String PREF_NAME = "AndroidHivePref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_UNAME = "name";

    // Email address (make variable public to access from outside)
    public static final String KEY_PASSWORD = "password";

    public static final String KEY_USERID = "userid";

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences("login", PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */

    //,boolean CSATEnable, boolean AssetVerification
    public void createLoginSession(String uname, String password, String userid, String DepartmentId, String RoleId, String IsCoordinator, String IsFieldAgent, String UserType, String CompanyId, String ParentCompanyId, String CheckedInTime, String CheckedInStatus, String isDefaultDepartment, int AppLocationSendingFrequency, int AppBatterySendingFrequency, boolean CSATEnable, boolean AssetVerification, String LastAction, String sDeviceId, String LastTransportMode){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        // Storing name in pref
        editor.putString(KEY_UNAME, uname);
        // Storing email in pref
        editor.putString(KEY_PASSWORD, password);
        // commit changes
        editor.putString(KEY_USERID, userid);
        editor.putString("DepartmentId",DepartmentId);
        editor.putString("RoleId",RoleId);
        editor.putString("IsCoordinator",IsCoordinator);
        editor.putString("IsFieldAgent",IsFieldAgent);
        editor.putString("UserType",UserType);
        editor.putString("CompanyId",CompanyId);
        editor.putString("ParentCompanyId",ParentCompanyId);
        editor.putString("CheckedInTime",CheckedInTime);
        editor.putString("CheckedInStatus",CheckedInStatus);
        editor.putString("IsDefaultDepartment",isDefaultDepartment);
        editor.putString("CheckedInStatus",CheckedInStatus);
        editor.putString("IsDefaultDepartment",isDefaultDepartment);
        editor.putInt("AppLocationSendingFrequency",AppLocationSendingFrequency);
        editor.putInt("AppBatterySendingFrequency",AppBatterySendingFrequency);
        editor.putBoolean("CSATEnable",CSATEnable);
        editor.putBoolean("AssetVerification",AssetVerification);
        editor.putString("LastAction",LastAction);
        editor.putString("DeviceId",sDeviceId);
        editor.putString("LastTransportMode",LastTransportMode);
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    /*public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, MainActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }*/



    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_UNAME, pref.getString(KEY_UNAME, null));

        // user email id
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));
        user.put(KEY_USERID,pref.getString(KEY_USERID,null));
        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
       /* Intent i = new Intent(_context, MainActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);*/


    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
