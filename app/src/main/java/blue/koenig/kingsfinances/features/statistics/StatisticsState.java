package blue.koenig.kingsfinances.features.statistics;

import java.util.List;

/**
 * Created by Thomas on 07.01.2018.
 */

public class StatisticsState {
    private final AssetsStatistics statistics;
    // between 0 and 1
    private final float savingRate;

    private final int position;
    private final List<String> yearsList;

    public StatisticsState(AssetsStatistics statistics, float savingRate, List<String> yearsList, int position) {
        this.statistics = statistics;
        this.savingRate = savingRate;
        this.yearsList = yearsList;
        this.position = position;
    }

    public float getSavingRate() {
        return savingRate;
    }

    public int getPosition() {
        return position;
    }

    public Builder toBuilder() {
        return new Builder(statistics, savingRate, yearsList, position);
    }

    public List<String> getYearsList() {
        return yearsList;
    }

    public AssetsStatistics getStatistics() {
        return statistics;
    }

    public class Builder {
        private int position;
        private AssetsStatistics statistics;
        // between 0 and 1
        private float savingRate;
        private List<String> yearsList;

        public Builder(AssetsStatistics statistics, float savingRate, List<String> yearsList, int position) {
            this.statistics = statistics;
            this.savingRate = savingRate;
            this.yearsList = yearsList;
            this.position = position;
        }

        public Builder yearsList(List<String> yearsList) {
            this.yearsList = yearsList;
            return this;
        }

        public StatisticsState build() {
            return new StatisticsState(statistics, savingRate, yearsList, position);
        }

        public Builder savingRate(float savingRate) {
            this.savingRate = savingRate;
            return this;
        }

        public Builder position(int position) {
            this.position = position;
            return this;
        }

        public Builder statistics(AssetsStatistics statistics) {
            this.statistics = statistics;
            return this;
        }


    }
}
