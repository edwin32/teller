/*
* Happy Melly Teller
* Copyright (C) 2013 - 2015, Happy Melly, http://www.happymelly.com
*
* This file is part of the Happy Melly Teller.
*
* Happy Melly Teller is free software ->  you can redistribute it and/or modify
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
* along with Happy Melly Teller.  If not, see <http -> //www.gnu.org/licenses/>.
*
* If you have questions concerning this license or the applicable additional
* terms, you may contact by email Sergey Kotlov, sergey.kotlov@happymelly.com
* or in writing Happy Melly One, Handelsplein 37, Rotterdam,
* The Netherlands, 3071 PR
*/
package controllers.acceptance.people

import _root_.integration.PlayAppSpec
import controllers.People
import helpers._
import models.service._
import models.{ License, LicenseView, SocialProfile }
import org.joda.money.Money
import org.joda.time.LocalDate
import org.scalamock.specs2.{ IsolatedMockFactory, MockContext }
import stubs._

class PeopleSpec extends PlayAppSpec with IsolatedMockFactory {

  override def is = s2"""

  Page with person's data should

    not contain accounting details                                       $e3
    contain a supporter badge if the person is a supporter               $e4
    contain a funder badge if the person is a funder                     $e5

  'Make a Facilitator' button should
    be visible to an Editor if a person has no licenses                  $e7
    not be visible to an Editor if a person has licenses                 $e8
    not be visible to a Viewer if a person has no licenses               $e9

  On automatic renewal cancellation the system should
    return 'Not found' if a person does not exists                       $e16
    return an error if a person is not a member                          $e17
    return an error if there is not subscription                         $e18
  """

  class TestPeople() extends People(FakeRuntimeEnvironment)
    with FakeSecurity with FakeServices

  val personService = mock[PersonService]
  val orgService = mock[OrganisationService]
  val accountService = mock[UserAccountService]
  val licenseService = mock[LicenseService]
  val controller = new TestPeople()
  controller.personService_=(personService)
  controller.orgService_=(orgService)
  controller.userAccountService_=(accountService)
  controller.licenseService_=(licenseService)

  val id = 1L
  val person = PersonHelper.one()
  person.socialProfile_=(new SocialProfile(email = "test@test.com"))

  trait DefaultMockContext extends MockContext {
    truncateTables()
    (personService.find(_: Long)) expects id returning Some(person)
    orgService.findActive _ expects () returning List()
  }

  trait ExtendedMockContext extends DefaultMockContext {
    licenseService.licenses _ expects (id) returning List()
  }

  trait EditorMockContext extends ExtendedMockContext {
    (accountService.findRole _).expects(id).returning(None)
    (accountService.findDuplicateIdentity _).expects(person).returning(None)
  }

  trait ViewerMockContext extends ExtendedMockContext {
    (accountService.findRole _).expects(id).returning(None).never()
    (accountService.findDuplicateIdentity _).expects(person).returning(None).never()
  }

  def e3 = new EditorMockContext {
    person.insert
    val member = MemberHelper.make(Some(1L), id, person = true, funder = false)
    person.member_=(member)
    controller.identity_=(FakeUserIdentity.admin)
    val result = controller.details(person.id.get).apply(fakeGetRequest())

    contentAsString(result) must not contain "Financial account"
    contentAsString(result) must not contain "Account history"
  }

  def e4 = new ViewerMockContext {
    person.insert
    val member = MemberHelper.make(Some(1L), id, person = true, funder = false)
    person.member_=(member)
    controller.identity_=(FakeUserIdentity.viewer)
    val result = controller.details(person.id.get).apply(fakeGetRequest())

    contentAsString(result) must contain("supporter")
    contentAsString(result) must not contain "/member/1/edit"
  }

  def e5 = new EditorMockContext {
    // we insert a person object here to prevent crashing on account retrieval
    // when @person.deletable is called
    person.insert
    val member = MemberHelper.make(Some(1L), id, person = true, funder = true)
    person.member_=(member)
    controller.identity_=(FakeUserIdentity.editor)
    val result = controller.details(person.id.get).apply(fakeGetRequest())

    contentAsString(result) must contain("funder")
  }

  def e7 = new EditorMockContext {
    person.insert
    val member = MemberHelper.make(Some(1L), id, person = true, funder = true)
    person.member_=(member)
    controller.identity_=(FakeUserIdentity.editor)
    val result = controller.details(person.id.get).apply(fakeGetRequest())

    // contentAsString(result) must contain("/person/1/licenses/new")
    contentAsString(result) must contain("Make a Facilitator")
  }

  def e8 = new DefaultMockContext {
    person.insert
    val member = MemberHelper.make(Some(1L), id, person = true, funder = true)
    person.member_=(member)

    val license = new License(Some(1L), id, 1L, "1",
      LocalDate.now(), LocalDate.now(), LocalDate.now().plusYears(1), true,
      Money.parse("EUR 10"), None)
    val licenses = List(LicenseView(BrandHelper.one, license))
    licenseService.licenses _ expects id returning licenses
    accountService.findRole _ expects id returning None
    accountService.findDuplicateIdentity _ expects person returning None
    controller.identity_=(FakeUserIdentity.editor)
    val result = controller.details(person.id.get).apply(fakeGetRequest())

    contentAsString(result) must not contain "Make a Facilitator"
  }

  def e9 = new ViewerMockContext {
    person.insert
    val member = MemberHelper.make(Some(1L), id, person = true, funder = true)
    person.member_=(member)
    controller.identity_=(FakeUserIdentity.viewer)
    val result = controller.details(person.id.get).apply(fakeGetRequest())

    contentAsString(result) must not contain "/person/1/licenses/new"
    contentAsString(result) must not contain "Make a Facilitator"
  }

  def e16 = new MockContext {
    truncateTables()
    (personService.find(_: Long)) expects id returning None
    val result = controller.cancel(person.id.get).apply(fakeGetRequest())

    status(result) must equalTo(NOT_FOUND)
  }

  def e17 = new MockContext {
    truncateTables()
    val person = PersonHelper.one()
    person.socialProfile_=(new SocialProfile(email = "test@test.com"))
    (personService.find(_: Long)) expects id returning Some(person)
    val result = controller.cancel(person.id.get).apply(fakeGetRequest())

    status(result) must equalTo(SEE_OTHER)
    header("Location", result) must beSome.which(_.contains("/person/1"))
  }

  def e18 = new MockContext {
    truncateTables()
    val person = PersonHelper.one()
    person.socialProfile_=(new SocialProfile(email = "test@test.com"))
    val member = MemberHelper.make(Some(1L), id, person = true, funder = true,
      renewal = false)
    person.member_=(member)
    (personService.find(_: Long)) expects id returning Some(person)
    val result = controller.cancel(person.id.get).apply(fakeGetRequest())

    status(result) must equalTo(SEE_OTHER)
    header("Location", result) must beSome.which(_.contains("/person/1"))
  }
}
