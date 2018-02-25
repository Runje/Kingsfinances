package blue.koenig.kingsfinances.dagger

import android.content.Context
import blue.koenig.kingsfamilylibrary.model.FamilyConfig
import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection
import blue.koenig.kingsfamilylibrary.view.family.LoginHandler
import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculator
import blue.koenig.kingsfinances.features.category_statistics.CategoryCalculatorService
import blue.koenig.kingsfinances.features.category_statistics.CategoryStatisticsPresenter
import blue.koenig.kingsfinances.features.category_statistics.FinanceCategoryCalculatorService
import blue.koenig.kingsfinances.features.expenses.CompensationCalculator
import blue.koenig.kingsfinances.features.expenses.ExpensesPresenter
import blue.koenig.kingsfinances.features.pending_operations.OperationExecutor
import blue.koenig.kingsfinances.features.statistics.AssetsCalculator
import blue.koenig.kingsfinances.features.statistics.AssetsCalculatorService
import blue.koenig.kingsfinances.features.statistics.FinanceAssetsCalculatorService
import blue.koenig.kingsfinances.features.statistics.StatisticsPresenter
import blue.koenig.kingsfinances.model.FinanceConfig
import blue.koenig.kingsfinances.model.FinanceContextConfig
import blue.koenig.kingsfinances.model.FinanceModel
import blue.koenig.kingsfinances.model.FinanceUserService
import blue.koenig.kingsfinances.model.calculation.DebtsCalculator
import blue.koenig.kingsfinances.model.calculation.FinanceStatisticsCalculatorService
import blue.koenig.kingsfinances.model.calculation.IncomeCalculator
import blue.koenig.kingsfinances.model.calculation.StatisticsCalculatorService
import blue.koenig.kingsfinances.model.database.*
import com.koenig.commonModel.Repository.BankAccountRepository
import com.koenig.commonModel.Repository.ExpensesRepository
import com.koenig.commonModel.Repository.GoalRepository
import com.koenig.commonModel.Repository.StandingOrderRepository
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.features.StandingOrderExecutor
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.joda.time.Period
import javax.inject.Singleton

/**
 * Created by Thomas on 19.11.2017.
 */
@Module
class FinanceModelModule {
    @Provides
    @Singleton
    internal fun provideFinanceModel(compensationCalculator: CompensationCalculator, config: FinanceConfig, debtsCalculator: DebtsCalculator, connection: ServerConnection, context: Context, handler: LoginHandler, database: FinanceDatabase, service: FinanceUserService, assetsCalculator: AssetsCalculator, incomeCalculator: IncomeCalculator, categoryCalculator: CategoryCalculator, standingOrderExecutor: StandingOrderExecutor): FinanceModel {
        return FinanceModel(compensationCalculator, config, connection, context, handler, database, service, assetsCalculator, incomeCalculator, categoryCalculator, standingOrderExecutor, debtsCalculator)
    }

    @Provides
    @Singleton
    internal fun provideDatabase(context: Context, userService: FinanceUserService, config: FinanceConfig): FinanceDatabase {
        return FinanceDatabase(context = context, userService = userService, config = config)
    }


    @Provides
    fun provideFamilyMembers(config: FamilyConfig): List<User> {
        return config.familyMembers
    }

    @Provides
    internal fun provideUserService(config: FinanceConfig): FinanceUserService {
        return FinanceUserService(config.familyMembers)
    }


    @Provides
    internal fun provideStatisticsPresenter(assetsCalculator: AssetsCalculator, incomeCalculator: IncomeCalculator, database: FinanceDatabase, config: FinanceConfig): StatisticsPresenter {
        return StatisticsPresenter(assetsCalculator, incomeCalculator, database.goalTable, config.familyMembers)
    }

    @Provides
    internal fun provideExpensesPresenter(config: FinanceConfig, connection: ServerConnection, expensesRepository: ExpensesRepository, debtsCalculator: DebtsCalculator, operationExecutor: OperationExecutor): ExpensesPresenter = ExpensesPresenter(expensesRepository, config.familyMembers, connection, config, debtsCalculator, operationExecutor)

