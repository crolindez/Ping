package es.carlosrolindez.ping;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Set;

/**
 * Created by Carlos on 06/08/2016.
 */

public class BtBroadcastReceiver extends BroadcastReceiver {
    private OnBtBroadcastInteractionListener mListener;
    private Set<BluetoothDevice> pairedDevices;
    private static final String TAG = "BtBroadcastReceiver";

    public BtBroadcastReceiver(OnBtBroadcastInteractionListener activityListener) {
        super();
        mListener = activityListener;
    }

    private Set<BluetoothDevice> getNames() {
        BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();

        if (mBTA != null) {
            pairedDevices = mBTA.getBondedDevices();
            return pairedDevices;

 /*           if (pairedDevices.size() > 0) {

                for (BluetoothDevice device : pairedDevices) {
                    String currentName = device.getName();
                    try {
                        currentName = iBt.getRemoteAlias(device);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }*/
        }
        return null;


    }


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            mListener.addMessage("BT FOUND");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            mListener.addDevice(device);

            // When discovery is finished, change the Activity title
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//               ((KBfinderActivity)context).setProgressBarIndeterminateVisibility(false);
            mListener.addMessage("BT DISCOVERY FINISHED");
            mListener.stopDiscovery();
        } else if (Constants.NameFilter.equals(action)) {
            mListener.addMessage("BT PAIRED DEVICES");
            mListener.showSet(getNames());

        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            mListener.addMessage("ACL CONNECTED");
//              mListener.connect(device);


        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            mListener.addMessage("ACL DISCONNECTED");
//            mListener.disconnect(device);
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                mListener.addMessage("BONDED");
                mListener.connectDevice(device);
            } else {
                mListener.addMessage("UNBONDED");
            }

        }

    };


/*
//    private WifiP2pManager mManager;
//    private WifiP2pManager.Channel mChannel;
//    private PingActivity mActivity;
//    private WifiP2pManager.PeerListListener mPeerListListener;
//    private WifiP2pManager.ConnectionInfoListener mConnectionListener;
//    private OnBtBroadcastInteractionListener mListener;

 //   public BtBroadcastReceiver(OnBtBroadcastInteractionListener activityListener) {
        super();
///        mManager = manager;
//        mChannel = channel;
//        mActivity = activity;
//        mPeerListListener = peerListListener;
//        mConnectionListener = connectionListener;
        mListener = activityListener;
    }
*/
/*    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                mListener.addMessage("Wifi P2P enabled");
            } else {
                mListener.addMessage("Wifi P2P NOT enabled");

            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            if (mManager != null) {
                mListener.addMessage("Wifi P2P request Peers");
                mManager.requestPeers(mChannel, mPeerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, mConnectionListener);
            } else {
                mListener.addMessage("connection changed to disconnected");
                mListener.changeConnectionState(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }
            mListener.addMessage("Device Changed action");

        }
    }
*/
    public interface OnBtBroadcastInteractionListener {
        void addMessage(String message);
        void showSet(Set<BluetoothDevice> pairedDevice);
        void addDevice(BluetoothDevice device);
        void stopDiscovery();
        void connectDevice(BluetoothDevice device);


    }

}
