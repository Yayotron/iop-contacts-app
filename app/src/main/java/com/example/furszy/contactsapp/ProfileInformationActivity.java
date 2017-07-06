package com.example.furszy.contactsapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.fermat.redtooth.crypto.CryptoBytes;
import org.fermat.redtooth.governance.utils.TextUtils;
import org.fermat.redtooth.profile_server.CantConnectException;
import org.fermat.redtooth.profile_server.CantSendMessageException;
import org.fermat.redtooth.profile_server.ModuleRedtooth;
import org.fermat.redtooth.profile_server.ProfileInformation;
import org.fermat.redtooth.profile_server.engine.futures.BaseMsgFuture;
import org.fermat.redtooth.profile_server.engine.futures.MsgListenerFuture;
import org.fermat.redtooth.profile_server.imp.ProfileInformationImp;
import org.fermat.redtooth.profile_server.utils.ProfileUtils;
import org.fermat.redtooth.profiles_manager.PairingRequest;

import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.fermat.redtooth.profile_server.imp.ProfileInformationImp.PairStatus.NOT_PAIRED;

/**
 * Created by furszy on 5/27/17.
 */
public class ProfileInformationActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ProfInfoActivity";

    public static final String INTENT_EXTRA_PROF_KEY = "prof_key";
    public static final String INTENT_EXTRA_PROF_NAME = "prof_name";
    public static final String INTENT_EXTRA_PROF_SERVER_ID = "prof_name";
    public static final String INTENT_EXTRA_SEARCH = "prof_search";


    ModuleRedtooth module;
    ProfileInformation profileInformation;

    private CircleImageView imgProfile;
    private TextView txt_name;
    private Button btn_connect;
    private ProgressBar progress_bar;

    private TextView txt_chat;

    private ExecutorService executor;

    private boolean searchForProfile = false;
    private byte[] keyToSearch;
    private String nameToSearch;

    private AtomicBoolean lock = new AtomicBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        module = ((App)getApplication()).getAnRedtooth().getRedtooth();

