package android.nmr.keeptrack;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/*
    Recycler Adapter. Fairly standard.
    Built with instructions from: https://www.youtube.com/watch?v=Vyqz_-sJGFk
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<String> mTimerItem = new ArrayList();
    private Context mContext;


    public RecyclerViewAdapter(ArrayList<String> timerItem, Context context) {
        mTimerItem = timerItem;
        mContext = context;
    }

    @NonNull
    @Override
    //INFLATES VIEW, same for most adapters will make. places views into position.
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    //ViewHolder constructor
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView timerElapsed;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            //Searches for ID set in xml
            timerElapsed = itemView.findViewById(R.id.timeElapsed);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

    @Override
    //Handles layouts
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");

        //change to timerName.
        holder.timerElapsed.setText(mTimerItem.get(position));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on:" + mTimerItem.get(position));
                //Shows time elapsed. CREATE FUNCTIONALITY?
                Toast.makeText(mContext, mTimerItem.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Gets number of items in list.
    @Override
    public int getItemCount() {
        return mTimerItem.size();
    }



}
