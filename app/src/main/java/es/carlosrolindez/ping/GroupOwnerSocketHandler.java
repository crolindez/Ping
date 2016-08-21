
package es.carlosrolindez.ping;

import android.os.Handler;

import java.io.IOException;
import java.net.ServerSocket;

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
        GameCommManager chat;
        ServerSocket socket = null;
        try {
            socket = new ServerSocket( PingActivity.SERVER_PORT);
            chat = new GameCommManager(socket.accept(), handler);
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
