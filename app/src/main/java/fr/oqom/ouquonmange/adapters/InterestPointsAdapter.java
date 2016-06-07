package fr.oqom.ouquonmange.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import fr.oqom.ouquonmange.R;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.utils.Callback;

public class InterestPointsAdapter extends RecyclerView.Adapter<InterestPointsAdapter.ViewHolder> {

    private final List<InterestPoint> interestPoints;
    private final Callback<InterestPoint> callback;

    public InterestPointsAdapter(List<InterestPoint> interestPoints, Callback<InterestPoint> callback) {
        this.interestPoints = interestPoints;
        this.callback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interest_point_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        InterestPoint interestPoint = interestPoints.get(position);
        holder.interestPointName.setText(interestPoint.name);
        holder.interestPointAddress.setText(interestPoint.address);
        holder.interestPoint = interestPoint;
    }

    @Override
    public int getItemCount() {
        return interestPoints.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public InterestPoint interestPoint;
        public CardView interestPointCardView;
        public TextView interestPointName;
        public TextView interestPointAddress;

        public ViewHolder(View v) {
            super(v);
            interestPointCardView = (CardView) v.findViewById(R.id.interest_point_cardView);
            interestPointName = (TextView) v.findViewById(R.id.interest_point_name);
            interestPointAddress = (TextView) v.findViewById(R.id.interest_point_address);
            interestPointCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.apply(interestPoint);
                }
            });
        }
    }
}
