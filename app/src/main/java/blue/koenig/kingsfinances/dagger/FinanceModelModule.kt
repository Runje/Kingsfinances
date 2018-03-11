package blue.koenig.kingsfinances.dagger

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import blue.koenig.kingsfamilylibrary.model.communication.ServerConnection
import blue.koenig.kingsfamilylibrary.view.family.LoginHandler
import blue.koenig.kingsfinances.features.category_statistics.CategoryStatisticsDbRepository
import blue.koenig.kingsfinances.features.category_statistics.CategoryStatisticsPresenter
import blue.koenig.kingsfinances.features.category_statistics.CategoryStatisticsRepository
import blue.koenig.kingsfinances.features.expenses.ExpensesPresenter
import blue.koenig.kingsfinances.features.pending_operations.OperationExecutor
import blue.koenig.kingsfinances.features.statistics.StatisticsPresenter
import blue.koenig.kingsfinances.model.FinanceContextConfig
import blue.koenig.kingsfinances.model.FinanceModel
import blue.koenig.kingsfinances.model.FinanceUserService
import blue.koenig.kingsfinances.model.calculation.DebtsCalculator
import blue.koenig.kingsfinances.model.calculation.IncomeCalculator
import blue.koenig.kingsfinances.model.database.*
import com.koenig.commonModel.FamilyConfig
import com.koenig.commonModel.Repository.*
import com.koenig.commonModel.User
import com.koenig.commonModel.finance.FinanceConfig
import com.koenig.commonModel.finance.features.StandingOrderExecutor
import com.koenig.commonModel.finance.statistics.AssetsCalculator
import com.koenig.commonModel.finance.statistics.CategoryCalculator
import com.koenig.commonModel.finance.statistics.CompensationCalculator
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import org.joda.time.YearMonth
import javax.inject.Named
import javax.inject.Singleton

/**
 * Created by Thomas on 19.11.2017.
 */
@Module
class FinanceModelModule {
    // TODO: aufteilen in kleinere module
    companion object {
        const val income_delta = "income_delta"
        const val income_absolute = "income_absolute"
        const val debts_delta = "debts_delta"
        const val debts_absolute = "debts_absolute"
    }

    @Provides
    @Singleton
    fun provideFinanceModel(compensationCalculator: CompensationCalculator, config: FinanceConfig, debtsCalculator: DebtsCalculator, connection: ServerConnection, context: Context, handler: LoginHandler, database: FinanceDatabase, service: FinanceUserService, assetsCalculator: AssetsCalculator, incomeCalculator: IncomeCalculator, categoryCalculator: CategoryCalculator, standingOrderExecutor: StandingOrderExecutor): FinanceModel = FinanceModel(compensationCalculator, config, connection, context, handler, database, service, assetsCalculator, incomeCalculator, categoryCalculator, standingOrderExecutor, debtsCalculator)

    @Provides
    @Singleton
    fun provideDatabase(context: Context, userService: FinanceUserService, config: FinanceConfig): FinanceDatabase = FinanceDatabase(context = context, userService = userService::getUserFromId, config = config)

    @Provides
    @Singleton
    fun provideSQLiteDatabase(database: FinanceDatabase) = database.writableDatabase

    @Provides
    fun provideFamilyMembers(config: FamilyConfig): List<User> = config.familyMembers

    @Provides
    fun provideUserService(config: FinanceConfig): FinanceUserService = FinanceUserService(config.familyMembers)

    @Provides
    fun provideStatisticsPresenter(assetsCalculator: AssetsCalculator, incomeCalculator: IncomeCalculator, database: FinanceDatabase, config: FinanceConfig): StatisticsPresenter = StatisticsPresenter(assetsCalculator, incomeCalculator, database.goalTable, config.familyMembers)

    @Provides
    fun provideExpensesPresenter(config: FinanceConfig, connection: ServerConnection, expensesRepository: ExpensesRepository, debtsCalculator: DebtsCalculator, operationExecutor: OperationExecutor): ExpensesPresenter = ExpensesPresenter(expensesRepository, config.familyMembers, connection, config, debtsCalculator, operationExecutor)

