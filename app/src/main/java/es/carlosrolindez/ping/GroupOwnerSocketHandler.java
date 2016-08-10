
package es.carlosrolindez.ping;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The implementation of a ServerSocket handler. This is used by the wifi p2p
 * group owner.
 */
public class GroupOwnerSocketHandler extends Thread {

    private static final String TAG = "GroupOwnerSocketHandler";
    private Handler handler;

 //   private final int THREAD_COUNT = 10;

    public GroupOwnerSocketHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        GameManager chat;
        ServerSocket socket = null;
        try {
            socket = new ServerSocket( PingActivity.SERVER_PORT);
            chat = new GameManager(socket.accept(), handler);
            new Thread(chat).start();
        } catch (IOException e) {
            try {
                if (socket != null && !socket.isClosed())
                    socket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            e.printStackTrace();
        }
    }


}
