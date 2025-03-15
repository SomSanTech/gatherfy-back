package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.entities.CustomCellStyle
import com.gatherfy.gatherfyback.repositories.EventRepository
import com.gatherfy.gatherfyback.repositories.FeedbackRepository
import com.gatherfy.gatherfyback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xddf.usermodel.chart.ChartTypes
import org.apache.poi.xddf.usermodel.chart.LegendPosition
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter


@Service
class ReportService(
    private val feedbackRepository: FeedbackRepository,
    private val questionService: QuestionService,
    private val answerService: AnswerService,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) {
    fun generateReport(username: String, eventId: Long): ByteArray{
        try {
            val user = userRepository.findByUsername(username)
            val exitingEvent = eventRepository.findEventByEventOwnerAndEventId(user?.users_id, eventId)
            if (exitingEvent === null) {
                throw EntityNotFoundException("Event id $eventId does not exist")
            }
            val workbook = XSSFWorkbook()
            val sheet: XSSFSheet = workbook.createSheet("Feedback")

            val style = createStyles(workbook)

            // Create Header
            val headerRow = sheet.createRow(0)
            headerRow.heightInPoints = 25F
            val questions = questionService.getAllQuestionByEventId(eventId)
            val questionsHeader = questions
                .mapIndexed { index, question -> "${index + 1}. ${question.questionText}" }

            val headers = listOf("Time Stamp", "Rating", "Comment") + questionsHeader
            headers.forEachIndexed { index, title ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(title)
                cell.cellStyle = style[CustomCellStyle.HEADER] as XSSFCellStyle?
            }

            // Data
            val feedbacks = feedbackRepository.findFeedbacksByEventId(eventId)
            val answers = answerService.getAnswerByEventId(eventId)

            val groupedAnswers = answers.groupBy { it.feedbackId }

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
                    cell.cellStyle =
                        if (isEvenRow) style[CustomCellStyle.ALTERNATE_ROW] as XSSFCellStyle? else style[CustomCellStyle.NORMAL_ROW] as XSSFCellStyle?
                }
            }

            // Set Column Widths
            setColumnsWidth(sheet, headers.size)
            sheet.createFreezePane(0, 1)

            createPieChart(sheet, feedbacks.map { it.feedbackRating }, "Feedback Rating", workbook, 3)

            // Find question type rating to create pie chart
            val groupedAnswersByQuestion = answers.groupBy { it.questionId }
            questions.forEachIndexed { index, question ->
                if (question.questionType == "rating") {
                    val previousPie = (index + 1) * 12 + 5
                    val questionAnswers = groupedAnswersByQuestion[question.questionId] ?: emptyList()
                    createPieChart(
                        sheet,
                        questionAnswers.map { it.answerText?.toInt() },
                        question.questionText!!,
                        workbook,
                        previousPie
                    )
                }
            }

            val outputStream = ByteArrayOutputStream()
            workbook.write(outputStream)
            outputStream.close()
            workbook.close()

            return outputStream.toByteArray()
        }catch (e: EntityNotFoundException){
            throw EntityNotFoundException(e.message)
        }
    }

    fun createPieChart(sheet: XSSFSheet, ratings: List<Int?>, pieTitle: String, workbook: Workbook, startRow: Int) {
        val style = createStyles(workbook)

        // Count occurrences of each rating (1-5)
        val ratingCounts = (1..5).map { rating -> ratings.count { it == rating } }

        // Write data to Excel sheet (needed for the chart)
        val chartSheet = sheet // Using the same sheet
        val lastColNum = sheet.getRow(1).lastCellNum.toInt()

        // Collect nonzero rating data
        val filteredRatings = mutableListOf<String>()
        val filteredCounts = mutableListOf<Int>()

        val headers = listOf("Rating","Response Count")
        headers.forEachIndexed { index, title ->
            val row = chartSheet.getRow(startRow - 1 ) ?: chartSheet.createRow(startRow - 1)
            row.heightInPoints = 22F
            val cell = row.createCell(/* columnIndex = */ lastColNum + index + 1)
            cell.setCellValue(title)
            cell.cellStyle = style[CustomCellStyle.BORDER_BOLD_CENTER] as XSSFCellStyle?
        }

        for (i in 1..5) {
            val row = chartSheet.getRow(startRow + i - 1) ?: chartSheet.createRow(startRow + i - 1)
            row.heightInPoints = 22F
            val colRating =  row.createCell(lastColNum + 1)
            colRating.setCellValue(i.toDouble()) // Rating
            colRating.cellStyle = style[CustomCellStyle.BORDER_CENTER] as XSSFCellStyle?
            val colResponse = row.createCell(lastColNum + 2)
            colResponse.setCellValue(ratingCounts[i - 1].toDouble()) // Count
            colResponse.cellStyle = style[CustomCellStyle.BORDER_CENTER] as XSSFCellStyle?
        }

        for (row in startRow..(startRow + 4)) {
            val rating = chartSheet.getRow(row)?.getCell(lastColNum + 1)?.numericCellValue?.toInt()
            val count = chartSheet.getRow(row)?.getCell(lastColNum + 2)?.numericCellValue?.toInt()

            if (count!! > 0) {
                filteredRatings.add(rating.toString())
                filteredCounts.add(count)
            }
        }

        sheet.setColumnWidth(lastColNum + 1, 256 * 20)
        sheet.setColumnWidth(lastColNum + 2, 256 * 20)

        val drawing = chartSheet.createDrawingPatriarch()
        val anchor = drawing.createAnchor(0, 0, 0, 0, lastColNum + 4, startRow - 1 , lastColNum + 11, startRow + 12)
        val chart = drawing.createChart(anchor)
        chart.setTitleText(pieTitle)
        chart.titleOverlay = false

        // Create Data Sources from filtered data
        val categoryDataSource = XDDFDataSourcesFactory.fromArray(filteredRatings.toTypedArray())
        val valuesDataSource = XDDFDataSourcesFactory.fromArray(filteredCounts.toTypedArray())

        // Create Pie Chart Data
        val pieChartData = chart.createData(ChartTypes.PIE, null, null)
        val series = pieChartData.addSeries(categoryDataSource, valuesDataSource)
        series.setShowLeaderLines(true)
        series.setTitle("Rating", null)

        val legend = chart.getOrAddLegend()
        legend.position = LegendPosition.BOTTOM
        pieChartData.setVaryColors(true)
        try {
            chart.plot(pieChartData)
        } catch (e: Exception) {
            println("Error plotting the pie chart: ${e.message}")
        }
    }


    private fun createStyles(workbook: Workbook): Map<CustomCellStyle, CellStyle> {
        val styles = mutableMapOf<CustomCellStyle, CellStyle>()

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
            fillForegroundColor = IndexedColors.INDIGO.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            verticalAlignment = VerticalAlignment.CENTER

        }
        styles[CustomCellStyle.HEADER] = headerStyle

        val normalStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.WHITE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            verticalAlignment = VerticalAlignment.CENTER
        }
        styles[CustomCellStyle.NORMAL_ROW] = normalStyle

        val alternateRowStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LEMON_CHIFFON.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            verticalAlignment = VerticalAlignment.CENTER
        }
        styles[CustomCellStyle.ALTERNATE_ROW] = alternateRowStyle

        val borderCenter = workbook.createCellStyle().apply {
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }
        styles[CustomCellStyle.BORDER_CENTER] = borderCenter

        val borderBoldCenter = workbook.createCellStyle().apply {
            setFont(fontBold)
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }
        styles[CustomCellStyle.BORDER_BOLD_CENTER] = borderBoldCenter

        val alignCenter = workbook.createCellStyle().apply {
            verticalAlignment = VerticalAlignment.CENTER
        }
        styles[CustomCellStyle.ALIGNMENT_CENTER] = alignCenter

        return styles
    }

    private fun setColumnsWidth(sheet: Sheet, columnCount: Int) {
        sheet.setColumnWidth(0, 256 * 20) // Timestamp column (wider)
        sheet.setColumnWidth(1, 256 * 10 ) // Rating column

        for (columnIndex in 2 until columnCount) {
            sheet.setColumnWidth(columnIndex, 256 * 35) // Other columns
        }
    }

}