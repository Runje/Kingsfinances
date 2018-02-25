package blue.koenig.kingsfinances

import android.support.test.InstrumentationRegistry
import blue.koenig.kingsfinances.model.FinanceConfig
import blue.koenig.kingsfinances.model.FinanceContextConfig
import blue.koenig.kingsfinances.model.database.FinanceDatabase
import com.koenig.FamilyConstants
import com.koenig.commonModel.database.UserService
import org.junit.Assert.assertEquals
import org.junit.Before
import java.sql.SQLException

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */

open class DatabaseTests {

    protected lateinit var financeDatabase: FinanceDatabase
    protected lateinit var config: FinanceConfig

    @Before
    @Throws(SQLException::class)
    fun setup() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        config = FinanceContextConfig(appContext)
        (config as FinanceContextConfig).init()

        Helper.init(config)
        assertEquals("blue.koenig.kingsfinances", appContext.packageName)

        financeDatabase = FinanceDatabase(appContext, "TestDatabase.sqlite", userService = UserService { _ -> FamilyConstants.ALL_USER }, config = config)
        financeDatabase.deleteAllEntrys()
    }

}
