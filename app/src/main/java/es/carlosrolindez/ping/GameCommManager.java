
package es.carlosrolindez.ping;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class GameCommManager implements Runnable {

    private boolean ownership = false;

    private static final String TAG = "GameCommManager";
    private InputStream iStream;
    private OutputStream oStream;
    private BluetoothSocket socket = null;
    private Handler handler;

    public GameCommManager(BluetoothSocket socket, Handler handler, boolean ownership) {
        this.socket = socket;
        this.handler = handler;
        this.ownership = ownership;
        Log.e(TAG,"constructor");
    }

    @Override
    public void run() {
        try {
            Log.e(TAG,"run");

            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[][] buffer = new byte[4][512];
            int bytes;
            int bufferNumber = 0;

            // sometimes InputStream was re-filled before activity has time to attend previous handler message
            // In that situation buffer was corrupted by new InputStream before being read by activity.
            // queue of 4 buffers was used to prevent that problem

            handler.obtainMessage(Constants.MY_HANDLE, this)
                    .sendToTarget();


            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer[bufferNumber%4]);
                    if (bytes == -1) {
                        break;
                    }
                    // Send the obtained bytes to the UI Activity
                    handler.obtainMessage(Constants.MESSAGE,
                            bytes, -1, buffer[bufferNumber%4]).sendToTarget();
                    bufferNumber++;
                } catch (IOException e) {
                    Log.e(TAG,"read socket failed");
                    handler.obtainMessage(Constants.MY_CLOSE, this)
                            .sendToTarget();
                    return;
                }
            }
        } catch (IOException e) {
            Log.e(TAG,"connecting socket failed");
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String buffer) {
        try {
            oStream.write(buffer.getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            Log.e(TAG,"write socket failed");
            handler.obtainMessage(Constants.MY_CLOSE, this)
                    .sendToTarget();
        }
    }

    public boolean getOwnership() {
        return ownership;
    }

    public void cancel() {

        if (socket!=null) {
            try {
                socket.close();
                Log.e(TAG,"Socket closed externally");
                handler.obtainMessage(Constants.MY_CLOSE, this)
                        .sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
