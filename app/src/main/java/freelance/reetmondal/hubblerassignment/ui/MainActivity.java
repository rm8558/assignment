package freelance.reetmondal.hubblerassignment.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import freelance.reetmondal.hubblerassignment.R;
import freelance.reetmondal.hubblerassignment.adapter.ReportAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG=MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_ADD_REPORT = 1001;

    @BindView(R.id.mainFAB)
    FloatingActionButton mainFab;

    @BindView(R.id.addReportFAB)
    FloatingActionButton addReportFAB;

    @BindView(R.id.addReportLabelTV)
    TextView addReportLabel;

    @BindView(R.id.mainConstraintLayout)
    ConstraintLayout mainConstraintLayout;

    @BindView(R.id.reportHeaderLL)
    LinearLayout reportHeaderLL;

    @BindView(R.id.reportCountValueTV)
    TextView reportCountValueTV;

    @BindView(R.id.reportRV)
    RecyclerView reportRV;

    private boolean fabTappedState;
    private JSONArray reportArray;
    private ReportAdapter reportAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        init();
        initViews();

        if(reportArray==null){
            reportArray=new JSONArray();
        }

        reportAdapter=new ReportAdapter(this,reportArray);
        reportRV.setHasFixedSize(true);
        reportRV.setAdapter(reportAdapter);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        reportRV.setLayoutManager(linearLayoutManager);
    }

    private void init(){
        fabTappedState=false;
    }


    @OnClick(R.id.mainFAB)
    public void onClickOfMainFAB(View view){
        float rotationAngle=0;
        if(fabTappedState){
            rotationAngle=-45;
        }
        else{
            rotationAngle=45;
        }

        fabTappedState=!fabTappedState;
        mainFab.animate().rotationBy(rotationAngle);

        new Handler(MainActivity.this.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleFabOptions();
            }
        },300);
    }

    private void toggleFabOptions() {
        if(fabTappedState) {
            addReportFAB.setVisibility(View.VISIBLE);
            addReportLabel.setVisibility(View.VISIBLE);
        }
        else{
            addReportFAB.setVisibility(View.GONE);
            addReportLabel.setVisibility(View.GONE);
        }
    }

    private void initViews(){
        reportHeaderLL.setVisibility(View.VISIBLE);
        mainFab.setVisibility(View.VISIBLE);
        reportRV.setVisibility(View.VISIBLE);
        mainConstraintLayout.setBackgroundColor(getResources().getColor(R.color.colorMainBG));
        mainFab.setRotation(0);
    }

    private void hideViews(){
        reportHeaderLL.setVisibility(View.GONE);
        reportRV.setVisibility(View.GONE);
        mainFab.setVisibility(View.GONE);
        addReportFAB.setVisibility(View.GONE);
        addReportLabel.setVisibility(View.GONE);
    }

    @OnClick({R.id.addReportFAB,R.id.addReportLabelTV})
    public void addNewReport(View view){
        hideViews();
        animateRevealShow(mainConstraintLayout);
    }

    /*@SuppressLint("RestrictedApi")*/
    private void animateRevealShow(final View viewRoot) {
        int cx=(addReportFAB.getLeft() + addReportFAB.getRight()) /2;
        int cy=(addReportFAB.getTop() + addReportFAB.getBottom())/2;

        animateRevealShow(viewRoot, addReportFAB.getWidth() / 2, R.color.colorAccent,
                cx, cy, new OnRevealAnimationListener(){
                    @Override
                    public void onRevealShow() {
                        new Handler(MainActivity.this.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent formIntent=new Intent(MainActivity.this,ReportFormActivity.class);
                                ActivityOptionsCompat activityOptionsCompat=ActivityOptionsCompat
                                        .makeSceneTransitionAnimation(MainActivity.this,null);

                                startActivityForResult(formIntent,REQUEST_CODE_ADD_REPORT/*,activityOptionsCompat.toBundle()*/);
                            }
                        },500);
                    }
                });
    }


    public void animateRevealShow(final View view, final int startRadius,
                                         final @ColorRes int color, int x, int y, final OnRevealAnimationListener listener) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius=(float) Math.hypot(view.getWidth(),view.getHeight());
            Animator anim=ViewAnimationUtils.createCircularReveal(view,x,y,startRadius,finalRadius);
            anim.setDuration(500);
            anim.setStartDelay(80);
            anim.setInterpolator(new FastOutLinearInInterpolator());
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setBackgroundColor(ContextCompat.getColor(MainActivity.this,color));
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    view.setBackgroundColor(ContextCompat.getColor(MainActivity.this,color));
                    view.setVisibility(View.VISIBLE);
                    if (listener != null) {
                        listener.onRevealShow();
                    }
                }
            });
            anim.start();
        }
    }

    public interface OnRevealAnimationListener{
        void onRevealShow();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_ADD_REPORT:
                init();
                initViews();
                if(resultCode== Activity.RESULT_OK){
                    if(data!=null){
                        String reportStr=data.getStringExtra(ReportFormActivity.REPORT_JSON);
                        if(reportStr!=null){
                            try{
                                if(reportArray==null){
                                    reportArray=new JSONArray();
                                }

                                JSONObject reportObject=new JSONObject(reportStr);
                                JSONObject newReportObject=new JSONObject();

                                getRequiredJSON(reportObject,newReportObject);

                                reportArray.put(newReportObject);
                                reportCountValueTV.setText(String.valueOf(reportArray.length()));
                            }catch (Exception e){
                                Log.e(TAG,e.getMessage(),e);
                            }
                        }
                    }
                }
                else{

                }
                break;
            default:
                super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void getRequiredJSON(JSONObject obj,JSONObject newObj){
        Iterator<String> iterator=obj.keys();

        while(iterator.hasNext()){
            String key=iterator.next();
            try {
                Object o = obj.get(key);

                if(o instanceof JSONObject){
                    getRequiredJSON(obj.getJSONObject(key),newObj);
                }
                else{
                    newObj.put(key,obj.getString(key));
                }
            }catch (Exception e){
                Log.e(TAG,e.getMessage(),e);
            }
        }

        /*List<String>keysFromObj=obj.keys();
        keys.addAll(keysFromObj);
        for(String key:keysFromObj){
            if(obj.get(key).getClass()==JSONObject.class){
                findKeys(obj.get(key),keys);
            }
        }*/
    }
}
