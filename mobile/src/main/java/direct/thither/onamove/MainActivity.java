package direct.thither.onamove;

import direct.thither.onamove.pages.ProductsResult;
import direct.thither.onamove.pages.SearchMenu;

import direct.thither.onamove.pages.SearchSettings;
import direct.thither.onamove.pages.StoreInfo;
import direct.thither.onamove.receivers.*;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.TelephonyManager;


public class MainActivity extends AppCompatActivity {
    private Globals mGlobals;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mMainTabs;

    private BroadcastReceiver networkStateReceiver=new networkStateReceiver();
    private BroadcastReceiver locationReceiver=new LocationReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGlobals = ((App)getApplication()).globals;
        mGlobals.load_preferences(
                getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE));

        setContentView(R.layout.activity_main);

        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if(tm!=null)mGlobals.set_param("isoLoc", tm.getNetworkCountryIso());

        locationReceiver.onReceive(this, getIntent());
        networkStateReceiver.onReceive(this, getIntent());

        run();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(locationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }
    @Override
    public void onPause() {
        unregisterReceiver(networkStateReceiver);
        unregisterReceiver(locationReceiver);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                run();
                break;
            case Activity.RESULT_CANCELED:
                break;
        }
    }

    private void run(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        mGlobals.set_status_bar(findViewById(R.id.bottom_status));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // http://www.devexchanges.info/2016/05/android-basic-training-course-combining.html
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ///SettingsMenu settingsMenu = new SettingsMenu(getWindow().getDecorView());
        new SearchSettings(this);
        new StoreInfo(this);

        mMainTabs = findViewById(R.id.tab_layout);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) {}
            @Override
            public void onPageSelected(int pos) { mSectionsPagerAdapter.selected(pos);}
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mMainTabs));
        mMainTabs.setTabGravity(TabLayout.GRAVITY_FILL);
        mMainTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private SectionsPagerAdapter(FragmentManager fm) { super(fm); }
        private int last_page;
        private int num_pages=2;

        @Override
        public Fragment getItem(int pos) {
            Globals.Render f = mGlobals.renders.get(Integer.toString(pos));
            if(f!=null) return f.getFragment();

            Fragment fnew;
            TabLayout.Tab tab = mMainTabs.newTab();
            switch (pos){

                case 1:
                    fnew = new ProductsResult();
                    //tab.setText("Results");
                    tab.setIcon(getResources().getDrawable(((ProductsResult)fnew).get_icon()));
                    //tab.setContentDescription(((ProductsResult)fnew).get_desc());
                    break;
                default:
                    fnew = new SearchMenu();
                    //tab.setText(((SearchMenu)fnew).get_title());
                    tab.setIcon(getResources().getDrawable(((SearchMenu)fnew).get_icon()));
                    //tab.setContentDescription(((SearchMenu)fnew).get_desc());
                    break;
            }
            mMainTabs.addTab(tab);
            return fnew;
        }
        private void selected(int pos) {
            if(pos == last_page)return;
            last_page = pos;
            mGlobals.renders.get(Integer.toString(pos)).set_active();
        }
        @Override
        public int getCount() { return num_pages; }
    }

}
