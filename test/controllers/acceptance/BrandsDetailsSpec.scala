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

import controllers.Brands
import helpers.{ PersonHelper, ProductHelper }
import integration.PlayAppSpec
import models.brand.{ BrandCoordinator, CertificateTemplate, EventType }
import models.service.{ BrandService, ProductService }
import models.service.brand.{ CertificateTemplateService, EventTypeService }
import org.scalamock.specs2.{ IsolatedMockFactory, MockContext }
import stubs.{ FakeRuntimeEnvironment, FakeUserIdentity, FakeServices, FakeSecurity }

/**
 * Tests Brands controller methods, rendering Details page
 */
class BrandsDetailsSpec extends PlayAppSpec with IsolatedMockFactory {
  override def is = s2"""

  On 'Event Types' tab on a Brand page a user should see
    a list of event types related to the requested brand          $e1
    no table if there is no event types                           $e2
    no 'Add Event Type' form if she is a Viewer                   $e3
    'Add Event Type' form if he is an Editor

  On 'Products' tab on a Brand page a user should see
    a list of products related to the requested brand             $e5
    no table if there is no products                              $e6

  On 'Certificate Templates' tab on a Brand page a user should see
    a list of templates related to the requested brand            $e7
    no table if there is no templates                             $e8
    no 'Add Certificate Template' button if she is a Viewer       $e9
    'Add Certificate Template' button if he is an Editor          $e10

  On 'Team' tab on a Brand page a user should see
    a list of team members of the requested brand                 $e11
    no 'Add Coordinator' or 'Delete' buttons if she is a Viewer   $e13
    'Add Coordinator' or 'Delete' buttons if he is an Editor      $e14

  """

  class TestBrands extends Brands(FakeRuntimeEnvironment)
    with FakeServices with FakeSecurity

  val controller = new TestBrands
  val eventTypeService = mock[EventTypeService]
  controller.eventTypeService_=(eventTypeService)
  val productService = mock[ProductService]
  controller.productService_=(productService)
  val certificateService = mock[CertificateTemplateService]
  controller.certificateService_=(certificateService)
  val brandService = mock[BrandService]
  controller.brandService_=(brandService)

  def e1 = {
    val eventTypes = List(
      EventType(Some(1L), 1L, "Meetup", Some("Yay, meetup!"), 8, false),
      EventType(Some(2L), 1L, "Meeting", None, 1, false),
      EventType(Some(3L), 1L, "CodeFest", Some("Coding sessions"), 16, false))
    (eventTypeService.findByBrand _).expects(1L).returning(eventTypes)
    val res = controller.renderTabs(1L, "types").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must contain("Meetup")
    contentAsString(res) must contain("Yay, meetup!")
    contentAsString(res) must contain("Meeting")
    contentAsString(res) must contain("CodeFest")
    contentAsString(res) must contain("Coding sessions")
  }

  def e2 = {
    (eventTypeService.findByBrand _).expects(1L).returning(List())
    val res = controller.renderTabs(1L, "types").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must not contain "<table"
  }

  def e3 = {
    (eventTypeService.findByBrand _).expects(1L).returning(List())
    val res = controller.renderTabs(1L, "types").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must not contain "Add Event Type"
  }

//  def e4 = {
//    (eventTypeService.findByBrand _).expects(1L).returning(List())
//    controller.identity_=(FakeUserIdentity.editor)
//    val res = controller.renderTabs(1L, "types").apply(fakeGetRequest())
//    status(res) must equalTo(OK)
//    contentAsString(res) must contain("Add Event Type")
//  }

  def e5 = {
    val products = List(
      ProductHelper.make("One", Some(1L)),
      ProductHelper.make("Two", Some(2L)),
      ProductHelper.make("Three", Some(3L)))
    (productService.findByBrand _).expects(1L).returning(products)
    controller.identity_=(FakeUserIdentity.viewer)
    val res = controller.renderTabs(1L, "products").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must contain("One")
    contentAsString(res) must contain("Two")
    contentAsString(res) must contain("Three")
  }

  def e6 = {
    (productService.findByBrand _).expects(1L).returning(List())
    val res = controller.renderTabs(1L, "products").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must not contain "<table"
  }

  def e7 = {
    val templates = List(
      CertificateTemplate(Some(1L), 1L, "EN", Array[Byte](), Array[Byte]()),
      CertificateTemplate(Some(2L), 1L, "DE", Array[Byte](), Array[Byte]()))
    (certificateService.findByBrand _).expects(1L).returning(templates)
    val res = controller.renderTabs(1L, "templates").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must contain("English")
    contentAsString(res) must contain("German")
  }

  def e8 = {
    (certificateService.findByBrand _).expects(1L).returning(List())
    val res = controller.renderTabs(1L, "templates").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must not contain "<table"
  }

  def e9 = {
    (certificateService.findByBrand _).expects(1L).returning(List())
    val res = controller.renderTabs(1L, "templates").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must not contain "Add Certificate Template"
  }

  def e10 = {
    (certificateService.findByBrand _).expects(1L).returning(List())
    controller.identity_=(FakeUserIdentity.editor)
    val res = controller.renderTabs(1L, "templates").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must contain("Add Certificate Template")
  }

  def e11 = {
    val team = List(
      (PersonHelper.one(), BrandCoordinator(Some(1L), 1L, 1L)),
      (PersonHelper.two(), BrandCoordinator(Some(2L), 1L, 2L)))
    (brandService.coordinators _).expects(1L).returning(team)
    controller.identity_=(FakeUserIdentity.viewer)
    val res = controller.renderTabs(1L, "team").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must contain("First Tester")
    contentAsString(res) must contain("Second Tester")
  }

  def e13 = {
    (brandService.coordinators _).expects(1L).returning(List())
    val res = controller.renderTabs(1L, "team").apply(fakeGetRequest())
    status(res) must equalTo(OK)
    contentAsString(res) must not contain "Add Coordinator"
    contentAsString(res) must not contain "glyphicon-trash"
  }

  def e14 = {
    val coordinators = List((PersonHelper.one(), BrandCoordinator(Some(1L), 1L, 1L)))
    (brandService.coordinators _) expects 1L returning coordinators
    controller.identity_=(FakeUserIdentity.editor)
    val res = controller.renderTabs(1L, "team").apply(fakeGetRequest("/1"))
    status(res) must equalTo(OK)
    contentAsString(res) must contain("Add Coordinator")
    contentAsString(res) must contain("glyphicon-trash")
  }
}
