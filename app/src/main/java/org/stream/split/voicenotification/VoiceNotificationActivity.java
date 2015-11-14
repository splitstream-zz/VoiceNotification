package org.stream.split.voicenotification;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


//TODO add "Back" functionality using back arrow(in place of dongle on appbar) or hardware back key

public class VoiceNotificationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener  {

    public NotificationCatcherService NotificationService;
    private boolean mServiceBound = false;
    NotificationManager mNotificationManager;
    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        fragmentTransaction.replace(R.id.frame_content,new NotificationsHistoryFragment()).commit();
        //NotificationService.dummyFunction();

        //creating persistent notification for purposes of informing user of running up
        mNotificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createPersistantAppNotification(mNotificationManager, R.integer.persistentNotificationID);
        setUpFab();




    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, NotificationCatcherService.class);
        intent.setAction(NotificationCatcherService.CUSTOM_BINDING);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mServiceBound) {
            unbindService(mConnection);
            mServiceBound = false;
        }
    }
    /***
     * setting up Floating Action button to issue notification for testing purposes
     */
    void setUpFab()
    {
        android.support.design.widget.FloatingActionButton fab = (android.support.design.widget.FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNotification(mNotificationManager, "Tytuł", "Na tydzień przed wyborami parlamentarnymi Andrzej Duda był gościem specjalnego wydania programu \"Kawa na ławę\". Bogdan Rymanowski pytał prezydenta m.in. o relacje z rządem, politykę zagraniczną i ocenę dobiegającej końca kampanii wyborczej.", "subtext", false, 9);
                Snackbar.make(v, "test notification was send", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.voice_notification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Snackbar.make(findViewById(R.id.coordinator_layout),"main activity", Snackbar.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        Fragment fragment = null;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Snackbar snackBar = null;

        if (id == R.id.choose_apps) {

            fragment = AppFragment.newInstance(AppFragment.APPLICATIONS_TO_SHOW.SHOW_FOLLOWED);

//            for screening purposes
//            LayoutInflater li = getLayoutInflater();
//            RelativeLayout container = (RelativeLayout) findViewById(R.id.frame_content);
//            li.inflate(R.layout.fragment_app_item,container);
            // not sure if there is need for custom toolbar
            //Toolbar toolbar = findViewById()
            //setSupportActionBar(toolbar);
        } else if (id == R.id.history) {
            fragment = new NotificationsHistoryFragment();

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            createNotification(mNotificationManager, "Tytuł", "Na tydzień przed wyborami parlamentarnymi Andrzej Duda był gościem specjalnego wydania programu \"Kawa na ławę\". Bogdan Rymanowski pytał prezydenta m.in. o relacje z rządem, politykę zagraniczną i ocenę dobiegającej końca kampanii wyborczej.", "subtext", false, 9);
            snackBar = Snackbar.make(drawer, "test notification was send", Snackbar.LENGTH_SHORT);
            NotificationService.dummyFunction();
        }

        if(fragment != null) {
            fragmentTransaction.replace(R.id.frame_content, fragment).commit();
        }

        if(snackBar != null)
            snackBar.show();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     *
     * @param notificationManager for adding new notification
     * @param notificationId Id for retrieving and updating purposes
     */
    public void createNotification(NotificationManager notificationManager,String title,String text, String subText, Boolean persistance, int notificationId)
    {
        Intent intent = new Intent(this,VoiceNotificationActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 01, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Resources res = getResources();
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setContentTitle(title)
                .setContentText(text)
                .setSubText(subText)
                .setOngoing(persistance)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_persistent_notification);

        notificationManager.notify(notificationId, builder.build());
    }

    public void createPersistantAppNotification(NotificationManager notificationManager, int notificationId)
    {
        Resources res = getResources();
        createNotification(notificationManager,
                res.getString(R.string.Notification_title),
                res.getString(R.string.Notification_text),
                res.getString(R.string.Notification_subtext),
                true,
                notificationId);

    }


    /**
     * Funkcja służaca do komunikacji pomiędzy Fragmentami
     * @param id
     */
    @Override
    public void onFragmentInteraction(long [] id) {
        Snackbar.make(this.findViewById(R.id.frame_content),"sdasd",Snackbar.LENGTH_SHORT).show();
    }

    /**
     *  Defines callbacks for service binding, passed to bindService()
     */
    protected ServiceConnection mConnection = new ServiceConnection() {
        public NotificationCatcherService NotificationService;
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NotificationCatcherService.NotificationCatcherBinder binder = (NotificationCatcherService.NotificationCatcherBinder) service;
            NotificationService = binder.getService();
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };
}
