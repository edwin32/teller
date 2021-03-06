/*
 * Happy Melly Teller
 * Copyright (C) 2013 - 2014, Happy Melly http://www.happymelly.com
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
 * If you have questions concerning this license or the applicable additional terms, you may contact
 * by email Sergey Kotlov, sergey.kotlov@happymelly.com or
 * in writing Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */

package controllers

import models.UserRole.Role._
import models.service.Services
import models.{ ActiveUser, ActivityRecorder, Contribution }
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import securesocial.core.RuntimeEnvironment

class Contributions(environment: RuntimeEnvironment[ActiveUser])
    extends Controller
    with Security
    with Services
    with Activities {

  override implicit val env: RuntimeEnvironment[ActiveUser] = environment

  /** HTML form mapping for creating and editing. */
  def contributionForm = Form(mapping(
    "id" -> ignored(Option.empty[Long]),
    "contributorId" -> nonEmptyText.transform(_.toLong, (l: Long) ⇒ l.toString),
    "productId" -> nonEmptyText.transform(_.toLong, (l: Long) ⇒ l.toString),
    "isPerson" -> text.transform(_.toBoolean, (b: Boolean) ⇒ b.toString),
    "role" -> nonEmptyText)(Contribution.apply)(Contribution.unapply))

  /**
   * Add new contribution to a product
   * @param page Label of a page where the action happened
   * @return
   */
  def create(page: String) = SecuredRestrictedAction(Editor) { implicit request ⇒
    implicit handler ⇒ implicit user ⇒

      val boundForm: Form[Contribution] = contributionForm.bindFromRequest
      val contributorId = boundForm.data("contributorId").toLong
      val route = if (page == "organisation") {
        routes.Organisations.details(contributorId).url
      } else if (page == "person") {
        routes.People.details(contributorId).url + "#contributions"
      } else {
        routes.Products.details(boundForm.data("productId").toLong).url
      }
      boundForm.bindFromRequest.fold(
        formWithErrors ⇒ Redirect(route).flashing("error" -> "A role for a contributor cannot be empty"),
        success ⇒ {
          val contributor: Option[ActivityRecorder] = if (success.isPerson)
            personService.find(success.contributorId)
          else
            orgService.find(success.contributorId)
          contributor map { c ⇒
            val contribution = success.insert
            val log = activity(contribution, user.person).connected.insert()
            Redirect(route).flashing("success" -> log.toString)
          } getOrElse {
            Redirect(route).flashing("error" -> "Contributor does not exist")
          }
        })
  }

  /**
   * Delete a contribution
   *
   * @param id Contribution identifier
   * @param page Label of a page where the action happened
   * @return
   */
  def delete(id: Long, page: String) = SecuredRestrictedAction(Editor) { implicit request ⇒
    implicit handler ⇒ implicit user ⇒

      Contribution.find(id).map { contribution ⇒
        val contributor: ActivityRecorder = if (contribution.isPerson)
          personService.find(contribution.contributorId).get
        else
          orgService.find(contribution.contributorId).get
        Contribution.delete(id)

        val log = activity(contribution, user.person).disconnected.insert()
        val route = if (page == "organisation") {
          routes.Organisations.details(contribution.contributorId).url
        } else if (page == "product") {
          routes.Products.details(contribution.productId).url
        } else {
          routes.People.details(contribution.contributorId).url + "#contributions"
        }
        Redirect(route).flashing("success" -> log.toString)
      }.getOrElse(NotFound)
  }

}
