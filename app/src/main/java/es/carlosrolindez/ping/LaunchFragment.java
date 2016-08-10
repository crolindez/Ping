package es.carlosrolindez.ping;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
            mListener.connect(position);

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
    }


    public interface OnDeviceSelected {
        void connect(int index);
    }



}
