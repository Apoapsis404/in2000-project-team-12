package no.uio.ifi.in2000.team12.team12project.data.exceptions

/* Frost: Status code 404 */
class NoDataException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    //constructor(cause: Throwable) : this(null, cause)
}