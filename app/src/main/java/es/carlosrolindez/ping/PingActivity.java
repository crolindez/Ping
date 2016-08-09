package es.carlosrolindez.ping;

import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.net.InetAddress;


public class PingActivity extends FragmentActivity  implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener,
        LaunchFragment.OnDeviceSelected,GameFragment.OnGameFragmentInteractionListener {

    private WifiP2pManager mManager;
    private static WifiP2pManager.Channel mChannel;
    private WiFiDirectBroadcastReceiver mReceiver = null;
    private static WifiP2pDeviceList mDeviceList;

    private final IntentFilter mIntentFilter = new IntentFilter();

    private LaunchFragment launchFragment;
    private GameFragment gameFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);



        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        launchFragment = new LaunchFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_container, launchFragment, "services").commit();

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, this);

    }

     @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                addMessage("Discovery Success");
            }

            @Override
            public void onFailure(int reasonCode) {
                addMessage("Discovery failure");
            }
        });
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList deviceList) {
        mDeviceList = deviceList;
        if (launchFragment!=null) launchFragment.showList(mDeviceList);
    }



    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {

        // InetAddress from WifiP2pInfo struct.
        InetAddress groupOwnerAddress = info.groupOwnerAddress;

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {

            addMessage("Group Formed.  I am the owner");
        } else if (info.groupFormed) {
            addMessage("Group Formed.  I am NOT the owner");
        }
    }

    public void addMessage(String text) {
        if (launchFragment != null) launchFragment.addMessage("Discovery failure");
    }

    public void connect(int position) {

        int index = 0;

        for (WifiP2pDevice device : mDeviceList.getDeviceList()) {
            if (index == position) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        addMessage("Connection Success");
                    }

                    @Override
                    public void onFailure(int reason) {
                        addMessage("Connection Failure");
                    }
                });
                break;
            }
            index++;
        }
    }

    public void onGameFragmentInteraction(String message){

    }

}
