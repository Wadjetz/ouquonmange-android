package fr.oqom.ouquonmange.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fr.oqom.ouquonmange.R;

/**
 * Created by hedhili on 07/07/2016.
 */
public class EmptyRecyclerViewAdapter extends RecyclerView.Adapter<EmptyRecyclerViewAdapter.ViewHolder> {

    private String mMessage;

    public EmptyRecyclerViewAdapter(){}

    public EmptyRecyclerViewAdapter(String message){
        mMessage = message;
    }

    @Override
    public EmptyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.empty_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        if(mMessage != null){
            viewHolder.mMessageView.setText(mMessage);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {}

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mMessageView;
        public ViewHolder(View view) {
            super(view);
            mMessageView = (TextView) view.findViewById(R.id.empty_list);
        }
    }
}