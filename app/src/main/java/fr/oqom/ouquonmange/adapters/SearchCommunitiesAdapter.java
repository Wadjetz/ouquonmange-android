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
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.utils.Callback;

public class SearchCommunitiesAdapter extends RecyclerView.Adapter<SearchCommunitiesAdapter.ViewHolder> {

    private List<Community> communities;
    private final Callback<Community> joinCallback;
    private final Callback<Community> detailsCallback;
    private Context context;

    public SearchCommunitiesAdapter(List<Community> communities, Context context, final Callback<Community> joinCallback, Callback<Community> detailsCallback) {
        this.communities = communities;
        this.joinCallback = joinCallback;
        this.context = context;
        this.detailsCallback = detailsCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.community_item_search, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Community community = communities.get(position);
        holder.communityNameTextView.setText(community.name);
        holder.communityDescriptionTextView.setText(community.description);
        holder.communityTypeTextView.setText(community.getCommunityType(context));
        holder.community = community;
    }

    @Override
    public int getItemCount() {
        return communities.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView communityCardView;
        public TextView communityNameTextView;
        public TextView communityDescriptionTextView;
        public TextView communityTypeTextView;
        public Button joinButton;
        public Community community;

        public ViewHolder(View v) {
            super(v);
            communityCardView = (CardView) v.findViewById(R.id.community_card);
            communityNameTextView = (TextView) v.findViewById(R.id.community_name);
            communityDescriptionTextView = (TextView) v.findViewById(R.id.community_description);
            communityTypeTextView = (TextView) v.findViewById(R.id.community_type);
            joinButton = (Button) v.findViewById(R.id.community_join);
            joinButton.setText(R.string.community_join);
            joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    joinCallback.apply(community);
                }
            });

            communityCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detailsCallback.apply(community);
                }
            });
        }
    }
}
