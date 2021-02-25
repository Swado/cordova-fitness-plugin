package com.dietplanner;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import android.content.Intent;
import android.util.Log;
import android.Manifest;
import androidx.annotation.NonNull;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_CUMULATIVE;
import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA;

public class FitnessPlugin extends CordovaPlugin {

    GoogleSignInClient mGoogleSignInClient;
    CallbackContext mCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("show".equals(action)) { 
            mCallbackContext = callbackContext;
            show(args.getString(0), callbackContext);
            return true;
        }

        return false;
    }

    private void show(String msg, CallbackContext callbackContext) {
        
            //Toast.makeText(webView.getContext(), "hola", Toast.LENGTH_SHORT).show();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

            mGoogleSignInClient = GoogleSignIn.getClient(cordova.getContext(), gso);

            //String PERMISSIONS  = "Manifest.permission.ACTIVITY_RECOGNITION";

            CordovaPlugin plugin = (CordovaPlugin) this;
            if(!cordova.hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)){
                Log.i("LogPermission","no permission");
                cordova.requestPermission(plugin,2,Manifest.permission.ACTIVITY_RECOGNITION);
            }
            else{
                Log.i("LogPermission","has permission");
                signIn();
            }
        
        
    }

    private void signIn() {
        Log.i("LogSignIn","entered signIn");
        //Toast.makeText(webView.getContext(), "heree", Toast.LENGTH_LONG).show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        CordovaPlugin plugin = (CordovaPlugin) this;
        cordova.startActivityForResult(plugin,signInIntent,1);

    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        super.onRequestPermissionResult(requestCode, permissions, grantResults);

        if(requestCode==2) {
            if (grantResults.length > 0){
                
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("PermissionResult","PERMISSION_GRANTED");
                    signIn();
                }
                else{
                    Log.i("PermissionResult","PERMISSION_NOT_GRANTED");
                    CordovaPlugin plugin = (CordovaPlugin) this;
                    cordova.requestPermission(plugin,2,Manifest.permission.ACTIVITY_RECOGNITION);
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Toast.makeText(cordova.getContext(),
        //         "Rec result", Toast.LENGTH_SHORT).show();
        Log.i("LogOnActivityResult","Activity result received "+requestCode);
        if (requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
            handleSignInResult(task);
        }

    }

    private void  subToStepCounter(final GoogleSignInAccount account) {


        Fitness.getRecordingClient(cordova.getContext(),account)
                .subscribe(TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("LogFitnessSub", "Successfully subscribed!");
                         mCallbackContext.success("Successfully subscribed!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Fitness notSub", "There was a problem subscribing.", e);
                mCallbackContext.error("Failed in subscribing to api");
            }
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Log.i("SignIn Success", "handleSignInResult:YA DONE ");
            GoogleSignInOptionsExtension fitnessOptions =
                    FitnessOptions.builder()
                            .addDataType(TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                            .build();

            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                GoogleSignIn.requestPermissions(cordova.getActivity(), 1, account, fitnessOptions);
            } else {
                subToStepCounter(account);
            }



        } catch (ApiException e) {
            Log.w("SignIn", "signInResult:failed code=" + e);
            mCallbackContext.error("Failed in subscribing to api");
        }
    }


}
