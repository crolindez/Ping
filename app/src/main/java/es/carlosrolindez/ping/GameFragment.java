package es.carlosrolindez.ping;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
