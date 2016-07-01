package fr.oqom.ouquonmange.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cardinalsolutions.sectioned_adapter.SectionedAdapter;

import java.util.List;

import fr.oqom.ouquonmange.R;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Event;
import fr.oqom.ouquonmange.utils.Callback;

public class EventsSectionedAdapter extends SectionedAdapter<Event> {

    private Callback<Event> callback;

    public EventsSectionedAdapter(List<Event> events, Callback<Event> callback) {
        this.setItemList(events);
        this.setCustomHeaderLayout(R.layout.recycler_header_events);
        this.callback = callback;
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, Event event, int viewType) {
        EventsViewHolder h = (EventsViewHolder) holder;
        h.eventNameTextView.setText(event.name);
        h.eventNameTextView.setText(event.name);
        h.eventDescriptionTextView.setText(event.description);
        h.dateStart.setText(Constants.timeFormat.format(event.date_start.toDate()));
        h.dateEnd.setText(Constants.timeFormat.format(event.date_end.toDate()));
        h.event = event;

    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventsViewHolder(view);
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder {
        public CardView eventCardView;
        public TextView eventNameTextView;
        public TextView eventDescriptionTextView;
        public TextView dateStart;
        public TextView dateEnd;
        public Event event;

        public EventsViewHolder(View v) {
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
