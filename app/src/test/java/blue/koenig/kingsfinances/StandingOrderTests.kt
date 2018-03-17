package blue.koenig.kingsfinances

/**
 * Created by Thomas on 28.01.2018.
 */
/*
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
        val order = StandingOrder("id", "", "", 0, CostDistribution(), DateTime(2015, 10, 30, 0, 0), FamilyConstants.UNLIMITED, Frequency.Monthly, 1, mutableMapOf())
        var dateTimes = order.getExecutionDatesUntil(DateTime(2015, 11, 1, 0, 0))
        var expectedArray = arrayOf(DateTime(2015, 10, 30, 0, 0))

        Assert.assertEquals(Arrays.asList(*expectedArray), dateTimes)

        dateTimes = order.getExecutionDatesUntil(DateTime(2016, 6, 1, 0, 0))
        expectedArray = arrayOf(DateTime(2015, 10, 30, 0, 0), DateTime(2015, 11, 30, 0, 0), DateTime(2015, 12, 30, 0, 0), DateTime(2016, 1, 30, 0, 0), DateTime(2016, 2, 29, 0, 0), DateTime(2016, 3, 30, 0, 0), DateTime(2016, 4, 30, 0, 0), DateTime(2016, 5, 30, 0, 0))

        Assert.assertEquals(Arrays.asList(*expectedArray), dateTimes)
        val d = DateTime(2015, 10, 1, 0, 0)
        Assert.assertEquals(DateTime(2015, 11, 1, 0, 0), d.plusMonths(1))
    }

    @Test
    @Throws(Exception::class)
    fun dueDates2() {
        val order = StandingOrder("", "", "", 0, CostDistribution(), TestHelper.getDay(2015, 2, 2), TestHelper.getDay(2016, 2, 2), Frequency.Monthly, 1, mutableMapOf())
        var dateTimes = order.getExecutionDatesUntil(DateTime.now())
        var expectedArray = mutableListOf<DateTime>()
        var date = TestHelper.getDay(2015, 2, 2)
        for (i in 0..12) {
            expectedArray.add(date)
            date = date.plus(Period.months(1))
        }

        Assert.assertEquals(expectedArray, dateTimes)
    }


}*/