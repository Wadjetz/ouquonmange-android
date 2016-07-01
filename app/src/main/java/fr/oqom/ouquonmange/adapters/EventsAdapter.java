package fr.oqom.ouquonmange.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import fr.oqom.ouquonmange.R;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Event;
import fr.oqom.ouquonmange.utils.Callback;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ListEventViewHolder> {

    private List<Event> eventsOfCommunities;
    private final Callback<Event> callback;

    public EventsAdapter(List<Event> eventsOfCommunities, final Callback<Event> callback) {
        this.eventsOfCommunities = eventsOfCommunities;
        this.callback = callback;
    }

    @Override
    public ListEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);
        ListEventViewHolder vh = new ListEventViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ListEventViewHolder holder, int position) {
        Event event = eventsOfCommunities.get(position);
        holder.eventNameTextView.setText(event.name);
        holder.eventDescriptionTextView.setText(event.description);
        holder.dateStart.setText(Constants.timeFormat.format(event.date_start.toDate()));
        holder.dateEnd.setText(Constants.timeFormat.format(event.date_start.toDate()));
        holder.event = event;
    }

    @Override
    public int getItemCount() {
        return eventsOfCommunities.size();
    }

    public class ListEventViewHolder extends RecyclerView.ViewHolder {
        public CardView eventCardView;
        public TextView eventNameTextView;
        public TextView eventDescriptionTextView;
        public TextView dateStart;
        public TextView dateEnd;

        public Event event;

        public ListEventViewHolder(View v) {
            super(v);
            eventCardView = (CardView) v.findViewById(R.id.event_cardView);
            eventNameTextView = (TextView) v.findViewById(R.id.event_name);
            eventDescriptionTextView = (TextView) v.findViewById(R.id.event_description);
            dateStart = (TextView) v.findViewById(R.id.event_date_start);
            dateEnd = (TextView) v.findViewById(R.id.event_date_end);

            eventCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.apply(event);
                }
            });

        }
    }
}