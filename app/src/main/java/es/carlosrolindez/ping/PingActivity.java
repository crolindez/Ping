package es.carlosrolindez.ping;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import java.util.Set;


public class PingActivity extends FragmentActivity  implements LaunchFragment.OnDeviceSelected,
                                                                BtBroadcastReceiver.OnBtBroadcastInteractionListener,
                                                                Handler.Callback {

    private final String TAG = "PingActivity";




    private BluetoothAdapter mBluetoothAdapter = null;
//    private BtBroadcastReceiver mReceiver = null;
//    private static boolean mBtIsBound = false;

    private final int MAX_NUM_DEVICES = 100;
    private BluetoothDevice[] mPairedDevices;
    private int numDevices;


    private ServerSocketHandler serviceThread;
    private ClientSocketHandler clientThread;

 //   private final IntentFilter mIntentFilter = new IntentFilter();
    private Handler handler = new Handler(this);

    private LaunchFragment launchFragment;
    private GameFragment gameFragment;

    private boolean ownership = false;

    private SharedPreferences preferences;
    private String playerName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 //       requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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

        launchFragment = new LaunchFragment();
        gameFragment = new GameFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_container, launchFragment, "services");
        ft.commit();

 //       mReceiver = new BtBroadcastReceiver(this);

 //       mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
//        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
//        mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//        mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);


    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG,"Start");

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        } else {
 //           registerReceiver(mReceiver, mIntentFilter);
            Set<BluetoothDevice> set = mBluetoothAdapter.getBondedDevices();
            if (set!=null) {
                mPairedDevices = set.toArray(new BluetoothDevice[MAX_NUM_DEVICES]);
                numDevices = set.size();
                Log.e(TAG,"List of devices");
            }

            showArrayDevices();
   //         doDiscovery();
        }


    }
     @Override
    protected void onResume() {
        super.onResume();
         Log.e(TAG,"Resume - Registering receiver");
 //        registerReceiver(mReceiver, mIntentFilter);
//         doDiscovery();
         startServiceThread();
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG,"Pause - Unregistering receiver");
 //       unregisterReceiver(mReceiver);
 //       stopDiscovery();
        stopServiceThread();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.bt_scan:
 //               doDiscovery();
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
 /*   private void doDiscovery() {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }
*/
    private void startServiceThread() {
        if (serviceThread!=null) serviceThread.cancel();
        serviceThread = new ServerSocketHandler(handler,mBluetoothAdapter);
        serviceThread.start();
        ownership = false;
    }
/*
    public void stopDiscovery() {
        setProgressBarIndeterminateVisibility(false);
        mBluetoothAdapter.cancelDiscovery();
    }
*/
    private void stopServiceThread() {
        if (serviceThread!=null) serviceThread.cancel();
        ownership = false;
    }

    @Override
    public boolean handleMessage(Message msg) {
        FragmentTransaction ft;
        switch (msg.what) {
            case Constants.MESSAGE:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                gameFragment.pushMessage(readMessage);
                break;

            case Constants.MY_CLOSE:
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, launchFragment, "services");
                ft.commit();
                break;

            case Constants.MY_HANDLE:
                Object obj = msg.obj;
                gameFragment.setGameFragment((GameCommManager) obj, ownership, playerName);
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, gameFragment, "game");
                ft.commit();
                break;

        }
        return true;
    }


    public void showArrayDevices() {
        if (mPairedDevices == null ) return;
        if (launchFragment == null ) return;

        launchFragment.deleteDeviceList();
        for (int index=0; index<numDevices; index++) {
            launchFragment.addDevice(mPairedDevices[index].getName());
  //          Log.e(TAG,mPairedDevices[index].getName() + " " + index);
        }

    }

    public void addDevice(BluetoothDevice newDevice) {
        if (newDevice==null) return;
        if ((numDevices+1)<MAX_NUM_DEVICES) {
            for (int index=0; index<numDevices; index++) {
                if (mPairedDevices[index].getName().equals(newDevice.getName())) return;
            }
            Log.e(TAG,"New " + newDevice.getName() + " " + numDevices);
            mPairedDevices[numDevices++] = newDevice;

        }


    }

    public void addMessage(String text) {
        if (launchFragment != null) launchFragment.addMessage(text);
    }

    public void connectDeviceItem(int position) {

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();


        if (MAX_NUM_DEVICES>position) {
            Log.e(TAG,mPairedDevices[position].getName() + " " + position);
 /*           if (mPairedDevices[position].getBondState() != BluetoothDevice.BOND_BONDED) {
                Log.e(TAG, "Create Bond");
                mPairedDevices[position].createBond();
            } else {*/
                connectDevice(mPairedDevices[position]);
                Log.e(TAG,"Connect Device");
  //          }
        } else {
            Log.e(TAG,"selected " + position + " out of " + MAX_NUM_DEVICES );
        }


    }

    public void connectDevice(BluetoothDevice device) {

//        stopDiscovery();

        if (clientThread!=null) clientThread.cancel();
        clientThread = new ClientSocketHandler(handler,device);
        clientThread.start();
        ownership = true;

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
