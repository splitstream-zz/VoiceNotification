package org.stream.split.voicenotification.Interfaces;

import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Enities.BundleKeyList;

/**
 * Created by split on 2016-02-23.
 */
public interface BundleKeyOwner {
    void addBundleKey(BundleKeyEntity entity);
    BundleKeyList getBundleKeyList();
}
