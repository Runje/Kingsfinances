package blue.koenig.kingsfinances

import com.koenig.commonModel.User
import com.koenig.commonModel.finance.FinanceConfig
import org.joda.time.DateTime

/**
 * Created by Thomas on 02.01.2018.
 */

object Helper {
    var milena = User("Milena")
    var thomas = User("Thomas", "T", "König", getDay(1987, 6, 14))
    fun getDay(year: Int, month: Int, day: Int): DateTime {
        return DateTime(year, month, day, 0, 0)
    }

    fun init(config: FinanceConfig) {
        config.familyMembers.forEach {
            if (it.name == "Thomas") thomas = it
            if (it.name == "Milena") milena = it
        }
    }


}
