package es.carlosrolindez.ping;

import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import java.net.InetAddress;


public class PingActivity extends FragmentActivity  implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener,
        LaunchFragment.OnDeviceSelected,GameFragment.OnGameFragmentInteractionListener, WiFiDirectBroadcastReceiver.OnWiFiDirectBroadcastInteractionListener,
        Handler.Callback {

    private static final String TAG = "PingActivity";

    public static final int SERVER_PORT = 4545;

    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiDirectBroadcastReceiver mReceiver = null;
    private WifiP2pDeviceList mDeviceList;

    private final IntentFilter mIntentFilter = new IntentFilter();
    private Handler handler = new Handler(this);

    private LaunchFragment launchFragment;
    private GameFragment gameFragment;

    private boolean ownership = false;



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

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
 //               (chatFragment).pushMessage("Buddy: " + readMessage);
                break;

            case MY_HANDLE:
                Object obj = msg.obj;
                gameFragment.setGameFragment((GameManager) obj, ownership);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, gameFragment, "game");
                ft.commit();


        }
        return true;
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
        Thread thread;


        // After the group negotiation, we can determine the group owner.
        if (!info.groupFormed){
            addMessage("NO Group Formed");
            return;
        }

        ownership = info.isGroupOwner;


        if (ownership) {
            Log.d(TAG, "Connected as group owner");
            addMessage("Group Formed.  I am the owner");
            thread = new GroupOwnerSocketHandler(handler);
            thread.start();

        } else {
            Log.d(TAG, "Connected as peer");
            addMessage("Group Formed.  I am NOT the owner");
            thread = new ClientSocketHandler(handler, info.groupOwnerAddress);
            thread.start();
        }



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