    @Provides
    @Singleton
    fun provideDebtsCalculator(database: FinanceDatabase, @Named(debts_delta) deltaRepository: MonthStatisticsRepository, @Named(debts_absolute) absoluteRepository: MonthStatisticsRepository) = DebtsCalculator(database.expensesTable, deltaRepository, absoluteRepository, Observable.just(YearMonth()))

    @Provides
    @Singleton
    fun provideAssetsCalculator(database: FinanceDatabase, config: FinanceConfig, assetsRepository: AssetsRepository): AssetsCalculator = AssetsCalculator(database.bankAccountTable, config.startDateObservable.map { it }, Observable.just(YearMonth()), assetsRepository)

    @Provides
    @Singleton
    @Named(income_delta)
    fun provideDeltaIncomeRepo(db: SQLiteDatabase): MonthStatisticsRepository = MonthStatisticDbRepository(MonthStatisticAndroidTable(income_delta, db))

    @Provides
    @Singleton
    @Named(income_absolute)
    fun provideAbsoluteIncomeRepo(db: SQLiteDatabase): MonthStatisticsRepository = MonthStatisticDbRepository(MonthStatisticAndroidTable(income_absolute, db))

    @Provides
    @Singleton
    @Named(debts_delta)
    fun provideDeltaDebtsRepo(db: SQLiteDatabase): MonthStatisticsRepository = MonthStatisticDbRepository(MonthStatisticAndroidTable(debts_delta, db))

    @Provides
    @Singleton
    @Named(debts_absolute)
    fun provideAbsoluteDebtsRepo(db: SQLiteDatabase): MonthStatisticsRepository = MonthStatisticDbRepository(MonthStatisticAndroidTable(debts_absolute, db))

    @Provides
    @Singleton
    fun provideIncomeRepository(@Named(income_delta) deltaRepository: MonthStatisticsRepository, @Named(income_absolute) absoluteRepository: MonthStatisticsRepository): IncomeRepository = IncomeDbRepository(deltaRepository, absoluteRepository)

    @Provides
    @Singleton
    fun provideIncomeCalculator(database: FinanceDatabase, config: FinanceConfig, incomeRepository: IncomeRepository): IncomeCalculator = IncomeCalculator(database.expensesTable, config.startDateObservable, incomeRepository)

    @Provides
    @Singleton
    fun provideCategoryCalculator(database: FinanceDatabase, categoryRepository: CategoryRepository): CategoryCalculator = CategoryCalculator(database.expensesTable, categoryRepository, Observable.just(YearMonth()))

    @Provides
    @Singleton
    fun provideCategoryRepository(database: FinanceDatabase): CategoryRepository = CategoryAndroidRepository(database.categoryTable, database.writableDatabase)

    @Provides
    fun provideCategoryStatisticsPresenter(categoryStatisticsRepository: CategoryStatisticsRepository, database: FinanceDatabase, config: FinanceConfig, connection: ServerConnection): CategoryStatisticsPresenter = CategoryStatisticsPresenter(categoryStatisticsRepository, database.goalTable, config, database.pendingTable, connection)

    @Provides
    fun provideCategoryStatisticsRepository(categoryRepository: CategoryRepository, goalRepository: GoalRepository): CategoryStatisticsRepository = CategoryStatisticsDbRepository(categoryRepository, goalRepository)

    @Provides
    fun provideStandingOrderExecutor(standingOrderRepository: StandingOrderRepository, expensesRepository: ExpensesRepository): StandingOrderExecutor = StandingOrderExecutor(standingOrderRepository = standingOrderRepository, expensesTable = expensesRepository)

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
    fun provideAssetsRepository(database: FinanceDatabase, bankAccountRepository: BankAccountRepository): AssetsRepository = AssetsAndroidRepository(database.writableDatabase, bankAccountRepository)

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
    fun provideFamilyConfig(context: Context): FamilyConfig = provideFinanceConfig(context)

    @Provides
    @Singleton
    fun provideCompensationCalculator(config: FinanceConfig, expensesRepository: ExpensesRepository, categoryCalculator: CategoryCalculator, allAssetsCalculator: AssetsCalculator): CompensationCalculator = CompensationCalculator(expensesRepository, categoryCalculator.deltaStatisticsForAll, allAssetsCalculator.deltaAssetsForAll, config)
}
