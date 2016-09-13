
package es.carlosrolindez.ping;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;

public class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private BluetoothAdapter mBluetoothAdapter = null;
    private Handler mHandler;
    private GameCommManager chat;

    private final BluetoothServerSocket mmServerSocket;

    public ClientSocketHandler(Handler handler, BluetoothAdapter mBluetoothAdapter) {
        mHandler = handler;
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.NameService, Constants.MY_UUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }

    public void run() {
        if (mmServerSocket==null) return;
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                chat = new GameCommManager(socket, mHandler);
                new Thread(chat).start();
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    break;
                } break;
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }

}