    @Provides
    @Singleton
    fun provideDebtsCalculator(database: FinanceDatabase, config: FinanceConfig) = DebtsCalculator(Period.months(1), database.expensesTable, FinanceStatisticsCalculatorService(config, "DEBTS"))

    @Provides
    @Singleton
    internal fun provideAssetsCalculator(database: FinanceDatabase, service: AssetsCalculatorService): AssetsCalculator {
        return AssetsCalculator(Period.months(1), database.bankAccountTable, service)
    }

    @Provides
    @Singleton
    internal fun provideAssetsCalculatorService(config: FinanceConfig): AssetsCalculatorService {
        return FinanceAssetsCalculatorService(config)
    }

    @Provides
    @Singleton
    internal fun provideIncomeFinanceStatisticsCalculatorService(config: FinanceConfig): StatisticsCalculatorService {
        return FinanceStatisticsCalculatorService(config, "INCOME")
    }

    @Provides
    @Singleton
    internal fun provideIncomeCalculator(database: FinanceDatabase, service: StatisticsCalculatorService): IncomeCalculator {
        return IncomeCalculator(Period.months(1), database.expensesTable, service)
    }

    @Provides
    @Singleton
    internal fun provideCategoryCalculator(database: FinanceDatabase, service: CategoryCalculatorService): CategoryCalculator {
        return CategoryCalculator(Period.months(1), database.expensesTable, service)
    }

    @Provides
    internal fun provideCategoryCalculatorService(config: FinanceConfig, database: FinanceDatabase): CategoryCalculatorService {
        return FinanceCategoryCalculatorService(config, database.goalTable)
    }

    @Provides
    internal fun provideCategoryStatisticsPresenter(calculator: CategoryCalculator, database: FinanceDatabase, config: FinanceConfig, connection: ServerConnection): CategoryStatisticsPresenter {
        return CategoryStatisticsPresenter(calculator, database.goalTable, config, database.pendingTable, connection)
    }

    @Provides
    internal fun provideStandingOrderExecutor(standingOrderRepository: StandingOrderRepository, expensesRepository: ExpensesRepository): StandingOrderExecutor {
        return StandingOrderExecutor(standingOrderRepository = standingOrderRepository, expensesTable = expensesRepository)
    }

    @Provides
    fun provideOperationExecutor(database: FinanceDatabase, connection: ServerConnection) = OperationExecutor(connection, database.pendingTable)

    @Provides
    fun provideExpensesRepository(database: FinanceDatabase, userIdObservable: Observable<String>): ExpensesRepository = ExpensesDbRepository(database.expensesTable, userIdObservable)

    @Provides
    fun provideStandingOrderRepository(database: FinanceDatabase, userIdObservable: Observable<String>): StandingOrderRepository = StandingOrderDbRepository(database.standingOrderTable, userIdObservable)

    @Provides
    fun provideGoalRepository(database: FinanceDatabase, userIdObservable: Observable<String>): GoalRepository = GoalDbRepository(database.goalTable, userIdObservable)

    @Provides
    fun provideBankAccountRepository(database: FinanceDatabase, userIdObservable: Observable<String>): BankAccountRepository = BankAccountDbRepository(database.bankAccountTable, userIdObservable)

    @Provides
    fun provideUserIdObservable(config: FamilyConfig): Observable<String> = config.userIdObservable

    @Provides
    @Singleton
    fun provideFinanceConfig(context: Context): FinanceConfig {
        val config = FinanceContextConfig(context)
        config.init()
        return config
    }

    @Provides
    @Singleton
    internal fun provideFamilyConfig(context: Context): FamilyConfig {
        return provideFinanceConfig(context)
    }

    @Provides
    @Singleton
    fun provideComepnsationCalculator(config: FinanceConfig, expensesRepository: ExpensesRepository, categoryCalculator: CategoryCalculator, allAssetsCalculator: AssetsCalculator): CompensationCalculator = CompensationCalculator(expensesRepository, categoryCalculator.deltaStatisticsForAll, allAssetsCalculator.deltaAssetsForAll, config)
}
