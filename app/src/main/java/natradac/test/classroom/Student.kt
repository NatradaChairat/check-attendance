package natradac.test.classroom

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Student(
    @SerializedName("studentID")
    @Expose
    var studentID: String,

    @SerializedName("name")
    @Expose
    var name: String,

    @SerializedName("no")
    @Expose
    var no: String,

    var status: String? = null

) {
    override fun toString(): String {
        return "Student [studentID=$studentID, name=$name, no=$no]"
    }
}