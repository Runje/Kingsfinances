package blue.koenig.kingsfinances.view.lists

import android.view.View
import android.widget.TextView


/**
 * Created by Thomas on 21.01.2018.
 */

class SpinnerAdapter : ListAdapter<String>() {
    override val itemLayout: Int
        get() = android.R.layout.simple_dropdown_item_1line


    override fun updateView(convertView: View, item: String, pos: Int) {
        convertView.findViewById<TextView>(android.R.id.text1).text = item

    }

}
