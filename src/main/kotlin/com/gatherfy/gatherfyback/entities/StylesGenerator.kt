package com.gatherfy.gatherfyback.entities

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.stereotype.Component

@Component
class StylesGenerator {

    fun prepareStyles(wb: Workbook): Map<CustomCellStyle, CellStyle>{
        val boldArial = createBoldFont(wb)

        val boldWithGrayBackground = createHeaderQuestionStyle(wb,boldArial)
        return mapOf(
            CustomCellStyle.BOLD_FONT_WITH_BACKGROUND to boldWithGrayBackground
        )
    }

    fun createBoldFont(wb: Workbook): Font{
        val font = wb.createFont()
        font.bold = true
        font.color = IndexedColors.WHITE.index
        return font
    }

    fun createHeaderQuestionStyle(wb: Workbook, bold: Font): CellStyle{
        val style = wb.createCellStyle()
        style.fillBackgroundColor = IndexedColors.DARK_BLUE.index
        return style
    }
}