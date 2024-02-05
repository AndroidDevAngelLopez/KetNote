import java.text.SimpleDateFormat
import java.util.Locale

fun Long.toHumanDate(): String {
    val format = SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH)
    return format.format(this)
}