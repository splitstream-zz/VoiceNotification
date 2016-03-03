package org.stream.split.voicenotification;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import org.stream.split.voicenotification.Fragments.BaseFragment;
import org.stream.split.voicenotification.Fragments.FollowedAppFragment;
import org.stream.split.voicenotification.Fragments.HistoryNotificationListFragment;
import org.stream.split.voicenotification.Fragments.SettingsFragment;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Helpers.NotificationServiceConnection;
import org.stream.split.voicenotification.Logging.BaseLogger;

//TODO dodać do poszczególnych fragmentów tytuły
//TODO dodać funkcjonalności związane z dodawaniem warunków, po spełnieniu których,
//TODO there is warning about notification access even when it is allowed.
//TODO Make proper logging class not only to output but db as well
public class VoiceNotificationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    //public static Fragment CURRENT_FRAGMENT;

    private final String TAG = "VoiceNotifiActivity";
    private NotificationManager mNotificationManager;
    private final int mTestingNotificationID = 6879;
    private FragmentManager mFragmentManager;
    private NotificationServiceConnection mServiceConnection;
    private NotificationServiceCallback mNotificationServiceCallback;
    private SharedPreferences mSharedPreferences;
    private boolean mIsVoiceActive;
    private Switch mIsVoiceActiveSwitch;
    private BaseLogger logger = BaseLogger.getInstance();
    private Snackbar mCheckNotificationAccess;

    /**
     * zmienna w której zapisywany jest timestamp naciśniecia klawisza back
     */
    private long mExitBackKeyTimestamp;
    /**
     * zmienna która podaje czas w ciągu którego podwójnie należy nacisnąć back klawisz
     * aby wyjść z aplikacji. podano
     */
    private long mExitBackKeyInterval = 3000;

    public boolean isVoiceActive() {
        return mIsVoiceActive;
    }

    public void setIsVoiceActive(boolean isVoiceActive) {
        this.mIsVoiceActive = isVoiceActive;
        String isVoiceActivePrefKey = getResources().getString(R.string.IS_VOICE_ACTIVE_PREFERENCE_KEY);
        logger.d(TAG, "set voice active = "+isVoiceActive);
        mSharedPreferences.edit().putBoolean(isVoiceActivePrefKey,isVoiceActive).apply();
    }

    public Fragment getCurrentFragment()
    {
        Fragment fragment = mFragmentManager.findFragmentById(R.id.frame_content);
        return fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.deleteDatabase(DBContract.DB_Name);
        //logger.addExcludedTag(DBHelper.TAG);
        //DbLogger<DbToLog> log = new DbLogger<>(DbLogger.PRIORITY_D,this.getBaseContext(),DbToLog.class);
        //logger.addLogger(log);

        //Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));

        Resources res = this.getResources();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        initializeActivity(mSharedPreferences, res);

        mServiceConnection = NotificationServiceConnection.getInstance();

        setContentView(R.layout.activity_voice_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFragmentManager = getFragmentManager();
        String historyFragmentTitle = res.getString(R.string.HISTORY_NOTIFICATION_LIST_FRAGMENT_TITLE);
        if(savedInstanceState == null) {
            HistoryNotificationListFragment fragment = HistoryNotificationListFragment.newInstance();
            fragment.setTitle(historyFragmentTitle);
            mFragmentManager.beginTransaction()
                    .add(R.id.frame_content, fragment)
                    .addToBackStack(fragment.getTitle())
                    .commit();
        }

        mNotificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        //setting background from splash screen back to color
        View root = drawer.getRootView();
        root.setBackgroundColor(Color.LTGRAY);
    }

    private void initializeActivity(SharedPreferences sharedPreferences, Resources res) {
        mIsVoiceActive = sharedPreferences.getBoolean(res.getString(R.string.IS_VOICE_ACTIVE_PREFERENCE_KEY),false);
    }

    private void checkNotificationAccess() {
        if(!NotificationService.isNotificationRelayActive()) {
            mCheckNotificationAccess = Snackbar.make(findViewById(R.id.coordinator_layout), "Notification access denied!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent notificationAccess = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(notificationAccess);
                        }
                    });
            mCheckNotificationAccess.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this,NotificationService.class);
        intent.setAction(NotificationService.CUSTOM_BINDING);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        //creating persistent notification
        checkNotificationAccess();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mNotificationServiceCallback = new NotificationServiceCallback();
        mServiceConnection.registerReceiver(mNotificationServiceCallback);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        Log.d(TAG, "onStop()");
        mServiceConnection.unregisterReceiver(mNotificationServiceCallback);
        unbindService(mServiceConnection);
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        Fragment currentFragment = getCurrentFragment();

        if (currentFragment instanceof HistoryNotificationListFragment) {
            if (System.currentTimeMillis() - mExitBackKeyTimestamp < mExitBackKeyInterval) {
                finish();
            } else {
                mExitBackKeyTimestamp = System.currentTimeMillis();
                Snackbar.make(findViewById(R.id.coordinator_layout), R.string.EXIT_SNACKBAR_TEXT, Snackbar.LENGTH_SHORT).show();
            }
        }
        else if (currentFragment instanceof BaseFragment && ((BaseFragment) currentFragment).isModified()){
                Snackbar.make(findViewById(R.id.coordinator_layout), R.string.UNSAVED_DATA_WARNING_SNACKBAR_TEXT, Snackbar.LENGTH_LONG)
                        .setAction(R.string.UNSAVED_DATA_SNACKBAR_BTN_LBL, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mFragmentManager.popBackStack();
                            }
                        }).show();
            }
        else
            mFragmentManager.popBackStack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.clear();
        getMenuInflater().inflate(R.menu.voice_notification, menu);
        MenuItem menuItem = menu.findItem(R.id.offSwitch);
        View view = MenuItemCompat.getActionView(menuItem);
        //Todo read from preferences state of the switch
        mIsVoiceActiveSwitch = (Switch)view.findViewById(R.id.switchForActionBar);
        mIsVoiceActiveSwitch.setChecked(isVoiceActive());
        mIsVoiceActiveSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch switcha = (Switch) v;
                setIsVoiceActive(switcha.isChecked());

            }
        });
        mIsVoiceActiveSwitch.refreshDrawableState();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                mFragmentManager.beginTransaction().replace(R.id.frame_content, new SettingsFragment())
                        .addToBackStack(item.getTitle().toString())
                        .commit();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        BaseFragment newFragment = null;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Snackbar snackBar = null;

        switch (id) {
            case R.id.choose_apps:
                newFragment = new FollowedAppFragment();
                newFragment.setTitle(getString(R.string.FOLLOWED_APP_FRAGMENT_TITLE));
                break;
            case R.id.history:
                newFragment = HistoryNotificationListFragment.newInstance();
                newFragment.setTitle(getString(R.string.HISTORY_NOTIFICATION_LIST_FRAGMENT_TITLE));
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                createTestingNotification();
                snackBar = Snackbar.make(drawer, "test notification was send", Snackbar.LENGTH_SHORT);
                break;
        }

        Fragment currentFragment = getCurrentFragment();
        if((newFragment != null && (currentFragment == null || newFragment.getClass() != currentFragment.getClass()))) {
            mFragmentManager.beginTransaction().replace(R.id.frame_content, newFragment)
                    .addToBackStack(newFragment.getTAG())
                    .commit();
        }

        if (snackBar != null)
            snackBar.show();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createTestingNotification()
    {
        Intent intent = new Intent(getApplicationContext(), VoiceNotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = Helper.createNotification(getApplicationContext(), pendingIntent, "Tytuł", "Na tydzień przed wyborami parlamentarnymi Andrzej Duda był gościem specjalnego wydania programu \"Kawa na ławę\". Bogdan Rymanowski pytał prezydenta m.in. o relacje z rządem, politykę zagraniczną i ocenę dobiegającej końca kampanii wyborczej.", "subtext", false);
        mNotificationManager.notify(mTestingNotificationID, notification);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Resources res = getResources();
        logger.d(TAG,"onSharedPreferenceChanged()");
        if(key.equals(res.getString(R.string.IS_VOICE_ACTIVE_PREFERENCE_KEY)))
        {
            Boolean isVoiceActive = sharedPreferences.getBoolean(key,false);
            mIsVoiceActiveSwitch.setChecked(isVoiceActive);
            mIsVoiceActive = isVoiceActive;
            logger.d(TAG,"onSharedPreferenceChanged(), isVoiceActive = " + isVoiceActive);
        }

    }
    private class NotificationServiceCallback extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getAction()) {
                case NotificationService.ACTION_NOTIFICATION_ACCESS_CHANGED:
                    if (intent.getBooleanExtra(NotificationService.EXTRA_IS_NOTIFICATION_ACCESS_GRANTED, false))
                        mCheckNotificationAccess.dismiss();
                    else
                        mCheckNotificationAccess.show();
                    break;
            }
        }
    }
}
