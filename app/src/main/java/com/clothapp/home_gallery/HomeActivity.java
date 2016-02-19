package com.clothapp.home_gallery;

//import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.clothapp.BaseActivity;
import com.clothapp.login_signup.MainActivity;
import com.clothapp.profile.ProfileActivity;
import com.clothapp.R;
import com.clothapp.profile.UserProfileActivity;
import com.clothapp.resources.CircleTransform;
import com.clothapp.resources.Image;
import com.clothapp.search.SearchAdapter;
import com.clothapp.settings.SettingsActivity;
import com.clothapp.upload.UploadCameraActivity;
import com.clothapp.upload.UploadGalleryActivity;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.util.List;

import static com.clothapp.resources.ExceptionCheck.check;

/**
 * Created by giacomoceribelli on 02/02/16.
 */
// public class HomeActivity extends BaseActivity {
public class HomeActivity extends AppCompatActivity {

    static FloatingActionsMenu menuMultipleActions;

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        setSupportActionBar(null);
//
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Ciao");

        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp_white);
        ab.setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setElevation(4);
//
///*
//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        ImageView imageView=new ImageView(getBaseContext());
//        imageView.setImageResource(R.mipmap.camera_icon);
//        //System.out.println(actionBar+"   "+imageView);
//        actionBar.setCustomView(imageView);
//*/
//
//        // setUpMenu();
//
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_home);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        String[] titles = getResources().getStringArray(R.array.home_titles);


        ViewPager viewPager = (ViewPager) findViewById(R.id.home_viewpager);

        //set adapter to  ViewPager
        viewPager.setAdapter(new HomeAdapter(getSupportFragmentManager(), titles));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        // UploadCameraActivity a new photo button menu initialization
        setupFloatingButton();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.home_appbar, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                // Log.d("UserProfileActivity", "android.R.id.home");
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.action_settings:
                // Log.d("UserProfileActivity", "R.id.action_settings");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {

        // Get default bitmap for user profile photo
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.com_facebook_profile_picture_blank_square);

        // Create a rounded bitmap from the user profile photo
        RoundedBitmapDrawable rounded = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        rounded.setCornerRadius(bitmap.getWidth());

        // Get drawer header
        View headerLayout = navigationView.getHeaderView(0);

        // Get the image view containing the user profile photo
        ImageView drawerProfile = (ImageView) headerLayout.findViewById(R.id.menu_profile_side_drawer_image);

        // Set the user profile photo to the just created rounded image
        drawerProfile.setImageDrawable(rounded);

        final String username = ParseUser.getCurrentUser().getUsername();

        // Set the drawer username
        TextView drawerUsername = (TextView) headerLayout.findViewById(R.id.menu_profile_side_drawer_username);
        drawerUsername.setText(username);

        // Set up onClickListener for each drawer item
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        Intent intent;

                        switch (menuItem.getItemId()) {

                            case R.id.nav_home:
                                Log.d("HomeActivity", "Clicked on R.id.nav_home");

                                intent = new Intent(HomeActivity.this, HomeActivity.class);
                                startActivity(intent);

                                finish();
                                break;

                            case R.id.nav_profile:
                                Log.d("HomeActivity", "Clicked on R.id.nav_profile");

                                intent = new Intent(HomeActivity.this, UserProfileActivity.class);
                                intent.putExtra("user", username);
                                startActivity(intent);

                                break;

                            case R.id.nav_logout:
                                Log.d("HomeActivity", "Clicked on R.id.nav_logout");

                                final ProgressDialog dialog = ProgressDialog.show(HomeActivity.this, "", "Logging out. Please wait...", true);
                                Thread logout = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ParseUser.logOut();
                                        Log.d("HomeActivity", "Logging out...");
                                    }
                                });
                                logout.start();

                                intent = new Intent(UserProfileActivity.activity, MainActivity.class);
                                dialog.dismiss();
                                startActivity(intent);

                                finish();
                                break;
                        }

                        // menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void setupFloatingButton(){
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.upload_action);

        com.getbase.floatingactionbutton.FloatingActionButton camera = new com.getbase.floatingactionbutton.FloatingActionButton(getBaseContext());
        camera.setTitle("Camera");
        camera.setIcon(R.mipmap.camera_icon);
        camera.setColorNormal(Color.rgb(210,36,37));
        camera.setColorPressed(Color.RED);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect the user to the upload activity and upload a photo
                Intent i = new Intent(getApplicationContext(), UploadCameraActivity.class);
                startActivity(i);
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton gallery = new com.getbase.floatingactionbutton.FloatingActionButton(getBaseContext());
        gallery.setTitle("Gallery");
        gallery.setIcon(R.mipmap.gallery_icon);
        gallery.setColorNormal(Color.rgb(210,36,37));
        gallery.setColorPressed(Color.RED);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect the user to the upload activity and upload a photo
                Intent i = new Intent(getApplicationContext(), UploadGalleryActivity.class);
                startActivity(i);
            }
        });
        menuMultipleActions.addButton(camera);
        menuMultipleActions.addButton(gallery);
    }

    @Override
    public void onBackPressed() {
        //  closing the floating action button if it is open
        if(menuMultipleActions.isExpanded()) {
            menuMultipleActions.collapse();
            return;
        }

        finish();
    }
}