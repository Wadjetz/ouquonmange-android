package fr.oqom.ouquonmange.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

import fr.oqom.ouquonmange.R;
import fr.oqom.ouquonmange.models.User;
import fr.oqom.ouquonmange.utils.Callback;

/**
 * Created by hedhili on 24/06/2016.
 */
public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {

    private List<User> membersOfCommunityByEvent;
    private Context context;



    public MembersAdapter(Context context, List<User> members) {
        this.context = context;
        this.membersOfCommunityByEvent = members;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = membersOfCommunityByEvent.get(position);
        holder.memberNameTextView.setText(user.username);
        holder.user = user;
    }

    @Override
    public int getItemCount() {
        return membersOfCommunityByEvent.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView memberCardView;
        public TextView memberNameTextView;
        public User user;
        public ViewHolder(View v) {
            super(v);
            //memberCardView = (CardView) v.findViewById(R.id.member_cardView);
            memberNameTextView = (TextView) v.findViewById(R.id.member_username);

        }
    }
}
