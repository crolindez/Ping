
package es.carlosrolindez.ping;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;

/**
 * The implementation of a ServerSocket handler. This is used by the wifi p2p
 * group owner.
 */
class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private final Handler handler;
    private BluetoothSocket mSocket;



    public ClientSocketHandler(Handler handler, BluetoothDevice device ) {

        this.handler = handler;

        try {
            mSocket = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        GameCommManager chat;

        // Make a connection to the BluetoothSocket
        try {
            mSocket.connect();
            chat = new GameCommManager(mSocket, handler, true);
            new Thread(chat).start();
        } catch (IOException e) {
            // Close the socket
            try {
                mSocket.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }

        }

    }


}
