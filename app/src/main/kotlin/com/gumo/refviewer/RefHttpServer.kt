package com.gumo.refviewer

import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class RefHttpServer(private val port: Int = 8077) {
    private var serverSocket: ServerSocket? = null
    private var running = false
    private val watchFolder = "/storage/emulated/0/Documents/references"

    fun start() {
        if (running) return
        running = true
        Thread {
            try {
                serverSocket = ServerSocket(port)
                while (running) {
                    val client = serverSocket?.accept() ?: break
                    Thread { handle(client) }.start()
                }
            } catch (_: Exception) {}
        }.start()
    }

    fun stop() {
        running = false
        serverSocket?.close()
    }

    private fun handle(client: Socket) {
        try {
            val request = client.getInputStream().bufferedReader().readLine() ?: return
            val path = request.split(" ").getOrElse(1) { "/" }
            val body = when {
                path.startsWith("/list") -> listFiles()
                else -> """{"error":"unknown path"}"""
            }
            PrintWriter(client.getOutputStream()).apply {
                println("HTTP/1.1 200 OK")
                println("Content-Type: application/json; charset=utf-8")
                println("Content-Length: ${body.toByteArray().size}")
                println()
                print(body)
                flush()
            }
        } catch (_: Exception) {
        } finally {
            client.close()
        }
    }

    private fun listFiles(): String {
        val folder = File(watchFolder)
        val files = folder.listFiles { f ->
            f.isFile && f.extension.lowercase() in listOf("md", "html", "htm", "txt")
        } ?: emptyArray()

        val items = files.sortedByDescending { it.lastModified() }.map { f ->
            val item = FileItem.from(f)
            val tagsJson = item.tags.joinToString(",") { "\"$it\"" }
            """{"name":"${f.name}","title":"${item.displayTitle}","tags":[$tagsJson]}"""
        }
        return "[${items.joinToString(",")}]"
    }
}
