package es.carlosrolindez.ping;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import java.util.Iterator;
import java.util.Set;


public class PingActivity extends FragmentActivity  implements LaunchFragment.OnDeviceSelected,
                                                                BtBroadcastReceiver.OnBtBroadcastInteractionListener,
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
    private Set<BluetoothDevice> mPairedDevices;
//    private static IBluetooth iBt = null;

    private ServerSocketHandler serviceThread;
    private ClientSocketHandler clientThread;





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
            Toast.makeText(this, getString(R.string.bt_not_available), Toast.LENGTH_LONG).show();
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



        launchFragment = new LaunchFragment();
        gameFragment = new GameFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_container, launchFragment, "services");
        ft.commit();


        mReceiver = new BtBroadcastReceiver(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        } else {
            registerReceiver(mReceiver, mIntentFilter);
         //   searchBtPairedNames();
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            showSet(mPairedDevices);
            doDiscovery();
        }


    }
     @Override
    protected void onResume() {
        super.onResume();
         registerReceiver(mReceiver, mIntentFilter);
         //searchBtPairedNames();
         doDiscovery();
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

        if (serviceThread!=null) serviceThread.cancel();
        serviceThread = new ServerSocketHandler(handler,mBluetoothAdapter);
        serviceThread.start();

    }

    public void stopDiscovery() {
        setProgressBarIndeterminateVisibility(false);

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.cancelDiscovery();

        if (serviceThread!=null) serviceThread.cancel();

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

/*
    public void searchBtPairedNames() {
        Intent intent = new Intent(IBluetooth.class.getName());


        Log.e(TAG,"search Paired Devices " + IBluetooth.class.getName());

        if (!mBtIsBound) {
            Log.e(TAG,"not bound");
            if (bindService(intent, mBtServiceConnection, Context.BIND_AUTO_CREATE)) {

            } else {
            }
        } else {
            Log.e(TAG,"bound");
            Intent intent2 = new Intent();
            intent2.setAction(Constants.NameFilter);
            sendBroadcast(intent2);
        }
    }

    public ServiceConnection mBtServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG,"mBtServiceConnected TRUE");
            mBtIsBound = true;
            iBt = IBluetooth.Stub.asInterface(service);
            Intent intent = new Intent();
            intent.setAction(Constants.NameFilter);
            sendBroadcast(intent);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG,"mBtServiceConnected FALSE");
            mBtIsBound = false;

        }

    };

*/

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
            thread = new ClientSocketHandler(handler);
            thread.start();

        } else {
            addMessage("Group Formed.  I am NOT the owner");
            thread = new ServerSocketHandler(handler, info.groupOwnerAddress);
            thread.start();
        }



    }
*/
    public void showSet(Set<BluetoothDevice> pairedDevices) {
        if (pairedDevices == null ) return;
        if (launchFragment == null ) return;
        String currentName;

        if (pairedDevices.size() > 0) {
            if (mPairedDevices==null) {
                try {
                    mPairedDevices.clear();
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                }
            }
            mPairedDevices = pairedDevices;
            launchFragment.deleteDeviceList();
            for (BluetoothDevice device : pairedDevices) {
                currentName = device.getName();
 /*               try {
                    currentName = iBt.getRemoteAlias(device);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }*/
                launchFragment.addDevice(currentName);
            }
        }
    }

    public void addDevice(BluetoothDevice device) {
        if (device==null) return;
        if (mPairedDevices.contains(device)) return;
        mPairedDevices.add(device);
    }

    public void addMessage(String text) {
        if (launchFragment != null) launchFragment.addMessage(text);
    }

    public void connectDeviceItem(int position) {

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        BluetoothDevice device=null;

        int index=0;

        for (Iterator<BluetoothDevice> it = mPairedDevices.iterator(); it.hasNext(); ) {
            device = it.next();
            Log.e(device.getName(),"" + position);

            if (index==position) {

                break;
            }
            index++;
        }
        if (index==position) {
            Log.e(device.getName(),"" + position);
            if (device.getBondState() != BluetoothDevice.BOND_BONDED)
                device.createBond();
            else {
                connectDevice(device);
            }
        }


    }

    public void connectDevice(BluetoothDevice device) {

        stopDiscovery();

        if (clientThread!=null) clientThread.cancel();
        clientThread = new ClientSocketHandler(handler,device);
        clientThread.start();


    }
/*
    public void closeService( ){

        unregisterReceiver(mReceiver);
        doUnbindServiceBt();
    }

    public void doUnbindServiceBt() {
        if (mBtIsBound) {
            try {
                unbindService(mBtServiceConnection);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }


    }
*/
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
