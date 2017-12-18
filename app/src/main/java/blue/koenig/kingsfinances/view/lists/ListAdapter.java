package blue.koenig.kingsfinances.view.lists;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import blue.koenig.kingsfinances.R;
import blue.koenig.kingsfinances.model.PendingOperation;

/**
 * Created by Thomas on 26.11.2017.
 */

public abstract class ListAdapter<T> extends BaseAdapter {
    private List<T> items;

    public ListAdapter() {
        this(new ArrayList<T>());
    }
    public ListAdapter(List<T> items)
    {
        update(items);
    }

    public void update(List<T> items)
    {
        this.items = items;
        Comparator<T> comparator = getComparator();
        if (comparator != null) Collections.sort(this.items, comparator);
        notifyDataSetChanged();
    }

    protected Comparator<T> getComparator() {
        return null;
    }

    @Override
    public int getCount()
    {
        return items.size();
    }

    @Override
    public Object getItem(int position)
    {
        return items.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return items.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        T item = items.get(position);
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(getItemLayout(), null);
        }

        initView(convertView, item);
        return convertView;
    }

    protected abstract void initView(View convertView, T item);

    protected abstract @LayoutRes int getItemLayout();
}
