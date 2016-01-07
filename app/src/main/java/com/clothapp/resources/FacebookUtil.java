package com.clothapp.resources;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.clothapp.resources.ExceptionCheck.check;

/**
 * Created by giacomoceribelli on 06/01/16.
 */
/*infromazioni qui: http://blog.grafixartist.com/facebook-login-with-parse-part-2/*/
public class FacebookUtil {
    private static String name;
    private static String email;
    private static String lastname;
    private static Date birthday;

    //funzione per prelevare le informazioni da facebook e inserirle in parse e nello SharedPref
    public static void getUserDetailsRegisterFB(ParseUser uth, View v, SharedPreferences userInfo) throws InterruptedException {
        final View vi = v;
        final ParseUser user = uth;
        final SharedPreferences userInformation = userInfo;
        // Prelevo informazioni da facebook
        Bundle parameters = new Bundle();
        //specifico i parametri che voglio ottenere da facebook
        parameters.putString("fields", "email,first_name,last_name,birthday");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(), "/me", parameters, HttpMethod.GET, new GraphRequest.Callback() {
            public void onCompleted(GraphResponse response) {
                /* Prelevo risultato */
                try {
                    email = response.getJSONObject().getString("email");
                    lastname = response.getJSONObject().getString("last_name");
                    name = response.getJSONObject().getString("first_name");

                    String dateStr = (String) response.getJSONObject().get("birthday");
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    birthday = sdf.parse(dateStr);


                    System.out.println("debug: informazioni prelevate da facebook");

                    //inserisco le info nel ParseUser
                    user.setPassword("password");
                    user.setEmail(email.toString());
                    user.put("name", name.toString().trim());
                    user.put("lastname", lastname.toString().trim());
                    user.put("date", birthday);
                    user.saveInBackground(new SaveCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                //caso in cui registrazione è andata a buon fine e non ci sono eccezioni
                                System.out.println("debug: informazioni inserite in parse");
                                userInformation.edit().putBoolean("isLogged", true).commit();
                                userInformation.edit().putString("username", user.getUsername().toString().trim()).commit();
                                userInformation.edit().putString("password", "password").commit();
                                userInformation.edit().putString("name", name.toString().trim()).commit();
                                userInformation.edit().putString("lastname", lastname.toString().trim()).commit();
                                userInformation.edit().putString("email", email.toString()).commit();
                                userInformation.edit().putString("date", birthday.toString()).commit();
                            } else {
                                //chiama ad altra classe per verificare qualsiasi tipo di errore dal server
                                check(e.getCode(), vi, e.getMessage());
                            }
                        }
                    });
                } catch (JSONException e) {
                    System.out.println("debug: eccezione nell'ottenere info da facebook");
                } catch (java.text.ParseException e) {
                    System.out.println("debug: eccezione nel formattare la data");
                }
            }
        }
        ).executeAsync();
    }

    //funzione per salvare nelle sharedPref i dati in caso di Login eseguito via Facebook
    public static void getUserDetailLoginFB(ParseUser user,View v, SharedPreferences userInformation)   {
        userInformation.edit().putBoolean("isLogged", true).commit();
        userInformation.edit().putString("username", user.get("username").toString().trim()).commit();
        userInformation.edit().putString("password", "password").commit();
        userInformation.edit().putString("name", user.get("name").toString().trim()).commit();
        userInformation.edit().putString("lastname", user.get("lastname").toString().trim()).commit();
        userInformation.edit().putString("date", user.get("date").toString().trim()).commit();
        userInformation.edit().putString("email", user.get("email").toString()).commit();

    }
}