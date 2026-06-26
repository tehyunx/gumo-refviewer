package com.gumo.refviewer

import java.io.File

data class FileItem(
    val file: File,
    val displayTitle: String,
    val tags: List<String>
) {
    companion object {
        fun from(file: File): FileItem {
            var title: String? = null
            val tags = mutableListOf<String>()
            try {
                file.bufferedReader().use { reader ->
                    val firstLine = reader.readLine()?.trimStart('﻿')?.trim()
                    if (firstLine == "---") {
                        var line = reader.readLine()
                        while (line != null && line.trim() != "---") {
                            when {
                                line.startsWith("title:") -> {
                                    val v = line.removePrefix("title:").trim().removeSurrounding("\"").removeSurrounding("'")
                                    if (v.isNotEmpty()) title = v
                                }
                                line.startsWith("tags:") -> {
                                    val tagStr = line.removePrefix("tags:").trim()
                                    if (tagStr.startsWith("[")) {
                                        tagStr.removeSurrounding("[", "]").split(",")
                                            .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                                            .filter { it.isNotEmpty() }
                                            .forEach { tags.add(it) }
                                    }
                                }
                            }
                            line = reader.readLine()
                        }
                    }
                }
            } catch (_: Exception) {}

            return FileItem(
                file = file,
                displayTitle = title ?: file.nameWithoutExtension,
                tags = tags
            )
        }
    }
}
