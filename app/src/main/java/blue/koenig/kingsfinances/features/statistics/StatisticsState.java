package blue.koenig.kingsfinances.features.statistics;

import java.util.List;

import blue.koenig.kingsfinances.model.calculation.StatisticEntry;

/**
 * Created by Thomas on 07.01.2018.
 */

public class StatisticsState {
    private final List<StatisticEntry> assets;
    // between 0 and 1
    private final float savingRate;

    private final float monthlyWin;

    private final float allWin;
    private final List<String> yearsList;

    public StatisticsState(List<StatisticEntry> assets, float savingRate, float monthlyWin, float allWin, List<String> yearsList) {
        this.assets = assets;
        this.savingRate = savingRate;
        this.monthlyWin = monthlyWin;
        this.allWin = allWin;
        this.yearsList = yearsList;
    }

    public List<StatisticEntry> getAssets() {
        return assets;
    }

    public float getSavingRate() {
        return savingRate;
    }

    public float getMonthlyWin() {
        return monthlyWin;
    }

    public float getAllWin() {
        return allWin;
    }

    public Builder toBuilder() {
        return new Builder(assets, savingRate, monthlyWin, allWin, yearsList);
    }

    public List<String> getYearsList() {
        return yearsList;
    }

    public class Builder {
        private List<StatisticEntry> assets;
        // between 0 and 1
        private float savingRate;
        private float monthlyWin;

        private float allWin;
        private List<String> yearsList;

        public Builder(List<StatisticEntry> assets, float savingRate, float monthlyWin, float allWin, List<String> yearsList) {
            this.assets = assets;
            this.savingRate = savingRate;
            this.monthlyWin = monthlyWin;
            this.allWin = allWin;
            this.yearsList = yearsList;
        }

        public Builder yearsList(List<String> yearsList) {
            this.yearsList = yearsList;
            return this;
        }

        public StatisticsState build() {
            return new StatisticsState(assets, savingRate, monthlyWin, allWin, yearsList);
        }

        public Builder assets(List<StatisticEntry> assets) {
            this.assets = assets;
            return this;
        }

        public Builder savingRate(int savingRate) {
            this.savingRate = savingRate;
            return this;
        }

        public Builder allWin(int allWin) {
            this.allWin = allWin;
            return this;
        }

        public Builder monthlyWin(int monthlyWin) {
            this.monthlyWin = monthlyWin;
            return this;
        }

    }
}
