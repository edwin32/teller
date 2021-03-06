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

package templates

import play.api.data.Form
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import securesocial.controllers.{ ChangeInfo, RegistrationInfo, ViewTemplates }
import securesocial.core.RuntimeEnvironment

/**
 * Renders templates for SecureSocial
 * @param env Environment
 */
class SecureSocialTemplates(env: RuntimeEnvironment[_]) extends ViewTemplates {
  implicit val implicitEnv = env

  override def getLoginPage(form: Form[(String, String)],
    msg: Option[String] = None)(implicit request: RequestHeader, lang: Lang) = {
    views.html.secure.login(form, msg)
  }

  override def getNotAuthorizedPage(implicit request: RequestHeader, lang: Lang) = {
    views.html.secure.notAuthorized()
  }

  override def getSignUpPage(form: Form[RegistrationInfo], token: String)(implicit request: RequestHeader, lang: Lang) = ???

  override def getStartSignUpPage(form: Form[String])(implicit request: RequestHeader, lang: Lang) = ???

  override def getResetPasswordPage(form: Form[(String, String)], token: String)(implicit request: RequestHeader, lang: Lang) = ???

  override def getStartResetPasswordPage(form: Form[String])(implicit request: RequestHeader, lang: Lang) = ???

  override def getPasswordChangePage(form: Form[ChangeInfo])(implicit request: RequestHeader, lang: Lang) = ???
}
