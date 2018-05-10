package freelance.reetmondal.hubblerassignment.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import freelance.reetmondal.hubblerassignment.R;
import freelance.reetmondal.hubblerassignment.model.ViewContainer;

public class ReportFormActivity extends AppCompatActivity {
    private static final String TAG=ReportFormActivity.class.getSimpleName();
    private static final int COMPOSITE_FIELD_CODE = 555;
    private static final String EXTRA_FOR_COMPOSITE_FIELD = "EXTRA_FOR_COMPOSITE_FIELD";
    private static final String EXTRA_COMPOSITE_FIELDS_VIEW = "EXTRA_COMPOSITE_FIELDS_VIEW";
    private static final String EXTRA_COMPOSITE_FIELD_NAME = "EXTRA_COMPOSITE_FIELD_NAME";
    private static final String EXTRA_COMPOSITE_FIELD_PREVIOUS_VALUES = "EXTRA_COMPOSITE_FIELD_PREVIOUS_VALUES";

    private String viewJson;
    public static final String REPORT_JSON = "REPORT_JSON";

    @BindView(R.id.formContainerLL)
    LinearLayout formContainer;

    @BindView(R.id.add_button)
    TextView addButtonTV;

    ArrayList<ViewContainer> viewContainers;

    private static final String NAME_KEY="field-name";
    private static final String TYPE_KEY="type";
    private static final String OPTIONS_KEY="options";
    private static final String REQUIRED_KEY="required";
    private static final String MIN_KEY="min";
    private static final String MAX_KEY="max";
    private static final String FIELDS_KEY="fields";

    private static final int SPINNER_ID=8558;

