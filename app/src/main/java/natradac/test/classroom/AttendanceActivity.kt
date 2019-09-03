package natradac.test.classroom

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.zxing.Result
import com.opencsv.CSVWriter
import kotlinx.android.synthetic.main.activity_classroom.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AttendanceActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var mScannerView: ZXingScannerView? = null
    private var studentList = mutableListOf<Student>()

    private var status = ""
    private var title = ""

    private var recordAttendance = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classroom)

        initScanView()

        when (intent.hasExtra("FILENAME")) {
            true -> {
                readStudentData(intent.extras.get("FILENAME") as String)
                title = "วิชา " + intent.extras.get("SUBJECT").toString() + " ห้อง " + intent.extras.get("ROOM").toString()
                tvTitle.text = title
            }
            false -> {
                Toast.makeText(applicationContext, "ดาว์นโหลดไฟล์นักเรียนล้มเหลว", Toast.LENGTH_LONG)
            }
        }

        initSpinner()
        initHeaderTable()
        initListener()
    }

    private fun initListener() {
        btnExport.setOnClickListener {
            exportToCSV()
        }
    }

    override fun handleResult(rawResult: Result?) {
        Log.d("QRcode Scanner", "Read: ${rawResult?.text}")
        rawResult?.apply {
            loadStudentInfo(this.text)
        }

        //mScannerView!!.resumeCameraPreview(this)
    }

    override fun onStart() {
        super.onStart()
        mScannerView?.apply { this.startCamera() }
    }

    override fun onPause() {
        super.onPause()
        mScannerView?.apply { this.stopCamera() }
    }

    override fun onResume() {
        super.onResume()
        mScannerView?.apply {
            this.setResultHandler(this@AttendanceActivity)
            this.startCamera()
        }

    }

    private fun initHeaderTable() {

        var fakeHeader = Student("", "ชื่อ", "เลขที่", "ผล")
        addStudentRowToTable(fakeHeader)
    }

    private fun initScanView() {

        mScannerView = ZXingScannerView(this)
        mScannerView!!.setResultHandler(this)

        scanner.addView(mScannerView)

        scanner.setOnClickListener {

            mScannerView!!.resumeCameraPreview(this)
        }
    }

    private fun initSpinner() {

        var statusList = listOf("มา", "มาสาย", "ลา", "ขาด")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            statusList
        )

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        statusSpinner.adapter = adapter

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                status = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                status = "-"

            }
        }

    }

    private fun readStudentData(fileName: String) {

        var json: String? = null
        try {
            val `is` = assets.open(fileName)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        studentList = Gson().fromJson(json, Array<Student>::class.java).toMutableList()
        Log.i("Test", "StudentList $studentList")

    }

    private fun loadStudentInfo(scanCode: String) {
        studentList.firstOrNull { it.studentID == scanCode }.let {
            when (it) {
                null -> {
                    Toast.makeText(applicationContext, "กรุณาแสกนใหม่อีกครั้ง", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Log.i("loadStudentInfo", "$it")
                    it.status = status
                    display(it)
                }
            }

        }

    }

    private fun display(student: Student) {
        Log.i("loadStudentInfo", "${student.no} ${student.status}")
        addStudentRowToTable(student = student)
        record(student = student)
    }

    private fun record(student: Student) {
        recordAttendance.add(student)
    }

    private fun addStudentRowToTable(student: Student) {
        val row = TableRow(this)
        val tvNo = TextView(this)
        val tvName = TextView(this)
        val tvStatus = TextView(this)

        tvNo.text = student.no
        tvNo.textAlignment = View.TEXT_ALIGNMENT_CENTER

        tvName.text = student.name
        tvStatus.text = student.status

        table.addView(row)
        row.addView(tvNo, TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(tvName, TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f))
        row.addView(tvStatus, TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f))
    }

    private fun exportToCSV() {

        var today = today()
        var baseDir =
            android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        var fileName = "เช็คชื่อ$today.csv"
        var filePath = baseDir + File.separator + fileName
        var file = File(filePath)
        var writer: CSVWriter


        writer = if (file.exists() && !file.isDirectory) {
            var mFileWriter = FileWriter(filePath, true)
            CSVWriter(mFileWriter)
        } else {
            CSVWriter(FileWriter(filePath))
        }

        var dataTopic = arrayOf("", "เช็คชื่อ $title วันที่ ${today()}", "")
        writer.writeNext(dataTopic)

        var dataHeader = arrayOf("เลขที่", "ชื่อ", "ผล")
        writer.writeNext(dataHeader)

        for (i in recordAttendance) {
            var data = arrayOf(i.no, i.name, i.status)
            writer.writeNext(data)
        }

        writer.close()

    }

    private fun today(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
            formatter.format(localDateTime)

        } else {
            val localDateTime = Calendar.getInstance().time
            val formatter = SimpleDateFormat("ddMMyyyy")
            formatter.format(localDateTime)

        }

    }

    companion object {
        fun newIntent(context: Context, subject: String, room: String, file: String): Intent {
            return Intent(context, AttendanceActivity::class.java).apply {
                putExtra("FILENAME", file)
                putExtra("SUBJECT", subject)
                putExtra("ROOM", room)
            }
        }
    }
}