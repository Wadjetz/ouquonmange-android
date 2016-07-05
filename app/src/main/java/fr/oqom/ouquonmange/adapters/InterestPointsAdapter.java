package fr.oqom.ouquonmange.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
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

public class InterestPointsAdapter extends RecyclerView.Adapter<InterestPointsAdapter.InterestPointViewHolder> {

    private final List<InterestPoint> interestPoints;
    private final Callback<InterestPoint> callbackGroup;
    private final Callback<InterestPoint> callbackDetails;
    private final Callback<InterestPoint> callbackVote;
    private final Callback<InterestPoint> callbackCardAction;
    private Context context;

    public InterestPointsAdapter(Context context, List<InterestPoint> interestPoints, Callback<InterestPoint> callbackGroup, Callback<InterestPoint> callbackDetails, Callback<InterestPoint> callbackVote, Callback<InterestPoint> callbackCardAction) {
        this.context = context;
        this.interestPoints = interestPoints;
        this.callbackGroup = callbackGroup;
        this.callbackDetails = callbackDetails;
        this.callbackVote = callbackVote;
        this.callbackCardAction = callbackCardAction;
    }

    @Override
    public InterestPointViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interest_point_item, parent, false);
        return new InterestPointViewHolder(v, callbackGroup, callbackDetails, callbackVote, callbackCardAction);
    }

    @Override
    public void onBindViewHolder(InterestPointViewHolder holder, int position) {
        InterestPoint interestPoint = interestPoints.get(position);
        InterestPointViewHolder.setView(context, holder, interestPoint);
    }

    @Override
    public int getItemCount() {
        return interestPoints.size();
    }

    public static class InterestPointViewHolder extends RecyclerView.ViewHolder {
        private final Callback<InterestPoint> callbackGroup;
        private final Callback<InterestPoint> callbackDetails;
        private final Callback<InterestPoint> callbackVote;
        private final Callback<InterestPoint> callbackCardAction;

        public InterestPoint interestPoint;
        public CardView interestPointCardView;
        public TextView interestPointName;
        public TextView interestPointAddress;
        public TextView interestPointGroupsNumber;
        public TextView interestPointVotesNumber;
        public Button joinAction;
        public Button detailsAction;
        public Button voteAction;

        public static void setView(Context context, InterestPointViewHolder holder, InterestPoint interestPoint) {
            holder.interestPointName.setText(interestPoint.name);
            holder.interestPointAddress.setText(interestPoint.address);
            holder.interestPoint = interestPoint;

            holder.interestPointGroupsNumber.setText(interestPoint.members + " " + context.getString(R.string.groups));
            holder.interestPointVotesNumber.setText(interestPoint.votes + " " + context.getString(R.string.votes));

            if (interestPoint.members == 0) {
                holder.interestPointGroupsNumber.setBackgroundColor(Color.WHITE);
                holder.interestPointGroupsNumber.setTextColor(ContextCompat.getColor(context, R.color.primary_text));
            } else {
                holder.interestPointGroupsNumber.setBackground(ContextCompat.getDrawable(context, R.drawable.green_badge));
                holder.interestPointGroupsNumber.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor));
            }

            if (interestPoint.votes == 0) {
                holder.interestPointVotesNumber.setBackgroundColor(Color.WHITE);
                holder.interestPointVotesNumber.setTextColor(ContextCompat.getColor(context, R.color.primary_text));
            } else {
                holder.interestPointVotesNumber.setBackground(ContextCompat.getDrawable(context, R.drawable.blue_badge));
                holder.interestPointVotesNumber.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor));
            }

            if (interestPoint.isJoin) {
                holder.joinAction.setText(context.getString(R.string.quit_group));
                holder.joinAction.setTextColor(Color.parseColor("#B71C1C"));
            } else {
                holder.joinAction.setText(context.getString(R.string.join_group));
                holder.joinAction.setTextColor(Color.parseColor("#388E3C"));
            }

            if (interestPoint.isVote) {
                holder.voteAction.setText(context.getString(R.string.unvote_group));
                holder.voteAction.setTextColor(Color.parseColor("#B71C1C"));
            } else {
                holder.voteAction.setText(context.getString(R.string.vote_group));
                holder.voteAction.setTextColor(Color.parseColor("#388E3C"));
            }
        }

        public InterestPointViewHolder(View v, Callback<InterestPoint> callbackGroup, Callback<InterestPoint> callbackDetails, Callback<InterestPoint> callbackVote, Callback<InterestPoint> callbackCardAction) {
            super(v);
            this.callbackGroup = callbackGroup;
            this.callbackDetails = callbackDetails;
            this.callbackVote = callbackVote;
            this.callbackCardAction = callbackCardAction;
            interestPointCardView = (CardView) v.findViewById(R.id.interest_point_cardView);
            interestPointName = (TextView) v.findViewById(R.id.interest_point_name);
            interestPointAddress = (TextView) v.findViewById(R.id.interest_point_address);
            interestPointGroupsNumber = (TextView) v.findViewById(R.id.interest_point_groups_number);
            interestPointVotesNumber = (TextView) v.findViewById(R.id.interest_point_votes_number);
            joinAction = (Button) v.findViewById(R.id.action_join_group);
            detailsAction = (Button) v.findViewById(R.id.action_details);
            voteAction = (Button) v.findViewById(R.id.action_vote_group);
            joinAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InterestPointViewHolder.this.callbackGroup.apply(interestPoint);
                }
            });

            detailsAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InterestPointViewHolder.this.callbackDetails.apply(interestPoint);
                }
            });
            voteAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InterestPointViewHolder.this.callbackVote.apply(interestPoint);
                }
            });

            interestPointCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InterestPointViewHolder.this.callbackCardAction.apply(interestPoint);
                }
            });
        }
    }
}