//        Uri data = getIntent().getData();
//        String scheme = data.getScheme(); // "http"
//        String host = data.getHost(); // "twitter.com"
//        List<String> params = data.getPathSegments();
//        String first = params.get(0); // "status"
//        String second = params.get(1); // "1234"
//
//        Log.i("APP",data.toString());

        Bundle extras = getIntent().getExtras();
        if (extras!=null && extras.containsKey(INTENT_EXTRA_PROF_KEY)){
            byte[] pubKey = extras.getByteArray(INTENT_EXTRA_PROF_KEY);
            if (!extras.containsKey(INTENT_EXTRA_SEARCH)){
                profileInformation = module.getKnownProfile(CryptoBytes.toHexString(pubKey));
            }else{
                keyToSearch = pubKey;
                nameToSearch = extras.getString(INTENT_EXTRA_PROF_NAME);
                searchForProfile = true;
            }

        }

        setContentView(R.layout.profile_information_main);
        imgProfile = (CircleImageView) findViewById(R.id.profile_image);
        txt_name = (TextView) findViewById(R.id.txt_name);
        btn_connect = (Button) findViewById(R.id.btn_connect);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        txt_chat = (TextView) findViewById(R.id.txt_chat);
        txt_chat.setOnClickListener(this);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (profileInformation.getPairStatus()==NOT_PAIRED) {
                                MsgListenerFuture listener = new MsgListenerFuture();
                                listener.setListener(new BaseMsgFuture.Listener<Integer>() {
                                    @Override
                                    public void onAction(int messageId, Integer object) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ProfileInformationActivity.this, "Pairing sent!", Toast.LENGTH_LONG).show();
                                                hideLoading();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFail(int messageId, int status, final String statusDetail) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ProfileInformationActivity.this, "Pairing fail, detail: " + statusDetail, Toast.LENGTH_LONG).show();
                                                hideLoading();
                                            }
                                        });
                                    }
                                });
                                module.requestPairingProfile(profileInformation.getPublicKey(), profileInformation.getProfileServerId(), listener);
                            } else if (profileInformation.getPairStatus() == ProfileInformationImp.PairStatus.WAITING_FOR_MY_RESPONSE){
                                // if is not paired and the search is true i can accept the pairing invitation
                                if (searchForProfile) {
                                    /*module.acceptPairingProfile(profileInformation.getProfileServerId(), profileInformation.getPublicKey());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ProfileInformationActivity.this, "Sending acceptance", Toast.LENGTH_LONG).show();
                                        }
                                    });*/
                                }else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideLoading();
                                        }
                                    });
                                }
                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ProfileInformationActivity.this,"Status not implemented yet.. "+profileInformation.getPairStatus().name(),Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        if (profileInformation==null && !searchForProfile){
            onBackPressed();
            return;
        }
        if (profileInformation!=null) {
            txt_name.setText(profileInformation.getName());
            if (profileInformation.getImg() != null && profileInformation.getImg().length > 1) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(profileInformation.getImg(), 0, profileInformation.getImg().length);
                imgProfile.setImageBitmap(bitmap);
            }
        }

        if (searchForProfile){
            showLoading();
        }else
            hideLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (executor==null){
            executor = Executors.newSingleThreadExecutor();
        }
        if (searchForProfile){
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        MsgListenerFuture<ProfileInformation> msgListenerFuture = new MsgListenerFuture();
                        msgListenerFuture.setListener(new BaseMsgFuture.Listener<ProfileInformation>() {
                            @Override
                            public void onAction(int messageId, final ProfileInformation object) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        profileInformation = object;
                                        // now check if i have a request for this profile
                                        PairingRequest pairingRequest = anRedtooth.getProfilePairingRequest(profileInformation.getHexPublicKey());
                                        if (pairingRequest!=null){
                                            ProfileInformationImp.PairStatus pairStatus = ProfileUtils.PairingRequestToPairStatus(anRedtooth.getProfile(),pairingRequest);
                                            profileInformation.setPairStatus(pairStatus);
                                        }
                                        txt_name.setText(profileInformation.getName());
                                        hideLoading();
                                    }
                                });
                            }

                            @Override
                            public void onFail(int messageId, int status, String statusDetail) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ProfileInformationActivity.this,"Search profile on network fail\nTry again later",Toast.LENGTH_LONG).show();
                                        hideLoading();
                                        onBackPressed();
                                    }
                                });
                            }
                        });
                        anRedtooth.getProfileInformation(CryptoBytes.toHexString(keyToSearch),msgListenerFuture);

                    } catch (CantSendMessageException e) {
                        e.printStackTrace();
                    } catch (CantConnectException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (executor!=null){
            executor.shutdownNow();
            executor = null;
        }
    }

    private void showLoading(){
        progress_bar.setVisibility(View.VISIBLE);
    }

    private void hideLoading(){
        progress_bar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id==R.id.txt_chat){
            Toast.makeText(v.getContext(),"Sending chat request..",Toast.LENGTH_LONG).show();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        MsgListenerFuture<Boolean> readyListener = new MsgListenerFuture<>();
                        readyListener.setListener(new BaseMsgFuture.Listener<Boolean>() {
                            @Override
                            public void onAction(int messageId, Boolean object) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ProfileInformationActivity.this, "Chat request sent", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onFail(int messageId, int status, String statusDetail) {
                                Log.e(TAG, "fail chat request: " + statusDetail);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ProfileInformationActivity.this, "Chat request fail", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                        anRedtooth.requestChat(profileInformation, readyListener, TimeUnit.SECONDS, 45);
                    }catch (Exception e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(v.getContext(),"Chat call fail",Toast.LENGTH_LONG).show();
                                onBackPressed();
                            }
                        });
                    }
                }
            });

        }
    }
}
