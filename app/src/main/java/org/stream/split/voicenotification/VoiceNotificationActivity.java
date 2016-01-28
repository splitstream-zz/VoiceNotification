package org.stream.split.voicenotification;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Debug;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
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

import org.stream.split.voicenotification.DataAccessLayer.DBContract;
import org.stream.split.voicenotification.DataAccessLayer.DBHelper;
import org.stream.split.voicenotification.Exceptions.ExceptionHandler;
import org.stream.split.voicenotification.Fragments.BaseFragment;
import org.stream.split.voicenotification.Fragments.FollowedAppFragment;
import org.stream.split.voicenotification.Fragments.NotificationsHistoryFragment;
import org.stream.split.voicenotification.Fragments.SettingsFragment;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Helpers.NotificationServiceConnection;
import org.stream.split.voicenotification.Interfaces.OnFragmentInteractionListener;
import org.stream.split.voicenotification.Logging.BaseLogger;
import org.stream.split.voicenotification.Logging.LogToDB;

//TODO make some nice splash screen
//TODO dodać do poszczególnych fragmentów tytuły
//TODO extend fragment menager and make things more comprehensible
//TODO dodać funkcjonalności związane z dodawaniem warunków, po spełnieniu których,
//TODO there is warning about notification access even when it is allowed.
//TODO add setting to turn off persistent notification.
//TODO turn off persistent notification after turning off switch
//TODO Make proper logging class not only to output but db as well
//TODO change persistent notification accordingly to app setting
//TODO settings should store data to initialize application e.g. state of the voice utterences (on/of switch)
public class VoiceNotificationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static Fragment CURRENT_FRAGMENT;

    private final String TAG = "VoiceNotifiActivity";
    private NotificationManager mNotificationManager;
    private final int mTestingNotificationID = 6879;
    private FragmentManager mFragmentManager;
    private NotificationServiceConnection mServiceConnection;
    private SharedPreferences mSharedPreferences;
    private boolean mIsVoiceActive;
    private Switch mIsVoiceActiveSwitch;
    private BaseLogger logger = BaseLogger.getInstance();

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
        String isVoiceActivePrefKey = getResources().getString(R.string.pref_is_voice_active_key);
        logger.d(TAG, "set voice active = "+isVoiceActive);
        mSharedPreferences.edit().putBoolean(isVoiceActivePrefKey,isVoiceActive).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.deleteDatabase(DBContract.DB_Name);
        logger.addExcludedTag(DBHelper.TAG);
        LogToDB log = new LogToDB(LogToDB.PRIORITY_E);
        logger.addLogger(log);

        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));

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
        if(savedInstanceState == null) {
            NotificationsHistoryFragment fragment = new NotificationsHistoryFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.frame_content, fragment)
                    .addToBackStack("history fragment")
                    .commit();
        }

        mNotificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //creating persistent notification
        checkNotificationAccess();

        //setting background from splash screen back to color
        View root = drawer.getRootView();
        root.setBackgroundColor(Color.LTGRAY);
    }

    private void initializeActivity(SharedPreferences sharedPreferences, Resources res) {
        mIsVoiceActive = sharedPreferences.getBoolean(res.getString(R.string.pref_is_voice_active_key),false);
    }

    private void checkNotificationAccess() {
        if(!NotificationService.isNotificationRelayActive())
            Snackbar.make(findViewById(R.id.coordinator_layout),"Notification access denied!",Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent notificationAccess = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(notificationAccess);
                        }
                    }).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this,NotificationService.class);
        intent.setAction(NotificationService.CUSTOM_BINDING);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        Log.d(TAG, "onStop()");
        unbindService(mServiceConnection);
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if(fragment instanceof BaseFragment)
            setTitle(((BaseFragment) fragment).getTitle());
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        if(CURRENT_FRAGMENT instanceof BaseFragment && ((BaseFragment) CURRENT_FRAGMENT).isModified()) {
            Snackbar.make(findViewById(R.id.coordinator_layout), "Discard unsaved data?", Snackbar.LENGTH_LONG)
                    .setAction("YES", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((BaseFragment) CURRENT_FRAGMENT).finish();
                            mFragmentManager.popBackStack();
                        }
                    }).show();
            return;
        }
        if(CURRENT_FRAGMENT instanceof NotificationsHistoryFragment) {
            if (System.currentTimeMillis() - mExitBackKeyTimestamp < mExitBackKeyInterval) {
                Log.d(TAG, String.valueOf(System.currentTimeMillis() - mExitBackKeyTimestamp));
                finish();
            } else {
                mExitBackKeyTimestamp = System.currentTimeMillis();
                Snackbar.make(findViewById(R.id.coordinator_layout), "Naciśnij kliwisz back aby wyjśc z aplikajci", Snackbar.LENGTH_SHORT).show();
            }
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
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.replace(R.id.frame_content, new SettingsFragment())
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

        Fragment fragment = null;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Snackbar snackBar = null;

        switch (id) {
            case R.id.choose_apps:
                fragment = new FollowedAppFragment();
                break;
            case R.id.history:
                fragment = new NotificationsHistoryFragment();
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                createTestingNotification();
                snackBar = Snackbar.make(drawer, "test notification was send", Snackbar.LENGTH_SHORT);
                break;
        }

        if(fragment != null && fragment.getClass() != CURRENT_FRAGMENT.getClass()) {
            mFragmentManager.beginTransaction().replace(R.id.frame_content, fragment)
                    .addToBackStack(item.getTitle().toString())
                    .commit();

            CURRENT_FRAGMENT = fragment;
        }

        if(snackBar != null)
            snackBar.show();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createTestingNotification()
    {
        Intent intent = new Intent(getApplicationContext(), VoiceNotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = Helper.createNotification(getApplicationContext(), pendingIntent, "Tytuł", "Na tydzień przed wyborami parlamentarnymi Andrzej Duda był gościem specjalnego wydania programu \"Kawa na ławę\". Bogdan Rymanowski pytał prezydenta m.in. o relacje z rządem, politykę zagraniczną i ocenę dobiegającej końca kampanii wyborczej.", "subtext", false);
        if(Debug.isDebuggerConnected())
        {
            Log.d(TAG, "create test notification DEBUG MODE ON");

            UserHandle userHandle = android.os.Process.myUserHandle();

            StatusBarNotification sbn = new StatusBarNotification(this.getPackageName(),"",mTestingNotificationID,"tag?",18,19,3,notification,userHandle, System.currentTimeMillis() );
            mServiceConnection.sendTestNotification(sbn);

        }
        mNotificationManager.notify(mTestingNotificationID, notification);

    }


    /**
     * Funkcja służaca do komunikacji pomiędzy Fragmentami
     * @param id
     */
    @Override
    public void onFragmentInteraction(long [] id) {
        Snackbar.make(this.findViewById(R.id.frame_content),"sdasd",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Resources res = getResources();
        logger.d(TAG,"onSharedPreferenceChanged()");
        if(key.equals(res.getString(R.string.pref_is_voice_active_key)))
        {
            Boolean isVoiceActive = sharedPreferences.getBoolean(key,false);
            mIsVoiceActiveSwitch.setChecked(isVoiceActive);
            mIsVoiceActive = isVoiceActive;
            logger.d(TAG,"onSharedPreferenceChanged(), isVoiceActive = " + isVoiceActive);
        }

    }
}
