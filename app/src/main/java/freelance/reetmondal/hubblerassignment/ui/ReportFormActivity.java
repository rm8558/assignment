package freelance.reetmondal.hubblerassignment.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import freelance.reetmondal.hubblerassignment.R;
import freelance.reetmondal.hubblerassignment.model.ViewContainer;

public class ReportFormActivity extends AppCompatActivity {
    private static final String TAG=ReportFormActivity.class.getSimpleName();

    private String viewJson;
    public static final String REPORT_JSON = "REPORT_JSON";

    @BindView(R.id.formContainerLL)
    LinearLayout formContainer;

    ArrayList<ViewContainer> viewContainers;

    private static final String NAME_KEY="field-name";
    private static final String TYPE_KEY="type";
    private static final String OPTIONS_KEY="options";
    private static final String REQUIRED_KEY="required";
    private static final String MIN_KEY="min";
    private static final String MAX_KEY="max";

    private static final int SPINNER_ID=8558;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_form);

        ButterKnife.bind(this);

        readJSON();

        viewContainers=new ArrayList<ViewContainer>();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            int actionBarId = getResources().getIdentifier("action_bar_container", "id", "android");
            getWindow().getEnterTransition().excludeTarget(actionBarId, true);
        }

        parseJsonAndInitForm();
    }

    private void readJSON(){
        try {
            InputStream inputStream = getAssets().open("ui_config.json");
            byte buffer[]=new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            viewJson=new String(buffer);
        }catch(Exception e){
            Log.e(TAG,e.getMessage(),e);
        }


    }

    private void showErrorToastAndExit(){
        Toast.makeText(ReportFormActivity.this,R.string.error_form_creation_failed,Toast.LENGTH_LONG)
                .show();
        finish();
    }

    private void parseJsonAndInitForm(){
        try{
            if(viewJson !=null) {
                JSONArray viewArray = new JSONArray(viewJson);

                if(viewArray.length()>0){
                    initFormViews(viewArray);
                }
                else{
                    showErrorToastAndExit();
                }
            }
        }catch(JSONException jsonX){
            showErrorToastAndExit();
        }catch(Exception e){
            showErrorToastAndExit();
        }
    }

    private void initFormViews(JSONArray jsonArray){
        if(viewContainers==null){
            viewContainers=new ArrayList<ViewContainer>();
        }
        for(int i=0;i<jsonArray.length();i++){
            try {
                JSONObject viewObj = jsonArray.getJSONObject(i);
                ViewContainer viewContainer= getViewContainer(viewObj,ReportFormActivity.this);
                if(viewContainer==null){
                    continue;
                }
                else{
                    if(viewContainer.getView()!=null) {
                        formContainer.addView(viewContainer.getView());
                        viewContainers.add(viewContainer);
                    }
                }

            }catch (Exception e){

            }
        }
    }

    private ViewContainer getViewContainer(JSONObject viewObj, Context context){
        ViewContainer viewContainer=null;

        try {
            if (viewObj != null
                    && viewObj.has(NAME_KEY)
                    && !viewObj.isNull(NAME_KEY)) {
                String name=convertToPascalCase(viewObj.getString(NAME_KEY));
                viewContainer = new ViewContainer();
                viewContainer.setName(name);

                if(viewObj.has(TYPE_KEY)
                        && !viewObj.isNull(TYPE_KEY)){
                    String type=viewObj.getString(TYPE_KEY);

                    TextInputLayout textInputLayout=new TextInputLayout(context);
                    textInputLayout.setLayoutParams(
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));
                    textInputLayout.setErrorEnabled(true);

                    if(type.equalsIgnoreCase("text")){
                        EditText editText=new EditText(context);
                        editText.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        editText.setHint(name);
                        editText.setSingleLine();
                        textInputLayout.addView(editText);

                        viewContainer.setView(textInputLayout);
                    }
                    else if(type.equalsIgnoreCase("number")){
                        EditText editText=new EditText(context);
                        editText.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        editText.setHint(name);
                        editText.setSingleLine();
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        textInputLayout.addView(editText);

                        Integer min=null,
                                max=null;
                        if (viewObj.has(MIN_KEY)
                                && !viewObj.isNull(MIN_KEY)){
                            min=viewObj.getInt(MIN_KEY);
                        }

                        if(viewObj.has(MAX_KEY)
                                && !viewObj.isNull(MAX_KEY)){
                            max=viewObj.getInt(MAX_KEY);
                        }

                        viewContainer.setMaxInteger(max);
                        viewContainer.setMinInteger(min);

                        if(min!=null
                                || max!=null){
                            View.OnFocusChangeListener onFocusChangeListener=null;
                            if(min != null
                                    && max == null){
                                onFocusChangeListener=getOnFocusChangedListenerForSingle(min.intValue(),true);
                            }
                            else if(max != null
                                    && min == null){
                                onFocusChangeListener=getOnFocusChangedListenerForSingle(max.intValue(),false);
                            }
                            else{
                                onFocusChangeListener=getOnFocusChangedListenerForMinMax(min.intValue(),max.intValue());
                            }

                            if(onFocusChangeListener!=null){
                                editText.setOnFocusChangeListener(onFocusChangeListener);
                            }
                        }

                        viewContainer.setView(textInputLayout);
                    }
                    else if(type.equalsIgnoreCase("decimal")){
                        EditText editText=new EditText(context);
                        editText.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        editText.setHint(name);
                        editText.setSingleLine();
                        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        textInputLayout.addView(editText);

                        Double min=null,
                                max=null;
                        if (viewObj.has(MIN_KEY)
                                && !viewObj.isNull(MIN_KEY)){
                            min=viewObj.getDouble(MIN_KEY);
                        }

                        if(viewObj.has(MAX_KEY)
                                && !viewObj.isNull(MAX_KEY)){
                            max=viewObj.getDouble(MAX_KEY);
                        }

                        viewContainer.setMaxDouble(max);
                        viewContainer.setMinDouble(min);

                        if(min!=null
                                || max!=null){
                            View.OnFocusChangeListener onFocusChangeListener=null;
                            if(min != null
                                    && max == null){
                                onFocusChangeListener=getOnFocusChangedListenerForSingle(min.doubleValue(),true);
                            }
                            else if(max != null
                                    && min == null){
                                onFocusChangeListener=getOnFocusChangedListenerForSingle(max.doubleValue(),false);
                            }
                            else{
                                onFocusChangeListener=getOnFocusChangedListenerForMinMax(min.doubleValue(),max.doubleValue());
                            }

                            if(onFocusChangeListener!=null){
                                editText.setOnFocusChangeListener(onFocusChangeListener);
                            }
                        }

                        viewContainer.setView(textInputLayout);
                    }
                    else if(type.equalsIgnoreCase("multiline")){
                        EditText editText=new EditText(context);
                        editText.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        editText.setHint(name);
                        editText.setMinLines(3);
                        textInputLayout.addView(editText);

                        viewContainer.setView(textInputLayout);
                    }
                    else if(type.equalsIgnoreCase("dropdown")){

                        if(viewObj.has(OPTIONS_KEY)
                                && !viewObj.isNull(OPTIONS_KEY)){
                            ArrayList<String> options=new ArrayList<>();

                            JSONArray optionsArray=viewObj.getJSONArray(OPTIONS_KEY);
                            for(int pos=0;pos<optionsArray.length();pos++){
                                options.add(optionsArray.getString(pos));
                            }

                            if(!options.isEmpty()){
                                LinearLayout linearLayout=new LinearLayout(context);
                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                linearLayout.setLayoutParams(
                                        new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));

                                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                                layoutParams.leftMargin=10;

                                TextView spinnerLabel=new TextView(context);
                                spinnerLabel.setLayoutParams(
                                        layoutParams);
                                spinnerLabel.setText(name);

                                Spinner spinner=new Spinner(context);
                                spinner.setLayoutParams(
                                        new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));
                                spinner.setId(SPINNER_ID);

                                ArrayAdapter<String> optionsAdapter
                                        =new ArrayAdapter<String>(context,
                                        android.R.layout.simple_spinner_dropdown_item,
                                        options);

                                spinner.setAdapter(optionsAdapter);
                                linearLayout.addView(spinnerLabel);
                                linearLayout.addView(spinner);

                                viewContainer.setView(linearLayout);
                            }
                        }
                    }
                    else{
                        return null;
                    }
                }

                if(viewObj.has(REQUIRED_KEY)
                        && !viewObj.isNull(REQUIRED_KEY)){
                    viewContainer.setRequired(viewObj.getBoolean(REQUIRED_KEY));
                }
            }
        }catch (Exception e){
            Log.e(TAG,e.getMessage(),e);
        }
        return viewContainer;
    }

    private View.OnFocusChangeListener getOnFocusChangedListenerForMinMax(final double min, final double max){
        View.OnFocusChangeListener onFocusChangeListener=new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(v instanceof EditText){
                        EditText editText=(EditText)v;
                        try{
                            double value=Double.parseDouble(editText.getText().toString());

                            if(value<min){
                                editText.setText(String.valueOf(min));
                            }
                            else if(value>max){
                                editText.setText(String.valueOf(max));
                            }
                        }catch (NumberFormatException numFX){
                            editText.setText("");
                        }
                    }
                }
            }
        };

        return onFocusChangeListener;
    }

    private View.OnFocusChangeListener getOnFocusChangedListenerForMinMax(final int min, final int max){
        View.OnFocusChangeListener onFocusChangeListener=new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(v instanceof EditText){
                        EditText editText=(EditText)v;
                        try{
                            double value=Double.parseDouble(editText.getText().toString());

                            if(value<min){
                                editText.setText(String.valueOf(min));
                            }
                            else if(value>max){
                                editText.setText(String.valueOf(max));
                            }
                        }catch (NumberFormatException numFX){
                            editText.setText("");
                        }
                    }
                }
            }
        };

        return onFocusChangeListener;
    }

    private View.OnFocusChangeListener getOnFocusChangedListenerForSingle(final int limitValue, final boolean minFlag){
        View.OnFocusChangeListener onFocusChangeListener=new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(v instanceof EditText){
                        EditText editText=(EditText)v;
                        try{
                            int value=Integer.parseInt(editText.getText().toString());

                            if(minFlag){
                                if(value<limitValue){
                                    editText.setText(String.valueOf(limitValue));
                                }
                            }
                            else{
                                if(value>limitValue){
                                    editText.setText(String.valueOf(limitValue));
                                }
                            }
                        }catch (NumberFormatException numFX){
                            editText.setText("");
                        }
                    }
                }
            }
        };

        return onFocusChangeListener;

    }

    private View.OnFocusChangeListener getOnFocusChangedListenerForSingle(final double limitValue, final boolean minFlag){
        View.OnFocusChangeListener onFocusChangeListener=new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if(v instanceof EditText){
                        EditText editText=(EditText)v;
                        try{
                            double value=Double.parseDouble(editText.getText().toString());

                            if(minFlag){
                                if(value<limitValue){
                                    editText.setText(String.valueOf(limitValue));
                                }
                            }
                            else{
                                if(value>limitValue){
                                    editText.setText(String.valueOf(limitValue));
                                }
                            }
                        }catch (NumberFormatException numFX){
                            editText.setText("");
                        }
                    }
                }
            }
        };

        return onFocusChangeListener;

    }

    @OnClick(R.id.cancel_button)
    public void cancelReportSave(View view){
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.add_button)
    public void addNewReport(View view){
        int errorCount=0;
        JSONObject respObj=new JSONObject();

        for(ViewContainer viewContainer:viewContainers){
            try {
                if (viewContainer != null) {
                    if (viewContainer.getView() != null) {
                        View mainView = viewContainer.getView();

                        if (mainView instanceof TextInputLayout) {
                            TextInputLayout textInputLayout = (TextInputLayout) mainView;
                            textInputLayout.setError(null);
                            String input = textInputLayout.getEditText().getText().toString();

                            if (input != null) {
                                if (viewContainer.isRequired()) {
                                    if (input.trim().isEmpty()) {
                                        errorCount++;
                                        textInputLayout.setError("Mandatory field");
                                    } else {
                                        respObj.put(viewContainer.getName(), input);
                                    }
                                } else {
                                    if(viewContainer.getMinDouble()!=null
                                            || viewContainer.getMaxDouble()!=null){
                                        try{
                                            double value=Double.parseDouble(input);
                                            if(viewContainer.getMinDouble()!=null
                                                    && viewContainer.getMaxDouble()==null){
                                                if(value<viewContainer.getMinDouble().doubleValue()){
                                                    errorCount++;
                                                    textInputLayout.setError("Min: "+viewContainer.getMinDouble().toString());
                                                }
                                                else{
                                                    respObj.put(viewContainer.getName(), input);
                                                }
                                            }
                                            else if(viewContainer.getMinDouble()==null
                                                    && viewContainer.getMaxDouble()!=null){
                                                if(value>viewContainer.getMaxDouble().doubleValue()){
                                                    errorCount++;
                                                    textInputLayout.setError("Max: "+viewContainer.getMaxDouble().toString());
                                                }
                                                else{
                                                    respObj.put(viewContainer.getName(), input);
                                                }
                                            }
                                            else{
                                                if(value>viewContainer.getMaxDouble().doubleValue()){
                                                    errorCount++;
                                                    textInputLayout.setError("Max: "+viewContainer.getMaxDouble().toString());
                                                }
                                                else if(value<viewContainer.getMinDouble().doubleValue()){
                                                    errorCount++;
                                                    textInputLayout.setError("Min: "+viewContainer.getMinDouble().toString());
                                                }
                                                else{
                                                    respObj.put(viewContainer.getName(), input);
                                                }
                                            }
                                        }catch (NumberFormatException numFX){
                                            if(!input.trim().isEmpty()) {
                                                errorCount++;
                                                textInputLayout.setError("NaN");
                                            }
                                        }
                                    }
                                    else if(viewContainer.getMinInteger()!=null
                                            || viewContainer.getMaxInteger()!=null){
                                        try{
                                            int value=Integer.parseInt(input);
                                            if(viewContainer.getMinInteger()!=null
                                                    && viewContainer.getMaxInteger()==null){
                                                if(value<viewContainer.getMinInteger().intValue()){
                                                    errorCount++;
                                                    textInputLayout.setError("Min: "+viewContainer.getMinInteger().toString());
                                                }
                                                else{
                                                    respObj.put(viewContainer.getName(), input);
                                                }
                                            }
                                            else if(viewContainer.getMinInteger()==null
                                                    && viewContainer.getMaxInteger()!=null){
                                                if(value>viewContainer.getMaxInteger().intValue()){
                                                    errorCount++;
                                                    textInputLayout.setError("Max: "+viewContainer.getMaxInteger().toString());
                                                }
                                                else{
                                                    respObj.put(viewContainer.getName(), input);
                                                }
                                            }
                                            else{
                                                if(value>viewContainer.getMaxInteger().intValue()){
                                                    errorCount++;
                                                    textInputLayout.setError("Max: "+viewContainer.getMaxInteger().toString());
                                                }
                                                else if(value<viewContainer.getMinInteger().intValue()){
                                                    errorCount++;
                                                    textInputLayout.setError("Min: "+viewContainer.getMinInteger().toString());
                                                }
                                                else{
                                                    respObj.put(viewContainer.getName(), input);
                                                }
                                            }
                                        }catch (NumberFormatException numFX){
                                            if(!input.trim().isEmpty()) {
                                                errorCount++;
                                                textInputLayout.setError("NaN");
                                            }
                                        }
                                    }
                                    else{
                                        respObj.put(viewContainer.getName(), input);
                                    }
                                }
                            }
                        } else if (mainView instanceof LinearLayout) {
                            LinearLayout linearLayout = (LinearLayout) mainView;
                            Spinner spinner=(Spinner)linearLayout.findViewById(SPINNER_ID);

                            if(spinner.getSelectedItem()!=null
                                    && spinner.getSelectedItem() instanceof String){
                                String selectedItem=(String)spinner.getSelectedItem();

                                if (viewContainer.isRequired()) {
                                    if(selectedItem.trim().isEmpty()){
                                        errorCount++;
                                        Toast.makeText(ReportFormActivity.this,viewContainer.getName()+" is mandatory",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        respObj.put(viewContainer.getName(), selectedItem);
                                    }
                                }
                                else{
                                    respObj.put(viewContainer.getName(), selectedItem);
                                }
                            }

                        }
                    }
                }
            }catch (Exception e){
                Log.e(TAG,e.getMessage(),e);
            }
        }

        if(errorCount==0){
            Log.d(TAG,respObj.toString());
            Intent reportIntent=new Intent();
            reportIntent.putExtra(REPORT_JSON,respObj.toString());
            setResult(Activity.RESULT_OK,reportIntent);
            finish();
        }
    }

    private String convertToPascalCase(String text){
        if(text!=null
                && !text.isEmpty()) {
            StringBuffer buffer = new StringBuffer(text.substring(0,1).toUpperCase());
            buffer.append(text.substring(1,text.length()));
            return buffer.toString();
        }
        return text;
    }
}
