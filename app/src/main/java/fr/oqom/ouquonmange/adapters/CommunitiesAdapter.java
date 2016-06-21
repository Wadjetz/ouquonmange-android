package fr.oqom.ouquonmange.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import fr.oqom.ouquonmange.R;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class CommunitiesAdapter extends RecyclerView.Adapter<CommunitiesAdapter.ViewHolder> {

    private List<Community> communities;
    private final Callback<Community> callback;
    private final Callback2<Community, Boolean> callbackDefaultCommunity;
    private Context context;

    public CommunitiesAdapter(List<Community> communities, Context context, final Callback<Community> callback, final Callback2<Community, Boolean> callbackDefaultCommunity) {
        this.communities = communities;
        this.callback = callback;
        this.callbackDefaultCommunity = callbackDefaultCommunity;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.community_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Community community = communities.get(position);
        holder.communityNameTextView.setText(community.name);
        holder.communityDescriptionTextView.setText(community.description);
        holder.defaultCommunityAction.setChecked(community.isDefault);
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
        public Button calendarButton;
        public CheckBox defaultCommunityAction;
        public Community community;

        public ViewHolder(View v) {
            super(v);
            communityCardView = (CardView) v.findViewById(R.id.community_card);
            communityNameTextView = (TextView) v.findViewById(R.id.community_name);
            communityDescriptionTextView = (TextView) v.findViewById(R.id.community_description);
            calendarButton = (Button) v.findViewById(R.id.community_calendar);
            defaultCommunityAction = (CheckBox) v.findViewById(R.id.default_community_action);
            calendarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.apply(community);
                }
            });

            defaultCommunityAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callbackDefaultCommunity.apply(community, defaultCommunityAction.isChecked());
                }
            });
        }
    }
}
