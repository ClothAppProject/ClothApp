package com.clothapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import java.util.Arrays;
import java.util.List;

import static com.clothapp.resources.FacebookUtil.*;
import static com.clothapp.resources.ExceptionCheck.check;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Nascondo la tastiera all'avvio di quest'activity
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // final SharedPreferences userInformation = getSharedPreferences(getString(R.string.info), MODE_PRIVATE);

        // Facebook button initialization
        Button facebook_login = (Button) findViewById(R.id.login_button_facebook);

        // Add a listener to the Facebook button
        facebook_login.setOnClickListener(new View.OnClickListener() { //metto bottone login in ascolto del click
            @Override
            public void onClick(View v) {

                final View vi = v;
                final SharedPreferences userInformation = getSharedPreferences(getString(R.string.info), MODE_PRIVATE);

                // Inizializzo barra di caricamento
                dialog = ProgressDialog.show(MainActivity.this, "",
                        "Logging with Facebook. Please wait...", true);

                // Create a thread to manage Facebook Login in background.
                Thread facebook = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // Specifico i campi ai quali sono interessato quando richiedo permesso ad utente
                        List<String> permissions = Arrays.asList("email", "public_profile", "user_birthday");

                        // Eseguo la chiamata per il login via facebook con parse
                        ParseFacebookUtils.logInWithReadPermissionsInBackground(MainActivity.this, permissions, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException err) {
                                if (err != null) {
                                    // Chiudo barra di caricamento
                                    dialog.dismiss();

                                    // Controllo che non ci siano eccezioni parse
                                    check(err.getCode(), vi, err.getMessage());
                                } else if (user == null) {
                                    // Login via facebook cancellato dall'utente
                                    Log.d("MainActivity", "Login attraverso Facebook cancellato dall'utente.");

                                    // Chiudo barra di caricamento
                                    dialog.dismiss();
                                } else if (user.isNew()) {
                                    // L'utente non è registrato con facebook, eseguo registrazione con facebook
                                    Log.d("MainActivity", "L'utente non registrato con Facebook, eseguo registrazione con Facebook");

                                    try {
                                        //chiamo per inserire le informazioni di facebook nel database parse (l'utente è già stato creato)
                                        getUserDetailsRegisterFB(user, vi, userInformation);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    // Redirect user to Splash Screen Activity
                                    Intent form_intent = new Intent(MainActivity.this, SplashScreen.class);
                                    startActivity(form_intent);

                                    // Chiudo barra di caricamento
                                    dialog.dismiss();

                                    finish();
                                } else {
                                    // Login eseguito correttamente attraverso facebook
                                    Log.d("MainActivity", "Login eseguito correttamente attraverso Facebook");

                                    getUserDetailLoginFB(user, vi, userInformation);

                                    // Redirect user to Splash Screen Activity
                                    Intent form_intent = new Intent(getApplicationContext(), SplashScreen.class);
                                    startActivity(form_intent);

                                    // Chiudo barra di caricamento
                                    dialog.dismiss();

                                    finish();
                                }
                            }
                        });
                    }
                });

                // Start Facebook Login thread
                facebook.start();
            }
        });

        // Normal login button initialization
        Button login = (Button) findViewById(R.id.login_button);

        // Add a listener to the normal login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View vi = v;

                // Prendo tutti valori, li metto nel bundle e li attacco al form intent per mandarla alla prossima activity
                final EditText edit_username = (EditText) findViewById(R.id.edit_username);
                final EditText edit_password = (EditText) findViewById(R.id.edit_password);

                if (checknull(edit_password.getText().toString().trim(), edit_username.getText().toString().trim())) {
                    Snackbar.make(v, "I campi non devono essere vuoti", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    // Inizializzo barra di caricamento
                    dialog = ProgressDialog.show(MainActivity.this, "",
                            "Logging in. Please wait...", true);

                    // Create a thread to manage the login in background
                    Thread login = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ParseUser.logIn(edit_username.getText().toString().trim(), edit_password.getText().toString().trim());
                                System.out.println("debug: Login eseguito correttamente");

//                                // Inserisco i valori nelle sharedPref
//                                ParseUser uth = ParseUser.getCurrentUser();
//                                userInformation.edit().putBoolean("isLogged", true).commit();
//                                userInformation.edit().putString("username", uth.get("username").toString().trim()).commit();
//                                // userInformation.edit().putString("password", cryptoPswd(uth.get("password").toString())).commit();
//                                userInformation.edit().putString("name", uth.get("name").toString().trim()).commit();
//                                userInformation.edit().putString("lastname", uth.get("lastname").toString().trim()).commit();
//                                userInformation.edit().putString("date", uth.get("date").toString().trim()).commit();
//                                userInformation.edit().putString("email", uth.get("email").toString()).commit();

                                // Redirect user to Splash Screen Activity
                                Intent form_intent = new Intent(getApplicationContext(), SplashScreen.class);
                                startActivity(form_intent);

                                // Chiudo la progressdialogbar
                                dialog.dismiss();

                                finish();

                            } catch (ParseException e) {
                                // Chiudo la progressdialogbar
                                dialog.dismiss();

                                if (e.getCode() == 101) {
                                    // Siccome il codice 101 è per 2 tipi di errori faccio prima il controllo qua e in caso chiamo gli altri
                                    Log.d("MainActivity", "Errore: " + e.getMessage());

                                    Snackbar.make(vi, "Username o Password errati...", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                } else {
                                    check(e.getCode(), vi, e.getMessage());
                                }
                            }
                        }
                    });

                    // Start normal login thread
                    login.start();
                }
            }
        });

        // Signup button initialization
        Button register = (Button) findViewById(R.id.register_button);

        // Add an OnClick listener to the signup button
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();

                // Redirect user to signup Activity.
                Intent form_intent = new Intent(getApplicationContext(), SignupActivity.class);
                form_intent.putExtras(bundle);
                startActivity(form_intent);
            }
        });
    }

    // Dopo login su facebook ritorna qui
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    // Funzione per controllare che ne username ne password siano vuote
    public boolean checknull(String p, String u) {
        return (p.equals("") || u.equals(""));
    }

}
