/*
 * Happy Melly Teller
 * Copyright (C) 2013 - 2015, Happy Melly http://www.happymelly.com
 *
 * This file is part of the Happy Melly Teller.
 *
 * Happy Melly Teller is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Happy Melly Teller is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Happy Melly Teller.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you have questions concerning this license or the applicable additional
 * terms, you may contact by email Sergey Kotlov, sergey.kotlov@happymelly.com
 * or in writing
 * Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */
package models.service.event

import models.EventInvoice
import models.database.EventInvoices
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

class EventInvoiceService {

  def update(invoice: EventInvoice): Unit = DB.withSession {
    implicit session: Session ⇒
      _update(invoice)
  }

  def _update(invoice: EventInvoice)(implicit session: Session): Unit =
    TableQuery[EventInvoices].filter(_.id === invoice.id)
      .map(_.forUpdate)
      .update((invoice.invoiceTo, invoice.invoiceBy, invoice.number))
}

object EventInvoiceService {
  private val _instance = new EventInvoiceService

  def get: EventInvoiceService = _instance
}