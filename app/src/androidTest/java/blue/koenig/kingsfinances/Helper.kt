package blue.koenig.kingsfinances

import org.joda.time.DateTime

/**
 * Created by Thomas on 02.01.2018.
 */

object Helper {

    fun getDay(year: Int, month: Int, day: Int): DateTime {
        return DateTime(year, month, day, 0, 0)
    }


}
