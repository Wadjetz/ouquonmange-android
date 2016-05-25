package fr.oqom.ouquonmange.models;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import fr.oqom.ouquonmange.R;
import fr.oqom.ouquonmange.utils.Callback;

/**
 * Created by hedhili on 25/05/2016.
 */
public class ListEventAdapter extends RecyclerView.Adapter<ListEventAdapter.ListEventViewHolder> {
    private List<EventOfCommunity> eventsOfCommunities;
    private final Callback<EventOfCommunity> callback;

    public ListEventAdapter(List<EventOfCommunity> eventsOfCommunities,final Callback<EventOfCommunity> callback) {
        this.eventsOfCommunities = eventsOfCommunities;
        this.callback = callback;
    }

    @Override
    public ListEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item,parent,false);
        ListEventViewHolder levh = new ListEventViewHolder(v);
        return levh;
    }

    @Override
    public void onBindViewHolder(ListEventViewHolder holder, int position) {
        EventOfCommunity eventOfCommunity = eventsOfCommunities.get(position);
        holder.eventNameTextView.setText(eventOfCommunity.name);
        holder.eventDescriptionTextView.setText(eventOfCommunity.description);
        holder.eventOfCommunity = eventOfCommunity;
    }

    @Override
    public int getItemCount() {
        return eventsOfCommunities.size();
    }

    public class ListEventViewHolder extends RecyclerView.ViewHolder {
        public CardView eventCardView;
        public TextView eventNameTextView;
        public TextView eventDescriptionTextView;
        public Button displayEventbutton;
        public EventOfCommunity eventOfCommunity;

        public ListEventViewHolder(View v) {
            super(v);
            eventCardView = (CardView) v.findViewById(R.id.event_cardView);
            eventNameTextView = (TextView) v.findViewById(R.id.event_name);
            eventDescriptionTextView = (TextView) v.findViewById(R.id.event_description);
            displayEventbutton = (Button) v.findViewById(R.id.event_button);
            displayEventbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.apply(eventOfCommunity);
                }
            });
        }
    }
}