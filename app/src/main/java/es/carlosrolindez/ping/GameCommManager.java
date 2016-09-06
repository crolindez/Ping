
package es.carlosrolindez.ping;

import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class GameCommManager implements Runnable {

    private Socket socket = null;
    private Handler handler;

    public GameCommManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "GameCommManager";

    @Override
    public void run() {
        try {

            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[][] buffer = new byte[4][512];
            int bytes;
            int bufferNumber = 0;

            // sometimes InputStream was re-filled before activity has time to attend previous handler message
            // In that situation buffer was corrupted by new InputStream before being read by activity.
            // queue of 4 buffers was used to prevent that problem

            handler.obtainMessage(PingActivity.MY_HANDLE, this)
                    .sendToTarget();


            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer[bufferNumber%4]);
                    if (bytes == -1) {
                        break;
                    }
                    // Send the obtained bytes to the UI Activity
                    handler.obtainMessage(PingActivity.MESSAGE,
                            bytes, -1, buffer[bufferNumber%4]).sendToTarget();
                    bufferNumber++;
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
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
        }
    }

}