    private boolean currentFieldIsCompositeField=false;
    private String viewJsonStrFromIntent;
    private String sourceCompositeField;
    private String existingValuesJsonStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_form);

        ButterKnife.bind(this);

        Intent receivedIntent=getIntent();
        if(receivedIntent!=null){
            currentFieldIsCompositeField=receivedIntent.getBooleanExtra(EXTRA_FOR_COMPOSITE_FIELD,false);
            viewJsonStrFromIntent=receivedIntent.getStringExtra(EXTRA_COMPOSITE_FIELDS_VIEW);
            sourceCompositeField=receivedIntent.getStringExtra(EXTRA_COMPOSITE_FIELD_NAME);
            existingValuesJsonStr=receivedIntent.getStringExtra(EXTRA_COMPOSITE_FIELD_PREVIOUS_VALUES);
        }

        initViews();

        if(!currentFieldIsCompositeField) {
            readJSON();
        }
        else{
            if(viewJsonStrFromIntent!=null
                    && !viewJsonStrFromIntent.trim().isEmpty()){
                viewJson=viewJsonStrFromIntent;
            }
        }

        viewContainers=new ArrayList<ViewContainer>();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            int actionBarId = getResources().getIdentifier("action_bar_container", "id", "android");
            getWindow().getEnterTransition().excludeTarget(actionBarId, true);
        }

        parseJsonAndInitForm();
        if(currentFieldIsCompositeField){
            populateFormWithPreviousData();
        }
    }

    private void populateFormWithPreviousData() {
        if(existingValuesJsonStr!=null
                && !existingValuesJsonStr.trim().isEmpty()){
            try{
                Log.d(TAG,"EXISTING_JSON: "+existingValuesJsonStr);
                JSONObject existingValuesObj=new JSONObject(existingValuesJsonStr);
                JSONObject actualValuesObj=existingValuesObj.getJSONObject(sourceCompositeField);

                for(ViewContainer viewContainer:viewContainers){
                    if(viewContainer!=null
                            && viewContainer.getName()!=null){
                        String name=viewContainer.getName();

                        if(actualValuesObj!=null){
                            if(actualValuesObj.has(name)
                                    && !actualValuesObj.isNull(name)){
                                View view=viewContainer.getView();

                                if(view instanceof TextInputLayout){
                                    TextInputLayout textInputLayout=(TextInputLayout)view;
                                    textInputLayout.getEditText().setText(actualValuesObj.getString(name));
                                }
                                else if (view instanceof LinearLayout) {
                                    Log.d(TAG,"TEST: "+actualValuesObj.getString(name));
                                    LinearLayout linearLayout = (LinearLayout) view;
                                    Spinner spinner = (Spinner) linearLayout.findViewById(SPINNER_ID);

                                    if (spinner!=null
                                            && spinner.getSelectedItem() != null
                                            && spinner.getSelectedItem() instanceof String) {
                                        //String selectedItem = (String) spinner.getSelectedItem();

                                        ArrayList<String> options=viewContainer.getOptions();
                                        if(options!=null
                                                && !options.isEmpty()){
                                            int pos=-1;
                                            for(int i=0;i<options.size();i++){
                                                if(options.get(i)!=null
                                                        && options.get(i).equalsIgnoreCase(actualValuesObj.getString(name))){
                                                    pos=i;
                                                    break;
                                                }
                                            }

                                            if(pos!=-1){
                                                spinner.setSelection(pos,false);
                                            }
                                        }

                                    }
                                    else{
                                        TextView textView=viewContainer.getCompositeTextView();

                                        if(textView!=null){
                                            try{
                                                JSONObject compObj=actualValuesObj.getJSONObject(name);
                                                JSONObject namedCompObj=new JSONObject();
                                                namedCompObj.put(name,compObj);
                                                if(compObj!=null) {
                                                    textView.setText(namedCompObj.toString());
                                                }
                                            }catch (Exception e){
                                                Log.e(TAG,e.getMessage(),e);
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }catch (Exception e){
                Log.e(TAG,e.getMessage(),e);
            }
        }
    }

    private void initViews() {
        if(currentFieldIsCompositeField) {
            addButtonTV.setText(R.string.add_composite_field_button_label);
        }
        else{
            addButtonTV.setText(R.string.add_report_button_label);
        }
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
                final String name=convertToPascalCase(viewObj.getString(NAME_KEY));
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

                                viewContainer.setOptions(options);

                                viewContainer.setView(linearLayout);
                            }
                        }
                    }
                    else if(type.equalsIgnoreCase("composite")){
                        if(viewObj.has(FIELDS_KEY)
                                && !viewObj.isNull(FIELDS_KEY)){
                            final JSONArray compositeViewsArray=viewObj.getJSONArray(FIELDS_KEY);
                            if(compositeViewsArray!=null
                                    && compositeViewsArray.length()>0){

                                LinearLayout mainLinearLayout=new LinearLayout(context);
                                mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
                                mainLinearLayout.setLayoutParams(
                                        new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));


                                LinearLayout linearLayout=new LinearLayout(context);
                                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                                linearLayout.setLayoutParams(
                                        new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        ));

                                TextView compositeLabel=new TextView(context);
                                compositeLabel.setText(name);
                                compositeLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                                compositeLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1.0f
                                ));

                                ImageView arrowView=new ImageView(context);
                                arrowView.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_right_arrow));
                                arrowView.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));


                                linearLayout.addView(compositeLabel);
                                linearLayout.addView(arrowView);

                                final TextView compositeFieldValues=new TextView(context);
                                //compositeFieldValues.setText("TEST");

                                mainLinearLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent compositeIntent=new Intent(ReportFormActivity.this,ReportFormActivity.class);
                                        compositeIntent.putExtra(EXTRA_FOR_COMPOSITE_FIELD,true);
                                        compositeIntent.putExtra(EXTRA_COMPOSITE_FIELDS_VIEW,compositeViewsArray.toString());
                                        compositeIntent.putExtra(EXTRA_COMPOSITE_FIELD_NAME,name);
                                        if(compositeFieldValues!=null
                                                && compositeFieldValues.getText()!=null
                                                && !compositeFieldValues.getText().toString().trim().isEmpty()){
                                            compositeIntent.putExtra(EXTRA_COMPOSITE_FIELD_PREVIOUS_VALUES,compositeFieldValues.getText().toString());
                                        }

                                        startActivityForResult(compositeIntent,COMPOSITE_FIELD_CODE);
                                    }
                                });


                                mainLinearLayout.addView(linearLayout);
                                mainLinearLayout.addView(compositeFieldValues);
                                viewContainer.setView(mainLinearLayout);
                                viewContainer.setCompositeTextView(compositeFieldValues);
                                viewContainer.setViewComposite(true);
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
                    if(!viewContainer.isViewComposite()) {
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
                                        if (viewContainer.getMinDouble() != null
                                                || viewContainer.getMaxDouble() != null) {
                                            try {
                                                double value = Double.parseDouble(input);
                                                if (viewContainer.getMinDouble() != null
                                                        && viewContainer.getMaxDouble() == null) {
                                                    if (value < viewContainer.getMinDouble().doubleValue()) {
                                                        errorCount++;
                                                        textInputLayout.setError("Min: " + viewContainer.getMinDouble().toString());
                                                    } else {
                                                        respObj.put(viewContainer.getName(), input);
                                                    }
                                                } else if (viewContainer.getMinDouble() == null
                                                        && viewContainer.getMaxDouble() != null) {
                                                    if (value > viewContainer.getMaxDouble().doubleValue()) {
                                                        errorCount++;
                                                        textInputLayout.setError("Max: " + viewContainer.getMaxDouble().toString());
                                                    } else {
                                                        respObj.put(viewContainer.getName(), input);
                                                    }
                                                } else {
                                                    if (value > viewContainer.getMaxDouble().doubleValue()) {
                                                        errorCount++;
                                                        textInputLayout.setError("Max: " + viewContainer.getMaxDouble().toString());
                                                    } else if (value < viewContainer.getMinDouble().doubleValue()) {
                                                        errorCount++;
                                                        textInputLayout.setError("Min: " + viewContainer.getMinDouble().toString());
                                                    } else {
                                                        respObj.put(viewContainer.getName(), input);
                                                    }
                                                }
                                            } catch (NumberFormatException numFX) {
                                                if (!input.trim().isEmpty()) {
                                                    errorCount++;
                                                    textInputLayout.setError("NaN");
                                                }
                                            }
                                        } else if (viewContainer.getMinInteger() != null
                                                || viewContainer.getMaxInteger() != null) {
                                            try {
                                                int value = Integer.parseInt(input);
                                                if (viewContainer.getMinInteger() != null
                                                        && viewContainer.getMaxInteger() == null) {
                                                    if (value < viewContainer.getMinInteger().intValue()) {
                                                        errorCount++;
                                                        textInputLayout.setError("Min: " + viewContainer.getMinInteger().toString());
                                                    } else {
                                                        respObj.put(viewContainer.getName(), input);
                                                    }
                                                } else if (viewContainer.getMinInteger() == null
                                                        && viewContainer.getMaxInteger() != null) {
                                                    if (value > viewContainer.getMaxInteger().intValue()) {
                                                        errorCount++;
                                                        textInputLayout.setError("Max: " + viewContainer.getMaxInteger().toString());
                                                    } else {
                                                        respObj.put(viewContainer.getName(), input);
                                                    }
                                                } else {
                                                    if (value > viewContainer.getMaxInteger().intValue()) {
                                                        errorCount++;
                                                        textInputLayout.setError("Max: " + viewContainer.getMaxInteger().toString());
                                                    } else if (value < viewContainer.getMinInteger().intValue()) {
                                                        errorCount++;
                                                        textInputLayout.setError("Min: " + viewContainer.getMinInteger().toString());
                                                    } else {
                                                        respObj.put(viewContainer.getName(), input);
                                                    }
                                                }
                                            } catch (NumberFormatException numFX) {
                                                if (!input.trim().isEmpty()) {
                                                    errorCount++;
                                                    textInputLayout.setError("NaN");
                                                }
                                            }
                                        } else {
                                            respObj.put(viewContainer.getName(), input);
                                        }
                                    }
                                }
                            } else if (mainView instanceof LinearLayout) {
                                LinearLayout linearLayout = (LinearLayout) mainView;
                                Spinner spinner = (Spinner) linearLayout.findViewById(SPINNER_ID);

                                if (spinner.getSelectedItem() != null
                                        && spinner.getSelectedItem() instanceof String) {
                                    String selectedItem = (String) spinner.getSelectedItem();

                                    if (viewContainer.isRequired()) {
                                        if (selectedItem.trim().isEmpty()) {
                                            errorCount++;
                                            Toast.makeText(ReportFormActivity.this, viewContainer.getName() + " is mandatory",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            respObj.put(viewContainer.getName(), selectedItem);
                                        }
                                    } else {
                                        respObj.put(viewContainer.getName(), selectedItem);
                                    }
                                }

                            }
                        }
                    }
                    else{
                        JSONObject additionalObj=viewContainer.getCompositeValueObj();
                        if(additionalObj!=null){
                            Log.d(TAG,"REPORT_JSON: "+respObj.toString());
                            Log.d(TAG,"COMPOSITE_JSON: "+additionalObj.toString());
                            Iterator<String> keysIterator=additionalObj.keys();

                            while(keysIterator.hasNext()){
                                String key=keysIterator.next();
                                Object valOb=additionalObj.get(key);

                                if(valOb instanceof JSONObject){
                                    respObj.put(key,additionalObj.getJSONObject(key));
                                }
                                else{
                                    respObj.put(key,additionalObj.getString(key));
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
            //Log.d(TAG,respObj.toString());
            Intent reportIntent=new Intent();

            //Log.d(TAG,"REPORT_JSON"+respObj.toString());
            reportIntent.putExtra(REPORT_JSON,respObj.toString());
            if(currentFieldIsCompositeField
                    && sourceCompositeField!=null
                    && !sourceCompositeField.trim().isEmpty()){
                reportIntent.putExtra(EXTRA_COMPOSITE_FIELD_NAME,sourceCompositeField);
            }
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

    private void updateViewData(String fieldName,String dataJsonStr){
        if(fieldName!=null
                && !fieldName.trim().isEmpty()
                && dataJsonStr!=null
                && !dataJsonStr.trim().isEmpty()
                ){
            ViewContainer requiredViewContainer=null;

            for(ViewContainer viewContainer:viewContainers){
                if(viewContainer!=null
                        && viewContainer.getName()!=null
                        && viewContainer.getName().equalsIgnoreCase(fieldName)){
                    requiredViewContainer=viewContainer;
                    break;
                }
            }

            if(requiredViewContainer!=null){
                try {
                    JSONObject dataObj=new JSONObject(dataJsonStr);
                    JSONObject namedDataObj=new JSONObject();
                    namedDataObj.put(fieldName,dataObj);

                    Log.d(TAG,"NAMED_JSON: "+namedDataObj.toString());

                    requiredViewContainer.setCompositeValueObj(namedDataObj);
                    //requiredViewContainer.setCompositeValueObj(dataObj);

                    TextView compositeFieldsTV=requiredViewContainer.getCompositeTextView();
                    if(compositeFieldsTV!=null){
                        compositeFieldsTV.setText(namedDataObj.toString());
                        //compositeFieldsTV.setText(dataObj.toString());
                    }
                }catch (Exception e){
                    Log.e(TAG,e.getMessage(),e);
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case COMPOSITE_FIELD_CODE:
                if(resultCode==Activity.RESULT_OK){
                    if(data!=null){
                        String fieldName=data.getStringExtra(EXTRA_COMPOSITE_FIELD_NAME);
                        String jsonStr=data.getStringExtra(REPORT_JSON);

                        updateViewData(fieldName,jsonStr);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
