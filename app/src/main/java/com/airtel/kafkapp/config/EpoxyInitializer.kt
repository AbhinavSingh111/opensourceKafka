package com.airtel.kafkapp.config

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.EpoxyController
import com.airtel.data.config.initializers.AppInitializer

class EpoxyInitializer : AppInitializer {
    override fun init(application: Application) {
        // Make EpoxyController async
        val handlerThread = HandlerThread("epoxy")
        handlerThread.start()

        Handler(handlerThread.looper).also {
            EpoxyController.defaultDiffingHandler = it
            EpoxyController.defaultModelBuildingHandler = it
        }

        EpoxyController.setGlobalDuplicateFilteringDefault(true)

        // Also setup Carousel to use a more sane snapping behavior
        Carousel.setDefaultGlobalSnapHelperFactory(null)

        EpoxyController.setGlobalDebugLoggingEnabled(true)
    }
}
