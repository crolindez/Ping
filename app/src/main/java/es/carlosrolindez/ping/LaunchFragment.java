package es.carlosrolindez.ping;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;



public class LaunchFragment extends Fragment {

    private static final String TAG = "LaunchFragment";

    private OnDeviceSelected mListener;

    private ArrayAdapter<String> messageListAdapter = null;
    private ArrayList<String> messageList;
    private ArrayAdapter<String> peerListAdapter = null;
    private ArrayList<String> peerList;

    private View mContentView = null;

    public LaunchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.fragment_launch, container, false);

        getActivity().getActionBar().show();

        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.e(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.e(TAG, "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            uiOptions &= (~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            uiOptions &= (~View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= 18) {
            uiOptions &=(~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);



        return mContentView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDeviceSelected) {
            mListener = (OnDeviceSelected) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDeviceSelected");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView messageView = (ListView) mContentView.findViewById(R.id.messages);
        messageList = new ArrayList<>();
        messageListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, messageList);
        messageView.setAdapter(messageListAdapter);

        ListView peerView = (ListView) mContentView.findViewById(R.id.peers);
        peerList = new ArrayList<>();
        peerListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, peerList);
        peerView.setAdapter(peerListAdapter);
        peerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
            mListener.connect(peerList.get(position));

            }

        });

    }



    public void addMessage(String message) {
        messageList.add(0, message);
        messageListAdapter.notifyDataSetChanged();
    }

    public void showList(WifiP2pDeviceList list) {
        peerList.clear();
        for (WifiP2pDevice device:list.getDeviceList()) {
            peerList.add(0,device.deviceName);
            //peerList.add(1,"" + device.status);
        }
        peerListAdapter.notifyDataSetChanged();

        messageList.add(0, "New device list");
        messageListAdapter.notifyDataSetChanged();

    //    LinearLayout window =(LinearLayout) mContentView.findViewById(R.id.game_zone);

     //   height = window.getHeight();
     //   width = window.getWidth();
        //       ViewGroup.LayoutParams params = window.getLayoutParams();
        //       height = params.height;
        //       width = params.width;
     //   xGU = width / XGU;
     //   yGU = height / YGU;

  //      messageList.add(0, "width = " + width);
  //      messageList.add(0, "height = " + height);
  //      messageList.add(0, "xGU = " + xGU);
  //      messageList.add(0, "yGU = " + yGU);
  //      messageListAdapter.notifyDataSetChanged();



    }


    public interface OnDeviceSelected {
        void connect(String name);
    }



}
