package com.polidea.rxandroidbledemoscanning;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleScanResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ListFragment extends Fragment {

    private final ArrayList<RxBleDevice> keysList = new ArrayList<>();
    private final LinkedHashMap<RxBleDevice, Model> results = new LinkedHashMap<>();
    private ListView listView;
    private Subscription refreshListSubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new ListView(inflater.getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view;
        listView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        listView.setAdapter(null);
        listView = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshListSubscription = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                );
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshListSubscription.unsubscribe();
        refreshListSubscription = null;
    }

    public void put(RxBleScanResult scanResult) {
        final RxBleDevice bleDevice = scanResult.getBleDevice();
        Model model = results.get(bleDevice);
        if (model == null) {
            model = new Model();
            model.address = bleDevice.getMacAddress();
            model.name = bleDevice.getName();
            results.put(bleDevice, model);
            keysList.add(bleDevice);
        }
        model.rssi = scanResult.getRssi();
        model.lastSeenTimestamp = System.currentTimeMillis();
    }

    private static class Model {

        String address;

        String name;

        int rssi;

        long lastSeenTimestamp;
    }

    private final BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return results.size();
        }

        @Override
        public Model getItem(int position) {
            return results.get(keysList.get(position));
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_row, null);
            }
            Model model = getItem(position);
            ListRow listRow = (ListRow) convertView;
            listRow.setNameText(String.format("%s\n%s", model.address, model.name));
            listRow.setRssiText(String.valueOf(model.rssi));
            listRow.setLastSeenText(lastSeenText(model.lastSeenTimestamp));
            return listRow;
        }
    };

    private String lastSeenText(long lastSeenTimestamp) {
        final long difference = System.currentTimeMillis() - lastSeenTimestamp;
        return String.format("%d s", TimeUnit.MILLISECONDS.toSeconds(difference));
    }
}
