package org.stream.split.voicenotification.Enities;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by B on 2015-11-05.
 */
public class HistoryNotificationEntity extends NotificationEntity {
        long mId;
        long mOccurrenceTime;
        String mTinkerText;

        public long getID() {
                return mId;
        }

        public void setID(long ID) {
                this.mId = ID;
        }


        public String getTinkerText() {
                return mTinkerText;
        }

        public void setTinkerText(String TinkerText) {
                this.mTinkerText = TinkerText;
        }

        public long getOccurrenceTime() {
                return mOccurrenceTime;
        }

        public void setOccurrenceTime(long OccurrenceTime) {
                this.mOccurrenceTime = OccurrenceTime;
        }

        public HistoryNotificationEntity(int sbnId, String packageName, String applicationLabel, long occurrenceTime) {
                super(sbnId, packageName, applicationLabel);
                mOccurrenceTime = occurrenceTime;
        }
}
