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
                showError("");
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
                showError("");
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
        String accumulator = "DEVICES: ";
        for (WifiP2pDevice device : available_devices) {
            accumulator = accumulator + device.deviceName + " ";
        }
        txtView.setText(accumulator);
    }

    public void showDevices(WifiP2pDevice connected_device) {
        TextView txtView = (TextView) findViewById(R.id.p2p_devices);
        txtView.setText("DEVICES: " + connected_device.deviceName);
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

    // Our two target devices are "Android_82bc" and "Android_56b4"
    public static int target_device_index(List<WifiP2pDevice> peers) {
        int counter = 0;
        for (WifiP2pDevice peer : peers) {
            String name = peer.deviceName;
            if (deviceNameContains(name, "Android_82bc") || deviceNameContains(name, "Android_82bc")) { // || contains_sink(name)) {
                return counter;
            }
            counter++;
        }
        return -1;
    }

    // return true if string contains the string "android"
    public static boolean deviceNameContains(String deviceName, String s) {
        Pattern pattern = Pattern.compile("s", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(deviceName);
        return matcher.find();
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
