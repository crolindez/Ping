package es.carlosrolindez.ping;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;


public class PingActivity extends FragmentActivity  implements LaunchFragment.OnDeviceSelected,
                                                                BtBroadcastReceiver.OnBtBroadcastInteractionListener,
                                                                GameFragment.OnGameFragmentInteractionListener,
                                                                Handler.Callback {

    private static final String TAG = "PingActivity";

//    public static final int SERVER_PORT = 4545;


    public static final int MESSAGE = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;

/*    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BtBroadcastReceiver mReceiver = null;
    private WifiP2pDeviceList mDeviceList;
*/

    private BluetoothAdapter mBluetoothAdapter = null;
    private BtBroadcastReceiver mReceiver = null;
    private static boolean mBtIsBound = false;
 //   private static IBluetooth iBt = null;




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
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_launch);

        preferences = getSharedPreferences("Name", MODE_PRIVATE);
        playerName = preferences.getString("username", getResources().getString(R.string.player));

        if (playerName.equals(getResources().getString(R.string.player)))
            createDialog();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.bt_not_availabe), Toast.LENGTH_LONG).show();
            finish();
        }

//        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
//        mChannel = mManager.initialize(this, getMainLooper(), null);



//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mIntentFilter.addAction(Constants.NameFilter);
        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

  //      registerReceiver(mBtReceiver, mIntentFilter);


        searchBtPairedNames(/*context*/);

        launchFragment = new LaunchFragment();
        gameFragment = new GameFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_container, launchFragment, "services");
        ft.commit();

        mReceiver = new BtBroadcastReceiver(handler,this);

    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        }

    }
     @Override
    protected void onResume() {
        super.onResume();
         registerReceiver(mReceiver, mIntentFilter);
         searchBtPairedNames();
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        registerReceiver(mReceiver, mIntentFilter);
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
            case R.id.bt_scan:
                // Launch the DeviceListActivity to see devices and do scan
                doDiscovery();
                return true;
            case R.id.menu_player_name:
                createDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
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


    public void searchBtPairedNames(/*Context context*/) {
        Intent intent = new Intent(IBluetooth.class.getName());

        if (!mBtIsBound) {
            if (bindService(intent, mBtServiceConnection, Context.BIND_AUTO_CREATE)) {

            } else {
            }
        } else {
            Intent intent2 = new Intent();
            intent2.setAction(Constants.NameFilter);
            sendBroadcast(intent2);
        }
    }

    public ServiceConnection mBtServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBtIsBound = true;
 //           iBt = IBluetooth.Stub.asInterface(service);
            Intent intent = new Intent();
            intent.setAction(Constants.NameFilter);
            sendBroadcast(intent);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBtIsBound = false;

        }

    };



 /*   protected void peersAgain() {
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
    }*/


 /*   @Override
    public void onPeersAvailable(WifiP2pDeviceList deviceList) {
        mDeviceList = deviceList;
        if (launchFragment!=null) launchFragment.showList(mDeviceList);
    }*/



/*    @Override
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
*/
    public void addMessage(String text) {
        if (launchFragment != null) launchFragment.addMessage(text);
    }

 /*   public void changeConnectionState(boolean state) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.frame_container, launchFragment, "services");
        ft.commit();
        peersAgain();
    }
*/

 /*   public void connect(String name) {



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
*/
 /*   public void closeConnection(int code){

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
*/
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
