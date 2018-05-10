package freelance.reetmondal.hubblerassignment.model;

import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class ViewContainer {
    public static final int VIEW_CONTENT_TEXT=1;

    private View view;
    private boolean required;
    private String name;

    private Double minDouble;
    private Double maxDouble;

    private Integer minInteger;
    private Integer maxInteger;

    private ArrayList<String> options;

    private JSONObject compositeValueObj;
    private TextView compositeTextView;
    private boolean viewComposite;

    public ViewContainer() {
        required=false;
    }

    public ViewContainer(View view) {
        this.view = view;
    }

    public ViewContainer(View view, boolean required) {
        this.view = view;
        this.required = required;
    }

    public ViewContainer(View view, boolean required, String name) {
        this.view = view;
        this.required = required;
        this.name = name;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMinDouble() {
        return minDouble;
    }

    public void setMinDouble(Double minDouble) {
        this.minDouble = minDouble;
    }

    public Double getMaxDouble() {
        return maxDouble;
    }

    public void setMaxDouble(Double maxDouble) {
        this.maxDouble = maxDouble;
    }

    public Integer getMinInteger() {
        return minInteger;
    }

    public void setMinInteger(Integer minInteger) {
        this.minInteger = minInteger;
    }

    public Integer getMaxInteger() {
        return maxInteger;
    }

    public void setMaxInteger(Integer maxInteger) {
        this.maxInteger = maxInteger;
    }

    public JSONObject getCompositeValueObj() {
        return compositeValueObj;
    }

    public void setCompositeValueObj(JSONObject compositeValueObj) {
        this.compositeValueObj = compositeValueObj;
    }

    public TextView getCompositeTextView() {
        return compositeTextView;
    }

    public void setCompositeTextView(TextView compositeTextView) {
        this.compositeTextView = compositeTextView;
    }

    public boolean isViewComposite() {
        return viewComposite;
    }

    public void setViewComposite(boolean viewComposite) {
        this.viewComposite = viewComposite;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }
}
