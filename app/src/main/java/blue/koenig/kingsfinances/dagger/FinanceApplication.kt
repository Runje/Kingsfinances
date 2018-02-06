package blue.koenig.kingsfinances.dagger

import blue.koenig.kingsfamilylibrary.FamilyApplication
import blue.koenig.kingsfamilylibrary.dagger.AppModule
import org.slf4j.LoggerFactory

/**
 * Created by Thomas on 17.09.2017.
 */

class FinanceApplication : FamilyApplication() {
    private val logger = LoggerFactory.getLogger(javaClass.simpleName)

    val financeAppComponent: FinanceAppComponent
        get() = familyAppComponent as FinanceAppComponent

    override fun initDagger() {
        familyAppComponent = initDagger(this)
    }

    protected fun initDagger(application: FinanceApplication): FinanceAppComponent {
        return DaggerFinanceAppComponent.builder().appModule(AppModule(application)).build()
    }
}
