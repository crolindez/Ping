
package es.carlosrolindez.ping;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

public class ServerSocketHandler extends Thread {

    private static final String TAG = "ServerSocketHandler";
    private BluetoothAdapter mBluetoothAdapter = null;
    private Handler mHandler;
    private GameCommManager chat;

    private final BluetoothServerSocket mmServerSocket;

    public ServerSocketHandler(Handler handler, BluetoothAdapter mBluetoothAdapter) {
        mHandler = handler;
        BluetoothServerSocket tmp = null;
        Log.e(TAG,"ServerSocket created");
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.NameService, Constants.MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmServerSocket = tmp;
    }

    public void run() {
        Log.e(TAG,"ServerSocket run");
        if (mmServerSocket==null) return;
        BluetoothSocket socket = null;
        Log.e(TAG,"ServerSocket next step");
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
                Log.e(TAG,"ServerSocket accepted");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"ServerSocket rejected");
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                chat = new GameCommManager(socket, mHandler);
                Log.e(TAG,"GameCommManager created");
                new Thread(chat).start();
                try {
                    mmServerSocket.close();
                    Log.e(TAG,"ServerSocket closed after GameCommManager");
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                } break;
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
            Log.e(TAG,"ServerSocket closed externally");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
