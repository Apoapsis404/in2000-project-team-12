package no.uio.ifi.in2000.team12.team12project.data.exceptions

/* Frost: Status code 429 and 503 */
class ServiceBusyException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
