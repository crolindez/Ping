package es.carlosrolindez.ping;

import android.app.ActionBar;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.nio.charset.Charset;


public class GameFragment extends Fragment {

    private static final String TAG = "GameFragment";

    private boolean connectionOwner;

    private GameCommManager gameManager = null;

    private OnGameFragmentInteractionListener mListener;

    private TextView title;
    private String player;

    private String message;

    private final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    // MESSAGE HEADERS
    private final String BALL = "BALL";
    private final String PLAYER = "PLAYER";
    private final String INIT = "INIT";
    private final String GOAL = "GOAL";

    private PingGameClass pingGame;

    public GameFragment() {
        connectionOwner = false;
    }

    public void setGameFragment(GameCommManager manager, boolean ownership) {
        connectionOwner = ownership;
        gameManager = manager;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mContentView = inflater.inflate(R.layout.fragment_game, container, false);
        ActionBar ab = getActivity().getActionBar();
        if (ab!=null) ab.hide();

        mListener = (OnGameFragmentInteractionListener) getActivity();

        title = (TextView) mContentView.findViewById(R.id.title);
        

        title.setText(player);

        int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        ImageView ball = (ImageView) mContentView.findViewById(R.id.ball);
        ImageView playerLeft = (ImageView) mContentView.findViewById(R.id.player1);
        ImageView playerRight = (ImageView) mContentView.findViewById(R.id.player2);
        ImageView topbar = (ImageView) mContentView.findViewById(R.id.topbar);
        ImageView bottombar = (ImageView) mContentView.findViewById(R.id.bottombar);

        pingGame = new PingGameClass(ball, playerLeft, playerRight, topbar, bottombar);


        RelativeLayout window = (RelativeLayout) mContentView.findViewById(R.id.game_zone);
        window.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // Preventing extra work because method will be called many times.
                pingGame.updateWindowConstants(top,bottom,left,right);


            }
        });


        if (connectionOwner) {
            player = "Player 1";

        } else {
            player = "Player 2";
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    message = INIT ;
                    gameManager.write(message.getBytes(Charset.defaultCharset()));
                    new Thread(new GameRunnable(false)).start();
                }
            }, 500);

        }


        return mContentView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener.closeConnection(0);
        mListener = null;
    }


    public void pushMessage(String mes) {
        String arg[] = mes.split(" ");
        Log.e(TAG,mes);
        if (arg[0].equals(INIT)) {
            new Thread(new GameRunnable(true)).start();
            message = BALL + pingGame.ballMessage() ;
            gameManager.write(message.getBytes(Charset.defaultCharset()));

        } else if ((arg[0].equals(BALL)) && (arg.length == 5)){
            pingGame.setState(PingGameClass.PLAYING);
            pingGame.setBall(   Float.parseFloat(arg[1]),
                                Float.parseFloat(arg[2]),
                                Float.parseFloat(arg[3]),
                                Float.parseFloat(arg[4]));
            tg.startTone(ToneGenerator.TONE_DTMF_8,50);

        } else if (arg[0].equals(PLAYER)) {

        } else if (arg[0].equals(GOAL)) {

        } else {
            //// TODO: 21/08/2016
        }
    }

    public interface OnGameFragmentInteractionListener {
        void closeConnection(int code);

    }

    public class GameRunnable implements Runnable {

        boolean owner;

        GameRunnable(boolean owner) {
            this.owner = owner;
        }

        public void run() {


            final long tbs = (1000 / 15);   // time (in milliseconds) between samples
            long gameTimer= System.currentTimeMillis();

            try {
                tg.startTone(ToneGenerator.TONE_DTMF_8,150);
                Thread.sleep(1000);
                tg.startTone(ToneGenerator.TONE_DTMF_8,150);
                Thread.sleep(1000);
                tg.startTone(ToneGenerator.TONE_DTMF_8,400);

                if (owner) {
                    pingGame.setState(PingGameClass.PLAYING);
                    message = BALL + pingGame.ballMessage();
                    gameManager.write(message.getBytes(Charset.defaultCharset()));
                }

                while (true) {
                    if (pingGame.getState() == PingGameClass.PLAYING) {
                        Long timer = System.currentTimeMillis();
                        if (timer > (gameTimer + tbs)) {
                            gameTimer = timer;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (pingGame.moveBall()) {
                                        tg.startTone(ToneGenerator.TONE_DTMF_8, 50);
                                        message = BALL + pingGame.ballMessage();
                                        gameManager.write(message.getBytes(Charset.defaultCharset()));
                                    }
                                }
                            });
                        }
                    }
                    if (pingGame.getState() == PingGameClass.END) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
