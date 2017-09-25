package org.furszy.contacts.ui.send_request;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.furszy.contacts.BaseActivity;
import org.furszy.contacts.R;
import org.furszy.contacts.scanner.ScanActivity;
import org.libertaria.world.core.services.pairing.PairingMsg;
import org.libertaria.world.crypto.CryptoBytes;
import org.libertaria.world.profile_server.ProfileInformation;
import org.libertaria.world.profile_server.engine.MessageQueueManager;
import org.libertaria.world.profile_server.engine.futures.BaseMsgFuture;
import org.libertaria.world.profile_server.engine.futures.MsgListenerFuture;
import org.libertaria.world.profile_server.utils.ProfileUtils;
import org.libertaria.world.profiles_manager.PairingRequest;

import static android.Manifest.permission.CAMERA;
import static org.furszy.contacts.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by furszy on 6/21/17.
 */

public class SendRequestActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SendRequestActivity";

    private static final int SCANNER_RESULT = 122;
    private View root;
    private EditText edit_uri;
    private Button btn_add;
    private ProgressBar progressBar;
    private SendPairingReceiver sendPairingReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.sendPairingReceiver = new SendPairingReceiver();
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        super.onCreateView(savedInstanceState, container);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2998ff")));
        setTitle("Send Request");
        root = getLayoutInflater().inflate(R.layout.send_request_activity, container, true);
        edit_uri = (EditText) root.findViewById(R.id.edit_uri);
        root.findViewById(R.id.img_qr).setOnClickListener(this);
        btn_add = (Button) root.findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);

        progressBar = (ProgressBar) root.findViewById(R.id.progress_bar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.img_qr) {
            if (!checkPermission(CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permsRequestCode = 200;
                    String[] perms = {"android.permission.CAMERA"};
                    requestPermissions(perms, permsRequestCode);
                }
            }
            Intent intent = new Intent(v.getContext(), ScanActivity.class);
            startActivityForResult(intent, SCANNER_RESULT);
        } else if (id == R.id.btn_add) {
            try {
                btn_add.setEnabled(false);
                btn_add.setBackground(getResources().getDrawable(R.drawable.bg_button_light_blue, null));
                String uri = edit_uri.getText().toString();
                if (uri.length() < 1) {
                    enableSendBtn();
                    return;
                }
                if (!ProfileUtils.isValidUriProfile(uri)) {
                    enableSendBtn();
                    Snackbar.make(v, "Invalid URI Format", Snackbar.LENGTH_LONG).show();
                    return;
                }
                final ProfileUtils.UriProfile profile = ProfileUtils.fromUri(uri);
                if (profile.getPubKey().equals(profilesModule.getProfile(selectedProfPubKey).getHexPublicKey())) {
                    enableSendBtn();
                    Snackbar.make(v, R.string.pairing_yourself, Snackbar.LENGTH_LONG).show();
                    return;

                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MsgListenerFuture<ProfileInformation> future = new MsgListenerFuture<>();
                                future.setListener(new BaseMsgFuture.Listener<ProfileInformation>() {
                                    @Override
                                    public void onAction(int messageId, ProfileInformation object) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Log.i(TAG, "pairing request sent");
                                                Snackbar.make(v, R.string.pairing_success, Snackbar.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.INVISIBLE);
                                                enableSendBtn();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFail(int messageId, final int status, final String statusDetail) {
                                        Log.i("SendRequest", "request fail " + statusDetail);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Log.i(TAG, "pairing request fail, " + statusDetail);
                                                Snackbar.make(v, getString(R.string.pairing_fail, statusDetail), Snackbar.LENGTH_LONG).show();
                                                //Toast.makeText(SendRequestActivity.this,statusDetail,Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.INVISIBLE);
                                                enableSendBtn();
                                                //onBackPressed();
                                            }
                                        });
                                    }
                                });
                                pairingModule.requestPairingProfile(
                                        selectedProfPubKey,
                                        CryptoBytes.fromHexToBytes(profile.getPubKey()),
                                        profile.getName(),
                                        profile.getProfSerHost(),
                                        future
                                );

                            } catch (final IllegalArgumentException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "pairing request fail  " + e.getMessage());
                                        Snackbar.make(v, R.string.pairing_fail, Snackbar.LENGTH_LONG).show();
                                        enableSendBtn();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            } catch (final Exception e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        enableSendBtn();
                                        Log.i(TAG, "pairing request fail  " + e.getMessage());
                                        Snackbar.make(v, R.string.pairing_fail, Snackbar.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.INVISIBLE);

                                    }
                                });
                            }
                        }
                    }).start();
                }
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "pairing request fail  " + e.getMessage());
                Snackbar.make(v, R.string.pairing_fail, Snackbar.LENGTH_LONG).show();
            }
        }
    }


    private void enableSendBtn() {
        btn_add.setEnabled(true);
        btn_add.setBackground(getResources().getDrawable(R.drawable.bg_button_blue, null));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_RESULT) {
            if (resultCode == RESULT_OK) {
                try {
                    String address = data.getStringExtra(INTENT_EXTRA_RESULT);
                    //Toast.makeText(this,address,Toast.LENGTH_LONG).show();
                    edit_uri.setText(address);
                } catch (Exception e) {
                    Toast.makeText(this, "Bad profile URI", Toast.LENGTH_LONG).show();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class SendPairingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case MessageQueueManager.EVENT_MESSAGE_SUCCESSFUL: {
                    MessageQueueManager.Message message = (MessageQueueManager.Message) intent.getExtras().get(MessageQueueManager.EVENT_MESSAGE_FAILED);
                    PairingMsg pairingMsg = new PairingMsg();
                    try {
                        pairingMsg.decode(message.getMsg());
                        PairingRequest pairingRequest = pairingModule.getPairingRequest(pairingMsg.getPairingRequestId());
                        Notification not = new Notification.Builder(context)
                                .setContentTitle("Pairing request accepted!")
                                .setContentText("Your pairing request with " + pairingRequest.getRemoteName() + " has been accepted!")
                                .setSmallIcon(R.drawable.ic_add_contact)
                                .setAutoCancel(true)
                                .build();
                        notificationManager.notify((int) pairingRequest.getId(), not);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                case MessageQueueManager.EVENT_MESSAGE_FAILED: {
                    MessageQueueManager.Message message = (MessageQueueManager.Message) intent.getExtras().get(MessageQueueManager.EVENT_MESSAGE_FAILED);
                    PairingMsg pairingMsg = new PairingMsg();
                    try {
                        pairingMsg.decode(message.getMsg());
                        PairingRequest pairingRequest = pairingModule.getPairingRequest(pairingMsg.getPairingRequestId());
                        pairingModule.cancelPairingRequest(pairingRequest);
                        Notification not = new Notification.Builder(context)
                                .setContentTitle("Pairing request couldn't be sent.")
                                .setContentText("Your pairing request with " + pairingRequest.getRemoteName() + " couldn't be sent.")
                                .setSmallIcon(R.drawable.ic_close)
                                .setAutoCancel(true)
                                .build();
                        notificationManager.notify((int) pairingRequest.getId(), not);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }
}
