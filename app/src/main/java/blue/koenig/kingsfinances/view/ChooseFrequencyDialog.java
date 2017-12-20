package blue.koenig.kingsfinances.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.koenig.commonModel.Frequency;

import blue.koenig.kingsfinances.R;

/**
 * Created by Thomas on 20.12.2017.
 */

public class ChooseFrequencyDialog {
    Context context;
    FrequencyListener listener;
    private int factor;
    private Frequency frequency;

    public ChooseFrequencyDialog(Context context, int factor, Frequency frequency, FrequencyListener listener) {
        this.context = context;
        this.listener = listener;
        this.factor = factor;
        this.frequency = frequency;
    }

    public ChooseFrequencyDialog(Context context, FrequencyListener listener) {
        this(context, 1, Frequency.Monthly, listener);
    }

    public void show() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View layout = LayoutInflater.from(context).inflate(R.layout.expenses_dialog_frequency, null);
        final NumberPicker numberPicker = (NumberPicker) layout.findViewById(R.id.numberPicker);
        final RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.radiogroup_frequency);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(12);
        numberPicker.setValue(factor);


        int id = Frequency.FrequencyToIndex(frequency);
        ((RadioButton) radioGroup.getChildAt(id)).setChecked(true);
        builder.setView(layout);
        builder.setTitle(R.string.frequency);
        builder.setNegativeButton(R.string.cancel, null);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);
                Frequency frequency = Frequency.indexToFrequency(idx);
                listener.onPickFrequency(numberPicker.getValue(), frequency);
            }
        });

        builder.create().show();
    }

    public interface FrequencyListener {
        void onPickFrequency(int factor, Frequency frequency);
    }
}
