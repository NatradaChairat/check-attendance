package natradac.test.classroom

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {

    private val requestPermissionCode = 99

    private var subjectList = mutableListOf<Subject>()
    private var filename = ""
    private var subject = ""
    private var room = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        readSubject()

        initAskPermission()
    }

    private fun initListener() {
        next.setOnClickListener {
            startActivity(AttendanceActivity.newIntent(applicationContext, subject, room, filename))
        }
    }

    private fun initSpinner() {

        val roomList = emptyList<String>().toMutableList()
        for (i in subjectList) {
            roomList += "${i.subjectID} ม.${i.room}"
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            roomList
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        subjectSpinner.adapter = adapter

        subjectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                var code = parent.getItemAtPosition(position).toString()
                subject = code.subSequence(0, 6).toString()
                room = code.subSequence(9, 12).toString()

                Log.i("Test", "Spinner $code id= $subject room= $room")

                subjectList.first { it.subjectID == subject && it.room == room }.let {
                    filename = it.file
                }

                Log.i("Test", "Spinner filter filename: $filename")

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

    }

    private fun readSubject() {
        var json: String? = null
        try {
            val `is` = assets.open("subject.json")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        subjectList = Gson().fromJson(json, Array<Subject>::class.java).toMutableList()
        Log.i("Test", "StudentList $subjectList")

        initSpinner()
    }

    private fun initAskPermission() {

        if (!hasPermissions(this, arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE))) {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE), requestPermissionCode)
        } else {
            initListener()
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
                    initListener()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "กรุณาเปิดอนุญาตการเข้าถึงของแอพลิเคชั่นในการตั้งค่า",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


}
