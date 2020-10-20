package pl.huczeq.rtspplayer.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pl.huczeq.rtspplayer.R;

public class MyListPreferences extends androidx.preference.ListPreference {

    private final static String TAG = "MyListPreferences";

    List<String> dependentValues = new ArrayList<>();
    public MyListPreferences(Context context) {
        super(context);
    }

    public MyListPreferences(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyListPreferences(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public MyListPreferences(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        Log.d(TAG, "init");
        if (attrs != null) {
            Log.d(TAG, "attrs != null");
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyListPreferences);
            String dependentValue = a.getString(R.styleable.MyListPreferences_dependentValue);
            if(dependentValue != null) {
                Log.d(TAG, "dependentValue != null");
                Log.d(TAG, "Size: " + dependentValue.split(",").length);
                for(String s : dependentValue.split(",")) {
                    dependentValues.add(s);
                    Log.d(TAG, "Add: " + s);
                }
            }
            a.recycle();
        }
    }

    @Override
    public void setValue(String value) {
        String mOldValue = getValue();
        super.setValue(value);
        if (!value.equals(mOldValue)) {
            notifyDependencyChange(shouldDisableDependents());
        }
    }

    private boolean containtString(String value) {
        for(String s : this.dependentValues) {
            if(value.equals(s)) return true;
        }
        return false;
    }

    @Override
    public boolean shouldDisableDependents() {
        boolean shouldDisableDependents = super.shouldDisableDependents();
        String value = getValue();
        return shouldDisableDependents || value == null || containtString(value);
    }
}
