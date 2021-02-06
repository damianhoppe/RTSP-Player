package pl.huczeq.rtspplayer.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import pl.huczeq.rtspplayer.R;
import pl.huczeq.rtspplayer.data.objects.urls.Model;

public class ModelsSpinnerAdapter extends ArrayAdapter<Model> {
    Context context;
    List<Model> modelList;

    public ModelsSpinnerAdapter(Context context, List<Model> modelList) {
        super(context, R.layout.item_spinner_list, modelList);
        this.context = context;
        this.modelList = modelList;
    }

    @Override
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        TextView tv = (TextView) super.getDropDownView(i, view, viewGroup);
        tv.setText(this.modelList.get(i).getName());
        return tv;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView tv = (TextView) super.getView(i, view, viewGroup);
        tv.setText(this.modelList.get(i).getName());
        tv.setPadding(0,0,0,0);
        tv.setTextColor(context.getColor(R.color.textColor));
        return tv;
    }

    @Override
    public int getCount() {
        return this.modelList.size();
    }

    @Override
    public Model getItem(int i) {
        return this.modelList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setModelsList(List<Model> modelLists, Spinner spinner) {
        this.modelList = modelLists;
        notifyDataSetChanged();
    }
}
