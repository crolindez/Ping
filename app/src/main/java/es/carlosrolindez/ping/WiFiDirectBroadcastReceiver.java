package es.carlosrolindez.ping;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by Carlos on 06/08/2016.
 */

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "WiFiDirectBroadcastReceiver";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
//    private PingActivity mActivity;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pManager.ConnectionInfoListener mConnectionListener;
    private OnWiFiDirectBroadcastInteractionListener mListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       WifiP2pManager.PeerListListener peerListListener,
                                       WifiP2pManager.ConnectionInfoListener connectionListener,
                                       OnWiFiDirectBroadcastInteractionListener activityListener) {
        super();
        mManager = manager;
        mChannel = channel;
//        mActivity = activity;
        mPeerListListener = peerListListener;
        mConnectionListener = connectionListener;
        mListener = activityListener;
    }

    @Override
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

    public interface OnWiFiDirectBroadcastInteractionListener {
        void addMessage(String message);
        void changeConnectionState(boolean state);
    }

}
