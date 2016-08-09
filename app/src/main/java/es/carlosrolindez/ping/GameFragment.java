package es.carlosrolindez.ping;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



public class GameFragment extends Fragment {

    public boolean connectionOwner;
    private View mContentView = null;

    private OnGameFragmentInteractionListener mListener;

    public GameFragment() {
        connectionOwner = false;
    }

    public void changeOwnership(boolean ownership) {
        connectionOwner = ownership;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.fragment_game, container, false);
        TextView text = (TextView) mContentView.findViewById(R.id.title);
        String player;
        if (connectionOwner)
            player = "Player 1";
        else
            player = "Player 2";
        text.setText(player);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnGameFragmentInteractionListener {
        void closeConnection(int code);
    }
}
