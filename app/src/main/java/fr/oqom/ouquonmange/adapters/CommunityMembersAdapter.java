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
import fr.oqom.ouquonmange.models.CommunityMember;
import fr.oqom.ouquonmange.utils.Callback;

public class CommunityMembersAdapter extends RecyclerView.Adapter<CommunityMembersAdapter.ViewHolder> {

    private final Callback<CommunityMember> communityMemberRefuseCallback;
    private List<CommunityMember> communityMembers;
    private Context context;
    private Callback<CommunityMember> communityMemberAcceptCallback;

    public CommunityMembersAdapter(List<CommunityMember> communityMembers, Context context, Callback<CommunityMember> communityMemberAcceptCallback, Callback<CommunityMember> communityMemberRefuseCallback) {
        this.communityMembers = communityMembers;
        this.context = context;
        this.communityMemberAcceptCallback = communityMemberAcceptCallback;
        this.communityMemberRefuseCallback = communityMemberRefuseCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.community_member_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final CommunityMember communityMember = communityMembers.get(position);
        holder.memberNameTextView.setText(communityMember.username);
        holder.communityMember = communityMember;
        holder.acceptMemberAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                communityMemberAcceptCallback.apply(communityMember);
            }
        });

        holder.refuseMemberAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                communityMemberRefuseCallback.apply(communityMember);
            }
        });

        if (communityMember.status.equals("pending")) {
            holder.acceptMemberAction.setVisibility(View.VISIBLE);
            holder.refuseMemberAction.setVisibility(View.VISIBLE);
        }else{
            holder.acceptMemberAction.setVisibility(View.INVISIBLE);
            holder.refuseMemberAction.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return communityMembers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView memberCardView;
        public TextView memberNameTextView;
        public Button acceptMemberAction;
        public CommunityMember communityMember;
        public Button refuseMemberAction;

        public ViewHolder(View v) {
            super(v);
            memberCardView = (CardView) v.findViewById(R.id.community_member_cardview);
            memberNameTextView = (TextView) v.findViewById(R.id.member_username);
            acceptMemberAction = (Button) v.findViewById(R.id.accept_member_action);
            refuseMemberAction = (Button) v.findViewById(R.id.refuse_member_action);
        }
    }
}
