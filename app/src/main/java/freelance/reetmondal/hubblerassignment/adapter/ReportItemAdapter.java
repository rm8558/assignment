package freelance.reetmondal.hubblerassignment.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import freelance.reetmondal.hubblerassignment.R;

public class ReportItemAdapter extends RecyclerView.Adapter<ReportItemAdapter.ReportItemViewHolder> {
    private static final String TAG=ReportItemAdapter.class.getSimpleName();

    private Context context;
    private JSONObject reportObject;
    private ArrayList<String> keys;


    public ReportItemAdapter(Context context, JSONObject reportObject) {
        this.context = context;
        this.reportObject = reportObject;
        this.keys=new ArrayList<String>();

        if(this.reportObject!=null){
            Iterator<String> keysIterator=reportObject.keys();

            while(keysIterator.hasNext()){
                keys.add(keysIterator.next());
            }
        }
    }

    @NonNull
    @Override
    public ReportItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_report_item,parent,false);
        ReportItemViewHolder reportItemViewHolder=new ReportItemViewHolder(view);
        return reportItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReportItemViewHolder holder, int position) {
        try{
            holder.bindView(keys.get(position),reportObject.getString(keys.get(position)));
        }catch (Exception e){
            Log.e(TAG,e.getMessage(),e);
        }
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    public class ReportItemViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.reportItemLabel)
        TextView reportItemLabel;

        @BindView(R.id.reportItemData)
        TextView reportItemData;

        public ReportItemViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this,itemView);
        }

        public void bindView(String key,String value){
            reportItemLabel.setText(key);
            reportItemData.setText(value);
        }
    }
}
