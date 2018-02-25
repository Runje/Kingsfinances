package blue.koenig.kingsfinances.model

import android.content.Context
import blue.koenig.kingsfamilylibrary.model.FamilyConfig
import blue.koenig.kingsfamilylibrary.model.FamilyContextConfig
import blue.koenig.kingsfinances.R

/**
 * Created by Thomas on 16.02.2018.
 */

interface FinanceConfig : FamilyConfig {
    val overallString: String
    val futureString: String
    val compensationName: String
    val compensationCategory: String

}

class FinanceContextConfig(context: Context) : FamilyContextConfig(context), FinanceConfig {
    override val overallString: String = context.getString(R.string.overall)
    override val futureString: String = context.getString(R.string.future)
    override val compensationName: String = context.getString(R.string.compensation)
    override val compensationCategory: String = context.getString(R.string.compensation_category)

}