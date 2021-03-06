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
 * terms, you may contact by email Sergey Kotlov, sergey.kotlov@happymelly.com or
 * in writing Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */

package controllers.acceptance

import _root_.integration.PlayAppSpec
import controllers.{ Security, ProfilePhotos }
import models.service.PersonService
import play.api.mvc.Result
import stubs.{ FakeRuntimeEnvironment, AccessCheckSecurity }

import scala.concurrent.Future

class ProfilePhotosAccessSpec extends PlayAppSpec {
  class TestProfilePhotos() extends ProfilePhotos(FakeRuntimeEnvironment)
    with AccessCheckSecurity

  val controller = new TestProfilePhotos()

  "Method 'update'" should {
    "have 'edit' access rights for 'person' object" in {
      controller.update(1L).apply(fakePostRequest())
      controller.checkedDynamicObject must_== Some("person")
      controller.checkedDynamicLevel must_== Some("edit")
    }
  }

  "Method 'choose'" should {
    "have 'edit' access rights for 'person' object" in {
      controller.choose(1L).apply(fakeGetRequest())
      controller.checkedDynamicObject must_== Some("person")
      controller.checkedDynamicLevel must_== Some("edit")
    }
  }

}
