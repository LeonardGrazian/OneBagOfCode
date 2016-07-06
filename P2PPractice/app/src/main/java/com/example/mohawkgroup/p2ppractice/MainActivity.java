package com.example.mohawkgroup.p2ppractice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;
import java.util.List;
import java.util.regex.*;

public class MainActivity extends AppCompatActivity {
    WifiP2pManager mManager;
    Channel mChannel;
    BroadcastReceiver mReceiver;

    IntentFilter mIntentFilter;

    // array of names of devices that we want to connect with
    public static final String[] TARGET_DEVICE_NAMES = {"Android_82bc", "Android_56b4"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                showStatus("discovery success");
                showError(""); // something good just happened, clear error field
            }

            @Override
            public void onFailure(int reasonCode) {
                if (reasonCode == WifiP2pManager.ERROR) {
                    showError("discovery failure, internal error");
                } else if (reasonCode == WifiP2pManager.P2P_UNSUPPORTED) {
                    showError("discovery failure, p2p unsupported on this device");
                } else if (reasonCode == WifiP2pManager.BUSY) {
                    showError("discovery failure, framework busy");
                } else {
                    showError("discovery failure, unknown reason");
                }
            }
        });
    }

    public void connect(WifiP2pDevice device) {
//            WifiP2pConfig config = new WifiP2pConfig();
//            config.deviceAddress = device.deviceAddress;
//            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
//
//                @Override
//                public void onSuccess() {
//                    //success logic
//                }
//
//                @Override
//                public void onFailure(int reason) {
//                    //failure logic
//                }
//            });

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                showStatus("Successfully initiated connection");
                showError(""); // something good just happened, clear error field
//                updateButtonText("SEND");
            }

            @Override
            public void onFailure(int reason) {
                showError("failed to initiate connection");
//                updateButtonText("DISABLED");
            }
        });
    }

    // update text on screen with potential wifi connections
    public void showDevices(Collection<WifiP2pDevice> available_devices) {
        TextView txtView = (TextView) findViewById(R.id.p2p_devices);
        String accumulator = "DEVICES:  ";
        for (WifiP2pDevice device : available_devices) {
            accumulator = accumulator + device.deviceName + "  ";
        }
        txtView.setText(accumulator);
    }

    public void showStatus(String s) {
        TextView txtView = (TextView) findViewById(R.id.p2p_status);
        txtView.setText("STATUS: " + s);
    }

    public void updateButtonText(String s) {
        TextView txtView = (TextView) findViewById(R.id.send_button);
        txtView.setText(s);
    }

    public void showError(String s) {
        TextView txtView = (TextView) findViewById(R.id.p2p_errors);
        txtView.setText("ERROR: " + s);
    }

    // returns (lowest) index of a target device in peers
    // returns -1 if there is no target device in peers
    public static int targetDeviceIndex(List<WifiP2pDevice> peers) {
        for (int i = 0; i < peers.size(); i++) { // not ideal, but since it's an ArrayList
            String name = peers.get(i).deviceName;
            for (String targetName : TARGET_DEVICE_NAMES) {
                if (deviceNameContains(name, targetName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    // return true if string contains the deviceName contains targetName
    public static boolean deviceNameContains(String deviceName, String targetName) {
        return deviceName.toLowerCase().contains(targetName.toLowerCase());

    }

    // register the broadcast receiver with the intent values to be matched
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    // unregister the broadcast receiver
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}
