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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.nio.charset.Charset;


public class GameFragment extends Fragment {

    private static final String TAG = "GameFragment";

    private boolean connectionOwner;
    private View mContentView = null;
    private GameManager gameManager = null;

    private OnGameFragmentInteractionListener mListener;

    private TextView title;
    private TextView message;
    private String player;

    private int width, height;
    private double xGU, yGU; // game units
    private final double XGU = 200;
    private final double YGU = 100;
    private int iniX, iniY;
    public GameFragment() {
        connectionOwner = false;
    }

    public void setGameFragment(GameManager manager, boolean ownership) {
        connectionOwner = ownership;
        gameManager = manager;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.fragment_game, container, false);
        title = (TextView) mContentView.findViewById(R.id.title);
        message = (TextView) mContentView.findViewById(R.id.message_player);


        if (connectionOwner)
            player = "Player 1";
        else
            player = "Player 2";
        title.setText(player);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String message = "Hello from " + player;
                gameManager.write(message.getBytes(Charset.defaultCharset()));
            }
        }, 500);

        int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 18) {
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        RelativeLayout window = (RelativeLayout) mContentView.findViewById(R.id.game_zone);
        window.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // Preventing extra work because method will be called many times.
                if(height == (bottom - top))
                    return;

                height = (bottom - top);
                width = right - left;
                iniY = top;
                iniX = left;
                xGU = width / XGU;
                yGU = height / YGU;

                ImageView ball = (ImageView) mContentView.findViewById(R.id.ball);
                ball.setLayoutParams(new RelativeLayout.LayoutParams((int)(5*xGU), (int) (5*yGU)));
                ball.setX((float)(98*xGU));
                ball.setY((float)(48*yGU));

                ImageView player1 = (ImageView) mContentView.findViewById(R.id.player1);
                player1.setLayoutParams(new RelativeLayout.LayoutParams((int)(3*xGU), (int) (25*xGU)));
                player1.setX((float)(5*xGU));
                player1.setY((float)(height/2 - 10*yGU));

                ImageView player2 = (ImageView) mContentView.findViewById(R.id.player2);
                player2.setLayoutParams(new RelativeLayout.LayoutParams((int)(3*xGU), (int) (25*xGU)));
                player2.setX((float)(193*xGU));
                player2.setY((float)(height/2 - 10*yGU));

                ImageView topbar = (ImageView) mContentView.findViewById(R.id.topbar);
                topbar.setLayoutParams(new RelativeLayout.LayoutParams((int)(190*xGU), (int) (2*xGU)));
                topbar.setX((float)(5*xGU));
                topbar.setY((float)(2*yGU));

                ImageView bottombar = (ImageView) mContentView.findViewById(R.id.bottombar);
                bottombar.setLayoutParams(new RelativeLayout.LayoutParams((int)(190*xGU), (int) (2*xGU)));
                bottombar.setX((float)(5*xGU));
                bottombar.setY((float)(96*yGU));

            }
        });


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

    public void pushMessage(String mes) {
        if (message!=null)
            message.setText(mes);
    }

    public interface OnGameFragmentInteractionListener {
        void closeConnection(int code);

    }
}
