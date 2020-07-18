package com.example.godutch;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.godutch.ui.register.RegisterDialog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FacebookActivity extends AppCompatActivity implements RegisterDialog.RegisterDialogListener {
    private CallbackManager callbackManager;
    private OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private JSONObject userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null && !accessToken.isExpired())
            launchMainActivity(accessToken.getUserId());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton)findViewById(R.id.login_button);
        loginButton.setPermissions("email");
        loginButton.setLoginBehavior(LoginBehavior.WEB_VIEW_ONLY);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String postBody = "{\n" +
                        "\"access_token\": \"" + loginResult.getAccessToken().getToken() + "\"\n" +
                        "}";
                RequestBody body = RequestBody.create(postBody, JSON);
                Request request = new Request.Builder()
                        .url(String.format("%s/auth/login", Constants.SERVER_IP))
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        call.cancel();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        final String jsonString = response.body().string();
                        userData = null;
                        boolean isMember = false;
                        String id = null;
                        try {
                            userData = new JSONObject(jsonString);
                            isMember = userData.getBoolean("member");
                            id = userData.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (isMember)
                            launchMainActivity(id);
                        else
                            launchRegisterDialog();
                    }
                });
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
                finish();
            }
        });
    }

    public void launchRegisterDialog() {
        RegisterDialog registerDialog = new RegisterDialog();
        registerDialog.show(getSupportFragmentManager(), "Register");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void register(String accountNumber, String phoneNumber) throws JSONException {
        Iterator<String> keys = userData.keys();
        JSONObject postBody = new JSONObject();
        postBody.put("account_number", accountNumber);
        postBody.put("phone_number", phoneNumber);
        while (keys.hasNext()) {
            String key = keys.next();
            // insert keys except "member"
            if (userData.get(key) instanceof String)
                postBody.put(key, (String) userData.get(key));
        }

        RequestBody body = RequestBody.create(postBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(String.format("%s/auth/register", Constants.SERVER_IP))
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String jsonString = response.body().string();
                JSONObject data = null;
                String id = null;
                try {
                    data = new JSONObject(jsonString);
                    id = data.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                launchMainActivity(id);
            }
        });
    }

    public void launchMainActivity(String id) {
        Intent intent = new Intent(FacebookActivity.this, MainActivity.class);
        intent.putExtra("USER_ID", id);
        startActivity(intent);
        finish();
    }
}