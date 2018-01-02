package blue.koenig.kingsfinances.model.calculation;

import com.koenig.commonModel.Byteable;
import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.CostDistribution;
import com.koenig.commonModel.finance.Costs;

import org.joda.time.DateTime;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Thomas on 28.12.2017.
 */

public class Debts extends Byteable {
    private DateTime date;
    /**
     * Negative value means debts and positive means credit
     */
    private Map<User, Integer> debtsMap;

    public Debts(ByteBuffer buffer) {
        this.date = byteToDateTime(buffer);
        this.debtsMap = bytesToDebtsMap(buffer);
    }

    public Debts() {
        this.debtsMap = new HashMap<>();
    }

    public Debts(Debts debts) {
        date = debts.getDate();
        debtsMap = new HashMap<>(debts.debtsMap);
    }

    public Debts(DateTime date, CostDistribution costDistribution) {
        this(date);
        addCostDistribution(costDistribution);
    }

    public Debts(DateTime date) {
        this();
        this.date = date;
    }

    public Debts(DateTime date, Map<User, Integer> debts) {
        this.date = date;
        this.debtsMap = new HashMap<>(debts);
    }

    public static byte[] debtsMapToBytes(Map<User, Integer> map) {
        ByteBuffer buffer = ByteBuffer.allocate(getDebtsMapLength(map));
        buffer.putShort((short) map.size());
        for (Map.Entry<User, Integer> entry : map.entrySet()) {
            User user = entry.getKey();
            Integer integer = entry.getValue();
            user.writeBytes(buffer);
            buffer.putInt(integer == null ? 0 : integer);
        }

        return buffer.array();
    }

    public static HashMap<User, Integer> bytesToDebtsMap(ByteBuffer buffer) {
        short size = buffer.getShort();
        HashMap<User, Integer> result = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            User user = new User(buffer);
            int integer = buffer.getInt();
            result.put(user, integer);
        }

        return result;
    }

    private static short getDebtsMapLength(Map<User, Integer> map) {
        short size = 2;
        for (Map.Entry<User, Integer> entry : map.entrySet()) {
            User user = entry.getKey();
            size += user.getByteLength() + 4;
        }
        return size;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public void addCostDistribution(CostDistribution costDistribution) {
        addCostDistribution(costDistribution, false);
    }

    public void subtractCostDistribution(CostDistribution costDistribution) {
        addCostDistribution(costDistribution, true);
    }

    private void addCostDistribution(CostDistribution costDistribution, boolean inverse) {
        Map<User, Costs> distribution = costDistribution.getDistribution();
        for (User user : distribution.keySet()) {
            Integer integer = debtsMap.get(user);
            int oldDebts = integer == null ? 0 : integer;
            Costs costs = distribution.get(user);
            int newDebts = costs.Theory - costs.Real;
            int debts = inverse ? oldDebts - newDebts : oldDebts + newDebts;
            debtsMap.put(user, debts);
        }
    }

    public int getDebtsFor(User user) {
        Integer integer = debtsMap.get(user);
        return integer == null ? 0 : integer;
    }

    public Map<User, Integer> getDebts() {
        return debtsMap;
    }

    @Override
    public int getByteLength() {
        return getDateLength() + getDebtsMapLength(debtsMap);
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        writeDateTime(date, buffer);
        buffer.put(debtsMapToBytes(debtsMap));
    }

    public void addDebts(Debts debtsDelta) {
        addDebts(debtsDelta, false);
    }

    private void addDebts(Debts debtsDelta, boolean inverse) {
        for (User user : debtsDelta.debtsMap.keySet()) {
            Integer integer = debtsMap.get(user);
            int oldDebts = integer == null ? 0 : integer;
            int deltaDebts = debtsDelta.debtsMap.get(user);
            int newDebts = inverse ? oldDebts - deltaDebts : oldDebts + deltaDebts;
            debtsMap.put(user, newDebts);
        }
    }

    public void subtractDebts(Debts debts) {
        addDebts(debts, true);
    }
}
