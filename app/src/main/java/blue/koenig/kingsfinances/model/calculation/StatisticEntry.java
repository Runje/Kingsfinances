package blue.koenig.kingsfinances.model.calculation;

import com.koenig.commonModel.Byteable;
import com.koenig.commonModel.User;
import com.koenig.commonModel.finance.Balance;
import com.koenig.commonModel.finance.CostDistribution;
import com.koenig.commonModel.finance.Costs;

import org.joda.time.DateTime;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Thomas on 28.12.2017.
 */

public class StatisticEntry extends Byteable {
    private DateTime date;
    /**
     * Negative value means debts and positive means credit
     */
    private Map<User, Integer> entryMap;

    public StatisticEntry(ByteBuffer buffer) {
        this.date = byteToDateTime(buffer);
        this.entryMap = bytesToEntryMap(buffer);
    }

    public StatisticEntry() {
        this.entryMap = new HashMap<>();
    }

    public StatisticEntry(StatisticEntry statisticEntry) {
        date = statisticEntry.getDate();
        entryMap = new HashMap<>(statisticEntry.entryMap);
    }

    public StatisticEntry(DateTime date, CostDistribution costDistribution) {
        this(date);
        addCostDistribution(costDistribution);
    }

    public StatisticEntry(DateTime date) {
        this();
        this.date = date;
    }

    public StatisticEntry(DateTime date, Map<User, Integer> entryMap) {
        this.date = date;
        this.entryMap = new HashMap<>(entryMap);
    }

    public StatisticEntry(Balance balance, List<User> users) {
        this(balance.getDate());
        // distribute equally
        int n = users.size();
        int distributed = 0;
        int value = balance.getBalance() / n;
        for (int i = 0; i < n; i++) {
            User user = users.get(i);
            if (i == n - 1) {
                // last one gets the rest
                value = balance.getBalance() - distributed;
            }

            entryMap.put(user, value);
            distributed += value;
        }
    }

    public static byte[] entryMapToBytes(Map<User, Integer> map) {
        ByteBuffer buffer = ByteBuffer.allocate(getEntryMapLength(map));
        buffer.putShort((short) map.size());
        for (Map.Entry<User, Integer> entry : map.entrySet()) {
            User user = entry.getKey();
            Integer integer = entry.getValue();
            user.writeBytes(buffer);
            buffer.putInt(integer == null ? 0 : integer);
        }

        return buffer.array();
    }

    public static HashMap<User, Integer> bytesToEntryMap(ByteBuffer buffer) {
        short size = buffer.getShort();
        HashMap<User, Integer> result = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            User user = new User(buffer);
            int integer = buffer.getInt();
            result.put(user, integer);
        }

        return result;
    }

    private static short getEntryMapLength(Map<User, Integer> map) {
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
            Integer integer = entryMap.get(user);
            int oldDebts = integer == null ? 0 : integer;
            Costs costs = distribution.get(user);
            int newDebts = costs.Theory - costs.Real;
            int debts = inverse ? oldDebts - newDebts : oldDebts + newDebts;
            entryMap.put(user, debts);
        }
    }

    private void addTheoryCosts(CostDistribution costDistribution, boolean inverse) {
        Map<User, Costs> distribution = costDistribution.getDistribution();
        for (User user : distribution.keySet()) {
            Integer integer = entryMap.get(user);
            int oldDebts = integer == null ? 0 : integer;
            Costs costs = distribution.get(user);
            int newDebts = costs.Theory;
            int debts = inverse ? oldDebts - newDebts : oldDebts + newDebts;
            entryMap.put(user, debts);
        }
    }

    public int getEntryFor(User user) {
        Integer integer = entryMap.get(user);
        return integer == null ? 0 : integer;
    }

    public Integer getEntryForNullabe(User user) {
        return entryMap.get(user);
    }

    public Map<User, Integer> getEntryMap() {
        return entryMap;
    }

    @Override
    public int getByteLength() {
        return getDateLength() + getEntryMapLength(entryMap);
    }

    @Override
    public void writeBytes(ByteBuffer buffer) {
        writeDateTime(date, buffer);
        buffer.put(entryMapToBytes(entryMap));
    }

    public void addEntry(StatisticEntry statisticEntryDelta) {
        addEntry(statisticEntryDelta, false);
    }

    private void addEntry(StatisticEntry statisticEntryDelta, boolean inverse) {
        for (User user : statisticEntryDelta.entryMap.keySet()) {
            Integer integer = entryMap.get(user);
            int oldDebts = integer == null ? 0 : integer;
            int deltaDebts = statisticEntryDelta.entryMap.get(user);
            int newDebts = inverse ? oldDebts - deltaDebts : oldDebts + deltaDebts;
            entryMap.put(user, newDebts);
        }
    }

    public void subtractEntry(StatisticEntry statisticEntry) {
        addEntry(statisticEntry, true);
    }


    public int getSum() {
        int sum = 0;
        for (User user : entryMap.keySet()) {
            sum += entryMap.get(user);
        }

        return sum;
    }

    public void subtractTheoryCosts(CostDistribution costDistribution) {
        addTheoryCosts(costDistribution, true);
    }

    public void addTheoryCosts(CostDistribution costDistribution) {
        addTheoryCosts(costDistribution, false);
    }

    public void putEntry(User user, int value) {
        entryMap.put(user, value);
    }
}
