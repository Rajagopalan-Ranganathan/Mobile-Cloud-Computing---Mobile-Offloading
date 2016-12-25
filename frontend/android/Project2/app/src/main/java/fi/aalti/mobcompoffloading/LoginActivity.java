package fi.aalti.mobcompoffloading;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Arrays;

import fi.aalti.mobilecompoffloading.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A login screen that offers login.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CallbackManager callbackManager;
    private String fb_name, fb_email, fb_token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        setTitle("Login");

        if (AccessToken.getCurrentAccessToken() != null) {

            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            //i.putExtra("token", this.token);
            finish();
            startActivity(i);
        }

        //super.onCreate();
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email"));//"user_birthday", "user_friends"));
        //"public_profile", "email", "user_birthday", "user_friends"));
        // loginButton.setReadPermissions("email");
        //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));

        callbackManager = CallbackManager.Factory.create();


        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        mAuthTask = null;
                        showProgress(false);
                        Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        fb_token = loginResult.getAccessToken().getToken().toString();
                        Log.d("Token Expiry", loginResult.getAccessToken().getExpires().toString());


                        // App code
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());
                                        try {
                                            // Application code
                                            fb_name = object.getString("name");
                                            fb_email = object.getString("email");
                                            //fb_token = loginResult.getAccessToken().getUserId().toString();
                                            Log.d("FB User name:", fb_name);
                                            Log.d("FB Email", fb_email);
                                            Log.d("FB Token", fb_token);
                                            authfb();

                                            try {
                                                fb_login fb_ul = new fb_login(fb_name, fb_email, fb_token);

                                                backendApplicationService appService = new backendApplicationClient(getApplicationContext(), "test").getBackendApplicationService();

                                                Call<fb_login> fb_auth = appService.fb_auth(fb_ul);

                                                fb_auth.clone().enqueue(new Callback<fb_login>() {

                                                    @Override
                                                    public void onResponse(Call<fb_login> call, Response<fb_login> response) {
                                                        try {

                                                            if (response.body() != null) {
                                                                Log.d("Response", response.body().toString());
                                                                String token = response.body().token;
                                                                Log.d("token", token);
                                                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                                i.putExtra("token", token);
                                                                //Save token in SharedPreferences
                                                                SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
                                                                SharedPreferences.Editor editor = sharedPref.edit();
                                                                editor.putString("token", token);
                                                                editor.apply();
                                                                finish();
                                                                startActivity(i);

                                                            }

                                                        } catch (NullPointerException ex) {
                                                            Log.e("BackendConnection", "Response from backend is null");
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Call<fb_login> call, Throwable t) {
                                                        // Log error here since request failed
                                                        Log.e("Error", t.toString());
                                                    }
                                                });


                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                // return false;
                                            }


                                        } catch (Exception exp) {
                                        }


                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    public void requestUserProfile(LoginResult loginResult) {
                        GraphRequest.newMeRequest(
                                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject me, GraphResponse response) {
                                        if (response.getError() != null) {
                                            // handle error
                                        } else {
                                            try {
                                                String email = response.getJSONObject().get("email").toString();

                                                Log.e("Result", email);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            String id = me.optString("id");
                                            // send email and id to your web server
                                            Log.e("Result1", response.getRawResponse());
                                            Log.e("Result", me.toString());
                                        }
                                    }
                                }).executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }


                });


        //Uncomment the below line for faster unit testing
        mUsernameView.setText("test@test.com");

        mPasswordView = (EditText) findViewById(R.id.password);
        //Uncomment the below line for faster unit testing
        mPasswordView.setText("test123");
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {

                    Log.i("logging", "Password action listener");
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("logging", "clicked signInButton");
                attemptLogin();
            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }


    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(email)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isUsernameValid(String username) {
        return username.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private String token;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                UserLogin ul = new UserLogin(mEmail, mPassword);
                backendApplicationService appService = new backendApplicationClient(getApplicationContext(), "test").getBackendApplicationService();
                Call<UserLogin> authService = appService.auth(ul);
                Response<UserLogin> respService = authService.execute();

                if (respService.code() != HttpURLConnection.HTTP_OK) {
                    Log.d("Connection", "Error during login, response code: " + respService.code());
                    return false;
                }
                if (respService.body() != null) {
                    Log.d("Response", respService.body().toString());
                    token = respService.body().token;
                    Log.d("token", token);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }


                return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if (success) {
                Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_LONG).show();
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("token", this.token);
                finish();
                startActivity(i);
            } else {
                Toast.makeText(LoginActivity.this, "Login Failure", Toast.LENGTH_LONG).show();
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ActivityTutorial", "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("ActivityTutorial", "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ActivityTutorial", "onResume");
    }


    public void authfb() {
        if (AccessToken.getCurrentAccessToken() != null) {


            try {
                fb_login fb_ul = new fb_login(fb_name, fb_email, fb_token);

                backendApplicationService appService = new backendApplicationClient(getApplicationContext(), "test").getBackendApplicationService();


                Call<fb_login> fb_auth = appService.fb_auth(fb_ul);

                fb_auth.clone().enqueue(new Callback<fb_login>() {

                    @Override
                    public void onResponse(Call<fb_login> call, Response<fb_login> response) {
                        try {

                            if (response.body() != null) {
                                Log.d("Response", response.body().toString());
                                String token = response.body().token;
                                Log.d("token", token);
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                i.putExtra("token", token);
                                //Save token in SharedPreferences
                                SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("token", token);
                                editor.apply();
                                finish();
                                startActivity(i);

                            }

                        } catch (NullPointerException ex) {
                            Log.e("BackendConnection", "Response from backend is null");
                        }
                    }

                    @Override
                    public void onFailure(Call<fb_login> call, Throwable t) {
                        // Log error here since request failed
                        Log.e("Error", t.toString());
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

}