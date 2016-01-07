package com.clothapp.resources;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

/**
 * Created by giacomoceribelli on 02/01/16.
 */
public class ApplicationSupport extends Application {
//Questa classe viene chiamata ogni ciclo di vita intero dell'applicazione, infatti parse va invocato solamente una volta durante tutto
//il ciclo di vita dell'app mentre se si lasciava nella launcher se la launcher era richiamata andava in errore perchè si cercava
//di inizializzare parse un'altra volta.
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());

        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
        ParseFacebookUtils.initialize(this);
    }
}