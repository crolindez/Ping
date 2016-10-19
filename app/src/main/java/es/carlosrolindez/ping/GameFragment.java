package es.carlosrolindez.ping;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class GameFragment extends Fragment {

    private static final String TAG = "GameFragment";

    private String playerName;
    private int mLevel;

    private GameCommManager gameManager = null;
    private Handler comHandler;
    private GameRunnable gameRunnable = null;

    private String message;

    private final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

    // MESSAGE HEADERS
    private final String BALL = "BALL";
    private final String PLAYER = "PLAYER";
    private final String INIT = "INIT";
    private final String GOAL = "GOAL";

    private final int FPS = 20;

    private PingGameClass pingGame;

    private Handler handlerUp = null;
    private Handler handlerDown = null;
    private Runnable actionUp;
    private Runnable actionDown;

    private ImageView ball;
    private ImageView playerLeft;
    private ImageView playerRight;
    private ImageView topbar;
    private ImageView bottombar;
    private ImageView obstacle1;
    private ImageView obstacle2;
    private ImageButton buttonUp;
    private ImageButton buttonDown;
    private TextView leftScoreText;
    private TextView colonScoreText;
    private TextView rightScoreText;
    private TextView playerLeftText;
    private TextView playerRightText;



    public void setGameFragment(GameCommManager manager, String name, Handler handler, int level) {
        gameManager = manager;
        playerName = name;
        comHandler = handler;
        mLevel = level;
    }

    public Handler getHandler() {
        return comHandler;
    }


    public GameCommManager getGameCommManager() {
        return gameManager;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mContentView = inflater.inflate(R.layout.fragment_game, container, false);

        setRetainInstance(true);

        ActionBar ab = getActivity().getActionBar();
        if (ab!=null) ab.hide();

        int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();
        uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }


        getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        ball = (ImageView) mContentView.findViewById(R.id.ball);
        playerLeft = (ImageView) mContentView.findViewById(R.id.player1);
        playerRight = (ImageView) mContentView.findViewById(R.id.player2);
        topbar = (ImageView) mContentView.findViewById(R.id.topbar);
        bottombar = (ImageView) mContentView.findViewById(R.id.bottombar);
        obstacle1 = (ImageView) mContentView.findViewById(R.id.obstacle1);
        obstacle2 = (ImageView) mContentView.findViewById(R.id.obstacle2);
        buttonUp = (ImageButton) mContentView.findViewById(R.id.button_up);
        buttonDown = (ImageButton) mContentView.findViewById(R.id.button_down);
        leftScoreText = (TextView) mContentView.findViewById(R.id.leftScore);
        colonScoreText = (TextView) mContentView.findViewById(R.id.colon);
        rightScoreText = (TextView) mContentView.findViewById(R.id.rightScore);
        playerLeftText = (TextView) mContentView.findViewById(R.id.player_left);
        playerRightText = (TextView) mContentView.findViewById(R.id.player_right);

        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "seven_segments.ttf");
        leftScoreText.setTypeface(myTypeface);
        rightScoreText.setTypeface(myTypeface);
        colonScoreText.setTypeface(myTypeface);


        playerLeftText.setText(playerName);
        playerRightText.setText("Player 2");


        pingGame = new PingGameClass(ball, playerLeft, playerRight, topbar, bottombar, leftScoreText, rightScoreText,
                                        obstacle1, obstacle2);



        RelativeLayout window = (RelativeLayout) mContentView.findViewById(R.id.game_zone);
        window.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // Preventing extra work because method will be called many times.
                pingGame.updateWindowConstants(top,bottom,left,right);


            }
        });


        if (gameRunnable == null) {
            if (gameManager.getOwnership()) {
                gameRunnable = new GameRunnable(false);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        message = INIT + " " + playerName + " " + mLevel;
                        gameManager.write(message);
                        new Thread(gameRunnable).start();
                    }
                }, 500);
            }
        }

        actionUp = new Runnable() {
            @Override
            public void run() {
                if (pingGame.movePlayerUp()) {
                    message = PLAYER + pingGame.playerMessage();
                    gameManager.write(message);
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
                    gameManager.write(message);
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

    public void setLevel(int level) {
        int resource;
        int color;
        switch (level) {
            case Constants.LEVEL_EXPERT:
                resource = R.drawable.brick_expert;
                color = ContextCompat.getColor(getActivity(), R.color.colorExpert);
                buttonUp.setImageResource(R.drawable.up_expert);
                buttonDown.setImageResource(R.drawable.down_expert);
                obstacle1.setVisibility(View.VISIBLE);
                obstacle2.setVisibility(View.VISIBLE);
                break;
            case Constants.LEVEL_HARD:
                resource = R.drawable.brick_hard;
                color = ContextCompat.getColor(getActivity(), R.color.colorHard);
                buttonUp.setImageResource(R.drawable.up_hard);
                buttonDown.setImageResource(R.drawable.down_hard);
                obstacle1.setVisibility(View.VISIBLE);
                obstacle2.setVisibility(View.VISIBLE);
                break;
            case Constants.LEVEL_MEDIUM:
                resource = R.drawable.brick_medium;
                color = ContextCompat.getColor(getActivity(), R.color.colorMedium);
                buttonUp.setImageResource(R.drawable.up_medium);
                buttonDown.setImageResource(R.drawable.down_medium);
                obstacle1.setVisibility(View.INVISIBLE);
                obstacle2.setVisibility(View.INVISIBLE);
                break;
            case Constants.LEVEL_EASY:
            default:
                resource = R.drawable.brick_easy;
                color = ContextCompat.getColor(getActivity(), R.color.colorEasy);
                buttonUp.setImageResource(R.drawable.up_easy);
                buttonDown.setImageResource(R.drawable.down_easy);
                obstacle1.setVisibility(View.INVISIBLE);
                obstacle2.setVisibility(View.INVISIBLE);
        }

        ball.setImageResource(resource);
        playerLeft.setImageResource(resource);
        playerRight.setImageResource(resource);
        topbar.setImageResource(resource);
        bottombar.setImageResource(resource);
        obstacle1.setImageResource(resource);
        obstacle2.setImageResource(resource);
        leftScoreText.setTextColor(color);
        colonScoreText.setTextColor(color);
        rightScoreText.setTextColor(color);

        pingGame.setLevel(level);
    }


    public void pushMessage(String mes) {
        String arg[] = mes.split(" ");
        int index = 0;
        while (index < arg.length) {

            if ((arg[index].equals(INIT)) && ((arg.length - index) >= 3)) {
                playerRightText.setText(arg[index + 1]);
                mLevel = Integer.parseInt(arg[index + 2]);
                setLevel(mLevel);
                index++;
                if (gameRunnable == null) {
                    if (!gameManager.getOwnership()) {
                        message = INIT + " " + playerName + " " + mLevel;
                        gameManager.write(message);
                        gameRunnable = new GameRunnable(true);
                        new Thread(gameRunnable).start();

                    }
                }
            } else if ((arg[index].equals(BALL)) && ((arg.length - index) >= 5)) {
                pingGame.setState(PingGameClass.PLAYING);
                pingGame.setBall(Float.parseFloat(arg[index + 1]),
                        Float.parseFloat(arg[index + 2]),
                        Float.parseFloat(arg[index + 3]),
                        Float.parseFloat(arg[index + 4]));
                tg.startTone(ToneGenerator.TONE_DTMF_8, 50);
                index += 5;

            } else if ((arg[index].equals(PLAYER)) && ((arg.length - index) >= 2)) {
                pingGame.setPlayerRight(Float.parseFloat(arg[index + 1]));
                index += 2;
            } else if (arg[index].equals(GOAL)) {
                pingGame.setState(PingGameClass.GOAL);
                pingGame.leftGoal();
                pingGame.reset();
                tg.startTone(ToneGenerator.TONE_DTMF_2, 250);

                index++;
            } else {
                index++; //rubbish
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pingGame.setState(PingGameClass.END);
    }

    public class GameRunnable implements Runnable {

        final boolean owner;
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
                Thread.sleep(1000);

                if (owner) {
                    pingGame.setState(PingGameClass.PLAYING);
                    message = BALL + pingGame.ballMessage();
                    if (gameManager!=null) gameManager.write(message);
                }

                gameTimer= System.currentTimeMillis();

                while (true) {
                    if (pingGame.getState() == PingGameClass.PLAYING) {
                        Long timer = System.currentTimeMillis();
                        if (timer > (gameTimer + tbs)) {
                            gameTimer += tbs;
                            Activity activity = getActivity();
                            if (activity==null) break;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    int event = pingGame.moveBall();
                                    switch (event) {
                                        case PingGameClass.BOUNCE_PLAYER:
                                            message = BALL + pingGame.ballMessage();
                                            if (gameManager!=null) gameManager.write(message);
                                            tg.startTone(ToneGenerator.TONE_DTMF_8, 50);
                                            break;
                                        case PingGameClass.BOUNCE_WALL:
                                            tg.startTone(ToneGenerator.TONE_DTMF_8, 50);
                                            break;
                                        case PingGameClass.GOAL_MOVEMENT:
                                            message = GOAL + " ";
                                            if (gameManager!=null) gameManager.write(message);
                                            pingGame.setState(PingGameClass.GETTING_READY);
                                            tg.startTone(ToneGenerator.TONE_DTMF_2, 250);
                                            break;

                                    }

                                }
                            });
                        }
                    } else if (pingGame.getState() == PingGameClass.GETTING_READY) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        message = BALL + pingGame.ballMessage();
                        if (gameManager!=null) gameManager.write(message);
                        gameTimer= System.currentTimeMillis();
                        pingGame.setState(PingGameClass.PLAYING);
                    } else if  ( (pingGame.getState() == PingGameClass.GOAL) ||(pingGame.getState() == PingGameClass.START) ) {
                        gameTimer= System.currentTimeMillis();
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
