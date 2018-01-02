package blue.koenig.kingsfinances;

import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.CostDistribution;

/**
 * Created by Thomas on 02.01.2018.
 */

public class TestHelper {
    public static User milena = new User("Milena");
    public static User thomas = new User("Thomas");

    public static CostDistribution makeCostDistribution(int theoryThomas, int realThomas, int theoryMilena, int realMilena) {
        CostDistribution costDistribution = new CostDistribution();
        costDistribution.putCosts(thomas, realThomas, theoryThomas);
        costDistribution.putCosts(milena, realMilena, theoryMilena);
        return costDistribution;
    }
}
