package com.hero.depandency.journeyapps.barcodescanner.camera;

import com.hero.depandency.journeyapps.barcodescanner.SourceData;

/**
 * Callback for camera previews.
 */
public interface PreviewCallback {
    void onPreview(SourceData sourceData);
    void onPreviewError(Exception e);
}
