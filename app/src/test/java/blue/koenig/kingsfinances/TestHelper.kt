package blue.koenig.kingsfinances

import com.koenig.commonModel.User
import com.koenig.commonModel.finance.CostDistribution
import org.joda.time.DateTime

/**
 * Created by Thomas on 02.01.2018.
 */

object TestHelper {
    var milena = User("Milena")
    var thomas = User("Thomas")

    fun makeCostDistribution(theoryThomas: Int, realThomas: Int, theoryMilena: Int, realMilena: Int): CostDistribution {
        val costDistribution = CostDistribution()
        costDistribution.putCosts(thomas, realThomas, theoryThomas)
        costDistribution.putCosts(milena, realMilena, theoryMilena)
        return costDistribution
    }

    fun getDay(year: Int, month: Int, day: Int): DateTime {
        return DateTime(year, month, day, 0, 0)
    }




}
