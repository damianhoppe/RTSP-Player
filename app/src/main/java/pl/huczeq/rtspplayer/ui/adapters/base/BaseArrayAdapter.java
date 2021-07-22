package pl.huczeq.rtspplayer.ui.adapters.base;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseArrayAdapter<T> extends BaseAdapter implements Filterable {

    private Context context;
    private LayoutInflater layoutInflater;
    private T noneItem;
    private @LayoutRes int resource;
    private @IdRes int fieldId = 0;
    private @StringRes int emptyFieldResource;
    private List<T> dataSet;

    public BaseArrayAdapter(Context context, boolean noneEnabled, @LayoutRes int resource, @IdRes int fieldId, @StringRes int emptyFieldResource) {
        this(context, noneEnabled, resource, fieldId, emptyFieldResource, new ArrayList<>());
    }

    public BaseArrayAdapter(Context context, @LayoutRes int resource) {
        this(context, resource, new ArrayList<>());
    }

    public BaseArrayAdapter(Context context, @LayoutRes int resource, List<T> dataSet) {
        this(context, false, resource, 0, 0, dataSet);
    }

    public BaseArrayAdapter(Context context, boolean noneEnabled, @LayoutRes int resource, @IdRes int fieldId, @StringRes int emptyFieldResource, List<T> dataSet) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.resource = resource;
        this.fieldId = fieldId;
        this.emptyFieldResource = emptyFieldResource;
        this.dataSet = dataSet;
        if(noneEnabled)
            this.noneItem = buildNoneItem();
    }

    protected abstract T buildNoneItem();

    @Override
    public int getCount() {
        if(noneItem != null)
            return this.dataSet.size() + 1;
        return this.dataSet.size();
    }

    @Override
    public T getItem(int i) {
        if(noneItem != null)
            if(i==0)
                return this.noneItem;
            else
                return this.dataSet.get(i - 1);
        return this.dataSet.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final View view;
        final TextView text;
        if (convertView == null) {
            view = layoutInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        try {
            if (fieldId == 0) {
                text = (TextView) view;
            } else {
                text = view.findViewById(fieldId);
                if (text == null) {
                    throw new RuntimeException("Failed to find view with ID "
                            + context.getResources().getResourceName(fieldId)
                            + " in item layout");
                }
            }
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }
        final T item;

        if(noneItem != null && i == 0) {
            text.setText(emptyFieldResource);
            return view;
        }
        item = getItem(i);
        final String itemField = itemToString(item);
        text.setText(itemField);
        return view;
    }

    public String itemToString(T item) {
        return item.toString();
    }

    @Override
    public Filter getFilter() {
        return new ArrayFilter();
    }

    public void updateDataSet(List<T> newDataSet) {
        if(this.dataSet == newDataSet)
            return;
        this.dataSet = newDataSet;
        if(newDataSet == null) {
            notifyDataSetInvalidated();
        }else {
            notifyDataSetChanged();
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    public boolean isNone(T item) {
        return this.noneItem == item;
    }

    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            final FilterResults results = new FilterResults();
            results.values = dataSet;
            results.count = dataSet.size();
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
        }
    }
}
