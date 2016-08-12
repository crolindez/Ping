package es.carlosrolindez.ping;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;



public class LaunchFragment extends Fragment {

    private static final String TAG = "LaunchFragment";

    private OnDeviceSelected mListener;

    private ArrayAdapter<String> messageListAdapter = null;
    private ArrayList<String> messageList;
    private ArrayAdapter<String> peerListAdapter = null;
    private ArrayList<String> peerList;

    private View mContentView = null;

    private int width, height;
    private double xGU, yGU; // game units
    private final double XGU = 200;
    private final double YGU = 100;
    private int iniX, iniY;

    public LaunchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentView = inflater.inflate(R.layout.fragment_launch, container, false);

        LinearLayout window = (LinearLayout) mContentView.findViewById(R.id.game_zone);
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

        messageList.add(0, "width = " + width);
        messageList.add(0, "height = " + height);
        messageList.add(0, "xGU = " + xGU);
        messageList.add(0, "yGU = " + yGU);
        messageListAdapter.notifyDataSetChanged();



    }


    public interface OnDeviceSelected {
        void connect(String name);
    }



}
