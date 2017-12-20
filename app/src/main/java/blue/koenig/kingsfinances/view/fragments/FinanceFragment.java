package blue.koenig.kingsfinances.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.koenig.commonModel.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;

import blue.koenig.kingsfinances.dagger.FinanceApplication;
import blue.koenig.kingsfinances.model.FinanceModel;

/**
 * Created by Thomas on 19.12.2017.
 */

public abstract class FinanceFragment extends Fragment{
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    @Inject
    FinanceModel model;
    protected boolean initialized;
    protected List<User> familyMembers;

    public FinanceFragment()
    {
        // Required empty public constructor
        logger.info("Constructor finance fragment");
    }

    @Override
    public void onAttach(Context context)
    {
        logger.info("On Attach Context");
        super.onAttach(context);
        ((FinanceApplication) getActivity().getApplication()).getFinanceAppComponent().inject(this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        logger.info("Creating finance fragment");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        logger.info("Resume finance fragment");
        update();
    }

    protected abstract void update();

    public void updateFamilyMembers(List<User> members) {
        familyMembers = members;
        if (getView() == null) return;
        init(getView());
    }

    protected abstract void init(View view);
}
