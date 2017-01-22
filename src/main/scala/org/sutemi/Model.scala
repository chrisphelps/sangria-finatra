package org.sutemi

import sangria.schema._
import sangria.macros.derive._

case class Picture(width: Int, height: Int, url: Option[String])

trait Identifiable {
  def id: String
}

case class Product(id: String, name: String, description: String) extends Identifiable {
  def picture(size: Int): Picture =
    Picture(width = size, height = size, url = Some(s"//cdn.com/$size/$id.jpg"))
}


class ProductRepo {
  private val Products = List(
    Product("1", "Cheesecake", "Tasty"),
    Product("2", "Health Potion", "+50 HP"))

  def product(id: String): Option[Product] = Products find (_.id == id)

  def products: List[Product] = Products
}


object SchemaDefinition {

  implicit val PictureType =
    deriveObjectType[Unit, Picture](
      ObjectTypeDescription("The product picture"),
      DocumentField("url", "Picture CDN URL"))


  val IdentifiableType = InterfaceType(
    "Identifiable",
    "Entity that can be identified",

    fields[Unit, Identifiable](
      Field("id", StringType, resolve = _.value.id)))


  val ProductType =
    deriveObjectType[Unit, Product](
      Interfaces(IdentifiableType),
      IncludeMethods("picture"))


  val Id = Argument("id", StringType)

  val QueryType = ObjectType("Query", fields[ProductRepo, Unit](
    Field("product", OptionType(ProductType),
      description = Some("Returns a product with specific `id`."),
      arguments = Id :: Nil,
      resolve = c ⇒ c.ctx.product(c arg Id)),

    Field("products", ListType(ProductType),
      description = Some("Returns a list of all available products."),
      resolve = _.ctx.products)))


  val schema = Schema(QueryType)
}
