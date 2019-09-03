package natradac.test.classroom

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Subject (
    @SerializedName("subjectID")
    @Expose
    var subjectID: String,

    @SerializedName("subjectName")
    @Expose
    var subjectName: String,

    @SerializedName("room")
    @Expose
    var room: String,

    @SerializedName("file")
    @Expose
    var file: String

) {
    override fun toString(): String {
        return "Subject [subjectID=$subjectID, subjectName=$subjectName, room=$room]"
    }
}