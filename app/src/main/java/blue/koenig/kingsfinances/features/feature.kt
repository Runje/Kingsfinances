package blue.koenig.kingsfinances.features

import io.reactivex.disposables.CompositeDisposable
import org.slf4j.LoggerFactory

/**
 * Created by Thomas on 04.02.2018.
 */
abstract class FamilyPresenter<S : FamilyState, T : FamilyView<S>> {
    protected val logger = LoggerFactory.getLogger(this.javaClass.simpleName)!!
    protected val disposables = CompositeDisposable()
    protected lateinit var state: S
    protected var view: T? = null
    fun attachView(view: T) {
        this.view = view
        start()
    }

    abstract fun start()
    abstract fun stop()

    fun detachView() {
        this.view = null
        disposables.clear()
        stop()
    }

    fun update(newState: S) {
        state = newState
        view?.render(state)
    }
}

interface FamilyView<T : FamilyState> {
    fun render(state: T)
}

interface FamilyState