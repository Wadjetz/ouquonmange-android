package fr.oqom.ouquonmange.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import fr.oqom.ouquonmange.R;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.utils.Callback;

public class InterestPointsAdapter extends RecyclerView.Adapter<InterestPointsAdapter.ViewHolder> {

    private final List<InterestPoint> interestPoints;
    private final Callback<InterestPoint> callbackGroup;
    private final Callback<InterestPoint> callbackDetails;
    private final Callback<InterestPoint> callbackVote;
    private Context context;

    public InterestPointsAdapter(Context context, List<InterestPoint> interestPoints, Callback<InterestPoint> callbackGroup, Callback<InterestPoint> callbackDetails, Callback<InterestPoint> callbackVote) {
        this.context = context;
        this.interestPoints = interestPoints;
        this.callbackGroup = callbackGroup;
        this.callbackDetails = callbackDetails;
        this.callbackVote = callbackVote;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interest_point_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        InterestPoint interestPoint = interestPoints.get(position);
        holder.interestPointName.setText(interestPoint.name);
        holder.interestPointAddress.setText(interestPoint.address);
        holder.interestPoint = interestPoint;
        String buttonText = (interestPoint.isJoin ? context.getString(R.string.quit_group) : context.getString(R.string.join_group))+" ("+interestPoint.members+")";
        holder.joinAction.setText(buttonText);
        String buttonVote = (interestPoint.isVote ? context.getString(R.string.unvote_group) : context.getString(R.string.vote_group))+" ("+interestPoint.votes+")";
        holder.voteAction.setText(buttonVote);

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
        public Button joinAction;
        public Button detailsAction;
        public Button voteAction;

        public ViewHolder(View v) {
            super(v);
            interestPointCardView = (CardView) v.findViewById(R.id.interest_point_cardView);
            interestPointName = (TextView) v.findViewById(R.id.interest_point_name);
            interestPointAddress = (TextView) v.findViewById(R.id.interest_point_address);
            joinAction = (Button) v.findViewById(R.id.action_join_group);
            detailsAction = (Button) v.findViewById(R.id.action_details);
            voteAction = (Button) v.findViewById(R.id.action_vote_group);
            joinAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callbackGroup.apply(interestPoint);
                }
            });

            detailsAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callbackDetails.apply(interestPoint);
                }
            });
            voteAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callbackVote.apply(interestPoint);
                }
            });
        }
    }
}
