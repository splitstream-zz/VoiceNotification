package org.stream.split.voicenotification;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.UserHandle;
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
import android.widget.CompoundButton;
import android.widget.Switch;

import org.stream.split.voicenotification.Fragments.AppFragment;
import org.stream.split.voicenotification.Fragments.NotificationsHistoryFragment;
import org.stream.split.voicenotification.Fragments.SettingsFragment;
import org.stream.split.voicenotification.Helpers.Helper;
import org.stream.split.voicenotification.Helpers.NotificationServiceConnection;
import org.stream.split.voicenotification.Interfaces.OnFragmentInteractionListener;

import java.security.Timestamp;


//TODO add "Back" functionality using back arrow(in place of dongle on appbar) or hardware back key
//TODO dodać funkcjonalności związane z dodawaniem warunków, po spełnieniu których,
public class VoiceNotificationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {

    private final String TAG = "VoiceNotificationActivity";
    private NotificationManager mNotificationManager;
    private final int mPersistentNotificationID = 8976;
    private final int mTestingNotificationID = 6879;
    private FragmentManager mFragmentManager;
    private Intent mServiceIntent;
    private NotificationServiceConnection mServiceConnection;
    private Fragment mHistoryFragment;
    private int mBackKeyCount = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServiceIntent = new Intent(this, NotificationService.class);
        mServiceConnection = NotificationServiceConnection.getInstance();
        mServiceIntent.setAction(NotificationService.CUSTOM_BINDING);

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
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mHistoryFragment = new NotificationsHistoryFragment();
        fragmentTransaction.add(R.id.frame_content, mHistoryFragment)
                .addToBackStack("initial")
                .commit();

//        mFragmentManager.beginTransaction().add(R.id.frame_content, fragment)
//                .addToBackStack("initial")
//                .commit();

        //creating persistent notification for purposes of informing user of running up
        mNotificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createPersistentAppNotification();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mBackKeyCount = 2;
        bindNotificationService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        Log.d(TAG, "onStop()");

        unbindNotificationService();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(mFragmentManager.getBackStackEntryCount() > 1) {
            mFragmentManager.popBackStack();
        }
        else {
            if(mBackKeyCount > 0)
            {

            }
            else {

                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.voice_notification, menu);
        MenuItem menuItem = menu.findItem(R.id.offSwitch);
        View view = MenuItemCompat.getActionView(menuItem);
        Switch switcha = (Switch)view.findViewById(R.id.switchForActionBar);
        boolean checked = mServiceConnection.IsVoiceActive();
        switcha.setChecked(checked);
        switcha.refreshDrawableState();
        switcha.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mServiceConnection.setIsVoiceActive(isChecked);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //TODO zaimplementować funkcję, która będzie wyłączać aplikację całkowicie ( service, activity i persistent notification)
        switch(id) {
//            case R.id.action_turn_off:
//                turnOffApp();
//                break;
            case R.id.action_settings:
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                transaction.replace(R.id.frame_content, new SettingsFragment());
                transaction.commit();
            default:
                Snackbar.make(findViewById(R.id.coordinator_layout), "Item not implemented", Snackbar.LENGTH_SHORT).show();
                break;
        }

        return false;
    }

    private void turnOffApp()
    {
        mServiceConnection.unregisterAllRecivers();
        mNotificationManager.cancelAll();
        finish();
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
                fragment = AppFragment.newInstance(AppFragment.APPLICATIONS_TO_SHOW.SHOW_FOLLOWED);
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

        if(fragment != null) {
            mFragmentManager.beginTransaction().remove(mHistoryFragment).commit();
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.frame_content, fragment)
                    .addToBackStack(item.getTitle().toString())
                    .commit();
        }

        if(snackBar != null)
            snackBar.show();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void bindNotificationService()
    {
        // Bind to LocalService
        bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    private void unbindNotificationService()
    {
        // unBind to LocalService
        mServiceConnection.unregisterAllRecivers();
        if (mServiceConnection.isServiceBound()) {
            unbindService(mServiceConnection);
        }
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
            mServiceConnection.sentTestNotification(sbn);

        }
        mNotificationManager.notify(mTestingNotificationID, notification);

    }
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
