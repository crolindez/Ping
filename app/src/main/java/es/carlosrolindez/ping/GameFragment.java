package es.carlosrolindez.ping;

import android.content.Context;
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
    private View mContentView = null;
    private GameCommManager gameManager = null;

    private OnGameFragmentInteractionListener mListener;

    private TextView title;
    private String player;

    private String message;

    // MESSAGE HEADERS
    private final String BALL = "BALL";
    private final String PLAYER = "PLAYER";
    private final String INIT = "INIT";
    private final String GOAL = "GOAL";

    private Handler handler = new Handler();

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
        mContentView = inflater.inflate(R.layout.fragment_game, container, false);
        getActivity().getActionBar().hide();

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
            pingGame.reset();
        } else {
            player = "Player 2";
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    message = INIT + pingGame.initMessage();
                    gameManager.write(message.getBytes(Charset.defaultCharset()));
                }
            }, 500);
            pingGame.setState(pingGame.PLAYING);
            final int delay =50;
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    if (pingGame.getState()==pingGame.PLAYING) {
                        pingGame.moveBall();
                        h.postDelayed(this, delay);
                    }
                }
            }, delay);
        }


        return mContentView;
    }

    @Override
    public void onStop() {
        super.onStop();
        mListener.closeConnection(0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGameFragmentInteractionListener) {
            mListener = (OnGameFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    Handler h = new Handler();
    int delay = 1000; //milliseconds


    public void pushMessage(String mes) {
        String arg[] = mes.split(" ");
        Log.e(TAG,mes);
        if ((arg[0].equals(INIT)) && (arg.length == 3)) {
            pingGame.setState(pingGame.PLAYING);
            pingGame.setBall(Float.parseFloat(arg[1]),Float.parseFloat(arg[2]));
            final int delay =50;
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    if (pingGame.getState()==pingGame.PLAYING) {
                        pingGame.moveBall();
                        h.postDelayed(this, delay);
                    }
                }
            }, delay);
        } else if (arg[0].equals(BALL)) {

        } else if (arg[0].equals(PLAYER)) {

        } else if (arg[0].equals(GOAL)) {

        } else {
            //// TODO: 21/08/2016
        }
    }

    public interface OnGameFragmentInteractionListener {
        void closeConnection(int code);

    }
}
