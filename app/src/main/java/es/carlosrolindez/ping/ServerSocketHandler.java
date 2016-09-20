
package es.carlosrolindez.ping;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;

class ServerSocketHandler extends Thread {

    private static final String TAG = "ServerSocketHandler";
    private final Handler mHandler;

    private final BluetoothServerSocket mmServerSocket;

    public ServerSocketHandler(Handler handler, BluetoothAdapter mBluetoothAdapter) {
        mHandler = handler;
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.NameService, Constants.MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmServerSocket = tmp;
    }

    public void run() {
        GameCommManager chat;

        if (mmServerSocket==null) return;
        BluetoothSocket socket;

        try {
            socket = mmServerSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // If a connection was accepted
        if (socket != null) {
            // Do work to manage the connection (in a separate thread)
            chat = new GameCommManager(socket, mHandler, false);
            new Thread(chat).start();
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
