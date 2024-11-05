package com.raival.compose.file.explorer.screen.viewer

import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.base.BaseActivity
import com.raival.compose.file.explorer.common.extension.randomString

abstract class ViewerActivity : BaseActivity() {
    private var uri: Uri? = null
    private var uid: String = ""
    private var currentInstance: ViewerInstance? = null

    abstract fun onCreateNewInstance(uri: Uri, uid: String): ViewerInstance
    abstract fun onReady(instance: ViewerInstance)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (intent.data == null) {
            finish()
            return
        }

        uri = intent.data

        if (savedInstanceState != null) {
            uid = savedInstanceState.getString("uid", String.randomString(12))
        }

        if (uid.isEmpty()) {
            uid = String.randomString(12)
        }

        val instance = globalClass.viewersManager.instances.find { it.id == uid }

        if (instance == null) {
            globalClass.viewersManager.instances.add(
                onCreateNewInstance(uri!!, uid).also { currentInstance = it }
            )
        }

        checkPermissions()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("uid", uid)
    }

    override fun onDestroy() {
        super.onDestroy()
        currentInstance?.onClose()
        globalClass.viewersManager.instances.remove(currentInstance)
    }

    override fun onPermissionGranted() {
        onReady(currentInstance!!)
    }
}