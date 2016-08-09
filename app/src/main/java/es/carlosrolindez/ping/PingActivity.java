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
import android.support.v4.app.FragmentTransaction;

import java.net.InetAddress;


public class PingActivity extends FragmentActivity  implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener,
        LaunchFragment.OnDeviceSelected,GameFragment.OnGameFragmentInteractionListener, WiFiDirectBroadcastReceiver.OnWiFiDirectBroadcastInteractionListener {

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
        gameFragment = new GameFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_container, launchFragment, "services");
        ft.commit();

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, this, this);

    }

     @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        peersAgain();
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    protected void peersAgain() {
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
        if (!info.groupFormed){
            addMessage("NO Group Formed");
            return;
        }

        if (info.isGroupOwner) {

            addMessage("Group Formed.  I am the owner");
            gameFragment.changeOwnership(true);
        } else {

            addMessage("Group Formed.  I am NOT the owner");
            gameFragment.changeOwnership(false);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_container, gameFragment, "game");
        ft.commit();




    }

    public void addMessage(String text) {
        if (launchFragment != null) launchFragment.addMessage(text);
    }
    public void changeConnectionState(boolean state) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.frame_container, launchFragment, "services");
        ft.commit();
        peersAgain();
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

    public void closeConnection(int code){

        if (mManager != null && mChannel != null) {

            mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    addMessage("Disconnection Success");
                }

                @Override
                public void onFailure(int reason) {
                    addMessage("Disconnection Failure");
                }
            });


        }
    }

}
