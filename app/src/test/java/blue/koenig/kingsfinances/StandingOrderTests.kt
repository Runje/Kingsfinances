package blue.koenig.kingsfinances

import com.koenig.FamilyConstants
import com.koenig.commonModel.Frequency
import com.koenig.commonModel.finance.CostDistribution
import com.koenig.commonModel.finance.StandingOrder
import com.koenig.commonModel.finance.features.getExecutionDatesUntil
import org.joda.time.LocalDate
import org.joda.time.Period
import org.junit.Assert
import org.junit.Test
import java.util.*

/**
 * Created by Thomas on 28.01.2018.
 */

class StandingOrderTests {

    @Test
    fun makeDeterministicUUIDs() {
        val newUuid = UUID.nameUUIDFromBytes("0600ed53-1e03-421d-a6ee-60a6244d3a93".toByteArray())
        val newUuid2 = UUID.nameUUIDFromBytes("0600ed53-1e03-421d-a6ee-60a6244d3a93".toByteArray())
        Assert.assertEquals(newUuid, newUuid2)
        Assert.assertTrue(!newUuid.toString().equals("0600ed53-1e03-421d-a6ee-60a6244d3a93"))

    }

    @Test
    @Throws(Exception::class)
    fun dueDates() {
        val order = StandingOrder("id", "", "", 0, CostDistribution(), LocalDate(2015, 10, 30), FamilyConstants.UNLIMITED, Frequency.Monthly, 1, mutableMapOf())
        var dateTimes = order.getExecutionDatesUntil(LocalDate(2015, 11, 1))
        var expectedArray = arrayOf(LocalDate(2015, 10, 30))

        Assert.assertEquals(Arrays.asList(*expectedArray), dateTimes)

        dateTimes = order.getExecutionDatesUntil(LocalDate(2016, 6, 1))
        expectedArray = arrayOf(LocalDate(2015, 10, 30), LocalDate(2015, 11, 30), LocalDate(2015, 12, 30), LocalDate(2016, 1, 30), LocalDate(2016, 2, 29), LocalDate(2016, 3, 30), LocalDate(2016, 4, 30), LocalDate(2016, 5, 30))

        Assert.assertEquals(Arrays.asList(*expectedArray), dateTimes)
        val d = LocalDate(2015, 10, 1)
        Assert.assertEquals(LocalDate(2015, 11, 1), d.plusMonths(1))
    }

    @Test
    @Throws(Exception::class)
    fun dueDates2() {
        val order = StandingOrder("", "", "", 0, CostDistribution(), LocalDate(2015, 2, 2), LocalDate(2016, 2, 2), Frequency.Monthly, 1, mutableMapOf())
        var dateTimes = order.getExecutionDatesUntil(LocalDate())
        var expectedArray = mutableListOf<LocalDate>()
        var date = LocalDate(2015, 2, 2)
        for (i in 0..12) {
            expectedArray.add(date)
            date = date.plus(Period.months(1))
        }

        Assert.assertEquals(expectedArray, dateTimes)
    }


}