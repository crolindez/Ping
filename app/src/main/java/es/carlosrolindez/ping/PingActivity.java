package es.carlosrolindez.ping;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.net.InetAddress;


public class PingActivity extends FragmentActivity  implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener,
        LaunchFragment.OnDeviceSelected,GameFragment.OnGameFragmentInteractionListener, WiFiDirectBroadcastReceiver.OnWiFiDirectBroadcastInteractionListener,
        Handler.Callback {

    private static final String TAG = "PingActivity";

    public static final int SERVER_PORT = 4545;


    public static final int MESSAGE = 0x400 + 1;
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

    private SharedPreferences preferences;
    private String playerName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        preferences = getSharedPreferences("Name", MODE_PRIVATE);
        playerName = preferences.getString("username", getResources().getString(R.string.player));

        if (playerName.equals(getResources().getString(R.string.player)))
            createDialog();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_player_name:
                createDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                gameFragment.pushMessage(readMessage);
                break;

            case MY_HANDLE:
                Object obj = msg.obj;
                gameFragment.setGameFragment((GameCommManager) obj, ownership, playerName);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, gameFragment, "game");
                ft.commit();
                break;

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
            addMessage("Group Formed.  I am the owner");
            thread = new GroupOwnerSocketHandler(handler);
            thread.start();

        } else {
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


    public void connect(String name) {



        for (WifiP2pDevice device : mDeviceList.getDeviceList()) {
            if (name.equals(device.deviceName)) {
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

    public void createDialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = getLayoutInflater().inflate(R.layout.name_dialog, null);
        final EditText nameText = (EditText) view.findViewById(R.id.dialog_player_name);
        nameText.setText(playerName);
        builder.setView(view)
                .setMessage(R.string.intro_name)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (nameText == null) Log.e(TAG, "EditText empty");
                        Editable editable = nameText.getText();
                        if (editable == null) Log.e(TAG, "editable empty");
                        playerName = nameText.getText().toString();
                        if (playerName.isEmpty())
                            playerName = getResources().getString(R.string.player);
                        SharedPreferences.Editor edit = preferences.edit();
                        edit.putString("username", playerName);
                        edit.apply();
                    }
                });
        builder.show();
    }

}
