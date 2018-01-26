package blue.koenig.kingsfinances.model

import com.koenig.commonModel.Item

/**
 * Created by Thomas on 21.01.2018.
 */
class Goal(name: String, val goals: MutableMap<Int, Int>, val userId: String) : Item(name) {
    fun setGoal(year: Int, goal: Int) {
        goals[year] = goal
    }
}