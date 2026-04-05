package no.uio.ifi.in2000.team12.team12project.data.exceptions

/* Frost: Status code 401 */
class UnauthorizedException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}