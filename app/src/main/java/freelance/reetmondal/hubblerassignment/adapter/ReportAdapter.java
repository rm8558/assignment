package freelance.reetmondal.hubblerassignment.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import freelance.reetmondal.hubblerassignment.R;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private static final String TAG=ReportAdapter.class.getSimpleName();

    private Context context;
    private JSONArray reportArray;

    public ReportAdapter(Context context, JSONArray reportArray) {
        this.context = context;
        this.reportArray = reportArray;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_report,parent,false);
        ReportViewHolder reportViewHolder=new ReportViewHolder(view);
        return  reportViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        try {
            holder.bindView(reportArray.getJSONObject(position));
        }catch (Exception e){
            Log.e(TAG,e.getMessage(),e);
        }
    }

    @Override
    public int getItemCount() {
        if(reportArray==null){
            return 0;
        }
        return reportArray.length();
    }

    public class ReportViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.reportCard)
        CardView reportCard;

        @BindView(R.id.reportItemRV)
        RecyclerView reportItemRV;

        public ReportViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this,itemView);
        }

        public void bindView(JSONObject reportObj){
            ReportItemAdapter reportItemAdapter=new ReportItemAdapter(context,reportObj);
            LinearLayoutManager linearLayoutManager=new LinearLayoutManager(context);
            reportItemRV.setHasFixedSize(true);
            reportItemRV.setLayoutManager(linearLayoutManager);
            reportItemRV.setAdapter(reportItemAdapter);
        }
    }
}
