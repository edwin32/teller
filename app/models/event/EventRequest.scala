package models.event

import models.DateStamp
import org.joda.time.LocalDate

/**
 * Represents request for an event
 * @param id Request identifier
 * @param brandId Brand this event belongs to
 * @param countryCode Two-letter country code, ex. RU
 * @param city List of cities in any format
 * @param language List of language , separated by commas. Ex. Russian,English
 * @param start The earliest possible date
 * @param end The latest possible date
 * @param participantsNumber Number of participants
 * @param comment Comment
 * @param name Name of the person who makes this request
 * @param email Email of the person who makes this request
 * @param recordInfo Record info
 */
case class EventRequest(id: Option[Long],
  brandId: Long,
  countryCode: String,
  city: Option[String],
  language: String,
  start: Option[LocalDate],
  end: Option[LocalDate],
  participantsNumber: Int,
  comment: Option[String],
  name: String,
  email: String,
  recordInfo: DateStamp) {
}
