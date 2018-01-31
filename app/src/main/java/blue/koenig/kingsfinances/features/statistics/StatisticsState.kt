package blue.koenig.kingsfinances.features.statistics

import com.koenig.commonModel.User

/**
 * Created by Thomas on 07.01.2018.
 */

data class StatisticsState(val statistics: AssetsStatistics, // between 0 and 1
                           val savingRate: Float, val yearsList: List<String>, val position: Int, val users: List<User>)

