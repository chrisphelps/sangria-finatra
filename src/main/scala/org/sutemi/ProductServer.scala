package org.sutemi

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

import scala.concurrent.Future
import sangria.execution._
import sangria.marshalling.circe._
import io.circe.Json
import io.circe.syntax._
import io.circe.parser._
import sangria.parser.{QueryParser, SyntaxError}
import sangria.ast.Document

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object ProductServerMain extends ProductServer

class ProductServer extends HttpServer {

  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[ProductController]
  }

}


class ProductController extends Controller {

  get("/") { request: Request =>
    response.ok.file("/graphiql.html")
  }

  post("/graphql") { request: Request =>

    debug(s"request: ${request.getContentString()}")
    
    val resp = parse(request.getContentString()) match {
      case Right(json) => graphQLEndpoint(json)
      case Left(err) => Future.successful(response.badRequest(err.message))
    }

    resp
  }


  def graphQLEndpoint(json: Json) = {
    // todo not sure this is the best failure handling
    val query = json.hcursor.downField("query").as[String] match {
      case Right(s) => s
      case _ => ""
    }

    val operation = json.hcursor.downField("operationName").as[String] match {
      case Right(op) => Some(op)
      case _ => None
    }

    // todo this does not seem to build the empty json object correctly
    val vars = json.hcursor.downField("variables").focus match {
      case Some(vs) => vs
      case None => Map[String,String]().asJson
    }

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        executeGraphQLQuery(queryAst, operation, vars)

      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) => Future.successful(response.badRequest(error.getMessage))
    }
  }


  def executeGraphQLQuery(query: Document, op: Option[String], vars: Json) = {
    debug(s"query: $query")
    debug(s"operation: $op")
    debug(s"vars $vars")


    Executor.execute(SchemaDefinition.schema, query, new ProductRepo, variables = vars, operationName = op)
      .map(x => {
        debug(s"ok response: $x")
        response.ok(x.toString)
      })
      .recover {
        case error: QueryAnalysisError => {
          debug(s"QueryAnalysisError ${error.getMessage}")
          response.badRequest(error.getMessage)
        }
        case error: ErrorWithResolver => {
          debug(s"ErrorWithResolver ${error.getMessage}")
          response.internalServerError(error.getMessage)
        }
      }

  }
}