package org.sutemi

import org.scalatest.{FlatSpec, Matchers}
import sangria.macros._
import sangria.execution._
import sangria.marshalling.circe._
import io.circe.Json
import io.circe.syntax._
import io.circe.parser._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class SchemaSpec extends FlatSpec with Matchers {

  val query =
    graphql"""
    query MyProduct {
      product(id: "2") {
        name
        description

        picture(size: 500) {
          width, height, url
        }
      }

      products {
        name
      }
    }
  """

  val simplequery =
    graphql"""
      query SimpleProduct {
        product(id: "2") {
          name
        }
      }
    """

  val paramquery =
    graphql"""
      query ParamProduct($$prodId: String!) {
        product(id: $$prodId) {
          name, description
        }
      }
    """


  "Query" should "do a query" in {
    val result: Future[Json] =
      Executor.execute(SchemaDefinition.schema, query, new ProductRepo)

    val json = Await.result(result, 10.seconds)

    json should be(parse(
      """
        |{
        |  "data" : {
        |    "product" : {
        |      "name" : "Health Potion",
        |      "description" : "+50 HP",
        |      "picture" : {
        |        "width" : 500,
        |        "height" : 500,
        |        "url" : "//cdn.com/500/2.jpg"
        |      }
        |    },
        |    "products" : [
        |      {
        |        "name" : "Cheesecake"
        |      },
        |      {
        |        "name" : "Health Potion"
        |      }
        |    ]
        |  }
        |}
      """.stripMargin).right.get)
  }


  it should "do simpler query" in {
    val result: Future[Json] =
      Executor.execute(SchemaDefinition.schema, simplequery, new ProductRepo)

    val json = Await.result(result, 10.seconds)

    json should be(parse(
      """
        |{
        |  "data" : {
        |    "product" : {
        |      "name" : "Health Potion"
        |    }
        |  }
        |}
      """.stripMargin).right.get)
  }


  it should "do param query" in {
    val vars = Map("prodId" → "1").asJson
    val result: Future[Json] =
      Executor.execute(SchemaDefinition.schema, paramquery, new ProductRepo, variables = vars)

    val json = Await.result(result, 10.seconds)

    json should be(parse(
      """
        |{
        |  "data" : {
        |    "product" : {
        |      "name" : "Cheesecake",
        |      "description" : "Tasty"
        |    }
        |  }
        |}
      """.stripMargin).right.get)
  }
}
