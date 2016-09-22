package es.carlosrolindez.ping;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Set;


public class PingActivity extends FragmentActivity  implements LaunchFragment.OnDeviceSelected, Handler.Callback, GameFragment.OnGameFragmentInteractionListener {

    private final String TAG = "PingActivity";

    private BluetoothAdapter mBluetoothAdapter = null;


    private final int MAX_NUM_DEVICES = 100;
    private BluetoothDevice[] mPairedDevices;
    private int numDevices;


    private ServerSocketHandler serviceThread;
//    private ClientSocketHandler clientThread;
    private GameCommManager gameRunnable;

    private final Handler handler = new Handler(this);

    private LaunchFragment launchFragment;
    private GameFragment gameFragment;

    private boolean paused;

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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bt_not_enabled,Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
        } else {

            Set<BluetoothDevice> set = mBluetoothAdapter.getBondedDevices();
            if (set!=null) {
                mPairedDevices = set.toArray(new BluetoothDevice[MAX_NUM_DEVICES]);
                numDevices = set.size();
            }
        }


    }
     @Override
    protected void onResume() {
        super.onResume();
        paused = false;
         showArrayDevices();
         startServiceThread();
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
        closeConnection();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

    private void startServiceThread() {
        if (serviceThread!=null) {
            serviceThread.cancel();
            serviceThread = null;
        }
        serviceThread = new ServerSocketHandler(handler,mBluetoothAdapter);
        serviceThread.start();
    }

    public void closeConnection() {
        if (gameRunnable!=null) {
            gameRunnable.cancel();
            gameRunnable = null;
        } else if (serviceThread!=null) {
            serviceThread.cancel();
            serviceThread =  null;
        }
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
                if (!paused) {
                    ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.frame_container, launchFragment, "services");
                    ft.commit();
                    showArrayDevices();
                    startServiceThread();
                }
                break;

            case Constants.MY_HANDLE:
                if (serviceThread!=null) {
                    serviceThread.cancel();
                    serviceThread = null;
                }
                gameRunnable = (GameCommManager) msg.obj;
                gameFragment.setGameFragment(gameRunnable, playerName, this);
                ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_container, gameFragment, "game");
                ft.commit();
                break;

        }
        return true;
    }


    private void showArrayDevices() {
        if (mPairedDevices == null ) return;
        if (launchFragment == null ) return;

        launchFragment.deleteDeviceList();
        for (int index=0; index<numDevices; index++) {
            launchFragment.addDevice(mPairedDevices[index].getName());
        }

    }


    public void addMessage(String text) {
        if (launchFragment != null) launchFragment.addMessage(text);
    }

    public void connectDeviceItem(int position) {

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();


        if (MAX_NUM_DEVICES>position) {
            connectDevice(mPairedDevices[position]);
        } else {
        }
    }

    private void connectDevice(BluetoothDevice device) {

//        closeConnection();
        new ClientSocketHandler(handler,device).start();

    }

    private void createDialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = getLayoutInflater().inflate(R.layout.name_dialog, null);
        final EditText nameText = (EditText) view.findViewById(R.id.dialog_player_name);
        nameText.setText(playerName);
        builder.setView(view)
                .setMessage(R.string.intro_name)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Editable editable = nameText.getText();
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
