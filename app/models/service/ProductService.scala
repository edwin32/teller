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
package models.service

import models.{ ProductView, Product, Brand }
import models.database.{ ProductBrandAssociations, Products }
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Play.current

class ProductService {

  /**
   * Activates the given product
   *
   * @param id Product id
   */
  def activate(id: Long): Unit = switchState(id, true)

  /**
   * Assign this product to a brand
   * @TEST
   */
  def addBrand(productId: Long, brandId: Long): Unit = DB.withSession {
    implicit session ⇒
      TableQuery[ProductBrandAssociations] += ((None, productId, brandId))
  }

  /**
   * Returns list of related brand for the given product, sorted by name
   *
   * @param id Product id
   */
  def brands(id: Long): List[Brand] = DB.withSession { implicit session ⇒
    val associations = TableQuery[ProductBrandAssociations]
    val query = for {
      relation ← associations if relation.productId === id
      brand ← relation.brand
    } yield brand
    query.sortBy(_.name.toLowerCase).list
  }

  /**
   * Deactivates the given product
   *
   * @param id Product id
   */
  def deactivate(id: Long): Unit = switchState(id, false)

  /**
   * @TEST
   * @param productId
   * @param brandId
   */
  def deleteBrand(productId: Long, brandId: Long): Unit = DB.withSession {
    implicit session ⇒
      val associations = TableQuery[ProductBrandAssociations]
      associations.filter(x ⇒ x.productId === productId && x.brandId === brandId).delete
  }
  /**
   * Deletes a product with the given id
   *
   * @param id Product id
   */
  def delete(id: Long): Unit = DB.withSession { implicit session ⇒
    val products = TableQuery[Products]
    products.filter(_.id === id).delete
  }

  /**
   * Returns a product with the given id if exists
   *
   * @param id Product id
   */
  def find(id: Long) = DB.withSession { implicit session ⇒
    val products = TableQuery[Products]
    products.filter(_.id === id).firstOption
  }

  /** Returns list with active products */
  def findActive: List[Product] = DB.withSession { implicit session ⇒
    val products = TableQuery[Products]
    products.filter(_.active === true).sortBy(_.title.toLowerCase).list
  }

  /** Returns list with all products */
  def findAll: List[Product] = DB.withSession { implicit session ⇒
    val products = TableQuery[Products]
    products.sortBy(_.title.toLowerCase).list
  }

  /**
   * Returns sorted list of products for the given brand
   *
   * @param brandId Brand identifier
   */
  def findByBrand(brandId: Long): List[Product] = DB.withSession {
    implicit session ⇒
      val associations = TableQuery[ProductBrandAssociations]
      val query = for {
        relation ← associations if relation.brandId === brandId
        product ← relation.product
      } yield product
      query.sortBy(_.title.toLowerCase).list
  }

  /**
   * Returns list of derivative products for the given product
   *
   * @param parentId Product id
   */
  def findDerivatives(parentId: Long): List[Product] = DB.withSession {
    implicit session ⇒
      val products = TableQuery[Products]
      products.filter(_.parentId === parentId).list
  }

  /**
   * Inserts the given product to database
   *
   * @param product Product to insert
   * @return The given product with updated id
   */
  def insert(product: Product): Product = DB.withSession { implicit session ⇒
    val products = TableQuery[Products]
    val id = (products returning products.map(_.id)) += product
    product.copy(id = Some(id))
  }

  /**
   * Returns true if the given title is taken by other product except the given one
   * @TEST
   * @param title The title of interest
   * @param id Product id
   */
  def isTitleTaken(title: String, id: Long): Boolean = DB.withSession {
    implicit session ⇒
      val products = TableQuery[Products]
      products.filter(_.title === title).filter(_.id =!= id).exists.run
  }

  /**
   * Returns true if a product with the given title exists
   * @TEST
   * @param title Product title
   */
  def titleExists(title: String): Boolean = DB.withSession {
    implicit session ⇒
      val products = TableQuery[Products]
      products.filter(_.title === title).exists.run
  }

  /**
   * Updates the given product to database
   *
   * @TEST
   * @param product Product to update
   * @return The given product
   */
  def update(product: Product): Product = DB.withSession { implicit session ⇒
    val products = TableQuery[Products]
    val updateTuple = (product.title, product.subtitle, product.url,
      product.description, product.callToActionUrl, product.callToActionText,
      product.picture, product.category, product.parentId,
      product.updated, product.updatedBy)
    products.filter(_.id === product.id).map(_.forUpdate).update(updateTuple)
    product
  }

  /**
   * Deactivates/actives the given product
   *
   * @param id Product id
   * @param active If true, the product is activated
   */
  private def switchState(id: Long, active: Boolean): Unit = DB.withSession {
    implicit session: Session ⇒
      val products = TableQuery[Products]
      val query = for {
        product ← products if product.id === id
      } yield product.active
      query.update(active)
  }
}

object ProductService {
  private val instance = new ProductService

  def get: ProductService = instance
}

object ProductsCollection {

  /**
   * Fill products with brands (using only one query to database)
   * @TEST
   * @param products List of products
   */
  def brands(products: List[Product]): List[ProductView] = DB.withSession {
    implicit session ⇒
      val ids = products.map(_.id.get).distinct
      val associations = TableQuery[ProductBrandAssociations]

      val query = for {
        relation ← associations if relation.productId inSet ids
        brand ← relation.brand
      } yield (relation.productId, brand)
      val brands = query.list.groupBy(_._1).map(f ⇒ (f._1, f._2.map(_._2)))
      products map (p ⇒ ProductView(p, brands.getOrElse(p.id.get, List())))
  }
}