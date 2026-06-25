package com.gumo.refviewer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity() {
    private val watchFolder = "/storage/emulated/0/Documents/references"
    private lateinit var adapter: FileAdapter
    private lateinit var tvCount: TextView
    private var fileObserver: FileObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        tvCount = findViewById(R.id.tv_count)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        adapter = FileAdapter { file -> openFile(file) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter

        checkPermissionAndLoad()
    }

    private fun checkPermissionAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName"))
                startActivityForResult(intent, REQ_PERMISSION)
                return
            }
        }
        loadFiles()
        startWatching()
    }

    private fun loadFiles() {
        val folder = File(watchFolder)
        if (!folder.exists()) folder.mkdirs()
        val files = folder.listFiles { f ->
            f.isFile && f.extension.lowercase() in listOf("md", "html", "htm", "txt")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        val items = files.map { FileItem.from(it) }
        adapter.setFiles(items)
        tvCount.text = "${items.size}개 파일"
    }

    private fun startWatching() {
        val mask = FileObserver.CREATE or FileObserver.DELETE or
                FileObserver.MOVED_FROM or FileObserver.MOVED_TO or FileObserver.CLOSE_WRITE
        fileObserver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(File(watchFolder), mask) {
                override fun onEvent(event: Int, path: String?) = runOnUiThread { loadFiles() }
            }
        } else {
            @Suppress("DEPRECATION")
            object : FileObserver(watchFolder, mask) {
                override fun onEvent(event: Int, path: String?) = runOnUiThread { loadFiles() }
            }
        }
        fileObserver?.startWatching()
    }

    private fun openFile(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val mime = when (file.extension.lowercase()) {
            "html", "htm" -> "text/html"
            "md" -> "text/markdown"
            else -> "text/plain"
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "파일 열기"))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "새로고침")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) loadFiles()
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                loadFiles(); startWatching()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fileObserver?.stopWatching()
    }

    companion object { private const val REQ_PERMISSION = 100 }
}
