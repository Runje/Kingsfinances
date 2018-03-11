package blue.koenig.kingsfinances.model;

import com.koenig.commonModel.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Thomas on 27.12.2017.
 */

public class FinanceUserService {
    Map<String, User> userMap;

    public FinanceUserService(List<User> members) {
        setUser(members);
    }

    public void setUser(List<User> members) {
        userMap = new HashMap<>(members.size());
        for (User member : members) {
            userMap.put(member.getId(), member);
        }
    }


    public User getUserFromId(String id) throws SQLException {
        return userMap.get(id);
    }
}
