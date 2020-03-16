package com.klinker.android.twitter_l;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.klinker.android.twitter_l.services.event_cc.EventCC;
import com.klinker.android.twitter_l.services.event_cc.JsonObjectReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShowAssessments extends AppCompatActivity implements JsonObjectReceiver {

    private AssessmentAdapter mAdapter;

    private String TAG = "Assessments Activity";
    private EventCC eventCC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_assessments);
        RecyclerView recyclerView = findViewById(R.id.recyclerView_assessments);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new AssessmentAdapter(null);
        recyclerView.setAdapter(mAdapter);

        if (null == eventCC) {
            try {
                eventCC = new EventCC(this);
            } catch (IOException e) {
                Log.e(TAG, "Error creating EventCC");
                e.printStackTrace();
            }
        }

        eventCC.getFullEvent(this, getIntent().getStringExtra("eventId"));
    }

    @Override
    public void display(JSONObject assessments) {
        // populate the recycler view with the assessments
        try {
            JSONArray assessmentsArray = assessments.getJSONArray("assessments");
            mAdapter.assessmentsList.clear();
            for (int i = 0; i < assessmentsArray.length(); i++) {
                JSONObject assessment = assessmentsArray.getJSONObject(i);

                String id = "ID: " + assessment.getString("id");
                String timestamp = "Time: " + assessment.getString("timestamp");
                String creator = "Creator: " + assessment.getString("creator");
                String rating = "Rating: " + assessment.getString("rating");
                String trustworthiness = "Trust: " + assessment.getString("trustworthiness");

                String[] array = {id, timestamp, creator, rating, trustworthiness};
                mAdapter.assessmentsList.add(array);
            }
            mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * populates the recycler view with assessments
     */
    private class AssessmentAdapter extends RecyclerView.Adapter<AssessmentAdapter.MyViewHolder> {

        List<String[]> assessmentsList = new ArrayList<>();

        AssessmentAdapter(JSONObject assessments) {
            // parse json object to list of assessments
            if (null != assessments) {
                try {
                    JSONArray assessmentsArray = assessments.getJSONArray("assessments");
                    for (int i = 0; i < assessmentsArray.length(); i++) {
                        JSONObject assessment = assessmentsArray.getJSONObject(i);

                        String id = assessment.getString("id");
                        String timestamp = assessment.getString("timestamp");
                        String creator = assessment.getString("creator");
                        String rating = assessment.getString("rating");
                        String trustworthiness = assessment.getString("trustworthiness");

                        String[] array = {id, timestamp, creator, rating, trustworthiness};
                        assessmentsList.add(array);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        class MyViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            TextView assessmentId;
            TextView time;
            TextView creator;
            TextView rating;
            TextView trustworthiness;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                this.assessmentId = itemView.findViewById(R.id.assessment_id);
                this.time = itemView.findViewById(R.id.time);
                this.creator = itemView.findViewById(R.id.creator);
                this.rating = itemView.findViewById(R.id.rating);
                this.trustworthiness = itemView.findViewById(R.id.trustworthiness);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.assessment_view, parent, false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            String[] assessment = assessmentsList.get(position);
            holder.assessmentId.setText(assessment[0]);
            holder.time.setText(assessment[1]);
            holder.creator.setText(assessment[2]);
            holder.rating.setText(assessment[3]);
            holder.trustworthiness.setText(assessment[4]);
        }

        @Override
        public int getItemCount() {
            return assessmentsList.size();
        }
    }
}

