package Settings.Conditions;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by split on 2016-03-01.
 */
public class ActiveCallCondition extends BaseCondition {
    AudioManager mAudioManager;
    public ActiveCallCondition(Context context, boolean value)
    {
        super(value, context);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    @Override
    public boolean evaluate() {
        return getValue() && mAudioManager.getMode() == AudioManager.MODE_IN_CALL;
    }
}
