package com.clothapp.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clothapp.R;
import com.clothapp.home_gallery.HomeActivity;
import com.clothapp.login_signup.MainActivity;
import com.clothapp.profile.adapters.SectionsPagerAdapter;
import com.clothapp.profile.utils.ProfilePictureCameraActivity;
import com.clothapp.profile.utils.ProfilePictureGalleryActivity;
import com.clothapp.profile.utils.ProfileUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import static com.clothapp.resources.ExceptionCheck.check;

public class UserProfileActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    public static Context context;

    public static String username;

    public static RecyclerView viewProfileInfo;
    public static RecyclerView viewProfileUploadedPhotos;
    public static ViewPager viewPager;
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        // Get username from the calling activity.
        username = getIntent().getExtras().getString("user");

        // Set context to current context.
        context = UserProfileActivity.this;

        // Set activity to current activity.
        activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Set toolbar title to empty string so that it won't overlap with the tabs.
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // Set up drawer button
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_24dp_white);
        ab.setDisplayHomeAsUpEnabled(true);

        // Get the drawer view
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        viewPager = (ViewPager) findViewById(R.id.profile_viewpager);
        if (viewPager != null) {
            setupViewPagerContent(viewPager);
        }

        loadProfilePicture(navigationView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_user, menu);
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

    @Override
    public void onBackPressed() {
        finish();
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
                            Log.d("UserProfileActivity", "Clicked on R.id.nav_home");

                            intent = new Intent(UserProfileActivity.activity, HomeActivity.class);
                            startActivity(intent);

                            finish();
                            break;

                        case R.id.nav_profile:
                            Log.d("UserProfileActivity", "Clicked on R.id.nav_profile");

                            String currentUser = ParseUser.getCurrentUser().getUsername();

                            if (!currentUser.equals(username)) {
                                Log.d("UserProfileActivity", currentUser + "!=" + username);
                                intent = new Intent(UserProfileActivity.activity, UserProfileActivity.class);
                                intent.putExtra("user", currentUser);
                                startActivity(intent);
                            }

                            break;

                        case R.id.nav_logout:
                            Log.d("UserProfileActivity", "Clicked on R.id.nav_logout");

                            final ProgressDialog dialog = ProgressDialog.show(UserProfileActivity.this, "", "Logging out. Please wait...", true);
                            Thread logout = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    ParseUser.logOut();
                                    System.out.println("debug: logout eseguito");
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

    private void setupViewPagerContent(ViewPager viewPager) {

        // Create new adapter for ViewPager
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set ViewPager adapter
        viewPager.setAdapter(sectionsPagerAdapter);

        viewPager.setCurrentItem(1, false);

        // Set up TabLayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void loadProfilePicture(NavigationView navigationView) {

        View headerLayout = navigationView.getHeaderView(0);
        ImageView drawerImageView = (ImageView) headerLayout.findViewById(R.id.menu_profile_side_drawer_image);

        ImageView mainImageView = (ImageView) findViewById(R.id.profile_user_image);

        ProfileUtils.getParseUserProfileImage(this, username, mainImageView, drawerImageView);
        mainImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (username.equals(ParseUser.getCurrentUser().getUsername().toString())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                    builder.setTitle(R.string.choose_profile_picture)
                            //.set
                            .setItems(R.array.profile_picture_options, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            // Redirect the user to the ProfilePictureCameraActivity Activity
                                            Intent intentCamera = new Intent(activity.getApplicationContext(), ProfilePictureCameraActivity.class);
                                            activity.startActivity(intentCamera);
                                            break;
                                        case 1:
                                            // Redirect the user to the ProfilePictureGalleryActivity Activity
                                            Intent intentGallery = new Intent(activity.getApplicationContext(), ProfilePictureGalleryActivity.class);
                                            activity.startActivity(intentGallery);
                                            break;
                                        case 2:
                                            //delete profile picture
                                            ParseQuery<ParseObject> queryFotoProfilo = new ParseQuery<ParseObject>("UserPhoto");
                                            queryFotoProfilo.whereEqualTo("username", username);
                                            queryFotoProfilo.findInBackground(new FindCallback<ParseObject>() {
                                                @Override
                                                public void done(List<ParseObject> objects, ParseException e) {
                                                    if (e == null) {
                                                        if (objects.size() > 0) {
                                                            objects.get(0).deleteInBackground();
                                                            activity.finish();
                                                            activity.startActivity(activity.getIntent());
                                                        }
                                                    } else {
                                                        check(e.getCode(), v, e.getMessage());
                                                    }
                                                }
                                            });
                                            break;
                                    }
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }
}
