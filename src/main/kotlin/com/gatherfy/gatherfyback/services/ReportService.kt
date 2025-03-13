package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.entities.CustomCellStyle
import com.gatherfy.gatherfyback.repositories.AnswerRepository
import com.gatherfy.gatherfyback.repositories.FeedbackRepository
import com.gatherfy.gatherfyback.repositories.QuestionRepository
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xddf.usermodel.chart.ChartTypes
import org.apache.poi.xddf.usermodel.chart.LegendPosition
import org.apache.poi.xddf.usermodel.chart.XDDFChartData
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory
import org.apache.poi.xddf.usermodel.chart.XDDFPieChartData
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter


@Service
class ReportService(
    private val questionRepository: QuestionRepository,
    private val answerRepository: AnswerRepository,
    private val feedbackRepository: FeedbackRepository,
    private val questionService: QuestionService,
    private val answerService: AnswerService
) {

//    fun generateXlsxReport(): ByteArray {
//        val workbook = XSSFWorkbook()
//        return generateReport(workbook)
//    }

    fun generateReport(wb: Workbook, eventId: Long): ByteArray{
        val workbook = XSSFWorkbook()
        val sheet: XSSFSheet = workbook.createSheet("Feedback")

        val style = createStyles(workbook)

        // สร้าง Header
        val headerRow = sheet.createRow(0)
        headerRow.heightInPoints = 25F
        val questions = questionService.getAllQuestionByEventId(eventId)
        val feedbacks = feedbackRepository.findFeedbacksByEventId(eventId)
        val answers = answerService.getAnswerByEventId(eventId)

        // Header
        val questionsHeader = questions
            .mapIndexed { index, question -> "${index + 1}. ${question.questionText}" }

        val headers = listOf("Time Stamp","Rating","Comment") + questionsHeader
        headers.forEachIndexed { index, title ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(title)
            cell.cellStyle = style[CustomCellStyle.HEADER] as XSSFCellStyle?
        }

        // Data
        val groupedAnswers = answers.groupBy { it.feedbackId }
        val groupedAnswersByQuestion = answers.groupBy { it.questionId }

        val rows = feedbacks.map { feedback ->
            val timestamp = feedback.createdAt?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm:ss"))
            val rating = feedback.feedbackRating.toString()
            val comment = feedback.feedbackComment

            val feedbackAnswers = groupedAnswers[feedback.feedbackId] ?: emptyList()

            val orderedAnswers = questions.map { question ->
                feedbackAnswers.find { it.questionId == question.questionId }?.answerText ?: ""
            }
            listOf(timestamp, rating, comment) + orderedAnswers
        }

        rows.forEachIndexed { rowIndex, rowData ->
            val dataRow = sheet.createRow(rowIndex + 1)
            dataRow.heightInPoints = 22F
            val isEvenRow = rowIndex % 2 == 0
            rowData.forEachIndexed { colIndex, cellData ->
                val cell = dataRow.createCell(colIndex)
                cell.setCellValue(cellData)
                cell.cellStyle = if(isEvenRow) style[CustomCellStyle.ALTERNATE_ROW] as XSSFCellStyle? else style[CustomCellStyle.NORMAL_ROW] as XSSFCellStyle?
            }
        }

        // Set Column Widths
        setColumnsWidth(sheet, headers.size)

        // Freeze Header Row
        sheet.createFreezePane(0, 1)

//        createPieChart(sheet, feedbacks, headers.size + 2, 3, "Feedback Rating")
        createPieChart(sheet, feedbacks.map { it.feedbackRating }, "Feedback Rating", workbook, 3)

        questions.forEachIndexed { index, question ->
            if(question.questionType == "rating"){
                val previousPie = (index + 1) * 12 + 5
                val questionAnswers = groupedAnswersByQuestion[question.questionId] ?: emptyList()
                println("questionAnswers: $questionAnswers")
                createPieChart(sheet, questionAnswers.map { it.answerText?.toInt() }, question.questionText!!, workbook, previousPie)
            }
        }


        // แปลงเป็น ByteArray
        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        outputStream.close()
        workbook.close()

        return outputStream.toByteArray()
    }

    private fun createStyles(workbook: Workbook): Map<CustomCellStyle, CellStyle> {
        val styles = mutableMapOf<CustomCellStyle, CellStyle>()

        // Header Style (Dark Purple with White Text)
        val fontWhiteBold = workbook.createFont().apply {
            bold = true
            color = IndexedColors.WHITE.index
        }

        val fontBold = workbook.createFont().apply {
            bold = true
            color = IndexedColors.BLACK.index
        }

        val headerStyle = workbook.createCellStyle().apply {
            setFont(fontWhiteBold)
            fillForegroundColor = IndexedColors.INDIGO.index // Adjust to match Google Forms
            fillPattern = FillPatternType.SOLID_FOREGROUND
//            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
//            borderBottom = BorderStyle.THIN
//            borderTop = BorderStyle.THIN
//            borderLeft = BorderStyle.THIN
//            borderRight = BorderStyle.THIN
        }
        styles[CustomCellStyle.HEADER] = headerStyle

//         Normal Cell Style (With Borders)
        val normalStyle = workbook.createCellStyle().apply {
//            fillBackgroundColor = IndexedColors.WHITE.index
        }
        styles[CustomCellStyle.NORMAL_ROW] = normalStyle

        // Alternating Row Style (Light Gray)
        val alternateRowStyle = workbook.createCellStyle().apply {
//            cloneStyleFrom(normalStyle)
            fillForegroundColor = IndexedColors.LEMON_CHIFFON.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        styles[CustomCellStyle.ALTERNATE_ROW] = alternateRowStyle

        val borderBoldCenter = workbook.createCellStyle().apply {
            setFont(fontBold)
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            alignment = HorizontalAlignment.CENTER
        }
        styles[CustomCellStyle.BORDER_BOLD_CENTER] = borderBoldCenter

        return styles
    }

    private fun setColumnsWidth(sheet: Sheet, columnCount: Int) {
        sheet.setColumnWidth(0, 256 * 20) // Timestamp column (wider)
        sheet.setColumnWidth(1, 256 * 10 ) // Rating column

        for (columnIndex in 2 until columnCount) {
            sheet.setColumnWidth(columnIndex, 256 * 35) // Other columns
        }
    }

    fun createPieChart(sheet: XSSFSheet, ratings: List<Int?>, pieTitle: String, workbook: Workbook, startRow: Int) {
        val style = createStyles(workbook)

        // Count occurrences of each rating (1-5)
        val ratingCounts = (1..5).map { rating -> ratings.count { it == rating } }

        // Write data to Excel sheet (needed for the chart)
        val chartSheet = sheet // Using the same sheet
//        val startRow = 4 // Avoid overwriting data
        val lastColNum = sheet.getRow(1).lastCellNum.toInt()

        val headers = listOf("Rating","Count")
        headers.forEachIndexed { index, title ->
            val row = chartSheet.getRow(startRow - 1 ) ?: chartSheet.createRow(startRow - 1)
            val cell = row.createCell(/* columnIndex = */ lastColNum + index + 1)
            cell.setCellValue(title)
            cell.cellStyle = style[CustomCellStyle.BORDER_BOLD_CENTER] as XSSFCellStyle?
        }

        for (i in 1..5) {
            val row = chartSheet.getRow(startRow + i - 1) ?: chartSheet.createRow(startRow + i - 1)
            row.createCell(lastColNum + 1).setCellValue(i.toDouble()) // Rating (X-Axis)
            row.createCell(lastColNum + 2).setCellValue(ratingCounts[i - 1].toDouble()) // Count (Y-Axis)
        }

        // Create drawing and anchor
        val drawing = chartSheet.createDrawingPatriarch()
        val anchor = drawing.createAnchor(0, 0, 0, 0, lastColNum + 4, startRow - 1 , lastColNum + 11, startRow + 12)

        // Create chart
        val chart = drawing.createChart(anchor)
        chart.setTitleText(pieTitle)
        chart.titleOverlay = false

        // Define Data Sources
        val categoryDataSource = XDDFDataSourcesFactory.fromNumericCellRange(
            chartSheet, CellRangeAddress(startRow, startRow + 4, lastColNum + 1, lastColNum + 1)
        )
        val valuesDataSource = XDDFDataSourcesFactory.fromNumericCellRange(
            chartSheet, CellRangeAddress(startRow, startRow + 4, lastColNum + 2, lastColNum + 2)
        )
        println("valuesDataSource: $valuesDataSource")
        // Create Pie Chart Data
        val pieChartData = chart.createData(ChartTypes.PIE, null, null)
        val series = pieChartData.addSeries(categoryDataSource, valuesDataSource)
        series.setShowLeaderLines(true)

        val legend = chart.getOrAddLegend()
        legend.position = LegendPosition.BOTTOM
        pieChartData.setVaryColors(true)
        try {
            // Plot the data
            chart.plot(pieChartData)
        } catch (e: Exception) {
            println("Error plotting the pie chart: ${e.message}")
        }
    }
}