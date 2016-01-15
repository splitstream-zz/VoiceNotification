package org.stream.split.voicenotification;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.CompoundButton;
import android.widget.Switch;

import org.stream.split.voicenotification.Fragments.BaseFragment;
import org.stream.split.voicenotification.Fragments.FollowedAppFragment;
import org.stream.split.voicenotification.Fragments.NotificationsHistoryFragment;
import org.stream.split.voicenotification.Fragments.SettingsFragment;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Helpers.NotificationServiceConnection;
import org.stream.split.voicenotification.Interfaces.OnFragmentInteractionListener;
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
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {

    public static Fragment CURRENT_FRAGMENT;

    private final String TAG = "VoiceNotificationActivity";
    private NotificationManager mNotificationManager;
    private final int mPersistentNotificationID = 8976;
    private final int mTestingNotificationID = 6879;
    private FragmentManager mFragmentManager;
    private NotificationServiceConnection mServiceConnection;

    /**
     * zmienna w której zapisywany jest timestamp naciśniecia klawisza back
     */
    private long mExitBackKeyTimestamp;
    /**
     * zmienna która podaje czas w ciągu którego podwójnie należy nacisnąć back klawisz
     * aby wyjść z aplikacji. podano
     */
    private long mExitBackKeyInterval = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServiceConnection = NotificationServiceConnection.getInstance();
        mServiceConnection.initializeServiceState(getBaseContext());
        mServiceConnection.setActiveSpeechService(true);

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
        //creating persistent notification for purposes of informing user of running up
        mNotificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createPersistentAppNotification();
        checkNotificationAccess();
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
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        Log.d(TAG, "onStop()");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServiceConnection.unregisterAllReceivers();

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
        boolean checked = mServiceConnection.isSpeechServiceActive();
        Switch switcha = (Switch)view.findViewById(R.id.switchForActionBar);
        switcha.setChecked(checked);
        switcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServiceConnection.setActiveSpeechService(((Switch)v).isChecked());
            }
        });
        switcha.refreshDrawableState();
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
//                createTestingNotification();
//                snackBar = Snackbar.make(drawer, "test notification was send", Snackbar.LENGTH_SHORT);
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
//
//    private void createTestingNotification()
//    {
//        Intent intent = new Intent(getApplicationContext(), VoiceNotificationActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification notification = Helper.createNotification(getApplicationContext(), pendingIntent, "Tytuł", "Na tydzień przed wyborami parlamentarnymi Andrzej Duda był gościem specjalnego wydania programu \"Kawa na ławę\". Bogdan Rymanowski pytał prezydenta m.in. o relacje z rządem, politykę zagraniczną i ocenę dobiegającej końca kampanii wyborczej.", "subtext", false);
//        if(Debug.isDebuggerConnected())
//        {
//            Log.d(TAG, "create test notification DEBUG MODE ON");
//
//            UserHandle userHandle = android.os.Process.myUserHandle();
//
//            StatusBarNotification sbn = new StatusBarNotification(this.getPackageName(),"",mTestingNotificationID,"tag?",18,19,3,notification,userHandle, System.currentTimeMillis() );
//            mServiceConnection.sentTestNotification(sbn);
//
//        }
//        mNotificationManager.notify(mTestingNotificationID, notification);
//
//    }
    private void createPersistentAppNotification()
    {
        Intent intent = new Intent(getApplicationContext(),VoiceNotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = Helper.createPersistentAppNotification(getApplicationContext(), pendingIntent);
        mNotificationManager.notify(mPersistentNotificationID, notification);
    }

    /**
     * Funkcja służaca do komunikacji pomiędzy Fragmentami
     * @param id
     */
    @Override
    public void onFragmentInteraction(long [] id) {
        Snackbar.make(this.findViewById(R.id.frame_content),"sdasd",Snackbar.LENGTH_SHORT).show();
    }

}
