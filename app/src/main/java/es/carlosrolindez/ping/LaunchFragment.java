package es.carlosrolindez.ping;

import android.app.ActionBar;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class LaunchFragment extends Fragment {

    private static final String TAG = "LaunchFragment";

    private EditText name;
    private Button easyButton;
    private Button mediumButton;
    private Button hardButton;
    private Button expertButton;

    private String playerName;

    private OnLaunchUpdate mListener;

    public LaunchFragment() {
        // Required empty public constructor
    }

    public void setLaunchFragment(String name) {
        playerName = name;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLaunchUpdate) {
            mListener = (OnLaunchUpdate) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLaunchUpdate");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mContentView = inflater.inflate(R.layout.fragment_launch, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar ab = getActivity().getActionBar();
        if (ab!=null) ab.show();

        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();

        uiOptions &= (~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions &= (~View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions &=(~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);

        name = (EditText) mContentView.findViewById(R.id.player_name);
        name.setText(playerName);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.e("beforeTextChanged",s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("onTextChanged",s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e("afterTextChanged",s.toString());
                mListener.savePlayerName(name.getText().toString());
            }
        });

        easyButton = (Button) mContentView.findViewById(R.id.button_easy);
        easyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.startSelection(Constants.LEVEL_EASY);
            }
        });
        mediumButton = (Button) mContentView.findViewById(R.id.button_medium);
        mediumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.startSelection(Constants.LEVEL_MEDIUM);
            }
        });
        hardButton = (Button) mContentView.findViewById(R.id.button_hard);
        hardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.startSelection(Constants.LEVEL_HARD);
            }
        });
        expertButton = (Button) mContentView.findViewById(R.id.button_expert);
        expertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.startSelection(Constants.LEVEL_EXPERT);
            }
        });

        return mContentView;
    }




    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    public interface OnLaunchUpdate {
        void savePlayerName(String name);
        void startSelection(int level);
    }


}
