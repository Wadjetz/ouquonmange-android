package fr.oqom.ouquonmange.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.List;

import fr.oqom.ouquonmange.R;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.utils.Callback;

public class InterestPointsAdapter extends RecyclerView.Adapter<InterestPointsAdapter.ViewHolder> {

    private final List<InterestPoint> interestPoints;
    private final Callback<InterestPoint> callback;
    private Context context;

    public InterestPointsAdapter(Context context, List<InterestPoint> interestPoints, Callback<InterestPoint> callback) {
        this.context = context;
        this.interestPoints = interestPoints;
        this.callback = callback;
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
        UrlImageViewHelper.setUrlDrawable(holder.interestPointImageView, "http://www.pacinno.eu/wp-content/uploads/2014/05/placeholder1.png", R.drawable.side_nav_bar);
        String buttonText = interestPoint.isJoin ? context.getString(R.string.quit_group) : context.getString(R.string.join_group);
        holder.joinAction.setText(buttonText);
    }

    @Override
    public int getItemCount() {
        return interestPoints.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public InterestPoint interestPoint;
        public CardView interestPointCardView;
        public ImageView interestPointImageView;
        public TextView interestPointName;
        public TextView interestPointAddress;
        public Button joinAction;

        public ViewHolder(View v) {
            super(v);
            interestPointCardView = (CardView) v.findViewById(R.id.interest_point_cardView);
            interestPointName = (TextView) v.findViewById(R.id.interest_point_name);
            interestPointAddress = (TextView) v.findViewById(R.id.interest_point_address);
            interestPointImageView = (ImageView) v.findViewById(R.id.interest_point_photo);
            joinAction = (Button) v.findViewById(R.id.action_join_group);
            joinAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.apply(interestPoint);
                }
            });
        }
    }
}
