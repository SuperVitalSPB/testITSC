package ru.supervital.test.itsc.data;

import android.provider.BaseColumns;

/**
 * Created by Vitaly Oantsa on 07.04.2017.
 */

public class StepsHistoryContract {
    private StepsHistoryContract() {
    };

    public static final class StepsEntry implements BaseColumns {
        public final static String TABLE_NAME = "steps_history";

        public final static String COLUMN_DATE_STEP = "date_step";
        public final static String COLUMN_COUNT = "count";
    }
}