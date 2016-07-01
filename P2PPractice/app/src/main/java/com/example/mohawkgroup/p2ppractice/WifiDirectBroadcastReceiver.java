package com.example.mohawkgroup.p2ppractice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.*;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mohawk Group on 6/27/2016.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private Channel mChannel;
    private MainActivity mActivity;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        PeerListListener myPeerListListener = new PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                // Out with the old, in with the new.
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());

                // If an AdapterView is backed by this data, notify it
                // of the change.  For instance, if you have a ListView of available
                // peers, trigger an update.
//                ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
//                if (peers.size() == 0) {
//                    Log.d(WiFiDirectActivity.TAG, "No devices found");
//                    return;
//                }
            }
        };

        ConnectionInfoListener myConnectionInfoListener = new ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                // InetAddress from WifiP2pInfo struct.
                InetAddress groupOwnerAddress = info.groupOwnerAddress; //
                String groupOwnerAddressString = groupOwnerAddress.getHostAddress();

                // After the group negotiation, we can determine the group owner.
                if (info.groupFormed && info.isGroupOwner) {
                    // Do whatever tasks are specific to the group owner.
                    // One common case is creating a server thread and accepting
                    // incoming connections.
                } else if (info.groupFormed) {
                    // The other device acts as the client. In this case,
                    // you'll want to create a client thread that connects to the group
                    // owner.
                }
            }
        };

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                mActivity.showStatus("p2p wifi enabled");
            } else {
                // Wi-Fi P2P is not enabled
                mActivity.showError("p2p wifi is not enabled on this device");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, myPeerListListener);
            }
            mActivity.showDevices(peers);

            int android_index = MainActivity.target_device_index(peers);
            if (android_index > -1) {
                mActivity.showStatus("Android device detected");
                mActivity.connect(peers.get(android_index));
            } else {
                mActivity.showStatus("no android device detected");
                mActivity.updateButtonText("DISABLED");
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (mManager != null) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {

                    // We are connected with the other device, request connection
                    // info to find group owner IP
                    mActivity.showStatus("Connected to Android device");

                    mActivity.updateButtonText("SEND");
                    mManager.requestConnectionInfo(mChannel, myConnectionInfoListener);
                } else {
                    mActivity.showStatus("Connection lost");
                    mActivity.updateButtonText("DISABLED");
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
//            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                    .findFragmentById(R.id.frag_list);
//            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE))
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            if (mManager.EXTRA_DISCOVERY_STATE.equals(WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (!networkInfo.isConnected()) {
                    mActivity.updateButtonText("DISABLED");
                    mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            mActivity.showStatus("discovery success");
                            mActivity.showError("");
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            if (reasonCode == WifiP2pManager.ERROR) {
                                mActivity.showError("discovery failure, internal error");
                            } else if (reasonCode == WifiP2pManager.P2P_UNSUPPORTED) {
                                mActivity.showError("discovery failure, p2p unsupported on this device");
                            } else if (reasonCode == WifiP2pManager.BUSY) {
                                mActivity.showError("discovery failure, framework busy");
                            } else {
                                mActivity.showError("discovery failure, unknown reason");
                            }
                        }
                    });
                }
            }
        }

    }
}
