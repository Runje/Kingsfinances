package blue.koenig.kingsfinances.model

import android.content.Context
import blue.koenig.kingsfamilylibrary.model.FamilyContextConfig
import blue.koenig.kingsfinances.R
import com.koenig.commonModel.finance.FinanceConfig

/**
 * Created by Thomas on 16.02.2018.
 */



class FinanceContextConfig(context: Context) : FamilyContextConfig(context), FinanceConfig {
    override val overallString: String = context.getString(R.string.overall)
    override val futureString: String = context.getString(R.string.future)
    override val compensationName: String = context.getString(R.string.compensation)
    override val compensationCategory: String = context.getString(R.string.compensation_category)

}