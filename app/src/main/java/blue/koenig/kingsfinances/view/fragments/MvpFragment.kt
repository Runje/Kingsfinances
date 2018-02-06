package blue.koenig.kingsfinances.view.fragments

import android.support.v4.app.Fragment
import blue.koenig.kingsfinances.features.FamilyPresenter
import blue.koenig.kingsfinances.features.FamilyState
import blue.koenig.kingsfinances.features.FamilyView
import org.slf4j.LoggerFactory
import javax.inject.Inject

/**
 * Created by Thomas on 06.02.2018.
 */
abstract class MvpFragment<S : FamilyState, V : FamilyView<S>, P : FamilyPresenter<S, V>> : Fragment() {
    protected var logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    @Inject
    lateinit var presenter: P

    override fun onResume() {
        super.onResume()
        @Suppress("UNCHECKED_CAST")
        presenter.attachView(this as V)
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }
}