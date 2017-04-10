package ru.supervital.test.itsc.adapter;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import itsc.test.supervital.ru.R;


/**
 * Created by Vitaly Oantsa on 10.04.2017.
 */

public class HistoryListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = HistoryListRecyclerAdapter.class.getSimpleName();

    AppCompatActivity activity;
    private ArrayList<Steps> steps;

    public HistoryListRecyclerAdapter(ArrayList<Steps> steps, AppCompatActivity activity) {
        this.steps = steps;
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_steps, parent, false);
        return new ViewHolderTrack(v);
    }

    ViewHolderTrack holderTrack;
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        holderTrack = (ViewHolderTrack) holder;
        final Steps steps = this.steps.get(position);
        String date = steps.getDate(),
                count = steps.getCount().toString();

        holderTrack.date.setText(date);
        holderTrack.count.setText(count);
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    public static class ViewHolderTrack extends RecyclerView.ViewHolder {
        public TextView date, count;

        public ViewHolderTrack(View v) {
            super(v);
            date = (TextView) v.findViewById(R.id.date);
            count = (TextView) v.findViewById(R.id.count);
        }
    }

}