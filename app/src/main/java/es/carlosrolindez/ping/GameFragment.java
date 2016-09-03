package es.carlosrolindez.ping;

import android.app.ActionBar;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.nio.charset.Charset;


public class GameFragment extends Fragment {

    private static final String TAG = "GameFragment";

    private boolean connectionOwner;

    private GameCommManager gameManager = null;

    private OnGameFragmentInteractionListener mListener;

    private TextView leftScoreText;
    private TextView rightScoreText;

    private String message;

    private final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    // MESSAGE HEADERS
    private final String BALL = "BALL";
    private final String PLAYER = "PLAYER";
    private final String INIT = "INIT";
    private final String GOAL = "GOAL";

    private final int FPS = 15;

    private PingGameClass pingGame;

    private Handler handlerUp = null;
    private Handler handlerDown = null;
    private Runnable actionUp;
    private Runnable actionDown;

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

        leftScoreText = (TextView) mContentView.findViewById(R.id.leftScore);
        rightScoreText = (TextView) mContentView.findViewById(R.id.rightScore);


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
        ImageButton buttonUp = (ImageButton) mContentView.findViewById(R.id.button_up);
        ImageButton buttonDown = (ImageButton) mContentView.findViewById(R.id.button_down);

        pingGame = new PingGameClass(ball, playerLeft, playerRight, topbar, bottombar, leftScoreText, rightScoreText);


        RelativeLayout window = (RelativeLayout) mContentView.findViewById(R.id.game_zone);
        window.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // Preventing extra work because method will be called many times.
                pingGame.updateWindowConstants(top,bottom,left,right);


            }
        });


        if (connectionOwner) {

        } else {
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

        actionUp = new Runnable() {
            @Override
            public void run() {
                if (pingGame.movePlayerUp()) {
                    message = PLAYER + pingGame.playerMessage();
                    gameManager.write(message.getBytes(Charset.defaultCharset()));
                }
                if (handlerUp!=null)
                    handlerUp.postDelayed(this,1000/FPS);
            }
        };

        actionDown = new Runnable() {
            @Override
            public void run() {
                if (pingGame.movePlayerDown()) {
                    message = PLAYER + pingGame.playerMessage();
                    gameManager.write(message.getBytes(Charset.defaultCharset()));
                }
                if (handlerDown!=null)
                    handlerDown.postDelayed(this,1000/FPS);
            }
        };

        buttonUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (handlerUp != null) return true;
                        handlerUp = new Handler();
                        handlerUp.postDelayed(actionUp,1000/FPS);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (handlerUp == null) return true;
                        handlerUp.removeCallbacks(actionUp);
                        handlerUp = null;
                        return true;
                }
                return false;
            }


        });

        buttonDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (handlerDown != null) return true;
                        handlerDown = new Handler();
                        handlerDown.postDelayed(actionDown,1000/FPS);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (handlerDown == null) return true;
                        handlerDown.removeCallbacks(actionDown);
                        handlerDown = null;
                        return true;
                }
                return false;
            }



        });
        return mContentView;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener.closeConnection(0);
        pingGame.setState(PingGameClass.END);
        mListener = null;
    }


    public void pushMessage(String mes) {
        String arg[] = mes.split(" ");
        int index = 0;
        while (index < arg.length) {
            if (arg[index].equals(INIT)) {
                new Thread(new GameRunnable(true)).start();
                message = BALL + pingGame.ballMessage() ;
                gameManager.write(message.getBytes(Charset.defaultCharset()));
                index ++;

            } else if ((arg[index].equals(BALL)) && ((arg.length-index)>=5 ) ){
                pingGame.setState(PingGameClass.PLAYING);
                pingGame.setBall(   Float.parseFloat(arg[index+1]),
                                    Float.parseFloat(arg[index+2]),
                                    Float.parseFloat(arg[index+3]),
                                    Float.parseFloat(arg[index+4]) );
                tg.startTone(ToneGenerator.TONE_DTMF_8,50);
                index +=5;

            } else if ((arg[index].equals(PLAYER)) && ((arg.length-index)>=2 )) {
                pingGame.setPlayerRight( Float.parseFloat(arg[index+1]));
                index +=2;
            } else if (arg[index].equals(GOAL))  {
                pingGame.rightGoal();
                pingGame.reset();
                tg.startTone(ToneGenerator.TONE_DTMF_2, 250);
                pingGame.setState(PingGameClass.GOAL);
                index ++;
            } else {
                index++; //rubbish
            }

        }
    }

    public interface OnGameFragmentInteractionListener {
        void closeConnection(int code);

    }

    public class GameRunnable implements Runnable {

        boolean owner;
        long gameTimer;

        GameRunnable(boolean owner) {
            this.owner = owner;
        }

        public void run() {


            final long tbs = (1000 / FPS);   // time (in milliseconds) between samples


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

                gameTimer= System.currentTimeMillis();

                while (true) {
                    if (pingGame.getState() == PingGameClass.PLAYING) {
                        Long timer = System.currentTimeMillis();
                        if (timer > (gameTimer + tbs)) {
                            gameTimer += tbs;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int event = pingGame.moveBall();
                                    if (event == PingGameClass.BOUNCE_MOVEMENT) {
                                        tg.startTone(ToneGenerator.TONE_DTMF_8, 50);
                                        message = BALL + pingGame.ballMessage();
                                        gameManager.write(message.getBytes(Charset.defaultCharset()));
                                    } else if (event == PingGameClass.GOAL_MOVEMENT) {
                                        tg.startTone(ToneGenerator.TONE_DTMF_2, 250);
                                        message = GOAL + " ";
                                        gameManager.write(message.getBytes(Charset.defaultCharset()));
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        message = BALL + pingGame.ballMessage();
                                        gameManager.write(message.getBytes(Charset.defaultCharset()));
                                        gameTimer= System.currentTimeMillis();
                                    }
                                }
                            });
                        } else {
                            gameTimer= System.currentTimeMillis();
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
