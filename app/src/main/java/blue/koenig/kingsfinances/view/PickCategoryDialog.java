package blue.koenig.kingsfinances.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import blue.koenig.kingsfamilylibrary.view.TextValidator;
import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 02.11.2017.
 */

public class PickCategoryDialog {

    protected Context context;
    protected PickCategoryInteractor listener;
    protected ListView listView;
    protected ArrayAdapter<String> adapter;
    protected AlertDialog dialog;
    private ListView sublistView;
    private ArrayAdapter<String> subAdapter;
    private String main;
    private String sub;

    public PickCategoryDialog(Context context, @NotNull PickCategoryInteractor listener) {
        this.context = context;
        this.listener = listener;
        main = "";
    }

    public void show() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.pick_category, null);
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, listener.getMainCategorys());
        subAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        listView = (ListView) layout.findViewById(R.id.listView_main);
        listView.setAdapter(adapter);
        sublistView = (ListView) layout.findViewById(R.id.listView_subs);
        sublistView.setAdapter(subAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String item = adapter.getItem(position);

            subAdapter.clear();
            subAdapter.addAll(listener.getSubs(item));
            main = item;
            sub = "";
        });
        Button buttonAdd = (Button) layout.findViewById(R.id.button_add_main);
        final EditText categoryToAdd = (EditText) layout.findViewById(R.id.edit_main_category);
        categoryToAdd.addTextChangedListener(new TextValidator(categoryToAdd) {
            @Override
            public void validate(TextView textView, String text) {
                if (listener.isValid(text)) {
                    textView.setError(null);
                } else {
                    textView.setError(context.getString(blue.koenig.kingsfamilylibrary.R.string.invalid));
                }
            }
        });
        buttonAdd.setOnClickListener(v -> {
            String newItem = categoryToAdd.getText().toString();
            if (listener.isValid(newItem)) {
                listener.addMainCategory(newItem);
                adapter.add(newItem);
            }
        });

        Button buttonAddSub = (Button) layout.findViewById(R.id.button_add_sub);
        final EditText subCategoryToAdd = (EditText) layout.findViewById(R.id.edit_sub_category);
        subCategoryToAdd.addTextChangedListener(new TextValidator(subCategoryToAdd) {
            @Override
            public void validate(TextView textView, String text) {
                if (listener.isValid(text)) {
                    textView.setError(null);
                } else {
                    textView.setError(context.getString(blue.koenig.kingsfamilylibrary.R.string.invalid));
                }
            }
        });
        buttonAddSub.setOnClickListener(v -> {
            String newItem = subCategoryToAdd.getText().toString();
            if (listener.isValid(newItem)) {
                listener.addSubCategory(main, newItem);
                subAdapter.add(newItem);
            }
        });

        sublistView.setOnItemClickListener((adapterView, view, i, l) -> {
            sub = subAdapter.getItem(i);
        });
        builder.setView(layout);
        builder.setTitle(R.string.category);
        builder.setNegativeButton(blue.koenig.kingsfamilylibrary.R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, null);

        dialog = builder.create();


        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialog.show();
        Button confirmButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        confirmButton.setOnClickListener((v) -> {

            // only dismiss item if its valid
            if (!main.equals("")) {
                listener.onPick(main, sub);
                dialog.dismiss();
            } else {
                Toast.makeText(context, R.string.invalid_choice, Toast.LENGTH_LONG).show();
            }
        });

    }

}
