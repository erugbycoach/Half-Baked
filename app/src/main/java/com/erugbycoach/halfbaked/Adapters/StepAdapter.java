package com.erugbycoach.halfbaked.Adapters;

/**
 * Created by William D Howell on 11/21/2017.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.erugbycoach.halfbaked.Models.Instructions;
import com.erugbycoach.halfbaked.R;

import java.util.List;


public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepsViewHolder> {

    private LayoutInflater inflater;
    private List<Instructions> instructions;
    private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public StepAdapter(Context context, List<Instructions> instructions, ListItemClickListener listener) {
        inflater = LayoutInflater.from(context);
        this.instructions = instructions;
        mOnClickListener = listener;
    }

    @Override
    public StepsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = inflater.inflate(R.layout.recipe_step_item, parent, false);

        StepsViewHolder stepsViewHolder = new StepsViewHolder(rootView);

        return stepsViewHolder;
    }

    public void onBindViewHolder(StepsViewHolder holder, int position) {
        Instructions currentStep = instructions.get(position);

        String currentStepInstruction = currentStep.getShortDescription();
        // Referenced https://stackoverflow.com/questions/27470859/
        // apostrophe-converting-to-question-mark-in-textview
        // to convert Degree symbol to webcode
        holder.instructionsTV.setText(currentStepInstruction
                .replaceAll("\u00b0F", "&#176;"));
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }

    class StepsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView instructionsTV;

        public StepsViewHolder(View itemView) {
            super(itemView);

            instructionsTV = itemView.findViewById(R.id.instruction_step);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}

