package com.hovahs.swift.historyRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hovahs.swift.HistorySingleActivity;
import com.hovahs.swift.R;

/**
 * Created by manel on 10/10/2017.
 */

public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView rideId;
    public TextView time;
    public TextView destination;
    public HistoryViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideId = (TextView) itemView.findViewById(R.id.rideId);
        time = (TextView) itemView.findViewById(R.id.time);
        destination = (TextView) itemView.findViewById(R.id.destination);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);
    }
}
