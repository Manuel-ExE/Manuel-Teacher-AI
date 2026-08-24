package com.manuel.tai.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File

object PdfExporter {
    fun write(context: Context, fileName: String, text: String): File {
        val file = File(File(context.cacheDir, "exports").apply { mkdirs() }, "$fileName.pdf")
        val document = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f }
        val pageWidth = 595
        val pageHeight = 842
        val lines = text.replace("\r", "").split("\n")
        var pageNumber = 1
        var index = 0
        while (index < lines.size) {
            val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create())
            var y = 40f
            while (index < lines.size && y < pageHeight - 35) {
                val line = lines[index].take(95)
                page.canvas.drawText(line, 32f, y, paint)
                y += 16f
                index++
            }
            document.finishPage(page)
        }
        if (lines.isEmpty()) document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()).also { document.finishPage(it) }
        file.outputStream().use { document.writeTo(it) }
        document.close()
        return file
    }
}
