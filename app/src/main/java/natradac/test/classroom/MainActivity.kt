package natradac.test.classroom

import android.Manifest.permission.CAMERA
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_classroom.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.io.IOException
import java.nio.charset.Charset
import java.util.*


class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private val requestPermissionCode = 99

    private var mScannerView: ZXingScannerView? = null
    private var studentList = mutableListOf<Student>()

    private var status = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classroom)

        initAskPermission()
        readStudentData()

        initSpinner()
        initHeaderTable()
        initListener()
    }

    private fun initListener() {
        btnExport.setOnClickListener {
            exportToExcel()
        }
    }

    override fun handleResult(rawResult: Result?) {
        Log.d("QRcode Scanner", "Read: ${rawResult?.text}")
        rawResult?.apply {
            loadStudentInfo(this.text)
        }
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
            this.setResultHandler(this@MainActivity)
            this.setAutoFocus(true)
            this.startCamera()
        }

    }

    private fun initHeaderTable() {

        var fakeHeader = Student("", "ชื่อ", "เลขที่")
        addStudentRowToTable(fakeHeader, "ผล")
    }

    private fun initAskPermission() {

        if (!hasPermissions(this, arrayOf(CAMERA))) {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA), requestPermissionCode)
        } else {
            initScanView()
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            requestPermissionCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initScanView()
                } else {
                    Toast.makeText(applicationContext, "No Permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initScanView() {

        mScannerView = ZXingScannerView(this)
        mScannerView!!.setAutoFocus(true)
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

    private fun readStudentData() {

        var json: String? = null
        try {
            val `is` = assets.open("student.json")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        studentList = Gson().fromJson(json, Array<Student>::class.java).toMutableList()

    }

    private fun loadStudentInfo(scanCode: String) {
        studentList.firstOrNull { it.studentID == scanCode }.let {
            when (it) {
                null -> {
                    Toast.makeText(applicationContext, "กรุณาแสกนใหม่อีกครั้ง", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Log.i("loadStudentInfo", "$it")
                    display(it, status)
                }
            }

        }

    }

    private fun display(student: Student, status: String) {
        Log.i("loadStudentInfo", "${student.no} $status")
        addStudentRowToTable(student = student, status = status)
    }

    private fun addStudentRowToTable(student: Student, status: String) {
        val row = TableRow(this)
        val tvNo = TextView(this)
        val tvName = TextView(this)
        val tvStatus = TextView(this)

        tvNo.text = student.no
        tvNo.textAlignment = View.TEXT_ALIGNMENT_CENTER

        tvName.text = student.name
        tvStatus.text = status

        table.addView(row)
        row.addView(tvNo, TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(tvName, TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f))
        row.addView(tvStatus, TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f))
    }

    private val COLUMNs = arrayOf<String>("รหัสนักเรียน", "เลขที่", "ชื่อ")
    private val customers = Arrays.asList(
        Student("1", "Jack Smith", "Massachusetts"),
        Student("2", "Adam Johnson", "New York"),
        Student("3", "Katherin Carter", "Washington DC"),
        Student("4", "Jack London", "Nevada"),
        Student("5", "Jason Bourne", "California"))

    private fun exportToExcel() {
//        val workbook = XSSFWorkbook()
//        val createHelper = workbook.getCreationHelper()
//
//        val sheet = workbook.createSheet("Customers")
//
//        val headerFont = workbook.createFont()
//        headerFont.bold = true
//
//        val headerCellStyle = workbook.createCellStyle()
//        headerCellStyle.setFont(headerFont)
//
//        // Row for Header
//        val headerRow = sheet.createRow(0)
//
//        // Header
//        for (col in COLUMNs.indices) {
//            val cell = headerRow.createCell(col)
//            cell.setCellValue(COLUMNs[col])
//            cell.setCellStyle(headerCellStyle)
//        }
//
////        // CellStyle for Age
////        val ageCellStyle = workbook.createCellStyle()
////        ageCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#"))
//
//        var rowIdx = 1
//        for (customer in customers) {
//            val row = sheet.createRow(rowIdx++)
//            row.createCell(0).setCellValue(customer.studentID)
//            row.createCell(1).setCellValue(customer.no)
//            row.createCell(2).setCellValue(customer.name)
////            val ageCell = row.createCell(3)
////            ageCell.setCellValue(customer.age.toDouble())
////            ageCell.setCellStyle(ageCellStyle)
//        }
//
//        val fileOut = FileOutputStream("customers.xlsx")
//        workbook.write(fileOut)
//        fileOut.close()
//        workbook.close()
    }


}
